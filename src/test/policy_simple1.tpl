accept(Form) :-
  extract(Form, format, simpleContract),
  extract(Form, contract, Contract),
  print(Contract).
