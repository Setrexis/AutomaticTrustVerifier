package eu.lightest.verifier.wrapper;

import eu.lightest.verifier.exceptions.DNSException;
import iaik.security.provider.IAIK;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DNSHelperTest {
    
    public static final String CLAIM_EIDAS_URI = "eidas.lightest.nlnetlabs.nl";
    public static final String CLAIM_TEST_PTR = "test-scheme.lightest.nlnetlabs.nl";
    public static final String CLAIM_TESTWITHSMIMEA_PTR = "peppol_sp.uprc.lightest.nlnetlabs.nl";
    public static final String CLAIM_PREFIX = "_scheme._trust";
    public static final String TRUSTLIST_EIDAS = "https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml";
    private DNSHelper helper;
    
    private static void printMsg(Message msg) throws IOException, DNSException {
        List<Record> resp = DNSHelper.parseMessage(msg);
        for(Record rec : resp) {
            System.out.println(rec.toString());
        }
    }
    
    public static String buildDomain(String prefix, String host) {
        return prefix + "." + host + ".";
    }
    
    @BeforeClass
    public static void setUpProvider() {
        IAIK.addAsProvider();
    }
    
    @Before
    public void setUp() throws IOException {
        System.out.println("setup another test ...");
        
        this.helper = new DNSHelper(DNSHelper.DNS_CLOUDFLARE1);
    }
    
    @Test
    public void simpleAQuery1Test() throws IOException, DNSException {
        Message msg = this.helper.query("internetsociety.org", DNSHelper.RECORD_A);
        
        DNSHelperTest.printMsg(msg);
    }
    
    @Test
    public void simpleAQuery2Test() throws IOException, DNSException {
        Message msg = this.helper.query("dnssec-tools.org", DNSHelper.RECORD_A);
        
        DNSHelperTest.printMsg(msg);
    }
    
    @Test
    public void simpleAQuery3Test() throws IOException, DNSException {
        Message msg = this.helper.query("dnssec-deployment.org", DNSHelper.RECORD_A);
        
        DNSHelperTest.printMsg(msg);
    }
    
    @Test
    public void simpleTXT1QueryTest() throws IOException, DNSException {
        Message msg = this.helper.query("nlnetlabs.nl", DNSHelper.RECORD_TXT);
        
        DNSHelperTest.printMsg(msg);
    }
    
    @Test
    public void simpleTXT2QueryTest() throws IOException, DNSException {
        Message msg = this.helper.query("internetsociety.org", DNSHelper.RECORD_TXT);
        
        DNSHelperTest.printMsg(msg);
    }
    
    @Test
    public void simpleTXT3QueryTest() throws IOException, DNSException {
        List<String> txts = this.helper.queryTXT("nlnetlabs.nl");
        
        for(String txt : txts) {
            System.out.println("TXT: " + txt);
        }
        
        assert (txts.size() > 0);
    }
    
    @Test
    public void simpleTXT4QueryTest() throws IOException, DNSException {
        List<String> txts = this.helper.queryTXT("lightest.nlnetlabs.nl");
        
        assertEquals(0, txts.size());
    }
    
    @Test(expected = DNSException.class)
    public void DNSSECerror1Test() throws IOException, DNSException {
        Message msg = this.helper.query("dnssec-failed.org", DNSHelper.RECORD_A);
        
        DNSHelperTest.printMsg(msg);
    }
    
    @Test(expected = DNSException.class)
    public void DNSSECerror2Test() throws IOException, DNSException {
        Message msg = this.helper.query("rhybar.cz", DNSHelper.RECORD_A);
        
        DNSHelperTest.printMsg(msg);
    }
    
    @Test
    public void simplePTRQueryTest() throws IOException, DNSException {
        List<String> ptrs = this.helper.queryPTR(DNSHelperTest.buildDomain(DNSHelperTest.CLAIM_PREFIX, DNSHelperTest.CLAIM_TEST_PTR));
        
        for(String ptr : ptrs) {
            System.out.println("PTR: " + ptr);
        }
        
        assertNotEquals(0, ptrs.size());
    }
    
    @Test
    public void simpleSMIMEATest() throws IOException, DNSException, CertificateException {
        String DEMO_EMAIL = "instanttest@greatdane.io";
        String DEMO_ZONE = "2ce6704631094b8de8cd1191b7196ae0dd10f6b37b5423095f540ae5._smimecert.greatdane.io.";
        
        List<SMIMEAcert> records = this.helper.querySMIMEA(DEMO_ZONE);
        for(SMIMEAcert record : records) {
            record.init();
        }
    }
    
    @Test
    public void LIGHTestPTRTest() throws IOException, DNSException {
        // https://dns.google.com/query?name=_scheme._trust.test-scheme.lightest.nlnetlabs.nl&type=12&dnssec=true
        
        String claim = DNSHelperTest.CLAIM_TEST_PTR;
        String prefix = DNSHelperTest.CLAIM_PREFIX;
        String goal = DNSHelperTest.buildDomain(DNSHelperTest.CLAIM_PREFIX, DNSHelperTest.CLAIM_EIDAS_URI);
        
        List<String> ptrs = this.helper.queryPTR(prefix + "." + claim);
        
        for(String ptr : ptrs) {
            System.out.println("PTR: " + ptr);
        }
        
        assertEquals(1, ptrs.size());
        assertEquals(goal, ptrs.get(0));
    }
    
    @Test
    public void LIGHTestURITest() throws IOException, DNSException {
        // https://dns.google.com/resolve?name=_scheme._trust.eidas.lightest.nlnetlabs.nl.&type=256
        
        String claim = DNSHelperTest.CLAIM_EIDAS_URI;
        String prefix = DNSHelperTest.CLAIM_PREFIX;
        String url = DNSHelperTest.buildDomain(prefix, claim);
        System.out.println(url);
        
        List<String> uris = this.helper.queryURI(url);
        
        
        for(String uri : uris) {
            System.out.println("URI: " + uri);
        }
        
        assertEquals(1, uris.size());
        assertEquals(DNSHelperTest.TRUSTLIST_EIDAS, uris.get(0));
    }
    
}