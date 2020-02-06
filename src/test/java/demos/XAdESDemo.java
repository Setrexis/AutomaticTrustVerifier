package demos;

// Copyright (C) 2002 IAIK
// http://jce.iaik.at
//
// Copyright (C) 2003 - @year@ Stiftung Secure Information and
//                           Communication Technologies SIC
// http://www.sic.st
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.


import iaik.security.provider.IAIK;
import iaik.x509.X509Certificate;
import iaik.xml.crypto.XSecProvider;
import iaik.xml.crypto.utils.KeySelectorImpl;
import iaik.xml.crypto.xades.*;
import iaik.xml.crypto.xades.timestamp.TimeStampToken;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Provider;
import java.security.Security;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

/**
 * This is a simple example of validating the normative parts of a XADES-A
 * signature.
 *
 * @author mcentner
 */
public class XAdESDemo {
    
    public static final boolean debug = true; //(new File("debug.flag")).exists();
    //private static String DEMO_XML_FILE = "src/test/exampleData/TSL/eidas_AT_currenttl.xml";
    //private static String DEMO_XML_FILE = "src/test/exampleData/TSL/uprc_NEW_SP1.xml";
    //private static String DEMO_XML_FILE = "src/test/exampleData/TSL/uprc_NEW_SP1_BROKEN.xml";
    private static String DEMO_XML_FILE = "src/test/unhcr/unhcr-federation-new1.dafi-demo.xml";
    
    public static void main(String[] args)
            throws Exception {
    
        // Register IAIK Provider
        IAIK.addAsJDK14Provider();
//
//        if(args == null || args.length < 1) {
//            System.out.println("Usage: java " + XAdESDemo.class.getName() + " <signature.xml>");
//            System.exit(1);
//        }
    
        // Create a schema validating parser
        File schemaFile = new File("src/test/exampleData/TSL/ts_119612v020201_201601xsd.xsd");
    
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setExpandEntityReferences(false);
        dbf.setValidating(true);
        dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
        //dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", schemaFile);
        dbf.setAttribute("http://apache.org/xml/features/validation/schema/normalized-value",
                Boolean.FALSE);
    
        File inputFile = new File(XAdESDemo.DEMO_XML_FILE);
        if(!inputFile.isFile()) {
            throw new Exception("Cannot find input file '" + args[0] + "'");
        }
    
        System.out.println("Step 1 A ...");
    
        System.out.println("parse the signature document ...");
        // parse the signature document
        Document doc = dbf.newDocumentBuilder().parse(
                new BufferedInputStream(new FileInputStream(inputFile)));
    
        System.out.println("Find Signature element ...");
        // Find Signature element
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if(nl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        }
    
        System.out.println("Step 1 B ...");
    
        boolean atleastOneSigValid = false;
    
        System.out.println("Validating " + nl.getLength() + " ...");
    
        for(int i = 0; i < nl.getLength(); i++) {
            System.out.println("##### Signature #" + i + ":  ##################################################");
        
            Node sigNode = nl.item(0);
            boolean sigValid = XAdESDemo.validateSig(sigNode);
        
            if(sigValid) {
                atleastOneSigValid = true;
                System.out.println("Signature #" + i + " was valid.");
                break;
            } else {
                System.out.println("Signature #" + i + " was invalid.");
            }
        }
    
        if(!atleastOneSigValid) {
            System.out.println("Validated " + nl.getLength() + " signatures. None was valid.");
        }
    
    }
    
    private static boolean validateSig(Node sigNode) throws Exception {
        // Create a DOM XMLSignatureFactory that will be used to unmarshal the
        // document containing the XMLSignature
        Provider provider = new XSecProvider();
        Security.addProvider(provider);
        //move other XMLDsig provider to the end
        Provider otherXMLDsigProvider = Security.getProvider("XMLDSig");
        if(otherXMLDsigProvider != null) {
            Security.removeProvider(otherXMLDsigProvider.getName());
            Security.addProvider(otherXMLDsigProvider);
        }
        XMLSignatureFactory sfac = XMLSignatureFactory.getInstance("DOM", provider);
        
        // Create a DOMValidateContext and specify a KeyValue KeySelector
        // and document context
        DOMValidateContext valContext = new DOMValidateContext(new KeySelectorImpl(),
                sigNode);
        
        if(XAdESDemo.debug) {
            valContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        }
        //valContext.setBaseURI(baseURI.toString());
        
        System.out.println("Step 1 C ...");
        
        // unmarshal the XMLSignature
        XMLSignature signature = sfac.unmarshalXMLSignature(valContext);
        
        
        List keyInfo = signature.getKeyInfo().getContent();
        Iterator ki = keyInfo.iterator();
        
        System.out.println("Extracting Signer Cert ...");
        System.out.println(" Iterating over " + keyInfo.size() + " key info ...");
        
        while(ki.hasNext()) {
            XMLStructure info = (XMLStructure) ki.next();
            if(!(info instanceof X509Data)) {
                System.out.println(" Not X509Data, but " + info.getClass().getName());
                continue;
            }
            
            X509Data x509Data = (X509Data) info;
            
            Iterator xi = x509Data.getContent().iterator();
            while(xi.hasNext()) {
                Object o = xi.next();
                
                System.out.println(" found " + o.getClass().getName());
                if(o instanceof String) {
                    System.out.println("  --> " + (String) o);
                } else if(o instanceof X509Certificate) {
                    X509Certificate certificate = (X509Certificate) o;
                    System.out.println("  --> " + certificate.getSubjectDN());
                }
                
            }
        }
        
        
        // Validate the XMLSignature
        boolean coreValidity = signature.validate(valContext);
        
        System.out.println("Step 1 D ...");
        
        // Check core validation status
        if(coreValidity == false) {
            System.err.println("Signature failed core validation");
        } else {
            System.out.println("Signature passed core validation");
        }
        
        boolean sv = signature.getSignatureValue().validate(valContext);
        System.out.println("signature validation status: " + sv);
        // check the validation status of each Reference
        Iterator i = signature.getSignedInfo().getReferences().iterator();
        for(int j = 0; i.hasNext(); j++) {
            Reference ref = (Reference) i.next();
            boolean refValid = ref.validate(valContext);
            System.out.println("ref[" + j + "] validity status: " + refValid);
            System.out.println("  id:" + ref.getId());
            System.out.println("  uri:" + ref.getURI());
            System.out.println("  calculated Digest:" + Base64.getEncoder().encodeToString(ref.getCalculatedDigestValue()));
            System.out.println("  claimed Digest:   " + Base64.getEncoder().encodeToString(ref.getDigestValue()));
            
            if(!refValid) {
//                    System.out.println("digest input:");
//                    InputStream digestInput = ref.getDigestInputStream();
//                    byte[] buf = new byte[1024];
//                    int count;
//                    while((count = digestInput.read(buf)) != -1) {
//                        System.out.write(buf, 0, count);
//                    }
//                    System.out.println("\n");
            }
        }
        
        
        System.out.println("Step 2 ...");
        
        boolean sigTSsValid = true;
        boolean aTSsValid = true;
        // validate Signature- and ArchiveTimeStamps if present
        QualifyingProperties qp = ((XAdESSignature) signature).getQualifyingProperties();
        if(qp != null) {
            System.out.println("QualifyingProperties not null ...");
            
            UnsignedProperties up = qp.getUnsignedProperties();
            if(up != null) {
                System.out.println("UnsignedProperties not null ...");
                UnsignedSignatureProperties usp = up.getUnsignedSignatureProperties();
                if(usp != null) {
                    System.out.println("UnsignedSignatureProperties not null ...");
                    
                    List sigTSs = usp.getSignatureTimeStamps();
                    {
                        for(Iterator iter = sigTSs.iterator(); iter.hasNext(); ) {
                            SignatureTimeStamp sigTS = (SignatureTimeStamp) iter.next();
                            // validate time-stamp
                            
                            boolean sigTsValid = sigTS.validate(valContext);
                            if(sigTsValid) {
                                TimeStampToken tsToken = sigTS.getTimeStampToken();
                                
                                System.out.println("SignatureTimeStamp validation date = "
                                        + tsToken.getTime());
                            } else {
                                System.out.println("SignatureTimeStamp is invalid!");
                                //                return;
                            }
                            
                            sigTSsValid = sigTSsValid && sigTsValid;
                        }
                    }
                    {
                        List archTSs = usp.getArchiveTimeStamps();
                        
                        for(Iterator iter = archTSs.iterator(); iter.hasNext(); ) {
                            ArchiveTimeStamp archTS = (ArchiveTimeStamp) iter.next();
                            // validate time-stamp
                            
                            boolean aTsValid = archTS.validate(valContext);
                            if(aTsValid) {
                                TimeStampToken tsToken = archTS.getTimeStampToken();
                                
                                System.out.println("ArchiveTimeStamp validation date = "
                                        + tsToken.getTime());
                            } else {
                                System.out.println("ArchiveTimeStamp is invalid!");
                                //                return;
                            }
                            aTSsValid = aTSsValid && aTsValid;
                            
                            if(XAdESDemo.debug) {
                                System.out.println("\n\n<ValidateA>");
                                System.out.println("ArchiveTimeStamp input:");
                                //                System.out.write(((CachedInputStream) ats.getTimeStampInputStream()).getCachedBytes());
                                InputStream is = archTS.getTimeStampInputStream();
                                byte[] data = new byte[4096];
                                int r;
                                while((r = is.read(data)) != -1) {
                                    System.out.write(data, 0, r);
                                }
                                System.out.println("</ValidateA>");
                            }
                        }
                    }
                }
            } else {
                System.out.println("UnsignedProperties was null ...");
            }
        }
        
        System.out.println("Almost done? ...");
        
        System.out.println("");
        System.out.println("");
        if(!coreValidity || !sigTSsValid || !aTSsValid) {
            System.out.println("Validation Failed: \n coreValidity " + coreValidity
                    + "\n sigTSsValid " + sigTSsValid + "\n aTSsValid " + aTSsValid);
        } else {
            System.out.println("Validation OK: \n coreValidity " + coreValidity
                    + "\n sigTSsValid " + sigTSsValid + "\n aTSsValid " + aTSsValid);
        }
        
        return coreValidity && sigTSsValid && aTSsValid;
    }
}
