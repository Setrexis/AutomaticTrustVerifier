package eu.lightest.verifier.wrapper;

import eu.lightest.verifier.exceptions.DNSException;
import org.xbill.DNS.Message;
import org.xbill.DNS.Type;
import org.xbill.DNS.Record;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface NameResolverHelper {
    public static final int RECORD_A = Type.A;
    public static final int RECORD_CNAME = Type.CNAME;
    public static final int RECORD_URI = Type.URI;
    public static final int RECORD_PTR = Type.PTR;
    public static final int RECORD_TXT = Type.TXT;
    public static final int RECORD_TLSA = Type.TLSA;
    public static final int RECORD_SMIMEA = Type.SMIMEA;

    public List<String> queryTXT(String host) throws IOException, DNSException;

    public List<String> queryPTR(String host) throws IOException, DNSException;

    public List<String> queryURI(String host) throws IOException, DNSException;

    public List<SMIMEAcert> querySMIMEA(String host) throws IOException, DNSException;

    public <R extends Record> List<R> queryAndParse(String host, Class recordTypeClass, int recordTypeID) throws IOException, DNSException;
}
