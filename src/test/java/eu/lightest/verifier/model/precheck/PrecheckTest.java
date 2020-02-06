package eu.lightest.verifier.model.precheck;

import eu.lightest.horn.exceptions.HornFailedException;
import eu.lightest.verifier.ATVConfiguration;
import eu.lightest.verifier.client.ATVTest;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.StdOutReportObserver;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class PrecheckTest {
    
    private static Report report;
    private static File transaction;
    private static File tpl;
    
    @AfterClass
    public static void teardown() throws ConfigurationException {
        ATVConfiguration.init();
    }
    
    @BeforeClass
    public static void setupClass() {
        PrecheckTest.transaction = new File(ATVTest.PATH_TRANSACTION_PSO);
        PrecheckTest.tpl = new File(ATVTest.PATH_POLICY_PSO1);
        
        PrecheckTest.report = new Report();
        StdOutReportObserver stdout_reporter = new StdOutReportObserver();
        PrecheckTest.report.addObserver(stdout_reporter);
    }
    
    @Test
    public void pumpkinSeedOil1() throws HornFailedException {
        
        Precheck precheck = new Precheck(PrecheckTest.transaction, PrecheckTest.tpl, PrecheckTest.report);
        
        boolean status = precheck.runAllChecks();
        
        assertTrue(status);
    }
}