package eu.lightest.verifier.model.format.Delegation;

import com.google.gson.Gson;
import eu.lightest.delegation.api.DelegationApi;
import eu.lightest.delegation.api.model.xsd.DelegationType;
import eu.lightest.horn.specialKeywords.HornApiException;
import eu.lightest.verifier.model.delegation.Delegation;
import eu.lightest.verifier.model.format.AbstractFormatParser;
import eu.lightest.verifier.model.format.Delegation.DPFormat;
import eu.lightest.verifier.model.format.Delegation.JsonRevokedDelegationResponse;
import eu.lightest.verifier.model.format.FormatParser;
import eu.lightest.verifier.model.format.eIDAS_qualified_certificate.EidasCertFormat;
import eu.lightest.verifier.model.format.eIDAS_qualified_certificate.TslEntryFormat;
import eu.lightest.verifier.model.report.Report;
import eu.lightest.verifier.model.report.ReportStatus;
import eu.lightest.verifier.model.tpl.TplApiListener;
import eu.lightest.verifier.model.trustscheme.TrustScheme;
import eu.lightest.verifier.model.trustscheme.TslEntry;
import eu.lightest.verifier.model.trustscheme.X509Helper;
import iaik.x509.extensions.AuthorityKeyIdentifier;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base32;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

public class DelegationXMLFormat extends AbstractFormatParser {
    
    public static final String RESOLVETYPE_DELEGATION = "delegation";
    private static final String FILEPATH_FORM = "delegation.xml"; // Path to file inside container
    private static final String FORMAT_ID = "delegationxml";
    private static final Logger mLog = Logger.getLogger(DelegationXMLFormat.class);
    private Delegation mDelegation = null;
    private DelegationType mDelegationType = null;
    private String mDelegationXml = null;
    private EidasCertFormat mEidasParser = null;
    private TslEntryFormat mTslParser = null;
    private OkHttpClient mClient = new OkHttpClient();

    public DelegationXMLFormat(Object transaction, Report report) {
        super(transaction, report);

        addParser("proxyCert", this);
        addParser("mandatorCert", this);
        addParser("dp_format", new DPFormat(transaction, report));
        if(transaction instanceof String) {
            this.mDelegationXml = (String) transaction;
        } else {
            DelegationXMLFormat.mLog.error("Transaction of type " + transaction.getClass().toString() + ": Expected File");
            throw new IllegalArgumentException("Transaction of type " + transaction.getClass().toString() + ": Expected File");
        }
        
    }
    
    @Override
    public String getFormatId() {
        return DelegationXMLFormat.FORMAT_ID;
    }
    
    @Override
    public void init() throws Exception {
        if(this.mDelegationXml == null) {
            throw new Exception("Error while parsing form. Wrong format? (" + DelegationXMLFormat.FILEPATH_FORM + " not found.)");
        }
        
        JAXBContext jc;
        jc = JAXBContext.newInstance(DelegationType.class);
        Unmarshaller u = jc.createUnmarshaller();
        JAXBElement<DelegationType> resultObject = (JAXBElement) u.unmarshal(new StringReader(this.mDelegationXml));
        this.mDelegationType = resultObject.getValue();

        CertificateFactory cf = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(this.mDelegationType.getProxy());
        cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificate(bis);
        this.mEidasParser = new EidasCertFormat((X509Certificate) cert, this.report);
        addParser("trustScheme", this.mEidasParser);

        AuthorityKeyIdentifier aki = X509Helper.genAuthorityKeyIdentifier((X509Certificate) cert);

        List<String> claimedClaims = this.mEidasParser.getClaims();
        String claimedClaim = claimedClaims.get(0);

        TrustScheme newTrustScheme = this.mEidasParser.createTrustScheme(claimedClaim);
        TslEntry tslEntry = newTrustScheme.getTSLEntry(aki);
        this.mTslParser = new TslEntryFormat(tslEntry, this.report);
        addParser("TrustListEntry", this.mTslParser);

        /*
        if(!mDelegation.().equals(AH19Format.FORMAT_ID)) {
            throw new Exception("Error while parsing form. Wrong format? (" + AH19Format.FILEPATH_FORM + " not found.)");
        }
         */
        
    }
    
    @Override
    public boolean onExtract(List<String> path, String query, List<String> output) throws HornApiException {
        if(path.size() == 0) {
            switch(query) {
                case "format":
                case "notAfterDate":
                case "notBeforeDate":
                case "version":
                case "sequence":
                case "issuedDate":
                case "proxy":
                case "proxyCert":
                case "proxyKey":
                case "issuer":
                case "mandatorCert":
                case "mandatorKey":
                case "intermediary":
                case "substututionAllowed":
                case "delegationAllowed":
                case "trustScheme":
                case "delegationProvider":
                    return true;
            }
            return false;
        }
        
        String pId = path.get(0);
        DelegationXMLFormat.mLog.debug("Parser ID: " + pId);
        
        return getParser(pId).onExtract(pop(path), query, output);
    }
    
    @Override
    public ResolvedObj resolveObj(List<String> path) {
        
        if(path.size() > 1) {
            String parserId = path.get(0);
            FormatParser parser = getParser(parserId);
            
            return parser.resolveObj(pop(path));
        }
        
        switch(path.get(0)) {
            case "format":
                return genResolvedObj(getFormatId(), "STRING");
            case "notAfterDate":
                return genResolvedObj(this.mDelegationType.getValidity().getNotAfter(), "STRING");
            case "notBeforeDate":
                return genResolvedObj(this.mDelegationType.getValidity().getNotBefore(), "STRING");
            case "version":
                return genResolvedObj(this.mDelegationType.getInformation().getVersion(), "STRING");
            case "sequence":
                return genResolvedObj(this.mDelegationType.getInformation().getSequence(), "INT");
            case "issuedDate":
                return genResolvedObj(this.mDelegationType.getIssuedDate(), "STRING");
            case "proxy":
                return genResolvedObj(new String(this.mDelegationType.getProxy()), "STRING)");
            case "proxyCert":
            case "proxyKey":
                try {
                    CertificateFactory cf = null;
                    ByteArrayInputStream bis = new ByteArrayInputStream(this.mDelegationType.getProxy());
                    cf = CertificateFactory.getInstance("X.509");
                    Certificate cert = cf.generateCertificate(bis);
                    return genResolvedObj(cert, EidasCertFormat.RESOLVETYPE_X509CERT);
                } catch(CertificateException e) {
                    DelegationXMLFormat.mLog.error(e);
                    return null;
                }
            case "issuer":
                return genResolvedObj(new String(this.mDelegationType.getIssuer()), "STRING");
            case "mandatorCert":
            case "mandatorKey":
                try {
                    CertificateFactory cf = null;
                    ByteArrayInputStream bis = new ByteArrayInputStream(this.mDelegationType.getIssuer());
                    cf = CertificateFactory.getInstance("X.509");
                    Certificate cert = cf.generateCertificate(bis);
                    return genResolvedObj(cert, EidasCertFormat.RESOLVETYPE_X509CERT);
                } catch(CertificateException e) {
                    DelegationXMLFormat.mLog.error(e);
                    return null;
                }
            case "intermediary":
                return genResolvedObj(new String(this.mDelegationType.getIntermediary()), "STRING");
            case "substututionAllowed":
                return genResolvedObj(this.mDelegationType.isDelegationAllowed(), "BOOLEAN");
            case "delegationAllowed":
                return genResolvedObj(this.mDelegationType.isSubstitutionAllowed(), "BOOLEAN");
            case "trustScheme":
                try {
                    CertificateFactory cf = null;
                    ByteArrayInputStream bis = new ByteArrayInputStream(this.mDelegationType.getIssuer());
                    cf = CertificateFactory.getInstance("X.509");
                    Certificate cert = cf.generateCertificate(bis);
                    return genResolvedObj(((X509Certificate)cert).getIssuerAlternativeNames(), "STRING" );
                } catch (CertificateException e) {
                    mLog.error(e);
                    return null;
                }
            case "delegationProvider": {
                String hash;
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");

                    ByteArrayInputStream bis = new ByteArrayInputStream(this.mDelegationType.getProxy());
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate cert = cf.generateCertificate(bis);

                    Base32 base32 = new Base32();
                    hash = base32.encodeAsString(digest.digest(cert.getPublicKey().getEncoded()));
                    //return genResolvedObj(cert, EidasCertFormat.RESOLVETYPE_X509CERT );
                } catch (NoSuchAlgorithmException
                        | CertificateException e) {
                    mLog.error(e);
                    return null;
                }
                return genResolvedObj("https://dp.tug.do.nlnetlabs.nl/dp/api/v1/revoke/" + hash, "STRING");
            }
            case "fingerprint": {
                Gson gson = new Gson();
                JsonRevokedDelegationResponse data = gson.fromJson(path.get(0), JsonRevokedDelegationResponse.class);
                return genResolvedObj(data.getHash(), "STRING");
            }
        }

        return null;
    }
    
    @Override
    public boolean onPrint(PrintObj printObj) {

        ResolvedObj p = resolveObj(printObj.mPath);
        if(p != null) {
            this.report.addLine("" + p.mValue, ReportStatus.PRINT);
            return true;
        }
        
        return false;
    }
    
    
    @Override
    public boolean onVerifySignature(List<String> pathToSubject, List<String> pathToCert) throws HornApiException {
        DelegationXMLFormat.mLog.debug("HERE!");
        
        CertificateFactory certFactory = null;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(this.mDelegationType.getIssuer());
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);

            boolean result = DelegationApi.verifyDelegationXml(this.mDelegationXml, cert.getPublicKey(), false);
    
            return result;
    
        } catch(Exception e) {
            DelegationXMLFormat.mLog.error("", e);
        }
        return false;
    }

    @Override
    public boolean onLookup(List<String> pathToDomain, List<String> pathToLoadedDoc) throws HornApiException {
        this.mLog.debug("onLookup @" + this.getFormatId());
        printList("pathToDomain", pathToDomain);

        // not needed to use resolvObj here, since we used pathToDomain to find the parser
        ResolvedObj cachedResolvedObj = this.getCachedResolvedObj(pathToDomain, EidasCertFormat.RESOLVETYPE_HTTP_URL);
        if(cachedResolvedObj == null) {
            // if we did not cache it yet, we might need to load it directly
            cachedResolvedObj = this.resolveObj(pathToDomain);
            if(cachedResolvedObj == null) {
                return false;
            }
        }
        String discoveryPointer = (String) cachedResolvedObj.mValue;
        DelegationXMLFormat.mLog.info("Looking up " + discoveryPointer + " ...");

        // TODO: contact delegation provider at given address
        Request req = new Request.Builder()
                .url(discoveryPointer)
                .get()
                .build();

        Response response = null;
        try {
            response = this.mClient.newCall(req).execute();
            int statusCode = response.code();
            DelegationXMLFormat.mLog.debug("Status: " + statusCode);
            if(statusCode != 200) {
                DelegationXMLFormat.mLog.error("Error during DP query! " + statusCode);
                return false;
            }


            String responsedata = response.body().string();
            DelegationXMLFormat.mLog.debug("Response data: " + responsedata);
            if(responsedata.contains("REVOKED") == true) {
                DelegationXMLFormat.mLog.error("Delegation is already revoked!");
                return false;
            }
            else if ( responsedata.contains("ACTIVE") == false ) {
                mLog.error("Delegation does not exist!");
                return false;
            }


            pathToLoadedDoc.add("dp_format");
            pathToLoadedDoc.add(responsedata);

        } catch(IOException e) {
            DelegationXMLFormat.mLog.error(e);
            return false;
        }


        printList("pathToLoadedDoc", pathToLoadedDoc);
        return true;
    }

    @Override
    public boolean onVerifyHash(List<String> object, List<String> hash) throws HornApiException {
        DelegationXMLFormat.mLog.info("onVerifyHash @ " + this.getFormatId() + ":");
        printList("object", object);
        printList("hash", hash);

        hash.remove(0);
        hash.remove(0);
        hash.remove(0);
        String h;

        JsonRevokedDelegationResponse data = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            ByteArrayInputStream bis = new ByteArrayInputStream(this.mDelegationType.getProxy());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(bis);

            Base32 base32 = new Base32();
            h = base32.encodeAsString(digest.digest(cert.getPublicKey().getEncoded()));

            Gson gson = new Gson();
            data = gson.fromJson(hash.get(0), JsonRevokedDelegationResponse.class);
        } catch (NoSuchAlgorithmException
                |CertificateException e) {
            mLog.error(e);
            return false;
        }
        return h.contentEquals(data.getHash());
    }

    @Override
    protected void setRootListener(TplApiListener listener) {
        this.rootListener = listener;
        this.mEidasParser.setRootListener(listener);
    }
}
