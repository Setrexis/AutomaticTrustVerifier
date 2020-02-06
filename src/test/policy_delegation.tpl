accept(Form) :-
  extract(Form, format, theAuctionHouse2019),
  extract(Form, delegation, Delegation),
  extract(Delegation, format, delegationxml),
  extract(Delegation, notAfterDate, Date),
  extract(Delegation, notBeforeDate, Date2),
  extract(Delegation, issuer, Issuer),
  verify_signature(Delegation, Issuer),
  print(Date),
  print(Date2).


