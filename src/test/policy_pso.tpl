accept(Form) :-
  extract(Form, format, pumpkinSeedOil),

  extract(Form, item_id, ItemID),
   print(ItemID),
  extract(Form, amount, Amount),
   print(Amount),

  authorize_order(Form),

  extract(Form, certificate, Certificate),
  extract(Certificate, format, eIDAS_qualified_certificate),
  extract(Certificate, pubKey, PK),
  verify_signature(Form, PK),
  check_eIDAS_qualified(Certificate).

authorize_order(Form) :-
  extract(Form, item_id, 54678),
  extract(Form, amount, Amount),
   Amount <= 10.

authorize_order(Form) :-
  extract(Form, item_id, 42),
  extract(Form, amount, Amount),
   Amount <= 10.

authorize_order(Form) :-
  print(authorizing_order_failed),
  false().


check_eIDAS_qualified(Certificate) :-
  extract(Certificate, issuer, IssuerCertificate),
  extract(IssuerCertificate, trustScheme, TrustSchemeClaim),

  trustscheme(TrustSchemeClaim, eidas_qualified),
  trustlist(TrustSchemeClaim, IssuerCertificate, TrustListEntry),
  extract(TrustListEntry, format, trustlist_entry),

  extract(TrustListEntry, pubKey, PkIss),
  verify_signature(Certificate, PkIss).

