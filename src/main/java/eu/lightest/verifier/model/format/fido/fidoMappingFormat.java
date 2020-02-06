package eu.lightest.verifier.model.format.fido;

import eu.lightest.horn.specialKeywords.HornApiException;
import eu.lightest.verifier.model.format.AbstractFormatParser;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.ReportStatus;
import eu.lightest.verifier.wrapper.XMLUtil;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class fidoMappingFormat extends AbstractFormatParser {
    
    private static final String FORMAT_ID = "simpleFidoMapping";
    private static final String LEVEL_MEDIUM = "medium";
    private static final String LEVEL_LOW = "low";
    private static final String QUERY_LOA = "loa";
    
    private static Logger logger = Logger.getLogger(fidoMappingFormat.class);
    private XMLUtil mapping;
    private String loa = null;
    
    public fidoMappingFormat(Object transaction, Report report) {
        super(transaction, report);
        
        if(transaction instanceof String) {
            try {
                this.mapping = new XMLUtil((String) transaction);
            } catch(ParserConfigurationException | IOException | SAXException e) {
                fidoMappingFormat.logger.error("Error reading fido mapping: " + e.getMessage());
                throw new IllegalArgumentException(e);
            }
            
        } else {
            fidoMappingFormat.logger.error("Transaction of type: " + transaction.getClass().toString() + ",  expected: String");
            throw new IllegalArgumentException("Transaction of type: " + transaction.getClass().toString() + ",  expected: String");
        }
    }
    
    @Override
    public boolean onExtract(List<String> path, String query, List<String> output) throws HornApiException {
        if(path.size() == 0) {
            switch(query) {
                case AbstractFormatParser.QUERY_FORMAT:
                    return true;
                case fidoMappingFormat.QUERY_LOA:
                    return this.loa != null;
            }
        }
        
        return false;
    }
    
    @Override
    public ResolvedObj resolveObj(List<String> path) {
        fidoMappingFormat.logger.info("resolveObj @ " + this.getFormatId() + ": " + String.join(".", path));
        
        switch(path.get(0)) {
            case AbstractFormatParser.QUERY_FORMAT:
                return genResolvedObj(getFormatId(), "STRING");
            case fidoMappingFormat.QUERY_LOA:
                fidoMappingFormat.logger.info("Returning LoA " + this.loa);
                return genResolvedObj(this.loa, "STRING");
        }
        
        ResolvedObj cachedResolvedObj = this.getCachedResolvedObj(path);
        return cachedResolvedObj; // might be null
    }
    
    @Override
    public boolean onLookup(List<String> pathToRequest, List<String> pathToLoA) throws HornApiException {
        // this method is not really intended for this, but we don't have a special predicate for this ...
        fidoMappingFormat.logger.info("onLookup @ " + this.getFormatId() + ":");
    
        pathToLoA.add(fidoMappingFormat.QUERY_LOA);
    
        List<String> path = new ArrayList<>();
        path.add(pathToLoA.get(0));
        path.add(simpleFidoFormat.QUERY_REQUEST);
        ResolvedObj requestObj = this.rootListener.resolveObj(path);
        
        if(requestObj.mType != simpleFidoFormat.RESOLVETYPE_FIDOREQUEST || !(requestObj.mValue instanceof FidoRequest)) {
            fidoMappingFormat.logger.error("Expected FidoRequest, but retrieved " + requestObj.mType + " / " + requestObj.mValue.getClass().getName());
            return false;
        }
        
        FidoRequest request = (FidoRequest) requestObj.mValue;
        
        this.report.addLine("Calculating LoA for " + request);
        
        try {
            this.loa = calculateLevel(request);
            this.report.addLine("LoA=" + this.loa);
            this.rootListener.addReturnValue("loa", this.loa);
    
            return true;
            //return this.loa.equals(fidoMappingFormat.LEVEL_MEDIUM);
            
        } catch(Exception e) {
            this.report.addLine(e.getMessage(), ReportStatus.FAILED);
            return false;
        }
    }
    
    private String calculateLevel(FidoRequest request) throws Exception {
        String version = this.mapping.getElementByXPath("string(/fido-mapping/@version)");
        fidoMappingFormat.logger.info("Mapping version: " + version);
        
        String idMethod = getLevel(request.idMethod, FidoRequest.QUERY_idMethod);
        String idType = getLevel(request.idType, FidoRequest.QUERY_idType);
        String authMethod = getLevel(request.authMethod, FidoRequest.QUERY_authMethod);
        String authType = getLevel(request.authType, FidoRequest.QUERY_authType);
        
        return mergeLevel(idMethod, idType, authMethod, authType);
    }
    
    private String mergeLevel(String idMethod, String idType, String authMethod, String authType) {
        if(fidoMappingFormat.LEVEL_MEDIUM.equals(idMethod) &&
                fidoMappingFormat.LEVEL_MEDIUM.equals(idType) &&
                fidoMappingFormat.LEVEL_MEDIUM.equals(authMethod) &&
                fidoMappingFormat.LEVEL_MEDIUM.equals(authType)) {
            return fidoMappingFormat.LEVEL_MEDIUM;
            
        } else {
            return fidoMappingFormat.LEVEL_LOW;
        }
    }
    
    private String getLevel(int value, String field) throws Exception {
        return getLevel(Integer.toString(value), field);
    }
    
    private String getLevel(String value, String field) throws Exception {
        String xpath = "string(/fido-mapping/";
        xpath += field;
        xpath += "[@value=\"";
        xpath += value;
        xpath += "\"])";
        
        String level = this.mapping.getElementByXPath(xpath);
        if(level == null) {
            throw new Exception("No Mapping for " + field + "=" + value);
        }
        
        fidoMappingFormat.logger.info(field + "=" + value + " => " + "LoA=" + level);
        return level;
    }
    
    @Override
    public String getFormatId() {
        return fidoMappingFormat.FORMAT_ID;
    }
    
    @Override
    public void init() throws Exception {
        
    }
}
