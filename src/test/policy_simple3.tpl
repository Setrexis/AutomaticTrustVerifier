accept(Form) :-
  extract(Form, format, simpleContract),
  extract(Form, nonexistingfield, Contract),
  print(Contract).
