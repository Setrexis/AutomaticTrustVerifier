package demos;

import eu.lightest.verifier.client.ATVTest;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.validation.AdvancedSignature;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.executor.ValidationLevel;
import eu.europa.esig.dss.validation.reports.DetailedReport;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.validation.reports.SimpleReport;
import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.CommonCertificateSource;

import java.io.File;

public class DSSDemo1 {
    
    public static final String PATH_PREFIX = "src/test/uprc/";
    public static final String ASIC_CADES = DSSDemo1.PATH_PREFIX + "containers/peppol-transaction.asice";
    public static final String ASIC_XADES = DSSDemo1.PATH_PREFIX + "containers/peppol-transaction-xades.asice";
    public static final String ASIC_XADES2 = ATVTest.PATH_TRANSACTION_PDFinASIC;
    
    public static void main(String[] args) {
        System.out.println("...");
        
        //File container_xades = new File(DSSDemo1.ASIC_XADES);
        //DSSDemo1.printContainer(container_xades);
        //DSSDemo1.verifyContainer(container_xades);
        
        File container_cades = new File(DSSDemo1.ASIC_CADES);
        DSSDemo1.verifyContainer(container_cades);
//
//        File container_xades2 = new File(DSSDemo1.ASIC_XADES2);
//        DSSDemo1.verifyContainer(container_xades2);
        
        System.out.println("done!");
    }
    
    private static void verifyContainer(File container) {
        System.out.println("Container: " + container.getName());
        
        // First, we need a Certificate verifier
        CertificateVerifier cv = new CommonCertificateVerifier(true);
        
        
        // We can inject several sources. eg: OCSP, CRL, AIA, trusted lists
        
        // Capability to download resources from AIA
        //cv.setDataLoader(new CommonsDataLoader());
        
        // Capability to request OCSP Responders
        //cv.setOcspSource(new OnlineOCSPSource());
        
        // Capability to download CRL
        //cv.setCrlSource(new OnlineCRLSource());
        
        
        // We also can add missing certificates
        //cv.setAdjunctCertSource(adjunctCertSource);
        
        // Here is the document to be validated (any kind of signature file)
        DSSDocument document = new FileDocument(container);
        
        System.out.println(" type: " + document.getClass().getName());
        System.out.println(" mime: " + document.getMimeType().getMimeTypeString());
        
        // We create an instance of DocumentValidator
        // It will automatically select the supported validator from the classpath
        SignedDocumentValidator documentValidator = SignedDocumentValidator.fromDocument(document);
        
        System.out.println(" validator: " + documentValidator.getClass().getName());
        
        documentValidator.setValidationLevel(ValidationLevel.BASIC_SIGNATURES);
        
        // We add the certificate verifier (which allows to verify and trust certificates)
        documentValidator.setCertificateVerifier(cv);
        
        for(AdvancedSignature signature : documentValidator.getSignatures()) {
            System.out.println(" signer: " + signature.getSigningCertificateToken().getCertificate().getSubjectDN());
        }
        
        
        CertificateToken signingCertificateToken = new CertificateToken(documentValidator.getSignatures().get(0).getSigningCertificateToken().getCertificate());
        //System.out.println(signingCertificateToken.toString(" "));
        
        // We now add trust anchors (trusted list, keystore,...)
        CommonCertificateSource trustedCertSource = new CommonCertificateSource();
        //trustedCertSource.addCertificate(signingCertificateToken);
        cv.setTrustedCertSource(trustedCertSource);
        
        // Here, everything is ready. We can execute the validation (for the example, we use the default and embedded
        // validation policy)
        Reports reports = documentValidator.validateDocument();
        
        
        // We have 3 reports
        // The diagnostic data which contains all used and static data
        DiagnosticData diagnosticData = reports.getDiagnosticData();
        
        // The detailed report which is the result of the process of the diagnostic data and the validation policy
        DetailedReport detailedReport = reports.getDetailedReport();
        
        // The simple report is a summary of the detailed report (more user-friendly)
        SimpleReport simpleReport = reports.getSimpleReport();
        
        for(String sigId : simpleReport.getSignatureIdList()) {
            System.out.println(" signature: " + sigId + ":");
            System.out.println("  signer:   " + simpleReport.getSignedBy(sigId));
            System.out.println("  status:   " + (simpleReport.isSignatureValid(sigId) ? "valid" : "invalid"));
            
            for(String error : simpleReport.getErrors(sigId)) {
                System.out.println("  error:   " + error);
            }
            
            for(String warning : simpleReport.getWarnings(sigId)) {
                System.out.println("  warning: " + warning);
            }
        }
        
        System.out.println("...");
    }
    
    private static void printContainer(File container) {
        System.out.println("Container: " + container.getName());
        
        // First, we need a Certificate verifier
        CertificateVerifier cv = new CommonCertificateVerifier(true);
        
        // Here is the document to be validated (any kind of signature file)
        DSSDocument document = new FileDocument(container);
        
        SignedDocumentValidator documentValidator = SignedDocumentValidator.fromDocument(document);
        
        // We add the certificate verifier (which allows to verify and trust certificates)
        documentValidator.setCertificateVerifier(cv);
        
        for(AdvancedSignature signature : documentValidator.getSignatures()) {
            CertificateToken token = signature.getSigningCertificateToken();
            System.out.println(" signer: " + token.getCertificate().getSubjectDN());
            System.out.println(token.toString("  "));
        }
        
        System.out.println("...");
    }
}
