package eu.lightest.verifier.wrapper;

import eu.lightest.verifier.model.trustscheme.X509Helper;
import iaik.security.provider.IAIK;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.AuthorityKeyIdentifier;
import iaik.x509.extensions.SubjectKeyIdentifier;
import iaik.xml.crypto.XSecProvider;
import iaik.xml.crypto.utils.KeySelectorImpl;
import iaik.xml.crypto.xades.*;
import iaik.xml.crypto.xades.timestamp.TimeStampToken;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import sun.security.x509.X509CertImpl;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.*;

public class XAdESHelper extends AdvancedDocHelper {
    // XAdES verification helper,
    // based on IAIK-XAdES demo
    
    public static final String XSD_SCHEME_ETSI_TSL = "Examples/TSL/ts_119612v020201_201601xsd.xsd";
    private static Logger logger = Logger.getLogger(XAdESHelper.class);
    private final String schema;
    private final InputSource xmlSource;
    private boolean validateSchema = false;
    private XMLSignature signature = null;
    private List<X509Certificate> signerCerts = new ArrayList<>();
    private boolean certIsSelfsigned;
    private boolean alreadyVerified = false;
    private boolean allSigsOK = false;
    
    public XAdESHelper(byte[] xmlContent) {
        this(new String(xmlContent), null);
    }
    
    public XAdESHelper(String xmlContent, String pathToScheme) {
        this.xmlSource = new InputSource(new StringReader(xmlContent));
        this.schema = pathToScheme;
        
        if(this.schema == null) {
            this.validateSchema = false;
        }
    }
    
    public XAdESHelper(File xmlFile) throws FileNotFoundException {
        this(xmlFile, null);
    }
    
    public XAdESHelper(File xmlFile, String pathToScheme) throws FileNotFoundException {
        this.xmlSource = new InputSource(new FileInputStream(xmlFile));
        this.schema = pathToScheme;
        
        if(this.schema == null) {
            this.validateSchema = false;
        }
    }
    
    public void setValidateSchema(boolean validateSchema) {
        this.validateSchema = validateSchema;
    }
    
    @Override
    public boolean verify() {
        return verify(false);
    }
    
    @Override
    public boolean verify(boolean skipSignatureValidation) {
        if(this.alreadyVerified) {
            return this.allSigsOK;
        }
    
        this.signature = null;
    
        // Register IAIK Provider
        IAIK.addAsJDK14Provider();
    
        // Create a schema validating parser
        DocumentBuilder builder = null;
        try {
            builder = initDocumentBuiler();
        } catch(ParserConfigurationException e) {
            XAdESHelper.logger.error("Cannot initialize document builder: " + e.getMessage());
            return false;
        }
    
        // parse the signature document
        Document doc = null;
        try {
            InputSource source = this.xmlSource;
            doc = builder.parse(source);
        } catch(SAXException | IOException e) {
            XAdESHelper.logger.error("Cannot parse XML content: " + e.getMessage());
            return false;
        }
    
        // Find Signature element
        NodeList signatureNodes = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if(signatureNodes.getLength() == 0) {
            XAdESHelper.logger.error("Cannot find Signature element");
            return false;
        }

//        if(signatureNodes.getLength() > 1) {
//            XAdESHelper.logger.warn("Found " + signatureNodes.getLength() + " signature elements, using only first one.");
//            // TODO iterate over all sigs,
//            //      use setVerificationResult to store multiple results
//        }
    
        boolean atleastOneSigValid = false;
        XAdESHelper.logger.info("Validating " + signatureNodes.getLength() + " signatures ...");
    
        for(int i = 0; i < signatureNodes.getLength(); i++) {
            Node sigItem = signatureNodes.item(0);
            boolean sigValid = verifySignature(sigItem, skipSignatureValidation);
        
            if(sigValid) {
                atleastOneSigValid = true;
                XAdESHelper.logger.info("Signature #" + i + " was valid.");
                break;
            } else {
                XAdESHelper.logger.warn("Signature #" + i + " was invalid.");
            }
        }
    
        if(!atleastOneSigValid) {
            XAdESHelper.logger.info("Validated " + signatureNodes.getLength() + " signatures. None was valid.");
        }
    
        return atleastOneSigValid;
    }
    
    private boolean verifySignature(Node sigItem, boolean skipSignatureValidation) {
        XMLSignatureFactory sfac = initSigFactory();
        
        // Create a DOMValidateContext and specify a KeyValue KeySelector
        // and document context
        DOMValidateContext valContext = new DOMValidateContext(new KeySelectorImpl(), sigItem);

//        if(XAdESDemo.debug) {
        valContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
//        }
        
        if(this.signature == null) { // we already extracted a sig (e.g. in lightweight mode)
            // unmarshal the XMLSignature
            try {
                this.signature = sfac.unmarshalXMLSignature(valContext);
            } catch(MarshalException e) {
                XAdESHelper.logger.error("Cannot unmarshal XML signature: " + e.getMessage());
                XAdESHelper.logger.error("", e);
                return false;
            }
        }
        
        if(skipSignatureValidation) {
            // lightweight mode (e.g. to just load signer cert
            return true;
        }
        
        // Validate the XMLSignature
        boolean coreValidity = false;
        try {
            coreValidity = this.signature.validate(valContext);
        } catch(XMLSignatureException e) {
            XAdESHelper.logger.error("Cannot validate XML signature: " + e.getMessage());
            XAdESHelper.logger.error("", e);
            
            getCertificate(); // store cert anyway ...
            this.signature = null;
            return false;
        }
        
        
        // Check core validation status
        if(coreValidity == false) {
            XAdESHelper.logger.error("Signature failed core validation");
        } else {
            XAdESHelper.logger.info("Signature passed core validation");
        }
        
        boolean certValid = false;
        boolean sigTSsValid = true;
        boolean aTSsValid = true;
        boolean timestampsPresent = false;
        // validate Signature- and ArchiveTimeStamps if present
        // (Trust Lists use  XAdES-BES (for "Basic Electronic Signature"), so there are no timestamps.)
        QualifyingProperties qp = ((XAdESSignature) this.signature).getQualifyingProperties();
        if(qp != null) {
            XAdESHelper.logger.info("QualifyingProperties not null.");
            
            Date signingDate = qp.getSignedProperties().getSignedSignatureProperties().getSigningTime().getDate();
            certValid = this.verifyCertificate(signingDate);
            
            UnsignedProperties up = qp.getUnsignedProperties();
            if(up != null) {
                XAdESHelper.logger.info("UnsignedProperties not null.");
                UnsignedSignatureProperties usp = up.getUnsignedSignatureProperties();
                
                if(usp != null) {
                    XAdESHelper.logger.info("UnsignedSignatureProperties not null.");
                    XAdESHelper.logger.info("Validating Signature- and ArchiveTimeStamps ...");
                    timestampsPresent = true;
                    
                    sigTSsValid = validateSigTS(usp, valContext);
                    aTSsValid = validateArchTS(usp, valContext);
                }
            } else {
                XAdESHelper.logger.info("UnsignedProperties was null, no signature- and ArchiveTimeStamps validation performed.");
            }
        }
        
        if(!certValid || !coreValidity || !sigTSsValid || !aTSsValid) {
            XAdESHelper.logger.error("XAdES Validation Failed:");
            this.signature = null;
        } else {
            XAdESHelper.logger.info("XAdES Validation OK:");
        }
        XAdESHelper.logger.info("  cert Validity:               " + certValid);
        XAdESHelper.logger.info("    cert selfsigned:           " + this.certIsSelfsigned);
        XAdESHelper.logger.info("  core Validity:               " + coreValidity);
        if(timestampsPresent) {
            XAdESHelper.logger.info("  SignatureTimeStamp Validity: " + sigTSsValid);
            XAdESHelper.logger.info("  ArchiveTimeStamp Validity:   " + aTSsValid);
        }
        
        this.allSigsOK = certValid && coreValidity && sigTSsValid && aTSsValid;
        this.alreadyVerified = true;
        
        if(this.signerCerts != null && !this.signerCerts.isEmpty()) {
            setVerificationResult(this.signerCerts.get(0), this.allSigsOK);
        }
        
        return this.allSigsOK;
    }
    
    
    @Override
    public X509Certificate getCertificate() {
        if(this.signerCerts == null) {
            this.signerCerts = new ArrayList<>();
        }
        if(!this.signerCerts.isEmpty()) {
            return this.signerCerts.get(0);
        }
        
        if(this.signature == null) {
            XAdESHelper.logger.warn("No certificate available. Run verify() first.");
            return null;
        }
    
        List keyInfo = this.signature.getKeyInfo().getContent();
        Iterator ki = keyInfo.iterator();
    
        XAdESHelper.logger.info("Iterating over " + keyInfo.size() + " key info ...");
        
        while(ki.hasNext()) {
            XMLStructure info = (XMLStructure) ki.next();
            if(!(info instanceof X509Data)) {
                XAdESHelper.logger.info("Not X509Data, but " + info.getClass().getName());
                continue;
            }
            
            X509Data x509Data = (X509Data) info;
    
            Iterator xi = x509Data.getContent().iterator();
            while(xi.hasNext()) {
                Object o = xi.next();
    
                castAndExtractCert(o);
    
            }
        }
    
        if(this.signerCerts.size() > 1) {
            XAdESHelper.logger.warn("Found more than 1 Signer Cert. Using first one (usually leaf) for now.");
            // Since the xmldsig standard does not specify an ordering, this can be considered unstable.
            // see https://www.w3.org/TR/xmldsig-core1/#sec-X509Data
        }
    
        return this.signerCerts.get(0);
    }
    
    private void castAndExtractCert(Object o) {
        /*
        This is so complicated because ...
            "The same class definition, when loaded by different classloaders,
            is seen as two different classes by the JVM.
            So instanceof or casts between the two fail."
         */
        
        if(!(o instanceof X509Certificate)) {
            XAdESHelper.logger.warn("Not " + X509Certificate.class.getName() + ", but " + o.getClass().getName());
            if(o instanceof X509CertImpl) {
                XAdESHelper.logger.error("This might happen if IAIK JCE is not the first provider ...");
            }
        }
        
        try {
            XAdESHelper.logger.debug("ClassLoader of o is     " + o.getClass().getClassLoader());
            XAdESHelper.logger.debug("ClassLoader of class is " + X509Certificate.class.getClassLoader());
            
            Method encode = o.getClass().getMethod("getEncoded");
            byte[] encodedCert = (byte[]) encode.invoke(o);
    
            X509Certificate extractedCert = new X509Certificate(encodedCert);
            this.signerCerts.add(extractedCert);
    
            XAdESHelper.logger.info("X509Certificate extracted:" + extractedCert.getSubjectDN());
            
        } catch(ClassCastException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | CertificateException e) {
            XAdESHelper.logger.error("", e);
            XAdESHelper.logger.error("Cannot cast cert: " + e.getMessage());
        }
    }
    
    private boolean verifyCertificate(Date signingDate) {
        X509Certificate cert = getCertificate();
    
        if(cert == null) {
            XAdESHelper.logger.error("Cert is null. Not sure what to verify.");
            return false;
        }
        
        XAdESHelper.logger.info("Verifying cert    " + cert.getSubjectDN());
        XAdESHelper.logger.info("issued by         " + cert.getIssuerDN());
        XAdESHelper.logger.info("for doc signed at " + signingDate);
        
        try {
            cert.checkValidity(signingDate);
        } catch(CertificateExpiredException | CertificateNotYetValidException e) {
            XAdESHelper.logger.error("Cert validity check failed: " + e.getMessage());
            return false;
        }
        
        try {
            if(this.isCertSelfsigned()) {
                cert.verify();
            }
            
        } catch(CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            XAdESHelper.logger.error("Cert verification failed: " + e.getMessage());
            return false;
        }
        
        return true;
    }
    
    private boolean isCertSelfsigned() {
        X509Certificate cert = getCertificate();
        
        int pathLenConstraint = cert.getBasicConstraints();
        
        XAdESHelper.logger.info("pathLenConstraint:  " + (pathLenConstraint == Integer.MAX_VALUE ? "MAXINT" : pathLenConstraint));
        XAdESHelper.logger.info("isCa:               " + (pathLenConstraint == -1));
        
        AuthorityKeyIdentifier authorityKeyIdentifier = X509Helper.genAuthorityKeyIdentifier(cert);
        SubjectKeyIdentifier subjectKeyIdentifier = X509Helper.genSubjectKeyIdentifier(cert);
        
        this.certIsSelfsigned = false;
        if(authorityKeyIdentifier != null && subjectKeyIdentifier != null) {
            this.certIsSelfsigned = Arrays.equals(authorityKeyIdentifier.getKeyIdentifier(), subjectKeyIdentifier.get());
        }
        
        XAdESHelper.logger.info("isSelfsigned:       " + this.certIsSelfsigned);
        
        return this.certIsSelfsigned;
    }
    
    private boolean validateArchTS(UnsignedSignatureProperties usp, DOMValidateContext valContext) {
        boolean aTSsValid = true;
        List archTSs = usp.getArchiveTimeStamps();
        
        for(Iterator iter = archTSs.iterator(); iter.hasNext(); ) {
            ArchiveTimeStamp archTS = (ArchiveTimeStamp) iter.next();
            // validate time-stamp
            
            boolean aTsValid = false;
            try {
                aTsValid = archTS.validate(valContext);
                
                if(aTsValid) {
                    TimeStampToken tsToken = archTS.getTimeStampToken();
                    
                    XAdESHelper.logger.info("ArchiveTimeStamp validation date = "
                            + tsToken.getTime());
                } else {
                    XAdESHelper.logger.warn("ArchiveTimeStamp is invalid!");
                    //                return;
                }
                aTSsValid = aTSsValid && aTsValid;
            } catch(XMLSignatureException e) {
                XAdESHelper.logger.error("", e);
            }
        }
        
        return aTSsValid;
    }
    
    private boolean validateSigTS(UnsignedSignatureProperties usp, DOMValidateContext valContext) {
        boolean sigTSsValid = true;
        List sigTSs = usp.getSignatureTimeStamps();
        
        for(Iterator iter = sigTSs.iterator(); iter.hasNext(); ) {
            SignatureTimeStamp sigTS = (SignatureTimeStamp) iter.next();
            // validate time-stamp
            
            boolean sigTsValid = false;
            try {
                sigTsValid = sigTS.validate(valContext);
                
                if(sigTsValid) {
                    TimeStampToken tsToken = sigTS.getTimeStampToken();
                    
                    XAdESHelper.logger.info("SignatureTimeStamp validation date = "
                            + tsToken.getTime());
                } else {
                    XAdESHelper.logger.warn("SignatureTimeStamp is invalid!");
                    //                return;
                }
                
                sigTSsValid = sigTSsValid && sigTsValid;
            } catch(XMLSignatureException e) {
                XAdESHelper.logger.error("", e);
            }
        }
        
        return sigTSsValid;
    }
    
    
    private DocumentBuilder initDocumentBuiler() throws ParserConfigurationException {
    
    
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        XMLUtil.secureFactory(dbf);
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setExpandEntityReferences(false);
        
        if(this.validateSchema) {
            File schemaFile = new File(this.schema);
            dbf.setValidating(true);
            dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", schemaFile);
            dbf.setAttribute("http://apache.org/xml/features/validation/schema/normalized-value", Boolean.FALSE);
            dbf.setAttribute("http://apache.org/xml/features/disallow-doctype-decl", Boolean.FALSE);
        }
        
        return dbf.newDocumentBuilder();
    }
    
    private XMLSignatureFactory initSigFactory() {
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
        return XMLSignatureFactory.getInstance("DOM", provider);
    }
}
