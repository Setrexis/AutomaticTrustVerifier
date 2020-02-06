package eu.lightest.verifier.wrapper;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;

import static org.junit.Assert.*;

public class XAdESHelperTest {
    
    
    private static String scheme_path = "src/test/exampleData/TSL/ts_119612v020201_201601xsd.xsd";
    
    private static String XML_FILE_EIDAS_AT = "src/test/exampleData/TSL/eidas_AT_currenttl.xml";
    private static String XML_FILE_EIDAS_AT_CERTFAIL = "src/test/exampleData/TSL/eidas_AT_currenttl_CERTFAIL.xml";
    private static String XML_FILE_EIDAS_AT_SIGFAIL = "src/test/exampleData/TSL/eidas_AT_currenttl_SIGFAIL.xml";
    private static String XML_FILE_UPRC_SP1 = "src/test/exampleData/TSL/uprc_NEW_SP1.xml";
    private static String XML_FILE_UPRC_SP1_BROKEN = "src/test/exampleData/TSL/uprc_NEW_SP1_BROKEN.xml";
    private static String XML_FILE_TUBITAK = "src/test/exampleData/TSL/TR_eIDAS_eSignature_2019-11-27_TUG_signed.xml";
    private static String XML_FILE_UNHCRnew1 = "src/test/unhcr/unhcr-federation-new1.dafi-demo.xml";
    private static String XML_FILE_UNHCR6 = "src/test/unhcr/unhcr-federation-6.dafi-demo.xml";
    private static String XML_FILE_UNHCR6BROKEN = "src/test/unhcr/unhcr-federation-6.dafi-demo_BROKEN.xml";
    
    
    @Test
    public void eidasAT_OK_withoutSchema() throws IOException {
        String xml_content = stringFromFile(XAdESHelperTest.XML_FILE_EIDAS_AT);
        XAdESHelper xades = new XAdESHelper(xml_content, XAdESHelperTest.scheme_path);
        
        xades.setValidateSchema(false); // this is the default, but  just to be sure
        
        assertTrue(xades.verify());
        
        X509Certificate signerCert = xades.getCertificate();
        assertNotNull(signerCert);
        
        System.out.println(signerCert.getSubjectDN());
    }
    
    @Test
    public void eidasAT_OK_withSchema() throws IOException {
        String xml_content = stringFromFile(XAdESHelperTest.XML_FILE_EIDAS_AT);
        XAdESHelper xades = new XAdESHelper(xml_content, XAdESHelperTest.scheme_path);
        
        xades.setValidateSchema(true);
        
        assertTrue(xades.verify());
        
        X509Certificate signerCert = xades.getCertificate();
        assertNotNull(signerCert);
        
        System.out.println(signerCert.getSubjectDN());
    }
    
    @Test
    public void uprcSP1_OK() throws IOException {
        String xml_content = stringFromFile(XAdESHelperTest.XML_FILE_UPRC_SP1);
        
        XAdESHelper xades = new XAdESHelper(xml_content, XAdESHelperTest.scheme_path);
        assertTrue(xades.verify());
        
        X509Certificate signerCert = xades.getCertificate();
        assertNotNull(signerCert);
        
        System.out.println(signerCert.getSubjectDN());
    }
    
    @Test
    public void uprcSP1_FAIL() throws IOException {
        String xml_content = stringFromFile(XAdESHelperTest.XML_FILE_UPRC_SP1_BROKEN);
        
        XAdESHelper xades = new XAdESHelper(xml_content, XAdESHelperTest.scheme_path);
        assertFalse(xades.verify());
        
        X509Certificate signerCert = xades.getCertificate();
        assertNotNull(signerCert);
    }
    
    @Test
    public void eidasAT_CERTFAIL() throws IOException {
        String xml_content = stringFromFile(XAdESHelperTest.XML_FILE_EIDAS_AT_CERTFAIL);
        
        XAdESHelper xades = new XAdESHelper(xml_content, XAdESHelperTest.scheme_path);
        assertFalse(xades.verify());
        
        X509Certificate signerCert = xades.getCertificate();
        assertNull(signerCert);
    }
    
    @Test
    public void eidasAT_SIGFAIL() throws IOException {
        String xml_content = stringFromFile(XAdESHelperTest.XML_FILE_EIDAS_AT_SIGFAIL);
        
        XAdESHelper xades = new XAdESHelper(xml_content, XAdESHelperTest.scheme_path);
        assertFalse(xades.verify());
        
        X509Certificate signerCert = xades.getCertificate();
        assertNotNull(signerCert);
    }
    
    @Test
    public void tubitakPilot1() throws IOException {
        String xml_content = stringFromFile(XAdESHelperTest.XML_FILE_TUBITAK);
        XAdESHelper xades = new XAdESHelper(xml_content, XAdESHelperTest.scheme_path);
    
        xades.setValidateSchema(false); // this is the default, but  just to be sure
    
        assertTrue(xades.verify());
    
        X509Certificate signerCert = xades.getCertificate();
        assertNotNull(signerCert);
    
        System.out.println(signerCert.getSubjectDN());
    }
    
    
    @Test
    public void unhcr6() throws IOException {
        // positive test
        String xml_content = stringFromFile(XAdESHelperTest.XML_FILE_UNHCR6);
        XAdESHelper xades = new XAdESHelper(xml_content, XAdESHelperTest.scheme_path);
        
        xades.setValidateSchema(false);
        
        assertTrue("expected a valid signature here, but it failed.", xades.verify());
        
        X509Certificate signerCert = xades.getCertificate();
        assertNotNull(signerCert);
        
        System.out.println(signerCert.getSubjectDN());
    }
    
    @Test
    public void unhcr6broken() throws IOException {
        // negative test
        String xml_content = stringFromFile(XAdESHelperTest.XML_FILE_UNHCR6BROKEN);
        XAdESHelper xades = new XAdESHelper(xml_content, XAdESHelperTest.scheme_path);
        
        xades.setValidateSchema(false);
        
        assertFalse("expected a wrong signature here, but it was valid.", xades.verify());
        
        X509Certificate signerCert = xades.getCertificate();
        assertNotNull(signerCert);
        
        System.out.println(signerCert.getSubjectDN());
    }
    
    private String stringFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}