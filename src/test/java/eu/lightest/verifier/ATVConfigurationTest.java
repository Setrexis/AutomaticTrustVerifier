package eu.lightest.verifier;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class ATVConfigurationTest {
    
    
    protected final static String internalConfigPath = "src/test/customConfigForTesting.properties";
    
    @AfterClass
    public static void teardown() throws ConfigurationException {
        ATVConfiguration.init();
    }
    
    @Test
    public void simpleTest() throws ConfigurationException {
        ATVConfiguration.init();

//        ATVConfiguration.get().getBoolean("https_verification_enabled");
        ATVConfiguration.get().getBoolean("dane_verification_enabled");
        ATVConfiguration.get().getBoolean("dnssec_verification_enabled");
        
        int val3 = ATVConfiguration.get().getInt("overwritten_in_internal_config");
        assertEquals(999, val3);
        
        assertTrue(true);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void invalidKeyTest() throws ConfigurationException {
        ATVConfiguration.init();
    
        ATVConfiguration.get().getBoolean("somethingsomething");
        
        assertTrue(false);
    }
    
    @Test
    public void simpleCompositeTest() throws ConfigurationException {
        File customConfig = new File(this.internalConfigPath);
        ATVConfiguration.init(customConfig);
        
        // variable only in custom config:
        int val1 = ATVConfiguration.get().getInt("only_in_custom_config");
        assertEquals(42, val1);
    
        int val2 = ATVConfiguration.get().getInt("only_in_internal_config");
        assertEquals(7, val2);
        
        
        // variables in both configs config:
//        boolean b1 = ATVConfiguration.get().getBoolean("https_verification_enabled");
//        assertFalse(b1);
        boolean b2 = ATVConfiguration.get().getBoolean("dane_verification_enabled");
        assertFalse(b2);
        boolean b3 = ATVConfiguration.get().getBoolean("dnssec_verification_enabled");
        assertFalse(b3);
    
        int val3 = ATVConfiguration.get().getInt("overwritten_in_internal_config");
        assertEquals(666, val3);
    
        int val4 = ATVConfiguration.get().getInt("overwritten_in_internal_config");
        assertNotEquals(999, val4);
        
        
        assertTrue(true);
    }
    
    @Test
    public void prefixTest1() {
        System.out.println("");
        System.out.println("prefixTest1:");
        
        String prefix = "trustscheme_claim";
        Iterator<String> keys = ATVConfiguration.get().getKeys(prefix);
        
        String prefixRegex = "^" + prefix + ".";
        
        while(keys.hasNext()) {
            String key = keys.next();
            String keySuffix = key.replaceFirst(prefixRegex, "");
            String value = ATVConfiguration.get().getString(key);
            System.out.println(String.format(" * %-35s: %s", keySuffix, value));
        }
    }
    
    @Test
    public void prefixTest2() {
        System.out.println("");
        System.out.println("prefixTest2:");
        
        String prefix = "trustscheme_claim";
        Iterator<String> keys = ATVConfiguration.get().getKeys(prefix);
        
        Map<String, String> entries = ATVConfiguration.getForPrefix(prefix);
        Iterator<Map.Entry<String, String>> it = entries.entrySet().iterator();
        
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            System.out.println(String.format(" * %-35s: %s", entry.getKey(), entry.getValue()));
        }
    }
    
    @Test(expected = NoSuchElementException.class)
    public void invalidKeyCompositeTest() throws ConfigurationException {
        File customConfig = new File(this.internalConfigPath);
        ATVConfiguration.init(customConfig);
        
        ATVConfiguration.get().getBoolean("somethingsomething");
        
        assertTrue(false);
    }
}