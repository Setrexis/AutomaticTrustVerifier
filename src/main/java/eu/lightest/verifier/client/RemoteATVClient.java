package eu.lightest.verifier.client;

import com.google.gson.Gson;
import eu.lightest.verifier.controller.VerificationProcess;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.ReportStatus;
import okhttp3.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;

/**
 * Client for the Automated Trust Verifier REST API.
 * <p>
 * Use this class if you want to use the ATV via its REST API (not as library).
 */
public class RemoteATVClient implements ATVClient {
    
    private static MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static Logger logger = Logger.getLogger(RemoteATVClient.class);
    private static String ENDPOINT = "/api/v1/addInstance";
    
    private Gson gson;
    private OkHttpClient httpClient;
    private Report report;
    private String hostname;
    
    /**
     * @param hostname HTTP URL (scheme, host, port, path) to the ATV REST API.
     * @param report   {@link Report} object used to retrieve the verification report.
     */
    public RemoteATVClient(String hostname, Report report) {
        this.hostname = hostname;
        this.report = report;
        this.gson = new Gson();
        
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(Duration.ZERO)
                .connectTimeout(Duration.ZERO)
                .readTimeout(Duration.ZERO)
                .writeTimeout(Duration.ZERO)
                .build();
    }
    
    @Override
    public boolean verify(String pathPolicy, String pathTransaction) {
        try {
            String policy = fileToBase64(pathPolicy);
            String transaction = fileToBase64(pathTransaction);
            
            String requestJson = serialize(policy, transaction);
            
            String responseJson = sendRequest(requestJson);
            
            ATVResponse response = deserialize(responseJson);
            
            this.report.addLines(response.report);
            
            return response.result == VerificationProcess.STATUS_OK;
            
        } catch(IOException e) {
            this.report.addLine("Error while initializing ATV communication: " + e.getMessage(), ReportStatus.FAILED);
            RemoteATVClient.logger.error("Error while initializing ATV communication", e);
            return false;
        }
    }
    
    private ATVResponse deserialize(String responseJson) {
        return this.gson.fromJson(responseJson, ATVResponse.class);
    }
    
    private String sendRequest(String data) throws IOException {
        String url = this.hostname + RemoteATVClient.ENDPOINT;
        RemoteATVClient.logger.info("Sending POST request to " + url);
        RemoteATVClient.logger.info("Data: " + data);
        
        RequestBody body = RequestBody.create(RemoteATVClient.JSON, data);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        Response response = this.httpClient.newCall(request).execute();
        String responseString = response.body().string();
        
        RemoteATVClient.logger.info("ATV Response: " + responseString);
        if(!response.isSuccessful()) {
            throw new IOException("ATV error code: " + response.code());
        }
        
        return responseString;
    }
    
    private String serialize(String policy, String transaction) {
        ATVRequest requestData = new ATVRequest(policy, transaction);
        
        return this.gson.toJson(requestData);
    }
    
    private String fileToBase64(String path) throws IOException {
        File file = new File(path);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }
    
    @Override
    public String toString() {
        return this.hostname;
    }
    
    /**
     * Provides encapsulation for serializing data send to the ATV REST API.
     */
    class ATVRequest {
        
        private String policy;
        private String transaction;
        
        public ATVRequest(String policy, String transaction) {
            this.policy = policy;
            this.transaction = transaction;
        }
    }
    
    /**
     * Provides encapsulation for serializing data received from the ATV REST API.
     */
    class ATVResponse {
        
        private Collection<String> report;
        private String verificationResult;
        private int result;
    }
}
