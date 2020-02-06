package eu.lightest.verifier.model.format.peppolTransaction;

import eu.lightest.verifier.model.format.theAuctionHouse2019.AH19Format;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.transaction.TransactionContainer;
import eu.lightest.verifier.model.transaction.TransactionFactory;

import java.io.File;

public class PEPPOLTransactionFormat extends AH19Format {
    
    //    public static final String PATH_CONTRACT = "contract"; // TPL path
    private static final String FILEPATH_CONTRACT = "transaction"; // Path to file inside container
    
    private static final String FORMAT_ID = "peppolTransaction";
    //    private static Logger logger = Logger.getLogger(PEPPOLTransactionFormat.class);
    private final TransactionContainer transaction;
    private String contract;
    
    public PEPPOLTransactionFormat(Object transactionFile, Report report) {
        super(transactionFile, report);
        if(transactionFile instanceof File) {
            this.transaction = TransactionFactory.getTransaction((File) transactionFile);
        } else {
            throw new IllegalArgumentException("Transaction of type:" + transactionFile.getClass().toString() + ",  expected: File");
        }
    }
    
    @Override
    public String getFormatId() {
        return PEPPOLTransactionFormat.FORMAT_ID;
    }
    
    @Override
    public void init() throws Exception {
        this.contract = this.transaction.extractFileString(PEPPOLTransactionFormat.FILEPATH_CONTRACT);
        if(this.contract == null) {
            throw new Exception("Error while parsing form. Wrong format? (" + PEPPOLTransactionFormat.FILEPATH_CONTRACT + " not found.)");
        }
    }
    
}