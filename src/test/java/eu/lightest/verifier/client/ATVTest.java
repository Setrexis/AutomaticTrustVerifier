package eu.lightest.verifier.client;

import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.HornFailedException;
import eu.lightest.horn.specialKeywords.IAtvApiListener;
import eu.lightest.verifier.ATVConfiguration;
import eu.lightest.verifier.model.report.BufferedStdOutReportObserver;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.StdOutReportObserver;
import eu.lightest.verifier.model.tpl.TplApiListener;
import eu.lightest.verifier.model.transaction.TransactionContainer;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ATVTest {
    
    public static final String PATH_TRANSACTION_SIMPLE = "src/test/exampleData/iaik-test-scheme/LIGHTest_simpleContract.asice";
    public static final String PATH_TRANSACTION_EIDAS = "src/test/exampleData/theAuctionHouse/LIGHTest_theAuctionHouse.asice";
    public static final String PATH_TRANSACTION_DELEGATION = "src/test/exampleData/delegations/LIGHTest_DelegationExample.asice";
    public static final String PATH_TRANSACTION_CERT = "src/test/exampleData/UPRC/cert_UPRC.pem";
    public static final String PATH_TRANSACTION_UPRC_AP = "src/test/exampleData/UPRC/20191018.pem";
    public static final String PATH_TRANSACTION_UPRC_XADES = "src/test/uprc/containers/test.asice";
    public static final String PATH_TRANSACTION_PSO = "src/test/exampleData/pumpkinSeedOil_transaction/LIGHTest_pumpkinSeedOil.asice";
    public static final String PATH_TRANSACTION_XML1CERTFAIL = "src/test/exampleData/TSL/eidas_AT_currenttl_CERTFAIL.xml";
    public static final String PATH_TRANSACTION_XML1SIGFAIL = "src/test/exampleData/TSL/eidas_AT_currenttl_SIGFAIL.xml";
    public static final String PATH_TRANSACTION_PDFinASIC = "src/test/correos/document1pdf.asice";
    public static final String PATH_TRANSACTION_XMLinASIC = "src/test/correos/document2xml.asice";
    public static final String PATH_POLICY_SIMPLE1 = "src/test/policy_simple1.tpl";
    public static final String PATH_POLICY_SIMPLE2 = "src/test/policy_simple2.tpl";
    public static final String PATH_POLICY_SIMPLE3 = "src/test/policy_simple3.tpl";
    public static final String PATH_POLICY_EIDAS1 = "src/test/policy_eidas.tpl";
    public static final String PATH_POLICY_EIDAS_UPRC = "src/test/policy_eidas_uprc.tpl";
    public static final String PATH_POLICY_DELEGATION1 = "src/test/policy_delegation.tpl";
    public static final String PATH_POLICY_CERT = "src/test/exampleData/UPRC/policy_UPRC.tpl";
    public static final String PATH_POLICY_PSO1 = "src/test/policy_pso.tpl";
    public static final String PATH_POLICY_PSO2 = "src/test/policy_pso_simple.tpl";
    public static final String PATH_POLICY_PSODemo = "src/test/policy_pof_withTranslation.tpl";
    public static final String PATH_POLICY_TRANSLATION0WITHCAST = "src/test/policy_translation0_withCast.tpl";
    public static final String PATH_POLICY_TRANSLATION0 = "src/test/policy_translation0.tpl";
    public static final String PATH_POLICY_TRANSLATION1 = "src/test/policy_translation1.tpl";
    public static final String PATH_POLICY_TRANSLATION2 = "src/test/policy_translation2.tpl";
    public static final String PATH_POLICY_PDF1 = "src/test/policy_pades1.tpl";
    public static final String PATH_POLICY_PDF1CORREOS = "src/test/policy_pades1correos.tpl";
    public static final String PATH_POLICY_PDF2 = "src/test/policy_pades2.tpl";
    public static final String PATH_POLICY_XML1 = "src/test/policy_xades1.tpl";
    public static final String PATH_POLICY_XML2 = "src/test/policy_xades2.tpl";
    public static final String PATH_POLICY_XML1GENERIC = "src/test/policy_xades1generic.tpl";
    public static final String PATH_POLICY_XML2GENERIC = "src/test/policy_xades2generic.tpl";
    public static final String PATH_POLICY_XML1CORREOS = "src/test/policy_xades1correos.tpl";
    public static final String PATH_POLICY_GENERIC1 = "src/test/policy_generic.tpl";
    public static final String PATH_POLICY_EIDASbutPEPPOL = "src/test/policy_eidasButPeppol.tpl";
    public static final String PATH_POLICY_UPRC_AP = "src/test/policy_uprc_ap.tpl";
    public static final String PATH_POLICY_FIDO1 = "src/test/fido/fido1.tpl";
    public static final String PATH_TRANSACTION_PDF1 = "src/test/correos/document1.pdf";
    public static final String PATH_TRANSACTION_PDF_WITHTRANSL = "src/test/correos/correos_2019-11-20_withTranslation.pdf";
    public static final String PATH_TRANSACTION_PDF_ESEAL = "src/test/correos/pdf_eseal_signed_2019-11-29.pdf";
    public static final String PATH_TRANSACTION_PDF_ESIGNATURE = "src/test/correos/pdf_esignature_signed_2019-11-29.pdf";
    public static final String PATH_TRANSACTION_XML1 = "src/test/exampleData/TSL/eidas_AT_currenttl.xml";
    public static final String PATH_TRANSACTION_XML1CORREOS = "src/test/correos/xades_2019-11-21.xml";
    public static final String PATH_TRANSACTION_XMLinASICCERTFAIL = "src/test/correos/document2xmlCERTFAIL.asice";
    public static final String PATH_TRANSACTION_XMLinASICSIGFAIL = "src/test/correos/document2xmlSIGFAIL.asice";
    public static final String PATH_TRANSACTION_FIDO1 = "src/test/fido/fido1.json";
    private static TransactionContainer TRANSACTION_SIMPLE = null;
    private static TransactionContainer TRANSACTION_EIDAS = null;
    private static TransactionContainer TRANSACTION_CERT = null;
    private static Report report;
    private static BufferedStdOutReportObserver reportBuffer;
    
    
    @Parameterized.Parameter(0)
    public ATVClient atv;
    
    
    @BeforeClass
    public static void setupClass() throws ConfigurationException {
        ATVConfiguration.init();
    }
    
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        ATVTest.report = new Report();
        StdOutReportObserver stdout_reporter = new StdOutReportObserver();
        ATVTest.report.addObserver(stdout_reporter);
    
        ATVTest.reportBuffer = new BufferedStdOutReportObserver();
        ATVTest.report.addObserver(ATVTest.reportBuffer);
        
        List data = new ArrayList();
        
        data.add(new Object[]{new LocalATVClient(ATVTest.report)});
        //data.add(new Object[]{new RemoteATVClient("https://atvapi.tug.do.nlnetlabs.nl/atvapi", ATVTest.report)});
        
        return data;
    }
    
    @Before
    public void setup() throws ConfigurationException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        ATVConfiguration.init();
    }
    
    private boolean runInterpreter(String PATH_POLICY, String PATH_TRANSACTION) throws HornFailedException {
        System.out.println("Running " + this.atv + " ...");
    
        boolean atvStatus = this.atv.verify(PATH_POLICY, PATH_TRANSACTION);
    
        System.out.println("");
        System.out.println(this.atv.getClass().getSimpleName() + " REPORT: ");
        ATVTest.reportBuffer.print();
        ATVTest.reportBuffer.clearBuffer();
    
        return atvStatus;
    }
    
    private boolean runInterpreterDirectly(String PATH_POLICY, String PATH_TRANSACTION) throws HornFailedException {
        
        IAtvApiListener callback = new TplApiListener(new File(PATH_TRANSACTION), this.report);
        Interpreter interpreter = new Interpreter(callback);
        
        String tplFilePath = PATH_POLICY;
        String query = "accept(Form).";
        String inputVariable = "Form";
        
        return interpreter.run(tplFilePath, query, inputVariable);
    }
    
    @Test
    public void simpleTransaction1() throws HornFailedException {
        // simple transaction with simple policy and print
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_SIMPLE1, ATVTest.PATH_TRANSACTION_SIMPLE);
        
        assertTrue(status);
    }
    
    @Test
    public void simpleTransaction2_FAIL() throws HornFailedException {
        // simple transaction with invalid policy: wrong format extracted
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_SIMPLE2, ATVTest.PATH_TRANSACTION_SIMPLE);
    
        assertFalse(status); // expected: FAILED: Format extraction failed.
    }
    
    @Test
    public void simpleTransaction3_FAIL() throws HornFailedException {
        // simple transaction with invalid policy: wrong field accessed
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_SIMPLE3, ATVTest.PATH_TRANSACTION_SIMPLE);
    
        assertFalse(status); // expected: FAILED: Error in policy: Field 'nonexistingfield' does not exist in transaction.
    }
    
    @Test
    public void eidasTransaction1_FAIL() throws HornFailedException {
        // eidas policy but simple transaction
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_EIDAS1, ATVTest.PATH_TRANSACTION_SIMPLE);
    
        assertFalse(status); // expected: FAILED: Error while parsing form. Wrong format? (bid.xml not found.)
    }
    
    
    @Test
    public void eidasTransaction2() throws HornFailedException {
        // eidas policy and eidas transaction
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_EIDAS1, ATVTest.PATH_TRANSACTION_EIDAS);
        
        assertTrue(status);
    }
    
    @Test
    public void eidasButPeppolTransaction_FAIL() throws HornFailedException {
        // peppol policy and eidas transaction
    
        ATVConfiguration.get().setProperty("trustscheme_claim_default", "peppol_sp_qualified_claim");
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_EIDASbutPEPPOL, ATVTest.PATH_TRANSACTION_EIDAS);
    
        assertFalse(status); // expected: FAILED: Certificate not found on Trust Status List(s).
    }
    
    
    @Test
    public void pumpkinSeedOil0() throws HornFailedException {
        // eidas policy and eidas transaction
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_PSO2, ATVTest.PATH_TRANSACTION_PSO);
        
        assertTrue(status);
    }
    
    @Test
    public void pumpkinSeedOil1() throws HornFailedException {
        // eidas policy and eidas transaction
        
        boolean status = runInterpreter(ATVTest.PATH_POLICY_PSO1, ATVTest.PATH_TRANSACTION_PSO);
        
        assertTrue(status);
    }
    
    @Test
    public void pumpkinSeedOil2demo() throws HornFailedException {
        
        boolean status = runInterpreter(ATVTest.PATH_POLICY_PSODemo, ATVTest.PATH_TRANSACTION_PSO);
        
        assertTrue(status);
    }
    
    @Test
    //@Ignore("Disabled until #14 is done")
    public void translated00() throws HornFailedException {
        // fantasyland policy and eidas transaction
        // trusted = source = fantasyland (TTA)
        // claimed = target = eidas (TSPA)
        
        //ATVConfiguration.get().setProperty("dane_verification_enabled", false);
        
        boolean status = runInterpreter(ATVTest.PATH_POLICY_TRANSLATION0, ATVTest.PATH_TRANSACTION_EIDAS);
        
        assertTrue(status);
    }
    
    @Test
    public void translated01() throws HornFailedException {
        // fantasyland policy and eidas transaction
        // trusted = source = fantasyland (TTA)
        // claimed = target = eidas (TSPA)
        
        //ATVConfiguration.get().setProperty("dane_verification_enabled", false);
        
        boolean status = runInterpreter(ATVTest.PATH_POLICY_TRANSLATION2, ATVTest.PATH_TRANSACTION_EIDAS);
        
        assertTrue(status);
    }
    
    @Test
    //@Ignore("Disabled until LIGHTest/trustpolicyinterpreter#7 is done")
    public void translated0withCast() throws HornFailedException {
        // https://extgit.iaik.tugraz.at/LIGHTest/trustpolicyinterpreter/issues/7
    
        ATVConfiguration.get().setProperty("dane_verification_enabled", true);
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_TRANSLATION0WITHCAST, ATVTest.PATH_TRANSACTION_EIDAS);
    
        assertTrue(status);
    }
    
    @Test
    public void translated1() throws HornFailedException {
        // eidas policy and eidas transaction
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_TRANSLATION1, ATVTest.PATH_TRANSACTION_EIDAS);
        
        assertTrue(status);
    }
    
    @Test
    //@Ignore("Disabled until LIGHTest/TTA#4 is done")
    public void translated2() throws HornFailedException {
        // eidas policy and eidas transaction
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_TRANSLATION2, ATVTest.PATH_TRANSACTION_EIDAS);
        
        assertTrue(status);
    }
    
    
    @Test
    public void padesCorreos1() throws HornFailedException {
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_PDF1, ATVTest.PATH_TRANSACTION_PDF1);
        
        assertTrue(status);
    }
    
    @Test
    public void padesCorreos1correos() throws HornFailedException {
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_PDF1CORREOS, ATVTest.PATH_TRANSACTION_PDF1);
        
        assertTrue(status);
    }
    
    @Test
    public void padesCorreos1transl() throws HornFailedException {
        
        boolean status = runInterpreter(ATVTest.PATH_POLICY_PDF1CORREOS, ATVTest.PATH_TRANSACTION_PDF_WITHTRANSL);
        
        assertTrue(status);
    }
    
    @Test
    public void padesCorreosESeal() throws HornFailedException {
        
        boolean status = runInterpreter(ATVTest.PATH_POLICY_PDF1CORREOS, ATVTest.PATH_TRANSACTION_PDF_ESEAL);
        
        assertTrue(status);
    }
    
    @Test
    public void padesCorreosESignature() throws HornFailedException {
        
        boolean status = runInterpreter(ATVTest.PATH_POLICY_PDF1CORREOS, ATVTest.PATH_TRANSACTION_PDF_ESIGNATURE);
        
        assertTrue(status);
    }
    
    @Test
    public void padesCorreos1asic() throws HornFailedException {
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_PDF2, ATVTest.PATH_TRANSACTION_PDFinASIC);
        
        assertTrue(status);
    }
    
    @Test
    public void xadesTsl1() throws HornFailedException {
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_XML1, ATVTest.PATH_TRANSACTION_XML1);
    
        assertTrue(status);
    }
    
    
    @Test
    public void xadesCorreos() throws HornFailedException {
        
        boolean status = runInterpreter(ATVTest.PATH_POLICY_XML1CORREOS, ATVTest.PATH_TRANSACTION_XML1CORREOS);
        
        assertTrue(status);
    }
    
    @Test
    public void xadesTsl1certFail() throws HornFailedException {
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_XML1, ATVTest.PATH_TRANSACTION_XML1CERTFAIL);
        
        assertFalse(status);
    }
    
    @Test
    public void xadesTsl1sigFail() throws HornFailedException {
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_XML1, ATVTest.PATH_TRANSACTION_XML1SIGFAIL);
        
        assertFalse(status);
    }
    
    @Test
    public void xadesTsl1asic() throws HornFailedException {
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_XML2, ATVTest.PATH_TRANSACTION_XMLinASIC);
        
        assertTrue(status);
    }
    
    @Test
    public void xadesTsl1asicCertFail() throws HornFailedException {
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_XML2, ATVTest.PATH_TRANSACTION_XMLinASICCERTFAIL);
        
        assertFalse(status);
    }
    
    @Test
    public void xadesTsl1asicSigFail() throws HornFailedException {
    
        boolean status = runInterpreter(ATVTest.PATH_POLICY_XML2, ATVTest.PATH_TRANSACTION_XMLinASICSIGFAIL);
        
        assertFalse(status);
    }
    
    
    @Test
    public void fido1Test() throws HornFailedException {
        
        boolean status = runInterpreter(ATVTest.PATH_POLICY_FIDO1, ATVTest.PATH_TRANSACTION_FIDO1);
        
        assertTrue(status);
    }
}