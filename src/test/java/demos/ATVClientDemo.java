package demos;

import eu.lightest.verifier.client.ATVClient;
import eu.lightest.verifier.client.ATVTest;
import eu.lightest.verifier.client.LocalATVClient;
import eu.lightest.verifier.model.report.BufferedStdOutReportObserver;
import eu.lightest.verifier.model.report.Report;

public class ATVClientDemo {
    
    private static String PATH_POLICY = ATVTest.PATH_POLICY_PDF1CORREOS;
    private static String PATH_TRANSACTION = ATVTest.PATH_TRANSACTION_PDF_ESEAL;
    
    public static void main(String[] args) {
        Report report = new Report();
        BufferedStdOutReportObserver reportBuffer = new BufferedStdOutReportObserver();
        report.addObserver(reportBuffer);
        
        ATVClient atv = new LocalATVClient(report);
        
        boolean verificationStatus = atv.verify(ATVClientDemo.PATH_POLICY, ATVClientDemo.PATH_TRANSACTION);
        
        System.out.println("Transaction valid: " + verificationStatus);
        System.out.println("REPORT:");
        reportBuffer.print();
    }
}
