accept(Form) :-
    extract(Form, format, simpleFido),
    extract(Form, authority, Authority),

    lookup(Authority, Mapping),
    extract(Mapping, format, simpleFidoMapping),

    lookup(Mapping, LoA).
