package eu.lightest.verifier.model.trustscheme;

import eu.lightest.verifier.exceptions.DNSException;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.StdOutReportObserver;
import eu.lightest.verifier.wrapper.DNSHelperTest;
import eu.lightest.verifier.wrapper.TSPAHelperTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class TrustSchemeTest {
    
    private Report report;
    
    @Before
    public void prepare() {
        this.report = new Report();
        
        StdOutReportObserver stdout_reporter = new StdOutReportObserver();
        this.report.addObserver(stdout_reporter);
    }
    
    @Test
    public void trustSchemeEquality() {
        String tsl = "https://whatever";
        String schemeid = "blabla.lightest.eu";
    
        TrustScheme scheme1 = new TrustScheme(tsl, schemeid, null);
        TrustScheme scheme2 = new TrustScheme(tsl, schemeid, null);
        
        assertTrue(scheme1.equals(scheme2));
        assertTrue(scheme2.equals(scheme1));
    }
    
    @Test
    public void trustSchemeInequality() {
        String tsl11 = "https://whatever";
        String tsl12 = "https://whateverbutdifferent";
        String schemeid11 = "blabla.lightest.eu";
        String schemeid12 = "blabla.aminustrust.at";
    
        TrustScheme scheme11 = new TrustScheme(tsl11, schemeid11, null);
        TrustScheme scheme12 = new TrustScheme(tsl12, schemeid11, null);
        
        assertFalse(scheme11.equals(scheme12));
        assertFalse(scheme12.equals(scheme11));
    
        TrustScheme scheme21 = new TrustScheme(tsl11, schemeid11, null);
        TrustScheme scheme22 = new TrustScheme(tsl11, schemeid12, null);
        
        assertFalse(scheme21.equals(scheme22));
        assertFalse(scheme22.equals(scheme21));
    }
    
    @Test
    public void trustSchemeClaimEquality() {
        TrustSchemeClaim claim1 = new TrustSchemeClaim("bla.blub.eidas.eu");
        TrustSchemeClaim claim2 = new TrustSchemeClaim("bla.blub.eidas.eu");
        TrustSchemeClaim claim3 = new TrustSchemeClaim("lightest.nlnetlabs.eu");
        
        assertTrue(claim1.equals(claim2));
        assertFalse(claim1.equals(claim3));
    }
    
    @Test
    public void trustSchemeFactory1() throws IOException, DNSException {
        String claimString = DNSHelperTest.CLAIM_TEST_PTR;
        String schemeString = DNSHelperTest.buildDomain(DNSHelperTest.CLAIM_PREFIX, DNSHelperTest.CLAIM_EIDAS_URI);
        
        TrustSchemeClaim claim = new TrustSchemeClaim(claimString);
        System.out.println("claim: " + claim);
        
        TrustScheme scheme = TrustSchemeFactory.createTrustScheme(claim, this.report);
    
        assertNotNull(scheme);
    
        assertNotNull(scheme.getSchemeIdentifier());
        assertTrue(scheme.getSchemeIdentifier().equals(schemeString));
    
        assertNotNull(scheme.getTSLlocation());
        assertTrue(scheme.getTSLlocation().equals(DNSHelperTest.TRUSTLIST_EIDAS));
    }
    
    
    @Test
    public void trustSchemeFactory2() throws IOException, DNSException {
        TSPAHelperTest.SCHEME schemeInfo = new TSPAHelperTest.SCHEME_IAIK_Oct16();
    
        String claimString = schemeInfo.CLAIM;
        String schemeString = DNSHelperTest.buildDomain(DNSHelperTest.CLAIM_PREFIX, schemeInfo.SCHEME);
        
        TrustSchemeClaim claim = new TrustSchemeClaim(claimString);
        System.out.println("claim: " + claim);
        
        TrustScheme scheme = TrustSchemeFactory.createTrustScheme(claim, this.report);
        
        assertNotNull(scheme);
        
        assertNotNull(scheme.getSchemeIdentifier());
        assertTrue(scheme.getSchemeIdentifier().equals(schemeString));
        
        assertNotNull(scheme.getTSLlocation());
        assertTrue(scheme.getTSLlocation().equals(schemeInfo.TSL));
    }
    
    @Test
    public void trustSchemeFactory3() throws IOException, DNSException {
        // not really a test, more like a demo for SMIMEA retrieval
        
        String claimString = DNSHelperTest.CLAIM_TESTWITHSMIMEA_PTR;
        
        TrustSchemeClaim claim = new TrustSchemeClaim(claimString);
        System.out.println("claim: " + claim);
        
        TrustScheme scheme = TrustSchemeFactory.createTrustScheme(claim, this.report);
        
        assertNotNull(scheme);
        
        assertNotNull(scheme.getSchemeIdentifier());
        System.out.println(scheme.getSchemeIdentifier());
        
        assertNotNull(scheme.getTSLlocation());
        //System.out.println(scheme.getTSLcontent());
        //assertTrue(scheme.getTSLlocation().equals(DNSHelperTest.TRUSTLIST_EIDAS));
    }
    
    @Test
    public void trustSchemeFactoryFail() throws IOException, DNSException {
        String claimString = "not-existing." + DNSHelperTest.CLAIM_TEST_PTR;
        String schemeString = DNSHelperTest.buildDomain(DNSHelperTest.CLAIM_PREFIX, DNSHelperTest.CLAIM_EIDAS_URI);
    
        TrustSchemeClaim claim = new TrustSchemeClaim(claimString);
        System.out.println("claim: " + claim);
    
        TrustScheme scheme = TrustSchemeFactory.createTrustScheme(claim, this.report);
    
        assertNull(scheme);
    }
    
    @Test
    public void trustSchemeFactoryDAFI1() throws IOException, DNSException {
        // test for https://extgit.iaik.tugraz.at/LIGHTest/lightest-demo/issues/3
        String claimString = "jordan.dafi-demo.lightest.nlnetlabs.nl";
        
        TrustSchemeClaim claim = new TrustSchemeClaim(claimString);
        System.out.println("claim: " + claim);
        
        TrustScheme scheme = TrustSchemeFactory.createTrustScheme(claim, this.report);
        
        assertNotNull(scheme);
        
        assertNotNull(scheme.getSchemeIdentifier());
        System.out.println("Discovered scheme: " + scheme.getSchemeIdentifier());
        
        assertNotNull(scheme.getTSLlocation());
        System.out.println("Discovered TSL: " + scheme.getTSLlocation());
    }
    
    
    @Test
    public void trustSchemeFactoryDAFI1new() throws IOException, DNSException {
        // test for https://extgit.iaik.tugraz.at/LIGHTest/unhcr_demo/issues/1
        String claimString = "jordan.dafi-demo.lightest.nlnetlabs.nl";
        
        TrustSchemeClaim claim = new TrustSchemeClaim(claimString);
        System.out.println("claim: " + claim);
        
        TrustScheme scheme = TrustSchemeFactory.createTrustScheme(claim, this.report);
        
        assertNotNull(scheme);
        
        assertNotNull(scheme.getSchemeIdentifier());
        System.out.println("Discovered scheme: " + scheme.getSchemeIdentifier());
        
        assertNotNull(scheme.getTSLlocation());
        System.out.println("Discovered TSL: " + scheme.getTSLlocation());
    }
    
    
}