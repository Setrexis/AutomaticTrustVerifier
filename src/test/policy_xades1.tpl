accept(Form) :-
  extract(Form, format, xades),

  extract(Form, certificate, Certificate),
  extract(Certificate, format, eIDAS_qualified_certificate),

  extract(Certificate, pubKey, PK),
  verify_signature(Form, PK),

  check_eIDAS_qualified(Certificate).

check_eIDAS_qualified(Certificate) :-
  extract(Certificate, issuer, IssuerCertificate),
  extract(IssuerCertificate, trustScheme, TrustSchemeClaim),

  trustscheme(TrustSchemeClaim, eidas_qualified),
  trustlist(TrustSchemeClaim, IssuerCertificate, TrustListEntry),
  extract(TrustListEntry, format, trustlist_entry),

  extract(TrustListEntry, pubKey, PkIss),
  verify_signature(Certificate, PkIss).

