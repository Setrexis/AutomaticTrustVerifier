package eu.lightest.verifier.model.format.advancedDoc;

import eu.lightest.horn.specialKeywords.HornApiException;
import eu.lightest.verifier.model.format.AbstractFormatParser;
import eu.lightest.verifier.model.format.FormatParser;
import eu.lightest.verifier.model.format.eIDAS_qualified_certificate.EidasCertFormat;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.ReportStatus;
import eu.lightest.verifier.wrapper.AdvancedDocHelper;
import eu.lightest.verifier.wrapper.XAdESHelper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.cert.X509Certificate;
import java.util.List;

public class XAdESFormat extends AbstractFormatParser {
    
    private static final String PATH_CERT = "certificate";
    private static final String FORMAT_ID = "xades";
    
    private static Logger logger = Logger.getLogger(XAdESFormat.class);
    private AdvancedDocHelper doc = null;
    
    public XAdESFormat(Object transactionFile, Report report) {
        super(transactionFile, report);
        
        if(transactionFile instanceof File) {
            XAdESFormat.logger.info("Initializing XAdES format from file ...");
            try {
                this.doc = new XAdESHelper((File) transactionFile);
            } catch(FileNotFoundException e) {
                XAdESFormat.logger.error(e.getMessage());
                throw new IllegalArgumentException(e);
            }
            
        } else if(transactionFile instanceof byte[]) {
            XAdESFormat.logger.info("Initializing XAdES format from byte[] variable ...");
            this.doc = new XAdESHelper((byte[]) transactionFile);
            
        } else {
            XAdESFormat.logger.error("Transaction of type:" + transactionFile.getClass().toString() + ",  expected: File");
            throw new IllegalArgumentException("Transaction of type:" + transactionFile.getClass().toString() + ",  expected: File");
        }
    }
    
    @Override
    public boolean onExtract(List<String> path, String query, List<String> output) throws HornApiException {
        if(path.size() == 0) {
            switch(query) {
                case XAdESFormat.PATH_CERT:
                case AbstractFormatParser.QUERY_FORMAT:
                    return true;
            }
        }
        
        String parserId = path.get(0);
        XAdESFormat.logger.info("delegating to parser: " + parserId);
        return getParser(parserId).onExtract(pop(path), query, output);
    }
    
    @Override
    public boolean onVerifySignature(List<String> pathToSubject, List<String> pathToCert) throws HornApiException {
        
        if(pathToSubject.size() == 0) {
            
            ResolvedObj sigObj = this.rootListener.resolveObj(pathToCert);
            if(sigObj == null || !sigObj.mType.equals(EidasCertFormat.RESOLVETYPE_X509CERT) || !(sigObj.mValue instanceof X509Certificate)) {
                XAdESFormat.logger.error("Could not resolve certificate from " + String.join(".", pathToCert));
                this.report.addLine("Signature Verification failed: Certificate error.", ReportStatus.FAILED);
                return false;
            }
            
            X509Certificate cert = (X509Certificate) sigObj.mValue;
            XAdESFormat.logger.info("Verifying signature using cert: " + cert.getSubjectDN());
            
            boolean verificationOK = this.doc.verify((iaik.x509.X509Certificate) cert);
            if(!verificationOK) {
                this.report.addLine("Signature Verification failed.", ReportStatus.FAILED);
                return false;
            }
    
            this.report.addLine("XAdES Signature Verification successful.");
            return true;
            
        } else if(pathToSubject.size() == 1) {
            String parserId = pathToSubject.get(0);
            XAdESFormat.logger.info("delegating to parser: " + parserId);
            return getParser(parserId).onVerifySignature(pop(pathToSubject), pathToCert);
        }
        
        XAdESFormat.logger.warn("Invalid path: " + String.join(".", pathToSubject));
        return false;
    }
    
    @Override
    public ResolvedObj resolveObj(List<String> path) {
        //AH19Format.logger.info("resolveObj: " + String.join(".", path));
        
        if(path.size() > 1) {
            String parserId = path.get(0);
            FormatParser parser = getParser(parserId);
            
            return parser.resolveObj(pop(path));
        }
        
        switch(path.get(0)) {
            case XAdESFormat.PATH_CERT:
                return genResolvedObj(this.doc.getCertificate(), EidasCertFormat.RESOLVETYPE_X509CERT);
            case AbstractFormatParser.QUERY_FORMAT:
                return genResolvedObj(getFormatId(), "STRING");
        }
        
        return null;
    }
    
    @Override
    public String getFormatId() {
        return XAdESFormat.FORMAT_ID;
    }
    
    @Override
    public void init() throws Exception {
        // verify the doc and store the verification result ...
        this.doc.verify();
    }
    
}
