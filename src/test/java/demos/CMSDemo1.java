package demos;

import iaik.asn1.ObjectID;
import iaik.cms.CMSException;
import iaik.cms.SignedDataStream;
import iaik.cms.SignerInfo;
import iaik.cms.attributes.CMSContentType;
import iaik.cms.attributes.SigningTime;
import iaik.pkcs.PKCSParsingException;
import iaik.utils.Util;
import iaik.x509.X509Certificate;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;

public class CMSDemo1 {
    
    public static void main(String[] args) throws IOException, PKCSParsingException, iaik.cms.CMSException {
        String path = "src/test/exampleData/tta/azerbaijan1.tpl.p7s";
        InputStream inputStream = new FileInputStream(path);
        
        byte[] signedDataStream = CMSDemo1.getSignedDataStream(inputStream, null);
        
        System.out.println("Data: ");
        System.out.println(new String(signedDataStream));
    }
    
    
    /**
     * Parses a CMS <code>SignedData</code> object and verifies the signatures
     * for all participated signers.
     *
     * @param is the ContentInfo with inherent SignedData, as BER encoded byte array
     * @return the inherent message as byte array, or <code>null</code> if there
     * is no message included into the supplied <code>SignedData</code>
     * object
     * @throws CMSException if any signature does not verify
     * @throws IOException  if an I/O error occurs
     */
    public static byte[] getSignedDataStream(InputStream is, X509Certificate signerCert) throws IOException, PKCSParsingException, iaik.cms.CMSException {
        
        // we are testing the stream interface
        //ByteArrayInputStream is = new ByteArrayInputStream(signedData);
        // create the ContentInfo object
        SignedDataStream signed_data = new SignedDataStream(is);
        
        
        // get an InputStream for reading the signed content
        InputStream data = signed_data.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Util.copyStream(data, os, null);
        
        System.out.println("SignedData contains the following signer information:");
        SignerInfo[] signer_infos = signed_data.getSignerInfos();
        
        for(int i = 0; i < signer_infos.length; i++) {
            try {
                // verify the signed data using the SignerInfo at index i
                X509Certificate signer_cert = signed_data.verify(i);
                // if the signature is OK the certificate of the signer is returned
                System.out.println(" Signature OK from signer: " + signer_cert.getSubjectDN());
                // get signed attributes
                SigningTime signingTime = (SigningTime) signer_infos[i].getSignedAttributeValue(ObjectID.signingTime);
                if(signingTime != null) {
                    System.out.println(" This message has been signed at " + signingTime.get());
                }
                CMSContentType contentType = (CMSContentType) signer_infos[i].getSignedAttributeValue(ObjectID.contentType);
                if(contentType != null) {
                    System.out.println(" The content has CMS content type " + contentType.get().getName());
                }
                
            } catch(SignatureException ex) {
                // if the signature is not OK a SignatureException is thrown
                System.out.println(" Signature ERROR from signer: " + signed_data.getCertificate((signer_infos[i].getSignerIdentifier())).getSubjectDN());
                throw new CMSException(ex.toString());
            }
        }
        
        if(signerCert != null) {
            // now check alternative signature verification
            System.out.println("Checking the signature using the given cert:");
            try {
                SignerInfo signer_info = signed_data.verify(signerCert);
                // if the signature is OK the certificate of the signer is returned
                System.out.println("Signature OK from signer: " + signed_data.getCertificate(signer_info.getSignerIdentifier()).getSubjectDN());
                
            } catch(SignatureException ex) {
                // if the signature is not OK a SignatureException is thrown
                System.out.println("Signature ERROR from signer: " + signerCert.getSubjectDN());
                throw new CMSException(ex.toString());
            }
            
        }
        
        return os.toByteArray();
    }
}
