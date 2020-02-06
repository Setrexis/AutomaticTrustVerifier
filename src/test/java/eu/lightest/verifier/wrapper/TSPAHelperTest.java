package eu.lightest.verifier.wrapper;

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TSPAHelperTest {
    
    //private static final String TSPA = "https://lightest-dev.iaik.tugraz.at/tspa/api/v1/";
    private static final String TSPA = "https://tspa.tug.do.nlnetlabs.nl/tspa/api/v1/";
    private static Logger logger = Logger.getLogger(TSPAHelperTest.class);
    
    @Test
    public void testEIDAS() {
        boolean status = publish(new SCHEME_EIDAS());
        
        assertTrue(status);
    }
    
    @Test
    public void testAT() {
        boolean status = publish(new SCHEME_AT());
        
        assertTrue(status);
    }
    
    @Test
    public void testAT_CERTFAIL() {
        boolean status = publish(new SCHEME_AT_CERTFAIL(), true);
        
        assertFalse(status); // fails because not able to verify list (required to publish it)
    }
    
    @Test
    public void testAT_SIGFAIL() {
        boolean status = publish(new SCHEME_AT_SIGFAIL(), true);
        
        assertFalse(status); // fails because not able to verify list (required to publish it)
    }
    
    @Test
    public void testUPRC_Oct16() {
        boolean status = publish(new SCHEME_UPRC_Oct16());
        
        assertTrue(status);
    }
    
    @Test
    public void testUPRC_Oct16_LISTBROKEN() {
        boolean status = publish(new SCHEME_UPRC_Oct16_LISTBROKEN(), true);
        
        assertFalse(status); // fails because not able to verify list (required to publish it)
    }
    
    @Test
    public void testUPRC_IAIK_Oct16() {
        boolean status = publish(new SCHEME_IAIK_Oct16(), false);
        
        assertTrue(status);
    }
    
    @Test
    public void testFIDO1() {
        boolean status = publish(new SCHEME_FIDO(), false);
    
        assertTrue(status);
    }
    
    
    public boolean publish(SCHEME scheme) {
        return publish(scheme, false);
    }
    
    public boolean publish(SCHEME scheme, boolean disablePostCheck) {
        TSPAHelper tspaHelper = new TSPAHelper(TSPAHelperTest.TSPA, scheme.CLAIM, scheme.SCHEME, scheme.TSL);
        tspaHelper.setDisablePostCheck(disablePostCheck);
        
        boolean status = false;
        try {
            status = tspaHelper.publish();
        } catch(Exception e) {
            TSPAHelperTest.logger.error("", e);
            status = false;
        }
        
        return status;
    }
    
    
    public static class SCHEME_TUBITAK_ESIG extends SCHEME {
        
        public SCHEME_TUBITAK_ESIG() {
            super("https://lightest.iaik.tugraz.at/testschemes/TR_eIDAS_eSignature_2019-11-27_TUG_signed.xml",
                    "TR-eSignature.lightest.nlnetlabs.nl",
                    "tr-eidas-esignature.lightest.nlnetlabs.nl");
        }
    }
    
    public static class SCHEME_EIDAS extends SCHEME {
        
        public SCHEME_EIDAS() {
            super("https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml",
                    "test-scheme.lightest.nlnetlabs.nl",
                    "eidas.lightest.nlnetlabs.nl");
        }
    }
    
    public static class SCHEME_AT extends SCHEME {
        
        public SCHEME_AT() {
            super("https://www.signatur.rtr.at/currenttl.xml",
                    "atrust.stefanTSPAtest.lightest.nlnetlabs.nl",
                    "eidas_austria.stefanTSPAtest.lightest.nlnetlabs.nl");
        }
    }
    
    public static class SCHEME_AT_CERTFAIL extends SCHEME {
        
        public SCHEME_AT_CERTFAIL() {
            super("https://lightest.iaik.tugraz.at/testschemes/eidas_AT_currenttl_CERTFAIL.xml",
                    "atrust_certfail.stefanTSPAtest.lightest.nlnetlabs.nl",
                    "eidas_austria_certfail.stefanTSPAtest.lightest.nlnetlabs.nl");
        }
    }
    
    public static class SCHEME_AT_SIGFAIL extends SCHEME {
        
        public SCHEME_AT_SIGFAIL() {
            super("https://lightest.iaik.tugraz.at/testschemes/eidas_AT_currenttl_SIGFAIL.xml",
                    "atrust_sigfail.stefanTSPAtest.lightest.nlnetlabs.nl",
                    "eidas_austria_sigfail.stefanTSPAtest.lightest.nlnetlabs.nl");
        }
    }
    
    public static class SCHEME_UPRC_Oct16 extends SCHEME {
        
        public SCHEME_UPRC_Oct16() {
            super("https://lightest.iaik.tugraz.at/testschemes/uprc_NEW_SP1.xml",
                    "uprc_sp1claim_oct16.stefanTSPAtest.lightest.nlnetlabs.nl",
                    "uprc_sp1scheme_oct16.stefanTSPAtest.lightest.nlnetlabs.nl");
        }
    }
    
    public static class SCHEME_UPRC_Oct16_LISTBROKEN extends SCHEME {
        
        public SCHEME_UPRC_Oct16_LISTBROKEN() {
            super("https://lightest.iaik.tugraz.at/testschemes/uprc_NEW_SP1_BROKEN.xml",
                    "uprc_sp1claim_oct16_listbroken.stefanTSPAtest.lightest.nlnetlabs.nl",
                    "uprc_sp1scheme_oct16_listbroken.stefanTSPAtest.lightest.nlnetlabs.nl");
        }
    }
    
    public static class SCHEME_IAIK_Oct16 extends SCHEME {
        
        public SCHEME_IAIK_Oct16() {
            super("https://lightest.iaik.tugraz.at/testschemes/iaik-ts.xml",
                    "iaik-ca.lightest.nlnetlabs.nl",
                    "iaik-ts.lightest.nlnetlabs.nl");
        }
    }
    
    public static class SCHEME_FIDO extends SCHEME {
        
        public SCHEME_FIDO() {
            super("https://lightest.iaik.tugraz.at/fido/fido_mapping2_signed-xades-baseline-b_enveloped.xml",
                    "gd.fido.lightest.nlnetlabs.nl",
                    "gd-mappings.fido.lightest.nlnetlabs.nl");
        }
    }
    
    public static class SCHEME_DAFI6 extends SCHEME {
        // test for https://extgit.iaik.tugraz.at/LIGHTest/unhcr_demo/issues/1#note_22896
        
        public SCHEME_DAFI6() {
            super("https://tspa-unhcr.tug.do.nlnetlabs.nl/tspa/api/v1/scheme/unhcr-federation-6.dafi-demo.lightest.nlnetlabs.nl",
                    "unhcr.stefan-demos.lightest.nlnetlabs.nl",
                    "unhcr-federation-6.stefan-demos.lightest.nlnetlabs.nl");
        }
    }
    
    
    public static abstract class SCHEME {
        
        /**
         * HTTPS URL to the Trust Status List.
         */
        public final String TSL;
        /**
         * Hostname of the Claim (used for PTR record).
         */
        public final String CLAIM;
        /**
         * Hostname of the Scheme (used for URI & SMIMEA records).
         */
        public final String SCHEME;
        
        public SCHEME(String TSL, String CLAIM, String SCHEME) {
            this.TSL = TSL;
            this.CLAIM = CLAIM;
            this.SCHEME = SCHEME;
        }
    }
}