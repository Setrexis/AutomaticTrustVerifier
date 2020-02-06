package eu.lightest.verifier;

import eu.lightest.verifier.exceptions.DNSException;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.StdOutReportObserver;
import eu.lightest.verifier.model.trustscheme.TrustScheme;
import eu.lightest.verifier.model.trustscheme.TrustSchemeClaim;
import eu.lightest.verifier.model.trustscheme.TrustSchemeFactory;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class ATVConfigurationSchemesTest {
    
    
    private final static String internalConfigPath = ATVConfigurationTest.internalConfigPath;
    private static Report report;
    
    @Parameterized.Parameter(0)
    public String id;
    
    @Parameterized.Parameter(1)
    public String host;
    
    @BeforeClass
    public static void setup() throws ConfigurationException {
        ATVConfiguration.init();
        
        ATVConfigurationSchemesTest.report = new Report();
        
        StdOutReportObserver stdout_reporter = new StdOutReportObserver();
        ATVConfigurationSchemesTest.report.addObserver(stdout_reporter);
    }
    
    private static boolean skip(String key) {
        return !key.endsWith("_claim");
    }
    
    //@Parameterized.Parameters(name = "{index}: {0}")
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List data = new ArrayList();
        
        Map<String, String> configs = ATVConfiguration.getForPrefix("trustscheme_claim");
        Iterator<Map.Entry<String, String>> it = configs.entrySet().iterator();
        
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if(ATVConfigurationSchemesTest.skip((String) entry.getKey())) {
                continue;
            }
            
            Object[] record = new Object[]{entry.getKey(), entry.getValue()};
            data.add(record);
        }
        
        return data;
        
    }
    
    @Test
    public void identify() {
        System.out.println("id:   " + this.id);
        System.out.println("host: " + this.host);
    }
    
    @Test
    public void discover() throws IOException, DNSException {
        // not really a test, more like a demo for SMIMEA retrieval
        
        String claimString = this.host;
        TrustSchemeClaim claim = new TrustSchemeClaim(claimString);
        
        TrustScheme scheme = TrustSchemeFactory.createTrustScheme(claim, ATVConfigurationSchemesTest.report);
        
        assertNotNull(scheme);
        System.out.println("");
        
        assertNotNull(scheme.getSchemeIdentifier());
        System.out.println("Discovered Scheme: " + scheme.getSchemeIdentifier());
        
        assertNotNull(scheme.getTSLlocation());
        System.out.println("Discovered List:   " + scheme.getTSLlocation());
        
        
        //assertTrue(scheme.getTSLlocation().equals(DNSHelperTest.TRUSTLIST_EIDAS));
        
    }
    
}