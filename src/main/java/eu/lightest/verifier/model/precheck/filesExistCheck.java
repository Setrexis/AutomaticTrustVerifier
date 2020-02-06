package eu.lightest.verifier.model.precheck;

import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.ReportStatus;

import java.io.File;

public class filesExistCheck implements Prechecker {
    
    @Override
    public boolean check(File transaction, File policy, Report report) {
        boolean allFilesExist = true;
        
        if(!fileExists(transaction)) {
            report.addLine("Not able to read Transaction. Does it exist?", ReportStatus.FAILED);
            allFilesExist = false;
        }
        
        if(!fileExists(policy)) {
            report.addLine("Not able to read Policy. Does it exist?", ReportStatus.FAILED);
            allFilesExist = false;
        }
        
        return allFilesExist;
    }
    
    private boolean fileExists(File file) {
        return file.isFile() && file.exists();
    }
}
