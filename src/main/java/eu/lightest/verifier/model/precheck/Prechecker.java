package eu.lightest.verifier.model.precheck;

import eu.lightest.verifier.model.report.Report;

import java.io.File;

public interface Prechecker {
    
    boolean check(File transaction, File policy, Report report);
}
