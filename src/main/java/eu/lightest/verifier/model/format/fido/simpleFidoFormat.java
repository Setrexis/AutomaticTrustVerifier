package eu.lightest.verifier.model.format.fido;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import eu.lightest.horn.specialKeywords.HornApiException;
import eu.lightest.verifier.ATVConfiguration;
import eu.lightest.verifier.model.format.AbstractFormatParser;
import eu.lightest.verifier.model.format.FormatParser;
import eu.lightest.verifier.model.format.eIDAS_qualified_certificate.EidasCertFormat;
import eu.lightest.verifier.model.report.Report;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class simpleFidoFormat extends AbstractFormatParser {
    
    public static final String RESOLVETYPE_FIDOREQUEST = "FidoRequest";
    public static final String QUERY_REQUEST = "request";
    private static final String QUERY_AUTHORITY = "authority";
    private static final String FORMAT_ID = "simpleFido";
    private static Logger logger = Logger.getLogger(simpleFidoFormat.class);
    private final FidoRequest request;
    private final Gson gson;
    private String FIDO_AUTHORITY = ATVConfiguration.get().getString("trustscheme_claim.fido1");
    
    public simpleFidoFormat(Object transaction, Report report) {
        super(transaction, report);
        
        if(transaction instanceof File) {
            this.gson = new Gson();
            JsonReader reader = null;
            try {
                reader = new JsonReader(new FileReader((File) transaction));
            } catch(FileNotFoundException e) {
                simpleFidoFormat.logger.error("Error reading fido data: " + e.getMessage());
                throw new IllegalArgumentException(e);
            }
            
            this.request = this.gson.fromJson(reader, FidoRequest.class);
            if(this.request == null) {
                throw new IllegalArgumentException("Error reading fido data: Could not parse JSON.");
            }
            
        } else {
            simpleFidoFormat.logger.error("Transaction of type: " + transaction.getClass().toString() + ",  expected: File");
            throw new IllegalArgumentException("Transaction of type: " + transaction.getClass().toString() + ",  expected: File");
        }
        
    }
    
    
    @Override
    public String getFormatId() {
        return simpleFidoFormat.FORMAT_ID;
    }
    
    @Override
    public boolean onExtract(List<String> path, String query, List<String> output) throws HornApiException {
        if(path.size() == 0) {
            switch(query) {
                case AbstractFormatParser.QUERY_FORMAT:
                case FidoRequest.QUERY_authMethod:
                case FidoRequest.QUERY_authType:
                case FidoRequest.QUERY_idMethod:
                case FidoRequest.QUERY_idType:
                    return true;
                case simpleFidoFormat.QUERY_AUTHORITY:
                    return this.FIDO_AUTHORITY != null;
                case simpleFidoFormat.QUERY_REQUEST:
                    return this.request != null;
            }
        }
        
        String parserId = path.get(0);
        simpleFidoFormat.logger.info("delegating to parser: " + parserId);
        return getParser(parserId).onExtract(pop(path), query, output);
        
        //return false;
    }
    
    @Override
    public ResolvedObj resolveObj(List<String> path) {
        simpleFidoFormat.logger.info("resolveObj @ " + this.getFormatId() + ": " + String.join(".", path));
        
        if(path.size() > 1) {
            String parserId = path.get(0);
            FormatParser parser = getParser(parserId);
            
            return parser.resolveObj(pop(path));
        }
        
        switch(path.get(0)) {
            case AbstractFormatParser.QUERY_FORMAT:
                return genResolvedObj(getFormatId(), "STRING");
            case FidoRequest.QUERY_authMethod:
                return genResolvedObj(this.request.authMethod, "INT");
            case FidoRequest.QUERY_authType:
                return genResolvedObj(this.request.authType, "STRING");
            case FidoRequest.QUERY_idMethod:
                return genResolvedObj(this.request.idMethod, "INT");
            case FidoRequest.QUERY_idType:
                return genResolvedObj(this.request.idType, "INT");
            case simpleFidoFormat.QUERY_AUTHORITY:
                return genResolvedObj(this.FIDO_AUTHORITY, EidasCertFormat.RESOLVETYPE_HTTP_URL);
            case simpleFidoFormat.QUERY_REQUEST:
                return genResolvedObj(this.request, simpleFidoFormat.RESOLVETYPE_FIDOREQUEST);
        }
        
        ResolvedObj cachedResolvedObj = this.getCachedResolvedObj(path);
        return cachedResolvedObj; // might be null
    }
    
    @Override
    public void init() throws Exception {
        this.FIDO_AUTHORITY = this.FIDO_AUTHORITY.trim();
        if(!this.FIDO_AUTHORITY.startsWith("_scheme._trust")) {
            this.FIDO_AUTHORITY = "_scheme._trust." + this.FIDO_AUTHORITY;
        }
    }
}
