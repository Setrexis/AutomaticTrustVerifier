package demos;

import iaik.cms.CMSSignatureException;
import iaik.pdf.asn1objects.RevocationInfoArchival;
import iaik.pdf.cmscades.CmsCadesException;
import iaik.pdf.parameters.LegalContentAttestation;
import iaik.pdf.signature.*;
import iaik.security.provider.IAIK;
import iaik.tsp.TspVerificationException;
import iaik.x509.X509Certificate;
import iaik.x509.ocsp.CertStatus;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Calendar;

public class PAdESDemo {
    
    private static PdfSignatureInstance signatureInstance_;
    
    public static void main(String[] args) throws CMSSignatureException, PdfSignatureException, IOException, CmsCadesException, CertificateException, TspVerificationException {
        Security.addProvider(new IAIK());
        PAdESDemo.signatureInstance_ = PdfSignatureEngine.getInstance();
        
        String demo_doc = "src/test/correos/document1.pdf";
        
        PAdESDemo.verifySignedPdf(demo_doc);
    }
    
    /**
     * Verify given signed PDF document.
     *
     * @param fileToBeVerified the signed or certified PDF document.
     * @throws IOException              if the signed document can't be read
     * @throws PdfSignatureException    if errors during verification occur
     * @throws CmsCadesException        if the signature is invalid or certificates are revoked or missing
     * @throws TspVerificationException if timestamp is invalid
     * @throws CertificateException     if certificate already expired
     */
    private static void verifySignedPdf(String fileToBeVerified)
            throws IOException, PdfSignatureException, CmsCadesException, CMSSignatureException,
            TspVerificationException, CertificateException {
        
        // initialize engine with path of signed pdf (to be verified)
        PAdESDemo.signatureInstance_.initVerify(fileToBeVerified, null);
        
        System.out.println("\n#### verifying file " + fileToBeVerified + " ... ####\n");
        
        // this is a very rudimental signature verification, that only checks each
        // signature value
        PAdESDemo.signatureInstance_.verify();
        
        
        PdfSignatureDetails[] signatures = PAdESDemo.signatureInstance_.getSignatures();
        for(int i = 0; i < signatures.length; i++) {
            PdfSignatureDetails sig = signatures[i];
            
            // test signature details if signature is an approval signature (or a
            // certification signature)
            if(sig instanceof ApprovalSignature) {
                System.out.println("signature is an approval signature, lets test them ...");
                
                ApprovalSignature sigApp = (ApprovalSignature) sig;
                System.out.println("signature " + (i + 1) + " of " + signatures.length
                        + " signed by: " + sigApp.getSignerCertificate().getSubjectDN().toString());
                sigApp.verifySignatureValue();
                System.out.println("OK: signature valid.");
                
                // check validity of certificate at signing time
                X509Certificate certificate = sigApp.getSignerCertificate();
                Calendar signatureDate = sigApp.getSigningTime();
                certificate.checkValidity(signatureDate.getTime());
                System.out.println("OK: certificate valid at signing time.");
                
                if(sigApp.getSignatureTimeStampToken() != null) {
                    sigApp.verifySignatureTimestampImprint();
                    System.out.println("OK: timestamp signature valid.");
                } else {
                    System.out.println("FYI: no timestamp signature, so nothing to do here.");
                }
                
                RevocationInfoArchival revocationInfo = sigApp.getRevocationInformation();
                if(revocationInfo != null) {
                    CertStatus certStatus = sigApp.getOcspRevocationStatus();
                    if(certStatus != null && certStatus.getCertStatus() != CertStatus.GOOD
                            || sigApp.getCrlRevocationStatus()) {
                        System.out.println("FYI: signer certificate has been revoked");
                    } else {
                        System.out.println("FYI: signer certificate valid (not revoked)");
                    }
                } else {
                    System.out.println("FYI: no revocation, so nothing to do here.");
                }
                
            } else {
                System.out.println("FYI: signature is not an approval signature, so nothing to do here.");
            }
            
            // if PDF has been certified, you can also get some more infos
            if(sig instanceof CertificationSignature) {
                PAdESDemo.getCertificationInfos((CertificationSignature) sig, signatures, fileToBeVerified);
            } else {
                System.out.println("FYI: PDF is not certified, so nothing to do here.");
            }
            
            if(sig.isModified()) {
                System.out.println("signature " + sig.getName() + " has been modified.");
            } else {
                System.out.println("OK: Signature not modiefied.");
            }
        }
        
    }
    
    /**
     * Extract details about the certification signature (legal content attestation, allowed
     * modifications).
     *
     * @param sig              the certification signature
     * @param signatures       all standard signatures contained in the document
     * @param fileToBeVerified the document containing the signatures to be verified
     * @throws PdfSignatureException if more than 1 certification signature is contained or actual modifications do not
     *                               correspond with settings
     * @throws IOException           if the document can't be read
     */
    private static void getCertificationInfos(CertificationSignature sig,
                                              PdfSignatureDetails[] signatures, String fileToBeVerified)
            throws PdfSignatureException, IOException {
        System.out.println("\n#### File contains a certification signature. ####\n");
        
        LegalContentAttestation lca = sig.getLegalContentAttestation();
        if(lca != null) {
            System.out
                    .println("Certification signature contains the following attestation string: "
                            + lca.getAttestationString());
        }
        
        StringWriter modRevisions = new StringWriter();
        int i = 1;
        String separator = "";
        for(PdfSignatureDetails sigCur : signatures) {
            if(sigCur.isModified()) {
                // the original version of a revision - if you want to compare them
                sigCur.getRevision(fileToBeVerified + "_rev" + i);
                
                modRevisions.write(separator);
                modRevisions.write(Integer.toString(i));
                separator = ", ";
            }
            i++;
        }
        modRevisions.flush();
        String modRevisionsString = modRevisions.toString();
        if(modRevisionsString.length() != 0) {
            System.out.println("There have been changes after the following revisions: "
                    + modRevisions.toString());
        } else {
            System.out
                    .println("Document has not been changed, after the signature was applied.");
        }
        
        CertificationSignature.ModificationPermission perms = sig.getModificationPermission();
        if(perms != null) {
            System.out.println("Certification signature permits the following changes:");
            switch(perms) {
                case NoModifications:
                    System.out.println("No modification are allowed.");
                    if(modRevisionsString.length() != 0) {
                        throw new PdfSignatureException(
                                "Document has been modified, althoug no modifications allowed.");
                    }
                    break;
                case SignaturesFormsTemplates:
                    System.out.println(
                            "Filling in forms, instantiating page templates and signing is allowed.");
                    break;
                case AnnotationsSignaturesFormsTemplates:
                    System.out.println(
                            "Filling in forms, instantiating page templates, signing and creation, deletion, and modification of annotations is allowed.");
                    break;
            }
        }
    }
}
