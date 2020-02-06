package eu.lightest.verifier.model.tpl;

import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.HornFailedException;
import eu.lightest.horn.specialKeywords.IAtvApiListener;
import eu.lightest.verifier.ATVConfiguration;
import eu.lightest.verifier.client.ATVTest;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.StdOutReportObserver;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class ReferenceTest {
    
    public static final String PATH_POLICY_DELEGATION1 = "src/test/references/delegate_example.tpl.pl";
    public static final String PATH_POLICY_TRANSLATION1 = "src/test/references/translation_example.tpl.pl";
    public static final String PATH_POLICY_TRANSLATION2 = "src/test/references/translation_example2.tpl.pl";
    
    private static Report report;
    
    @BeforeClass
    public static void setupClass() throws ConfigurationException {
        
        ReferenceTest.report = new Report();
        
        StdOutReportObserver stdout_reporter = new StdOutReportObserver();
        ReferenceTest.report.addObserver(stdout_reporter);
    }
    
    @Before
    public void setup() throws ConfigurationException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        ATVConfiguration.init();
    }
    
    @Test
    public void ref_translation1() throws HornFailedException {
        // policy = eidas
        // transaction = eidas
    
        IAtvApiListener callback = new TplApiListener(new File(ATVTest.PATH_TRANSACTION_EIDAS), this.report);
        Interpreter interpreter = new Interpreter(callback);
        
        String tplFilePath = ReferenceTest.PATH_POLICY_TRANSLATION1;
        String query = "accept(Form).";
        String inputVariable = "Form";
        
        boolean status = interpreter.run(tplFilePath, query, inputVariable);
        
        assertTrue(status);
    }
    
    @Test
    public void ref_translation2_withDANE() throws HornFailedException {
        // policy = fantasyland
        // transaction = eidas
    
        IAtvApiListener callback = new TplApiListener(new File(ATVTest.PATH_TRANSACTION_EIDAS), this.report);
        Interpreter interpreter = new Interpreter(callback);
        
        String tplFilePath = ReferenceTest.PATH_POLICY_TRANSLATION2;
        String query = "accept(Form).";
        String inputVariable = "Form";
        
        boolean status = interpreter.run(tplFilePath, query, inputVariable);
        
        assertTrue(status);
    }
    
    @Test
    public void ref_translation2_withoutDANE() throws HornFailedException {
        // policy = fantasyland
        // transaction = eidas
        
        ATVConfiguration.get().setProperty("dane_verification_enabled", false);
    
        IAtvApiListener callback = new TplApiListener(new File(ATVTest.PATH_TRANSACTION_EIDAS), this.report);
        Interpreter interpreter = new Interpreter(callback);
        
        String tplFilePath = ReferenceTest.PATH_POLICY_TRANSLATION2;
        String query = "accept(Form).";
        String inputVariable = "Form";
        
        boolean status = interpreter.run(tplFilePath, query, inputVariable);
        
        assertTrue(status);
    }
    
}