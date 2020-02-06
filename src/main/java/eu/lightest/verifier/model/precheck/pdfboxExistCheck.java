package eu.lightest.verifier.model.precheck;

import eu.lightest.verifier.model.report.Report;
import iaik.pdf.pdfbox.PdfSignatureInstancePdfbox;
import iaik.pdf.signature.PdfSignatureEngine;
import iaik.pdf.signature.PdfSignatureInstance;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Some checks for PDF / PAdES validation.
 */
public class pdfboxExistCheck implements Prechecker {
    
    private static Logger logger = Logger.getLogger(pdfboxExistCheck.class);
    
    @Override
    public boolean check(File transaction, File policy, Report report) {
        
        boolean pdfboxPresent = checkPdfBox(report);
        if(pdfboxPresent) {
            pdfboxExistCheck.logger.info("Apache PDFBox is present!");
        }
        
        boolean IAIKpdfboxPresent = checkPdfBox(report);
        if(IAIKpdfboxPresent) {
            pdfboxExistCheck.logger.info("IAIK PdfSignatureInstancePdfbox is available!");
        }
        
        boolean pdfSigInstanceAvailable = checkPdfSigInstance(report);
        if(pdfSigInstanceAvailable) {
            pdfboxExistCheck.logger.info("PdfSignatureInstance is available!");
        } else {
            pdfboxExistCheck.logger.error("PdfSignatureInstance not available. Validation might fail if PDF support is required.");
        }
        
        return pdfboxPresent;
    }
    
    private boolean checkIAIKPdfBox(Report report) {
        try {
            Class.forName("iaik.pdf.pdfbox.PdfSignatureInstancePdfbox");
            
        } catch(ClassNotFoundException e) {
            report.addLine("IAIK PdfSignatureInstancePdfbox not present.");
            
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            pdfboxExistCheck.logger.error(sw.toString());
            return false;
        }
        try {
            PdfSignatureInstancePdfbox instance = new PdfSignatureInstancePdfbox();
            return instance != null;
            
        } catch(Exception e) {
            report.addLine("IAIK PdfSignatureInstancePdfbox present, but not able to initialize it.");
            
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            pdfboxExistCheck.logger.error(sw.toString());
            return false;
        }
    }
    
    private boolean checkPdfSigInstance(Report report) {
        PdfSignatureInstance signatureInstance = PdfSignatureEngine.getInstance();
        return signatureInstance != null;
    }
    
    private boolean checkPdfBox(Report report) {
        try {
            Class.forName("org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature");
            return true;
            
        } catch(ClassNotFoundException e) {
            report.addLine("Apache PDFBox not present. Validation might fail if PDF support is required.");
            
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            pdfboxExistCheck.logger.error(sw.toString());
            return false;
        }
    }
    
    private boolean fileExists(File file) {
        return file.isFile() && file.exists();
    }
}
