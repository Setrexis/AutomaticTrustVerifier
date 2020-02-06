package eu.lightest.verifier.client;

import eu.lightest.verifier.controller.VerificationProcess;
import eu.lightest.verifier.model.report.Report;

import java.io.File;

/**
 * Client for the Automated Trust Verifier library.
 * <p>
 * Use this class if you want to use the ATV directly (not via a REST API).
 */
public class LocalATVClient implements ATVClient {
    
    private Report report;
    private boolean prechecksPassed = false;
    
    /**
     * @param report {@link Report} object used to retrieve the verification report.
     */
    public LocalATVClient(Report report) {
        this.report = report;
    }
    
    @Override
    public boolean verify(String pathPolicy, String pathTransaction) {
        VerificationProcess verificationProcess = new VerificationProcess(new File(pathTransaction), new File(pathPolicy), this.report);
        
        boolean verificationStatus = verificationProcess.verify();
        this.prechecksPassed = verificationProcess.isPrechecksPassed();
        
        return verificationStatus;
    }
    
    public boolean prechecksPassed() {
        return this.prechecksPassed;
    }
    
    @Override
    public String toString() {
        return "local";
    }
}
