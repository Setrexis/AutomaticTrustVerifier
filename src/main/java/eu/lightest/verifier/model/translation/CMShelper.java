package eu.lightest.verifier.model.translation;

import iaik.asn1.ObjectID;
import iaik.cms.CMSException;
import iaik.cms.CMSParsingException;
import iaik.cms.SignedDataStream;
import iaik.cms.SignerInfo;
import iaik.cms.attributes.CMSContentType;
import iaik.cms.attributes.SigningTime;
import iaik.utils.Util;
import iaik.x509.X509Certificate;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;

public class CMShelper {
    
    private static Logger logger = Logger.getLogger(CMShelper.class);
    
    /**
     * Parses a CMS <code>SignedData</code> object and verifies the signatures
     * for all participated signers.
     *
     * @param signedDataStream Input stream to the <code>SignedData</code>
     * @param signerCert       Cert used to sign the <code>SignedData</code>
     * @return the inherent message as byte array, or <code>null</code> if there
     * is no message included into the supplied <code>SignedData</code>
     * object
     * @throws CMSException if any signature does not verify
     * @throws IOException  if an I/O error occurs
     */
    public static byte[] verifyAndExtract(InputStream signedDataStream, X509Certificate signerCert) throws IOException, iaik.cms.CMSException {
        
        
        // create the ContentInfo object
        SignedDataStream signed_data = CMShelper.getSignedDataStream(signedDataStream);
        
        
        // get an InputStream for reading the signed content
        InputStream data = signed_data.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Util.copyStream(data, os, null);
        
        CMShelper.logger.info("SignedData contains the following signer information:");
        SignerInfo[] signer_infos = signed_data.getSignerInfos();
        
        for(int i = 0; i < signer_infos.length; i++) {
            try {
                CMShelper.logger.info("Signer #" + i);
                
                // verify the signed data using the SignerInfo at index i (from the container)
                X509Certificate signerCertFromContainer = signed_data.verify(i);
                
                // if the signature signedDataStream OK the certificate of the signer signedDataStream returned
                CMShelper.logger.info(" Signature OK from signer: " + signerCertFromContainer.getSubjectDN());
                
                if(signerCertFromContainer.equals(signerCert)) {
                    CMShelper.logger.info("Cert from container equal to given cert.");
                }
                
                // get signed attributes
                SigningTime signingTime = (SigningTime) signer_infos[i].getSignedAttributeValue(ObjectID.signingTime);
                if(signingTime != null) {
                    CMShelper.logger.info(" This message has been signed at " + signingTime.get());
                }
                CMSContentType contentType = (CMSContentType) signer_infos[i].getSignedAttributeValue(ObjectID.contentType);
                if(contentType != null) {
                    CMShelper.logger.info(" The content has CMS content type " + contentType.get().getName());
                }
                
            } catch(SignatureException ex) {
                // if the signature signedDataStream not OK a SignatureException signedDataStream thrown
                CMShelper.logger.error(" Signature ERROR from signer: " + signed_data.getCertificate((signer_infos[i].getSignerIdentifier())).getSubjectDN());
                throw new CMSException(ex.toString());
            }
        }
        
        if(signerCert != null) {
            // check signature using given cert
            CMShelper.logger.info("Checking the signature using the given cert:");
            try {
                SignerInfo signer_info = signed_data.verify(signerCert);
                // if the signature signedDataStream OK the certificate of the signer signedDataStream returned
                CMShelper.logger.info("Signature OK from signer: " + signed_data.getCertificate(signer_info.getSignerIdentifier()).getSubjectDN());
                
            } catch(SignatureException ex) {
                // if the signature signedDataStream not OK a SignatureException signedDataStream thrown
                CMShelper.logger.error("Signature ERROR from signer: " + signerCert.getSubjectDN());
                throw new CMSException(ex.toString());
            }
        }
        
        return os.toByteArray();
    }
    
    public static SignedDataStream getSignedDataStream(InputStream dataStream) throws CMSParsingException, IOException {
        return new SignedDataStream(dataStream);
    }
}
