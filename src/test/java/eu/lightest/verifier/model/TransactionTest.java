package eu.lightest.verifier.model;

import demos.DSSDemo1;
import eu.lightest.verifier.client.ATVTest;
import eu.lightest.verifier.model.transaction.ASiCSignature;
import eu.lightest.verifier.model.transaction.TransactionContainer;
import eu.lightest.verifier.model.transaction.TransactionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.List;

public class TransactionTest {
    
    public static final String PATH_PREFIX = "src/test/uprc/";
    public static final String ASIC_CADES = DSSDemo1.PATH_PREFIX + "containers/peppol-transaction.asice";
    public static final String ASIC_XADES = DSSDemo1.PATH_PREFIX + "containers/peppol-transaction-xades.asice";
    public static final String ASIC_XADES2 = ATVTest.PATH_TRANSACTION_PDFinASIC;
    
    public static final String DSS_ASIC_XADES = "src/test/container-signed-xades-baseline-b.asice";
    public static final String DSS_ASIC_CADES = "src/test/container-signed-cades-baseline-b.asice";
    public static final String DSS_ASIC_CADES_t = "src/test/container-signed-cades-baseline-b_timestamp.asice";
    
    public static final String UPRC_CADES1 = "src/test/uprc/containers/peppol-asic-cades.asice";
    public static final String UPRC_CADES1NEWMANIFEST = "src/test/uprc/containers/peppol-asic-cades-newmanifest.asice";
    
    @Before
    public void setUp() {
        
        TransactionFactory.DD4J_ENABLED = false;
        
    }
    
    @Test
    public void asicTransationTest() {
        File transaction_file = new File("src/test/exampleData/theAuctionHouse/LIGHTest_theAuctionHouse.asice");
        TransactionContainer asicTransaction = TransactionFactory.getTransaction(transaction_file);
    
        doTest(asicTransaction);
    }
    
    
    @Test
    public void dssCADES() {
        File asics = new File(TransactionTest.DSS_ASIC_CADES);
        TransactionContainer transaction = TransactionFactory.getTransaction(asics);
        
        doTest(transaction);
    }
    
    @Test
    public void dssCADESt() {
        File asics = new File(TransactionTest.DSS_ASIC_CADES_t);
        TransactionContainer transaction = TransactionFactory.getTransaction(asics);
        
        doTest(transaction);
    }
    
    @Test
    public void dssXADES() {
        File asics = new File(TransactionTest.DSS_ASIC_XADES);
        TransactionContainer transaction = TransactionFactory.getTransaction(asics);
        
        doTest(transaction);
    }
    
    
    private void doTest(TransactionContainer transaction) {
        Assert.assertNotNull(transaction);
        
        X509Certificate signingCertificate = transaction.getSigningCertificate();
        Assert.assertNotNull(signingCertificate);
        
        for(String f : transaction.getFileList()) {
            System.out.println("* " + f);
        }
        
        
        List<ASiCSignature> sigs = transaction.getSignatures();
        Assert.assertNotNull(sigs);
        Assert.assertTrue(sigs.size() >= 1);
        
        Assert.assertTrue(transaction.verifySignature(signingCertificate, sigs.get(0)));
    }
}
