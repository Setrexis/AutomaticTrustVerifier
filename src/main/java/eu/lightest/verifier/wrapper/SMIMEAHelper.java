package eu.lightest.verifier.wrapper;

import eu.lightest.verifier.exceptions.DNSException;
import iaik.utils.Util;
import iaik.x509.X509Certificate;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.List;

public class SMIMEAHelper {
    
    private static Logger logger = Logger.getLogger(SMIMEAHelper.class);
    
    /**
     * Authenticating of a (XAdES) signed XML Document using DNSSEC / DANE.
     * Performs two steps:
     * <b>Step 1:</b> Verifies that the given Document document is signed and that the signature are valid.
     * <b>Step 2: </b> Verifies that the cert used to sign the given Document is equal to the one represented by
     * the SMIMEA records stored in the given hostname.
     *
     * @param hostname Hostname of the Document's SMIMEA records.
     * @param document The signed XML Document (e.g. a TSL or a TrustTranslation.)
     * @return <i>true</i> if all verifications succeeded.
     * @throws IOException
     */
    public static boolean verifyXMLdocument(String hostname, String document) throws IOException {
        NameResolverHelper dns = new GNSHelper();
        
        List<SMIMEAcert> smimeas = null;
        try {
            smimeas = dns.querySMIMEA(hostname);
        } catch(IOException | DNSException e) {
            SMIMEAHelper.logger.error("Loading of SMIMEA failed.");
            SMIMEAHelper.logger.error("", e);
            return false;
        }
        
        int numCerts = smimeas.size();
        
        if(numCerts <= 0) {
            SMIMEAHelper.logger.error("Found no SMIMEA records.");
            return false; // TODO set to true to work with localhost test setup
            
        } else { // numCerts > 0
            // tracking: https://extgit.iaik.tugraz.at/LIGHTest/AutomaticTrustVerifier/issues/3
            
            X509Certificate signingCert = SMIMEAHelper.verifyDocContent(document);
            if(signingCert == null) {
                SMIMEAHelper.logger.error("Verification of signature of Document failed.");
                return false;
            }
            
            SMIMEAHelper.logger.info("Found " + numCerts + " SMIMEA record(s) for this Document: ");
            
            boolean verificationStatus = false;
            
            for(SMIMEAcert record : smimeas) {
                record.init();
                
                verificationStatus = SMIMEAHelper.verifyDocCert(signingCert, record);
                if(verificationStatus == true) {
                    // we found at least one record the authenticates the cert used to sign the doc
                    break;
                }
                SMIMEAHelper.logger.info("---");
            }
            
            if(verificationStatus == true) {
                return true;
                
            } else {
                SMIMEAHelper.logger.error("Verification of Document cert failed, no chain from DNS.");
                return false;
            }
        }
    }
    
    
    private static X509Certificate verifyDocContent(String document) {
        XAdESHelper xades = new XAdESHelper(document, null);
        boolean docVerificationStatus = xades.verify();
    
        if(docVerificationStatus == false) {
            return null;
        }
    
        X509Certificate signerCert = xades.getCertificate();
    
        SMIMEAHelper.logger.info("Document signed by:   " + signerCert.getSubjectDN());
        SMIMEAHelper.logger.info("    Cert Fingerprint: " + Util.toString(signerCert.getFingerprintSHA()));
    
        SMIMEAHelper.logger.info("CHECK 1/2 PASSED: Document signature valid.");
    
        return signerCert;
    }
    
    private static boolean verifyDocCert(X509Certificate signingCert, SMIMEAcert record) {
        try {
            boolean certsMatch = record.match(signingCert);
            if(certsMatch) {
                SMIMEAHelper.logger.info("CHECK 2/2 PASSED: Document is signed by cert pinned in DNS.");
                return true;
            }
        } catch(CertificateEncodingException | NoSuchAlgorithmException e) {
            SMIMEAHelper.logger.error("Could not parse cert from Document: " + e.getMessage());
        }
        return false;
    }
}
