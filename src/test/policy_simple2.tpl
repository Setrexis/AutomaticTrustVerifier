accept(Form) :-
  extract(Form, format, nonexistingFormat),
  extract(Form, contract, Contract),
  print(Contract).
