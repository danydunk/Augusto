module orderingFIXED[elem]

private one sig Ord {
   First: set elem,
   Next: elem -> elem
} {
   pred/totalOrder[elem,First,Next]
}

fun first: one elem { Ord.First }


fun last: one elem { elem - (next.elem) }

fun prev : elem->elem { ~(Ord.Next) }

fun next : elem->elem { Ord.Next }

fun prevs [e: elem]: set elem { e.^(~(Ord.Next)) }

fun nexts [e: elem]: set elem { e.^(Ord.Next) }

pred lt [e1, e2: elem] { e1 in prevs[e2] }

pred gt [e1, e2: elem] { e1 in nexts[e2] }

pred lte [e1, e2: elem] { e1=e2 || lt [e1,e2] }

pred gte [e1, e2: elem] { e1=e2 || gt [e1,e2] }

fun larger [e1, e2: elem]: elem { lt[e1,e2] => e2 else e1 }

fun smaller [e1, e2: elem]: elem { lt[e1,e2] => e1 else e2 }

fun max [es: set elem]: lone elem { es - es.^(~(Ord.Next)) }

fun min [es: set elem]: lone elem { es - es.^(Ord.Next) }

assert correct {
  let mynext = Ord.Next |
  let myprev = ~mynext | {
     ( all b:elem | (lone b.next) && (lone b.prev) && (b !in b.^mynext) )
     ( (no first.prev) && (no last.next) )
     ( all b:elem | (b!=first && b!=last) => (one b.prev && one b.next) )
     ( !one elem => (one first && one last && first!=last && one first.next && one last.prev) )
     ( one elem => (first=elem && last=elem && no myprev && no mynext) )
     ( myprev=~mynext )
     ( elem = first.*mynext )
     (all disj a,b:elem | a in b.^mynext or a in b.^myprev)
     (no disj a,b:elem | a in b.^mynext and a in b.^myprev)
     (all disj a,b,c:elem | (b in a.^mynext and c in b.^mynext) =>(c in a.^mynext))
     (all disj a,b,c:elem | (b in a.^myprev and c in b.^myprev) =>(c in a.^myprev))
  }
}
run {} for exactly 0 elem expect 0
run {} for exactly 1 elem expect 1
run {} for exactly 2 elem expect 1
run {} for exactly 3 elem expect 1
run {} for exactly 4 elem expect 1
check correct for exactly 0 elem
check correct for exactly 1 elem
check correct for exactly 2 elem
check correct for exactly 3 elem
check correct for exactly 4 elem
check correct for exactly 5 elem
