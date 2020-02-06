package eu.lightest.verifier.model.format.Delegation;

import eu.lightest.horn.specialKeywords.HornApiException;
import eu.lightest.verifier.model.format.AbstractFormatParser;
import eu.lightest.verifier.model.format.FormatParser;
import eu.lightest.verifier.model.report.Report;
import org.apache.log4j.Logger;

import java.util.List;


public class DPFormat extends AbstractFormatParser {
    
    private static final String FORMATID = "dp_format";
    private static final Logger mDPFormatLogger = Logger.getLogger(DPFormat.class);
    private String mDPUrl = null;
    
    public DPFormat(Object transaction, Report report) {
        super(transaction, report);
        
        if(transaction instanceof String) {
            this.mDPUrl = (String) transaction;
        }
    }
    
    @Override
    public String getFormatId() {
        return DPFormat.FORMATID;
    }
    
    @Override
    public void init() throws Exception {
        
        if(this.mDPUrl == null) {
            throw new Exception("Error while parsing form. Wrong format? (No valid URL!)");
        }
    }
    
    @Override
    public boolean onExtract(List<String> path, String query, List<String> output) throws HornApiException {
        if(path.size() == 1) {
            switch(query) {
                case "format":
                case "fingerprint":
                    return true;
            }
            return false;
        }
        
        String pId = path.get(0);
        DPFormat.mDPFormatLogger.debug("Parser ID: " + pId);
        
        return getParser(pId).onExtract(pop(path), query, output);
    }
    
    @Override
    public ResolvedObj resolveObj(List<String> path) {
        
        if(path.size() > 1) {
            String parserId = path.get(0);
            FormatParser parser = getParser(parserId);
            
            return parser.resolveObj(pop(path));
        }
        
        switch(path.get(0)) {
            case "format":
                return genResolvedObj(getFormatId(), "STRING");
            case "fingerprint":
                return genResolvedObj("CALCULATE FINGERPRINT HERE!", "STRING");
        }
        return null;
    }
    
    @Override
    public boolean onLookup(List<String> pathToDomain, List<String> pathToLoadedDoc) throws HornApiException {
        return false;
    }
}
