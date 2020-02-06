package eu.lightest.verifier.model.translation;

import eu.lightest.verifier.ATVConfiguration;
import eu.lightest.verifier.exceptions.DNSException;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.StdOutReportObserver;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TrustTranslationTest {
    
    private static String TTA = "https://tta-lightest.eu:8445/ttaFM/mng/";
    
    private Report report;
    
    private String TRANSLATION_AZ = TrustTranslationFactory.POINTER_PREFIX + ".eidas.lightest.nlnetlabs.nl.lightest.nlnetlabs.nl.";
    
    
    @Before
    public void prepare() throws ConfigurationException {
        ATVConfiguration.init();
        
        this.report = new Report();
        
        StdOutReportObserver stdout_reporter = new StdOutReportObserver();
        this.report.addObserver(stdout_reporter);
    }
    
    @Test
    public void trustTranslationFactory1() throws IOException {
    
        TranslationPointer pointer = new TranslationPointer(this.TRANSLATION_AZ);
        TrustTranslation translation = TrustTranslationFactory.createTranslation(pointer, this.report);
    
        assertNotNull(translation);
    
        System.out.println(translation);
    }
    
    @Test
    public void trustTranslationFactory1withoutDANE() throws IOException {
        ATVConfiguration.get().setProperty("dane_verification_enabled", false);
        
        TranslationPointer pointer = new TranslationPointer(this.TRANSLATION_AZ);
        TrustTranslation translation = TrustTranslationFactory.createTranslation(pointer, this.report);
        
        assertNotNull(translation);
    
        translation.log();
    }
    
    @Test
    public void trustSchemeFactoryFail() throws IOException, DNSException {
        
        TranslationPointer pointer = new TranslationPointer("not-existing.lightest.nlnetlabs.nl");
        TrustTranslation translation = TrustTranslationFactory.createTranslation(pointer, this.report);
        
        assertNull(translation);
    }
}