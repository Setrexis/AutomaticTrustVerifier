package eu.lightest.verifier.wrapper;

import com.google.gson.Gson;
import eu.lightest.verifier.exceptions.DNSException;
import iaik.security.keystore.IAIKKeyStore;
import iaik.security.provider.IAIK;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GNSHelper implements NameResolverHelper, Dns {

    private final OkHttpClient client;

    private final String basic_auth_secret;

    private final Gson gson = new Gson();

    private static Logger logger = Logger.getLogger(GNSHelper.class);



    public GNSHelper() {
        // fix for JEP 229, see https://extgit.iaik.tugraz.at/LIGHTest/AutomaticTrustVerifier/issues/47#note_22080
        Security.setProperty("keystore.type", "jks");
        client = new OkHttpClient();
        basic_auth_secret = readBasicAuthSecret();
    }

    private String readBasicAuthSecret() {
        /*String command = "gnunet-config -f -s rest -o BASIC_AUTH_SECRET_FILE";
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        return reader.readLine();*/
        return "02R00J4KAR0A8";
    }

    @Override
    public List<String> queryTXT(String host) throws IOException {
        List<String> list = new ArrayList<>();
        for (Record record : this.query(host, "TXT")) {
            if (record instanceof TXTRecord) {
                String rdataToString = record.rdataToString();
                list.add(rdataToString);
            }
        }
        return list;
    }

    @Override
    public List<String> queryPTR(String host) throws IOException {
        List<String> list = new ArrayList<>();
        for (Record record : this.query(host, "PTR")) {
            if (record instanceof PTRRecord) {
                String rdataToString = record.rdataToString();
                list.add(rdataToString);
            }
        }
        return list;
    }

    @Override
    public List<String> queryURI(String host) throws IOException {
        List<String> list = new ArrayList<>();
        for (Record record : this.query(host, "URI")) {
            if (record instanceof URIRecord) {
                list.add(((URIRecord) record).getTarget());
            }
        }
        return list;
    }

    @Override
    public List<SMIMEAcert> querySMIMEA(String host) throws IOException {
        List<SMIMEAcert> list = new ArrayList<>();
        for (Record record : this.query(host, "SMIMEA")) {
            if (record instanceof SMIMEARecord) {
                SMIMEAcert smimeAcert = new SMIMEAcert((SMIMEARecord) record);
                list.add(smimeAcert);
            }
        }
        return list;
    }

    @Override
    public <R extends Record> List<R> queryAndParse(String host, Class recordTypeClass, int recordTypeID) throws IOException, DNSException {

        List<R> list = new ArrayList<R>();

        List<Record> response = this.query(host, ""+recordTypeID);

        for(Record elem : response) {

            if(recordTypeClass.isInstance(elem)) {
                R rec = (R) elem;
                list.add(rec);
            }
        }
        return list;
    }

    public List<Record> query(String host, int type) throws IOException {
        return this.query(host, Type.string(type));
    }

    public List<Record> query(String host, String type) throws IOException {
        if (host.endsWith("."))
            host = host.substring(0, host.length() - 1);
        String url = "http://localhost:7776/gns/" + host + "?record_type=" + type;

        logger.info("Querying GNS: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Basic " + basic_auth_secret)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            String body = response.body().string();
            GNSJSONHelper gnsJSONHelper = gson.fromJson(body, GNSJSONHelper.class);
            if(gnsJSONHelper.getData() == null)
                return new ArrayList<>();
            Name name = Name.fromString(gnsJSONHelper.getRecord_name() + ".");
            List<Record> list = new ArrayList<>();
            for (GNSJSONHelper.GNSRecord record : gnsJSONHelper.getData()) {
                Record fromString = Record.fromString(name, Type.value(record.getRecord_type()),
                        DClass.IN, 3600, new Tokenizer(record.getValue()), Name.root);
                list.add(fromString);
            }
            return list;
        }
    }

    private List<String> queryA(String host) {
        List<String> list = new ArrayList<>();
        try {
            for (Record record : this.query(host, "A")) {
                if (record instanceof ARecord) {
                    String rdataToString = record.rdataToString();
                    list.add(rdataToString);
                }
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return list;
    }

    private List<String> queryAAAA(String host) {
        List<String> list = new ArrayList<>();
        try {
            for (Record record : this.query(host, "AAAA")) {
                if (record instanceof AAAARecord) {
                    String rdataToString = record.rdataToString();
                    list.add(rdataToString);
                }
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return list;
    }

    @NotNull
    @Override
    public List<InetAddress> lookup(@NotNull String host) throws UnknownHostException {
        List<String> ips = this.queryA(host);
        ips.addAll(this.queryAAAA(host));
        if (ips.isEmpty()) {
            logger.error("No IP found for host: " + host);
            logger.error("Trying to resolve host with InetAddress.getAllByName()");
            List<InetAddress> list = new ArrayList<>();
            Collections.addAll(list, InetAddress.getAllByName(host));
            return list;
            //return new ArrayList<>();
        }
        List<InetAddress> list = new ArrayList<>();
        for (String ip : ips) {
            list.add(InetAddress.getByName(ip));
        }
        return list;
    }
}
