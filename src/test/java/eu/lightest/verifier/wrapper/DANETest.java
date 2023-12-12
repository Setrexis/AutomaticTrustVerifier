package eu.lightest.verifier.wrapper;

import eu.lightest.verifier.exceptions.DNSException;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.TLSARecord;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

// using tests sites from https://www.huque.com/dane/testsite/


public class DANETest extends HTTPSHelperTest {
    
    private HTTPSHelper helper = null;
    
    @Override
    @Before
    public void setUp() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        super.setUp();
        System.out.println("DANETest setup ...");
        
        this.helper = new HTTPSHelper();
        this.helper.enableDANE();
    }
    
    @Test
    public void goodTorTest() throws IOException {
        String res1;
        res1 = this.helper.get(new URL("https://www.torproject.org"));
    }
    
    @Test
    public void goodTorWithRedirectTest() throws IOException {
        String res1;
        res1 = this.helper.get(new URL("https://torproject.org"));
    }
    
    
    @Test(expected = SSLHandshakeException.class)
    public void badhashTest() throws IOException {
        // The signed TLSA record (DANE-EE) contains a hash value that doesn't match the server certificate.
        
        String res1;
        res1 = this.helper.get(new URL("https://badhash.dane.huque.com/"));
    }
    
    @Test(expected = SSLHandshakeException.class)
    public void badparamTest() throws IOException {
        // The signed TLSA record contains invalid (unusable) TLSA parameters.
        
        String res1;
        res1 = this.helper.get(new URL("https://badparam.dane.huque.com/"));
    }
    
    @Test(expected = DNSException.class)
    public void badsigTest() throws IOException, DNSException {
        // The TLSA record has an incorrect DNSSEC signature.
        
        NameResolverHelper dnsHelper = new GNSHelper();
        dnsHelper.queryAndParse("_443._tcp.badsig.busted.huque.com", TLSARecord.class, DNSHelper.RECORD_TLSA);
        //dnsHelper.query("_443._tcp.badsig.busted.huque.com", DNSHelper.RECORD_TLSA);
        
        String res1;
        res1 = this.helper.get(new URL("https://badsig.busted.huque.com/"));
    }
    
    @Test(expected = DNSException.class)
    public void expiredsigTest() throws IOException, DNSException {
        // The TLSA record has an expired DNSSEC signature.
        
        NameResolverHelper dnsHelper = new GNSHelper();
        dnsHelper.queryAndParse("_443._tcp.expiredsig.busted.huque.com", TLSARecord.class, DNSHelper.RECORD_TLSA);
        //dnsHelper.query("_443._tcp.expiredsig.busted.huque.com", DNSHelper.RECORD_TLSA);
        
        String res1;
        res1 = this.helper.get(new URL("https://expiredsig.busted.huque.com/"));
    }

//    @Test
//    public void goodpkixtaTest() throws IOException {
//        //The TLSA record (PKIX-TA) has a hash value that correctly matches the PKIX root CA issuer in the server certificate chain.
//
//        String res1;
//        res1 = this.helper.get(new URL("https://good-pkixta.dane.huque.com/"));
//    }
//
//    @Test(expected = SSLHandshakeException.class)
//    public void badpkixtaTest() throws IOException {
//        // The TLSA record (PKIX-TA) has a hash value that doesn't match any certificate issuer in the PKIX chain corresponding to the server certificate.
//
//        String res1;
//        res1 = this.helper.get(new URL("https://bad-pkixta.dane.huque.com/"));
//    }
    
    
}