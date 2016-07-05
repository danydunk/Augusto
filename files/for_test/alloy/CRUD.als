open util/ordering [Time] as T

---------------- Signatures ----------------
abstract sig Field { }
sig Required extends Field { }
sig Not_required extends Field { }
sig Field1 extends Required { } //TO ADAPT
sig Field2 extends Not_required { } //TO ADAPT
sig Unique in Field { }
sig Object { //TO ADAPT
	field1: lone Field1,
	field2: lone Field2,
}
one sig List {
	elements: Object set -> Time
}
//for the transition system
sig Time { }
abstract sig Operation {}
one sig Add extends Operation {}
one sig Track { 
	op: Operation lone -> Time,
	obj: Object lone -> Time
}
---------------- Declaration of the Unique Fields ----------------
fact unique_field { //TO ADAPT
	all f: Field1 | f in Unique
}
---------------- Constraints to clean the model ----------------
fact no_unconnected_fields {
	no f: Field | not f in (Object.field1+Object.field2)
}

fact no_objects_out_track {
	no o: Object, t: Track | #o.(t.obj) = 0
}
----------------Functions ----------------
fun unique_fields[o: Object] : Field {
  (o.field1+o.field2) & Unique
}
----------------Frame Condition Predicates ----------------
pred noExisitingObjectChange[l: List, t, t': Time] {
	all o: l.elements.t | o in l.elements.t'
}

----------------Operations ----------------
pred add[l: List, o: Object, t, t': Time] {
	-- Pre-condition
	all oo: l.elements.t | o != oo
	//if no duplicate object
	required_fields_not_empty [o] and unique_values_costraint [l, o ,t] => l.elements.t' = l.elements.t + o else  l.elements.t' = l.elements.t

	//required fields not empty
	//required_fields_not_empty [o]
	//unique values costraint
	//unique_values_costraint [l, o ,t]
	-- Post-condition
	//l.elements.t' = l.elements.t + o
	-- Frame condition
	noExisitingObjectChange[l, t, t']

	Track.op.t' = Add
	Track.obj.t' = o
}
----------------Initial State ----------------
pred init [t: Time] {
	no List.elements.t
	no Track.op.t
	no Track.obj.t
}
----------------Transition ----------------
pred trans [t, t': Time]  {
  (some o: Object, l: List | add[l, o, t, t'])
}
pred System {
	init [T/first]
	all t: Time - T/last | trans [t, T/next[t]]
}
----------------Predicates----------------
pred no_duplicate_object [l: List, o: Object, t: Time] {
	all o1: l.(elements.t)| o1 != o 
}
pred required_fields_not_empty [o: Object] {
	#o.field1 = 1
}
pred unique_values_costraint [l: List, o: Object, t: Time] {
	all o1: l.(elements.t), f1: unique_fields[o], f2: unique_fields[o1] | #(f1&f2) = 0
}
----------------Assertion ----------------
assert required_values_in_list { 
	System => all t: Time, o: List.(elements.t) | #o.field1 = 1 
}
assert unique_values_in_list { 
	System => all t: Time, o1, o2: List.(elements.t), f1: unique_fields[o1], f2: unique_fields[o2] | o1 != o2 => #(f1&f2) = 0
}
----------------Test  Cases----------------
pred obj_struct1 {
	some o: Object | #o.field1 = 1 and #o.field2 = 0
}
pred obj_struct2 {
	some o: Object | #o.field1 = 1 and #o.field2 = 1
}
pred obj_struct3 {
	some o: Object | #o.field1 = 0 and #o.field2 = 0
}
pred obj_struct4 {
	some o: Object | #o.field1 = 0 and #o.field2 = 1
}

pred add_pre_1 [t: Time, l: List, o: Object] {
	#o.field1 = 1
}

pred add_pre_2 [t: Time, l: List, o: Object]{
	all ol: List.elements.t, f1: unique_fields[o], f2: unique_fields[ol] | #(f1&f2) = 0 
}

pred comb1 {
	some t: Time, o: Object, l: List | add_pre_1 [t, l, o] and  add_pre_2 [t, l, o] and add[l, o, t, T/next[t]]
}

pred comb2 {
	some t: Time, o: Object, l: List | not (add_pre_1 [t, l, o] ) and  add_pre_2 [t, l, o] and add[l, o, t, T/next[t]]
}

pred comb3 {
	some t: Time, o: Object, l: List | not (add_pre_1 [t, l, o] ) and  not(add_pre_2 [t, l, o]) and add[l, o, t, T/next[t]]
}
pred comb4 {
	some t: Time, o: Object, l: List | add_pre_1 [t, l, o] and  not(add_pre_2 [t, l, o]) and add[l, o, t, T/next[t]]
}
run {}
