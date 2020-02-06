package eu.lightest.verifier.model.format.advancedDoc;

import eu.lightest.horn.specialKeywords.HornApiException;
import eu.lightest.verifier.model.format.AbstractFormatParser;
import eu.lightest.verifier.model.format.Delegation.DelegationXMLFormat;
import eu.lightest.verifier.model.format.FormatParser;
import eu.lightest.verifier.model.format.eIDAS_qualified_certificate.EidasCertFormat;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.ReportStatus;
import eu.lightest.verifier.model.transaction.ASiCSignature;
import eu.lightest.verifier.model.transaction.TransactionContainer;
import eu.lightest.verifier.model.transaction.TransactionFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.List;

public class PAdESinASICFormat extends AbstractFormatParser {
    
    private static final String PATH_CERT = "certificate";
    private static final String PATH_DELEGATION = "delegation";
    private static final String PATH_DOCUMENT = "pdf";
    private static final String FORMAT_ID = "padesInAsic";
    private static Logger logger = Logger.getLogger(PAdESinASICFormat.class);
    private static String RESOLVETYPE_PDF = "doc_pdf";
    private TransactionContainer transaction = null;
    private Object pdf;
    
    public PAdESinASICFormat(Object transactionFile, Report report) {
        super(transactionFile, report);
        if(transactionFile instanceof File) {
            this.transaction = TransactionFactory.getTransaction((File) transactionFile);
        } else {
            throw new IllegalArgumentException("Transaction of type:" + transactionFile.getClass().toString() + ",  expected: File");
        }
    }
    
    @Override
    public boolean onExtract(List<String> path, String query, List<String> output) throws HornApiException {
        if(path.size() == 0) {
            switch(query) {
                case PAdESinASICFormat.PATH_CERT:
                case AbstractFormatParser.QUERY_FORMAT:
                case PAdESinASICFormat.PATH_DOCUMENT:
                    return true;
                case PAdESinASICFormat.PATH_DELEGATION:
                    this.transaction.getFileList().contains("delegation.xml");
            }
        }
        
        String parserId = path.get(0);
        PAdESinASICFormat.logger.info("delegating to parser: " + parserId);
        return getParser(parserId).onExtract(pop(path), query, output);
    }
    
    @Override
    public boolean onVerifySignature(List<String> pathToSubject, List<String> pathToCert) throws HornApiException {
        PAdESinASICFormat.logger.info("onVerifySignature @ " + this.getFormatId());
        
        if(pathToSubject.size() == 0) {
            
            ResolvedObj sigObj = this.rootListener.resolveObj(pathToCert);
            if(sigObj == null || !sigObj.mType.equals(EidasCertFormat.RESOLVETYPE_X509CERT) || !(sigObj.mValue instanceof X509Certificate)) {
                PAdESinASICFormat.logger.error("Could not resolve certificate from " + String.join(".", pathToCert));
                this.report.addLine("Signature Verification failed: Certificate error.", ReportStatus.FAILED);
                return false;
            }
            
            X509Certificate cert = (X509Certificate) sigObj.mValue;
            PAdESinASICFormat.logger.info("Verifying signature using cert: " + cert.getSubjectDN());
            
            for(ASiCSignature signature : this.transaction.getSignatures()) {
                if(signature.getSigningX509Certificate().equals(cert)) {
                    PAdESinASICFormat.logger.info("Found signature for given cert.");
                    if(!this.transaction.verifySignature(cert, signature)) {
                        this.report.addLine("Signature Verification failed.", ReportStatus.FAILED);
                        return false;
                    }
                }
            }
            
            this.report.addLine("PAdES Container Signature Verification successful.");
            return true;
            
        }
        
        String parserId = pathToSubject.get(0);
        PAdESinASICFormat.logger.info("delegating to parser: " + parserId);
        FormatParser parser = getParser(parserId);
        if(parser != null) {
            return parser.onVerifySignature(pop(pathToSubject), pathToCert);
        }
        
        PAdESinASICFormat.logger.warn("Invalid path: " + String.join(".", pathToSubject));
        return false;
    }
    
    @Override
    public ResolvedObj resolveObj(List<String> path) {
        PAdESinASICFormat.logger.info("resolveObj @ " + this.getFormatId());
        
        if(path.size() > 1) {
            String parserId = path.get(0);
            FormatParser parser = getParser(parserId);
            
            return parser.resolveObj(pop(path));
        }
        
        switch(path.get(0)) {
            case PAdESinASICFormat.PATH_DOCUMENT:
                return genResolvedObj(this.pdf, PAdESinASICFormat.RESOLVETYPE_PDF);
            case PAdESinASICFormat.PATH_CERT:
                return genResolvedObj(this.transaction.getSigningCertificate(), EidasCertFormat.RESOLVETYPE_X509CERT);
            case AbstractFormatParser.QUERY_FORMAT:
                return genResolvedObj(getFormatId(), "STRING");
            case PAdESinASICFormat.PATH_DELEGATION:
                List<String> files = this.transaction.getFileList();
                for(String f : files) {
                    if(f.contains("delegation.xml") == true) {
                        return genResolvedObj(this.transaction.extractFileString(f), DelegationXMLFormat.RESOLVETYPE_DELEGATION);
                    }
                }
        }
        
        return null;
    }
    
    private Object getFirstPdf() {
        String firstPdfName = getFirstPdfName();
        if(firstPdfName == null) {
            PAdESinASICFormat.logger.error("No PDF found ...");
            return null;
        }
        
        PAdESinASICFormat.logger.info("Extracting " + firstPdfName);
        
        return this.transaction.extractFileBytes(firstPdfName);
    }
    
    private String getFirstPdfName() {
        String pdfName = null;
        
        for(String file : this.transaction.getFileList()) {
            if(file.endsWith(".pdf")) {
                pdfName = file;
                break;
            }
        }
        
        return pdfName;
    }
    
    @Override
    public String getFormatId() {
        return PAdESinASICFormat.FORMAT_ID;
    }
    
    @Override
    public void init() throws Exception {
        this.pdf = this.getFirstPdf();
    }
    
}
