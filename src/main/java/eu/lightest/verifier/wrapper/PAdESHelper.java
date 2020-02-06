package eu.lightest.verifier.wrapper;

import iaik.cms.CMSSignatureException;
import iaik.pdf.asn1objects.RevocationInfoArchival;
import iaik.pdf.cmscades.CmsCadesException;
import iaik.pdf.signature.*;
import iaik.security.provider.IAIK;
import iaik.tsp.TspVerificationException;
import iaik.x509.X509Certificate;
import iaik.x509.ocsp.CertStatus;
import org.apache.log4j.Logger;

import java.io.*;
import java.security.Security;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Calendar;

public class PAdESHelper extends AdvancedDocHelper {
    
    private static Logger logger = Logger.getLogger(PAdESHelper.class);
    private final InputStream fileToBeVerified;
    private X509Certificate signerCert;
    
    private boolean alreadyVerified = false;
    private boolean allSigsOK = false;
    
    public PAdESHelper(byte[] pdfContent) {
        this.fileToBeVerified = new ByteArrayInputStream(pdfContent);
    }
    
    public PAdESHelper(File pdfFile) throws FileNotFoundException {
        this.fileToBeVerified = new FileInputStream(pdfFile);
    }
    
    @Override
    public boolean verify() {
        return verify(false);
    }
    
    @Override
    public boolean verify(boolean skipSignatureValidation) {
        if(this.alreadyVerified) {
            return this.allSigsOK;
        }
    
        try {
            return verifyFile(skipSignatureValidation);
        } catch(IOException | PdfSignatureException e) {
            PAdESHelper.logger.error("PDF verification failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean verifyFile(boolean skipSignatureValidation) throws IOException, PdfSignatureException {
        Security.addProvider(new IAIK());
        PdfSignatureInstance signatureInstance = PdfSignatureEngine.getInstance();
    
        if(this.fileToBeVerified == null) {
            PAdESHelper.logger.error("No files to be verified given.");
            return false;
        }
    
        if(signatureInstance == null) {
            PAdESHelper.logger.error("Could not initialize PdfSignatureInstance.");
            return false;
//            PAdESHelper.logger.warn("PdfSignatureEngine failed. Initializing PdfSignatureInstancePdfbox manually ...");
//            // PdfSignatureEngine sometimes fails although pdfbox is present, so lets construct it directly.
//            // (ATV's pdfboxExistCheck precheck makes sure pdfbox is present here.)
//            signatureInstance = new PdfSignatureInstancePdfbox();
        }
    
        // initialize engine with path of signed pdf (to be verified)
        PAdESHelper.logger.info("  Initialize engine with path of signed pdf ...");
        signatureInstance.initVerify(this.fileToBeVerified, null);
        
        // this is a very rudimental signature verification, that only checks each
        // signature value
        PAdESHelper.logger.info("  Signature verification ...");
        signatureInstance.verify();
        this.allSigsOK = true;
    
        PAdESHelper.logger.info("  Testing all signatures ...");
        PdfSignatureDetails[] signatures = signatureInstance.getSignatures();
        for(int i = 0; i < signatures.length; i++) {
            PdfSignatureDetails sig = signatures[i];
            try {
                PAdESHelper.logger.info("    Testing signature #" + (i + 1) + " of " + signatures.length + " ...");
                testSig(sig, skipSignatureValidation);
                
            } catch(CmsCadesException | CMSSignatureException | CertificateNotYetValidException | CertificateExpiredException | TspVerificationException e) {
                PAdESHelper.logger.error("    Could not test signature #" + (i + 1) + ": " + e.getMessage());
                this.allSigsOK = false;
            }
        }
    
        this.alreadyVerified = true;
        return this.allSigsOK;
    }
    
    private void testSig(PdfSignatureDetails sig, boolean skipSignatureValidation) throws PdfSignatureException, CmsCadesException, CMSSignatureException, CertificateNotYetValidException, CertificateExpiredException, TspVerificationException {
        // test signature details if signature is an approval signature (or a
        // certification signature)
        if(sig instanceof ApprovalSignature) {
            ApprovalSignature sigApp = (ApprovalSignature) sig;
    
            X509Certificate certificate = sigApp.getSignerCertificate();
            if(skipSignatureValidation) {
                // lightweight mode, e.g. to extract the cert ...
                this.signerCert = certificate;
                return;
            }
    
            Calendar signatureDate = sigApp.getSigningTime();
    
            PAdESHelper.logger.info("      Verifying cert    " + certificate.getSubjectDN());
            PAdESHelper.logger.info("      issued by         " + certificate.getIssuerDN());
            PAdESHelper.logger.info("      for doc signed at " + signatureDate.getTime());
    
            PAdESHelper.logger.info("      Signature signed by: " + certificate.getSubjectDN().toString());
            sigApp.verifySignatureValue();
            PAdESHelper.logger.info("      CHECK: Signature valid.");
            
            // check validity of certificate at signing time
            certificate.checkValidity(signatureDate.getTime());
    
    
            PAdESHelper.logger.info("      CHECK: Certificate valid at signing time.");
            
            if(sigApp.getSignatureTimeStampToken() != null) {
                sigApp.verifySignatureTimestampImprint();
                PAdESHelper.logger.info("      CHECK: Timestamp signature valid.");
            }
            
            RevocationInfoArchival revocationInfo = sigApp.getRevocationInformation();
            if(revocationInfo != null) {
                CertStatus certStatus = sigApp.getOcspRevocationStatus();
                if(certStatus != null && certStatus.getCertStatus() != CertStatus.GOOD
                        || sigApp.getCrlRevocationStatus()) {
                    PAdESHelper.logger.error("      Signer certificate has been revoked");
                } else {
                    PAdESHelper.logger.info("      CHECK: Signer certificate valid (not revoked)");
                }
            } else {
                PAdESHelper.logger.info("      No revocation, so nothing to do here.");
            }
    
            setVerificationResult(certificate, true);
            this.signerCert = certificate;
            
        } else {
            PAdESHelper.logger.info("      Signature is not an approval signature, so nothing to do here.");
        }
        
        // if PDF has been certified, you can also get some more infos
        if(sig instanceof CertificationSignature) {
            PAdESHelper.logger.error("      PDF is certified, but retrieval of infos NOT IMPLEMENTED. ");
            //getCertificationInfos((CertificationSignature) sig, signatures, fileToBeVerified);
        }
        
        if(sig.isModified()) {
            PAdESHelper.logger.warn("      Signature " + sig.getName() + " has been modified.");
        } else {
            PAdESHelper.logger.info("      CHECK: Signature not modified.");
        }
    }
    
    @Override
    public X509Certificate getCertificate() {
        if(this.signerCert == null) {
            PAdESHelper.logger.error("No certificate available. Run verify() first.");
            return null;
        }
        
        return this.signerCert;
    }
}
