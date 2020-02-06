accept(Form) :-
	extract(Form, format, theAuctionHouse2019),

	extract(Form, certificate, Certificate),
	extract(Certificate, format, eIDAS_qualified_certificate),

	extract(Certificate, pubKey, PK),
	verify_signature(Form, PK),

	extract(Certificate, issuer, IssuerCertificate),

	trustschemeX(IssuerCertificate, eidas_qualified, TrustedTrustListEntry),
	extract(TrustedTrustListEntry, format, trustlist_entry),

	extract(TrustedTrustListEntry, serviceType, qualified_certificate_authority),
	extract(TrustedTrustListEntry, serviceAdditionalServiceInfo, for_esignatures),

	extract(TrustedTrustListEntry, pubKey, PkIss),
	verify_signature(Certificate, PkIss).


trustschemeX(IssuerCert, TrustedScheme, TrustListEntry) :-
	extract(IssuerCert, trustScheme, Claim),
	trustlist(Claim, IssuerCert, TrustListEntry),
	trustscheme(Claim, TrustedScheme).

trustschemeX(IssuerCert, TrustedScheme, TrustedTrustListEntry) :-
	extract(IssuerCert, trustScheme, Claim),
	trustlist(Claim, IssuerCert, TrustListEntry),
	encode_translation_domain(Claim, TrustedScheme, TTAdomain),
	lookup(TTAdomain, TranslationEntry),

	% TranslationEntry = translation table in e.g. XML
	translate(TranslationEntry, TrustListEntry, TrustedTrustListEntry).
