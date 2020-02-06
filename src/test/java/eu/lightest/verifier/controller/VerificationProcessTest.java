package eu.lightest.verifier.controller;

import eu.lightest.verifier.client.ATVTest;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.StdOutReportObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * This tests the verification process. You have to create your own ASIC to test this. Look at the ASiC creator on extgit
 */
public class VerificationProcessTest {
    
    private File contract;
    private File trust_policy;
    private Report report;
    private File fileThatsNotTere;
    
    @Before
    public void setUp() {
        this.contract = new File(ATVTest.PATH_TRANSACTION_SIMPLE);
        this.trust_policy = new File(ATVTest.PATH_POLICY_SIMPLE1);
        this.fileThatsNotTere = new File("/tmp/fileThatsNotTere");
    
        this.report = new Report();
        StdOutReportObserver stdout_reporter = new StdOutReportObserver();
        this.report.addObserver(stdout_reporter);
    
    
    }

//    @Test
//    public void checkSelfsignedTransactionTypeTest() throws IOException, ZipException {
//        Assert.assertEquals(this.verificationProcess.checkTransactionType(this.contract), "ASICS");
//    }
    
    @Test
    public void simpleTest() {
        VerificationProcess verificationProcess = new VerificationProcess(this.contract, this.trust_policy, this.report);
        boolean status = verificationProcess.verify();
        
        Assert.assertTrue(status);
    }
    
    @Test
    public void fileNotFound1() {
        
        VerificationProcess verificationProcess = new VerificationProcess(this.fileThatsNotTere, this.trust_policy, this.report);
        boolean status = verificationProcess.verify();
        
        Assert.assertFalse(status);
    }
    
    @Test
    public void fileNotFound2() {
        
        VerificationProcess verificationProcess = new VerificationProcess(this.contract, this.fileThatsNotTere, this.report);
        boolean status = verificationProcess.verify();
        
        Assert.assertFalse(status);
    }
    
    @Test
    public void fileNotFound3() {
        
        VerificationProcess verificationProcess = new VerificationProcess(this.fileThatsNotTere, this.fileThatsNotTere, this.report);
        boolean status = verificationProcess.verify();
        
        Assert.assertFalse(status);
    }
}
