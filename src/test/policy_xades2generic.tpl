accept(Form) :-
    extract(Form, format, xadesInAsic),

    % authenticate the container:
    extract(Form, certificate, Certificate),
    extract(Certificate, format, eIDAS_qualified_certificate),
    extract(Certificate, pubKey, PK),
    verify_signature(Form, PK),
    check_eIDAS_qualified(Certificate),

    extract(Form, xml, XML),
    extract(XML, format, xades),

    % authenticate the xml:
    extract(XML, certificate, XMLCertificate),
    extract(XMLCertificate, format, eIDAS_qualified_certificate),
    extract(XMLCertificate, pubKey, XMLPK),
    verify_signature(XML, XMLPK),
    check_eIDAS_qualified(XMLCertificate),

    handle_content(Form).

handle_content(Form) :-
  extract(Form, format, genericXML).


check_eIDAS_qualified(Certificate) :-
  extract(Certificate, issuer, IssuerCertificate),
  extract(IssuerCertificate, trustScheme, TrustSchemeClaim),

  trustscheme(TrustSchemeClaim, eidas_qualified),
  trustlist(TrustSchemeClaim, IssuerCertificate, TrustListEntry),
  extract(TrustListEntry, format, trustlist_entry),

  extract(TrustListEntry, pubKey, PkIss),
  verify_signature(Certificate, PkIss).

