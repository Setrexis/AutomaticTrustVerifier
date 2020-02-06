package eu.lightest.verifier.controller;

import com.google.common.io.Files;
import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.HornFailedException;
import eu.lightest.verifier.ATVConfiguration;
import eu.lightest.verifier.model.GitRepositoryState;
import eu.lightest.verifier.model.precheck.Precheck;
import eu.lightest.verifier.model.report.BufferedFileReportObserver;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.ReportStatus;
import eu.lightest.verifier.model.tpl.TplApiListener;
import eu.lightest.verifier.model.transaction.TransactionContainer;
import iaik.security.provider.IAIK;
import iaik.xml.crypto.XSecProvider;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Main interface to the Automated Trust Verifier (ATV).
 * <p>
 * Used to verify the given transaction with respect to the given TPL trust policy.
 */
public class VerificationProcess {
    
    public static final int STATUS_FAIL = 0;
    public static final int STATUS_OK = 1;
    private static Logger logger = Logger.getLogger(VerificationProcess.class);
    private Report report;
    private File transaction_file;
    private File policy_file;
    private TransactionContainer transaction;
    private TplApiListener callback;
    private boolean prechecksPassed = false;
    
    /**
     * Initialize Automated Trust Verifier (ATV).
     *
     * @param inputTransaction a LIGHTest transaction (e.g. ASIC container, signed XML/PDF document, etc.).
     * @param inputPolicy      a trust policy (in TPL format).
     * @param report           {@link Report} object used to retrieve the verification report.
     */
    public VerificationProcess(File inputTransaction, File inputPolicy, Report report) {
        this.transaction_file = inputTransaction;
        this.policy_file = inputPolicy;
        this.report = report;
        
        reportVersion();
    }
    
    public boolean isPrechecksPassed() {
        return this.prechecksPassed;
    }
    
    private void reportVersion() {
        String buildVersion = "";
        try {
            buildVersion = GitRepositoryState.get().getBuildVersion();
        } catch(IOException | NullPointerException e) {
            e.printStackTrace();
        }
        this.report.addLine("LIGHTest ATV " + buildVersion);
    }
    
    /**
     * Verify the configured transaction with respect to the configured TPL trust policy.
     * <p>
     * Configure the class via its constructor first.
     * If a {@link Report} was set, the verification report can be retrieved using it.
     *
     * @return result of the verification. <code>true</code> iff the given policy can be fulfilled.
     */
    public Boolean verify() {
        IAIK.addAsProvider(true);
        XSecProvider.addAsProvider(true);
        
        
        if(!runPrechecks()) {
            this.report.addLine("Pre-checks failed, ATV halted.", ReportStatus.FAILED);
            return false;
        }
        
        //Transaction transaction = TransactionFactory.getTransaction(this.transaction_file);
        this.callback = new TplApiListener(this.transaction_file, this.report);
        Interpreter interpreter = new Interpreter(this.callback);
        
        Interpreter.recordRPxTranscript = ATVConfiguration.get().getBoolean("tpl_recordRPxTranscript", false);
        Interpreter.recordRPxTranscriptLocation = ATVConfiguration.get().getString("tpl_recordRPxTranscript_path", "/tmp/lightest_rpx_transcript");
        
        String main_pred = ATVConfiguration.get().getString("tpl_main_predicate", "accept(Form).");
        String main_var = ATVConfiguration.get().getString("tpl_main_predicate_variable", "Form");
        //String[] params = {this.policy_file.getAbsolutePath(), main_pred, main_var};
        
        if(!isMainValid(main_pred, main_var, this.policy_file)) {
            return false;
        }
        
        boolean status = false;
        
        String tplFilePath = this.policy_file.getAbsolutePath();
        String query = main_pred;
        String inputVariable = main_var;
        
        try {
            status = interpreter.run(tplFilePath, query, inputVariable);
            
        } catch(HornFailedException e) {
            this.report.addLine("Interpreter Error: " + e.getClass().getTypeName() + " (" + e.getMessage() + ").", ReportStatus.FAILED);
            VerificationProcess.logger.error("Interpreter Error", e);
            
            return false;
            
        } catch(Exception e) {
            this.report.addLine("Fatal Error: " + e.getClass().getTypeName() + " (" + e.getMessage() + "), see log.", ReportStatus.FAILED);
            VerificationProcess.logger.error("Fatal Error", e);
            
            return false;
        }
        
        return status;
    }
    
    private boolean isMainValid(String mainPred, String mainVar, File pathToPolicy) {
        if(!mainPred.contains(mainVar)) {
            this.report.addLine(" Main Predicate (" + mainPred + ") does not contain Variable: " + mainVar + ".", ReportStatus.FAILED);
            return false;
        }
        
        String policy;
        try {
            policy = Files.toString(pathToPolicy, Charset.defaultCharset());
        } catch(IOException e) {
            this.report.addLine("Error loading Policy: " + e.getMessage(), ReportStatus.FAILED);
            return false;
        }
        
        if(mainPred.endsWith(".")) {
            mainPred = mainPred.substring(0, mainPred.length() - 1);
        }
        
        if(!policy.contains(mainPred)) {
            this.report.addLine("Policy does not contain Main Predicate: " + mainPred + "", ReportStatus.FAILED);
            return false;
        }
        
        return true;
    }
    
    private boolean runPrechecks() {
        Precheck precheck = new Precheck(this.transaction_file, this.policy_file, this.report);
        return this.prechecksPassed = precheck.runAllChecks();
    }
    
    /**
     * Starts the verification process and returns the result.
     * <p>
     * To get a more detailed verification result, use {@link Report} provided to the constructor,
     * e.g. with {@link BufferedFileReportObserver}.
     * <p>
     * This method is used to get the verification result in form of a status code.
     * If you need a status value, use {@link #checkTransactionForAPI(String)} instead.
     *
     * @return {@link #STATUS_FAIL} (0) or {@link #STATUS_OK} (1).
     */
    public int checkTransactionForAPI() {
        Boolean verify = verify();
        
        return verify == true ? VerificationProcess.STATUS_OK : VerificationProcess.STATUS_FAIL;
    }
    
    /**
     * Starts the verification process and returns the requested status value.
     * <p>
     * To get a more detailed verification result, use {@link Report} provided to the constructor,
     * e.g. with {@link BufferedFileReportObserver}.
     * <p>
     * This method is used to retrieve a status value, e.g. a level stored by an involved format parser.
     * If you don't need a specific field, use {@link #checkTransactionForAPI()} instead.
     * <p>
     * For a list of possible status values, consult the documentation of the involved format parsers.
     *
     * @param desiredStatusValue Name of the desired status value.
     * @return Iff the verification succeeded, the value of the status value requested via the <code>desiredStatusValue</code> parameter. If the verification failed, <code>null</code> is returned.
     */
    public String checkTransactionForAPI(String desiredStatusValue) {
        Boolean verify = verify();
        
        if(verify == true && this.callback != null) {
            return this.callback.getReturnValue(desiredStatusValue);
        }
        
        // else:
        return null;
    }
    
}
