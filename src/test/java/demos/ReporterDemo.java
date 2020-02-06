package demos;

import eu.lightest.verifier.model.report.BufferedFileReportObserver;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.ReportStatus;
import eu.lightest.verifier.model.report.StdOutReportObserver;

import java.io.File;
import java.io.FileNotFoundException;

public class ReporterDemo {
    
    public static void main(String[] args) throws FileNotFoundException {
        Report report = new Report();
        
        // Lets create a reporter and add it ...
        BufferedFileReportObserver file_reporter = new BufferedFileReportObserver();
        report.addObserver(file_reporter);
        
        // Lets create a second reporter, this time to STD OUT
        StdOutReportObserver stdout_reporter = new StdOutReportObserver();
        report.addObserver(stdout_reporter);
        
        // Report something ...
        report.addLine("This is a test with status!", ReportStatus.OK);
        report.addLine("This is a second test, without status ...");
        
        // reporter1 wants to save to a file, so lets do this.
        File target = new File("bufferedFileReporterDemo.txt");
        file_reporter.saveToFile(target);
    }
    
    
}
