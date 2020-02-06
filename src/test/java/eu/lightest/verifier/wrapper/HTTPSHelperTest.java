package eu.lightest.verifier.wrapper;

import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class HTTPSHelperTest {
    
    private HTTPSHelper helper = null;
    
    
    @Before
    public void setUp() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        System.out.println("HTTPSHelperTest setup ...");
        
        this.helper = new HTTPSHelper();
    }
    
    @Test
    public void get200Test() throws IOException {
        String res1;
        res1 = this.helper.get(new URL("https://httpbin.org/base64/TElHSFRlc3QgQVRWIHRlc3Q="));
        assertEquals("LIGHTest ATV test", res1);
        
        String res2;
        res2 = this.helper.get(new URL("https://httpbin.org/base64/TElHSFRlc3QgQVRWIHRlc3Q="));
        assertNotEquals("LIGHTest ATV test with is different", res2);
    }
    
    @Test
    public void get200TestTimeout1() throws IOException {
        this.helper.setTimeout(1, TimeUnit.MILLISECONDS);
        
        String res1;
        try {
            res1 = this.helper.get(new URL("https://httpbin.org/base64/TElHSFRlc3QgQVRWIHRlc3Q="));
            assertTrue(false);
        } catch(InterruptedIOException e) {
            e.printStackTrace();
        }
        
    }
    
    @Test(expected = InterruptedIOException.class)
    public void get200TestTimeout2() throws IOException {
        this.helper.setTimeout(1, TimeUnit.MILLISECONDS);
        
        String res1;
        res1 = this.helper.get(new URL("https://httpbin.org/base64/TElHSFRlc3QgQVRWIHRlc3Q="));
        assertEquals("LIGHTest ATV test", res1);
        
        String res2;
        res2 = this.helper.get(new URL("https://httpbin.org/base64/TElHSFRlc3QgQVRWIHRlc3Q="));
        assertNotEquals("LIGHTest ATV test with is different", res2);
    }
    
    @Test
    public void get400Test() throws IOException {
        String res;
        res = this.helper.get(new URL("https://httpbin.org/status/400"));
        assertEquals(null, res);
    }
    
    @Test
    public void get404Test() throws IOException {
        String res;
        res = this.helper.get(new URL("https://httpbin.org/status/404"));
        assertEquals(null, res);
    }
    
    @Test
    public void get500Test() throws IOException {
        String res;
        res = this.helper.get(new URL("https://httpbin.org/status/500"));
        assertEquals(null, res);
    }

//    @Test
//    public void get301Test() throws IOException {
//        String res;
//        res = this.helper.get(new URL("https://httpbin.org/status/301"));
//        assertNotEquals("", res);
//    }
    
    @Test(expected = SSLHandshakeException.class)
    public void getTLSexpiredTest() throws IOException {
        this.helper.get(new URL("https://expired.badssl.com/"));
    }
    
    @Test(expected = SSLPeerUnverifiedException.class)
    public void getTLSwronghostTest() throws IOException {
        this.helper.get(new URL("https://wrong.host.badssl.com/"));
    }
    
    @Test(expected = SSLHandshakeException.class)
    public void getTLSselfsignedTest() throws IOException {
        this.helper.get(new URL("https://self-signed.badssl.com/"));
    }
    
    @Test(expected = SSLHandshakeException.class)
    public void getTLSuntrustedTest() throws IOException {
        this.helper.get(new URL("https://untrusted-root.badssl.com/"));
    }

//    @Test(expected = SSLPeerUnverifiedException.class)
//    public void getTLSrevokedTest() throws IOException {
//        // ? https://www.imperialviolet.org/2014/04/19/revchecking.html
//        this.helper.get(new URL("https://revoked.badssl.com/"));
//    }
    
    @Test
    public void getTLS10enabledTest() throws IOException {
        this.helper.get(new URL("https://tls-v1-0.badssl.com:1010/"));
    }
    
    @Test
    public void getTLS11enabledTest() throws IOException {
        this.helper.get(new URL("https://tls-v1-1.badssl.com:1011/"));
    }
    
    @Test(expected = SSLHandshakeException.class)
    public void getTLS10disabledTest() throws IOException {
        this.helper.disableOldTLS();
        this.helper.get(new URL("https://tls-v1-0.badssl.com:1010/"));
    }
    
    @Test(expected = SSLHandshakeException.class)
    public void getTLS11disabledTest() throws IOException {
        this.helper.disableOldTLS();
        this.helper.get(new URL("https://tls-v1-1.badssl.com:1011/"));
    }
    
    @Test
    public void getTLS12Test() throws IOException {
        this.helper.get(new URL("https://tls-v1-2.badssl.com:1012/"));
    }
    
    @Test
    public void getLongTest() throws IOException {
        this.helper.get(new URL("https://long-extended-subdomain-name-containing-many-letters-and-dashes.badssl.com/"));
    }
    
    @Test
    public void getEVTest() throws IOException {
        this.helper.get(new URL("https://extended-validation.badssl.com/"));
    }
}