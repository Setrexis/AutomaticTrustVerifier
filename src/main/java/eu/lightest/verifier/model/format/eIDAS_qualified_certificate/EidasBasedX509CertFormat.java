package eu.lightest.verifier.model.format.eIDAS_qualified_certificate;

import eu.lightest.verifier.model.report.Report;

public class EidasBasedX509CertFormat extends EidasCertFormat {
    
    private static final String FORMAT_ID = "x509cert";
    
    public EidasBasedX509CertFormat(Object transaction, Report report) {
        super(transaction, report);
    }
    
    @Override
    public String getFormatId() {
        return EidasBasedX509CertFormat.FORMAT_ID;
    }
}
