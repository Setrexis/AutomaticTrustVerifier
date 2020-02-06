package eu.lightest.verifier.model.format.genericXML;

import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.StdOutReportObserver;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GenericXMLFormatTest {
    
    private static final String PATH_TRANSACTION_EIDAS = "src/test/exampleData/theAuctionHouse/LIGHTest_theAuctionHouse.asice";
    private static final String PATH_TRANSACTION_PSO = "src/test/exampleData/pumpkinSeedOil_transaction/LIGHTest_pumpkinSeedOil.asice";
    
    
    private static File transaction_eidas;
    private static File transaction_pso;
    private static Report report;
    
    
    @BeforeClass
    public static void setUp() throws Exception {
        GenericXMLFormatTest.transaction_eidas = new File(GenericXMLFormatTest.PATH_TRANSACTION_EIDAS);
        GenericXMLFormatTest.transaction_pso = new File(GenericXMLFormatTest.PATH_TRANSACTION_PSO);
        
        GenericXMLFormatTest.report = new Report();
        StdOutReportObserver observer = new StdOutReportObserver();
        GenericXMLFormatTest.report.addObserver(observer);
        
        
    }
    
    @Test
    public void initTest1() throws Exception {
    
        GenericXMLFormat parser1 = new GenericXMLFormat(GenericXMLFormatTest.transaction_eidas, GenericXMLFormatTest.report);
        
        parser1.init();
        
        assertTrue(true);
    }
    
    @Test
    public void initTest2() throws Exception {
    
        GenericXMLFormat parser2 = GenericXMLFormat.getParserForFormat("olamFormat2_0", GenericXMLFormatTest.transaction_eidas, GenericXMLFormatTest.report);
    
        assertNotNull(parser2);
    
        assertTrue(true);
    }
    
    @Test
    public void initTestPSO() throws Exception {
    
        String formatId = "pumpkinSeedOil";
        GenericXMLFormat parser2 = GenericXMLFormat.getParserForFormat(formatId, GenericXMLFormatTest.transaction_pso, GenericXMLFormatTest.report);
    
        assertNotNull(parser2);
    
        assertTrue(true);
    }
    
    
    @Test
    public void getSupportedFormats() {
    
        for(Map.Entry<String, InputStream> format : GenericXMLFormat.getSupportedFormats().entrySet()) {
            System.out.println(format.getKey() + ": " + format.getValue());
        }
        
        assertTrue(true);
    }
}