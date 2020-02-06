package eu.lightest.verifier.wrapper;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PAdESHelperTest {
    
    private static String PDF_FILE_CORREOS1 = "src/test/correos/document1.pdf";
    
    @Test
    public void correos1fromFile() throws IOException {
        PAdESHelper pades = new PAdESHelper(new File(PAdESHelperTest.PDF_FILE_CORREOS1));
        
        assertTrue(pades.verify());
        
        X509Certificate signerCert = pades.getCertificate();
        assertNotNull(signerCert);
        
    }
    
    @Test
    public void correos1fromArray() throws IOException {
        PAdESHelper pades = new PAdESHelper(bytesFromFile(PAdESHelperTest.PDF_FILE_CORREOS1));
        
        assertTrue(pades.verify());
        
        X509Certificate signerCert = pades.getCertificate();
        assertNotNull(signerCert);
        
    }
    
    private byte[] bytesFromFile(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }
    
}