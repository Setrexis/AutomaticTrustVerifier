accept(Form) :-
  extract(Form, format, pumpkinSeedOil),

  extract(Form, item_id, ItemID),
   print(ItemID),
  extract(Form, amount, Amount),
   print(Amount),

  authorize_order(Form).


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

