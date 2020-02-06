package eu.lightest.verifier.model.format;

import eu.lightest.verifier.wrapper.XMLUtil;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class XMLUtilTest {
    
    private static final String sample_xml = "src/test/exampleData/theAuctionHouse/bid.xml";
    private String xml;
    
    @Before
    public void setup() throws IOException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        
        this.xml = new String(Files.readAllBytes(Paths.get(XMLUtilTest.sample_xml)));
        
        //System.out.println(this.xml);
    }
    
    private void printAndAssert(String s) {
        assertNotNull(s);
        System.out.println(s);
    }
    
    @Test
    public void simpleTest1() throws IOException, SAXException, ParserConfigurationException {
        XMLUtil helper = new XMLUtil(this.xml);
        
        printAndAssert(helper.getAttribute("format"));
        printAndAssert(helper.getElement("person.name"));
        printAndAssert(helper.getElement("lot_number"));
        printAndAssert(helper.getElement("bid"));
    }
    
    @Test
    public void invalidKeyTest1() throws IOException, SAXException, ParserConfigurationException {
        XMLUtil helper = new XMLUtil(this.xml);
        
        assertNull(helper.getElement("somethingsomething"));
        assertNull(helper.getElement("bla.blub"));
        assertNull(helper.getElement("person.nowthis"));
    }
    
}