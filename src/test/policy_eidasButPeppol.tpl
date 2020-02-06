accept(Form) :-
  extract(Form, format, theAuctionHouse2019),
  extract(Form, bid, Bid),  Bid <= 1500,
  extract(Form, certificate, Certificate),
  extract(Certificate, format, eIDAS_qualified_certificate),
  extract(Certificate, pubKey, PK),
  verify_signature(Form, PK),
  check_Peppol_qualified(Certificate).

check_Peppol_qualified(Certificate) :-
  extract(Certificate, issuer, IssuerCertificate),
  extract(IssuerCertificate, trustScheme, TrustSchemeClaim),

  trustscheme(TrustSchemeClaim, peppol_sp_new_qualified),
  trustlist(TrustSchemeClaim, IssuerCertificate, TrustListEntry),
  extract(TrustListEntry, format, trustlist_entry),

  extract(TrustListEntry, pubKey, PkIss),
  verify_signature(Certificate, PkIss).

