open util/ordering [Time] as T
open util/ternary
open util/relation
sig Time { }
abstract sig Operation { }
sig Click extends Operation {
	clicked: Action_widget,
}
sig Fill extends Operation {
	filled: Input_widget,
	with: Value,
}
sig Go extends Operation {
	where: Window,
}
one sig Track {
	op: Operation lone -> Time,
}
abstract sig Window {
	aws: set Action_widget,
	iws: set Input_widget,
}
one sig General extends Window { }
abstract sig Action_widget {
	goes: lone Window,
}
sig Value { }
abstract sig Input_widget {
	content: Value lone -> Time,
}
one sig Current_window {
	is_in: Window one -> Time,
}
abstract sig Ok extends Action_widget { }
abstract sig Cancel extends Action_widget { }
abstract sig Trigger extends Action_widget { }
abstract sig Form extends Window { }
abstract sig Initial extends Window { }
abstract sig Confirm extends Window { }
sig Property {
	associated_to: Input_widget,
	has_value: Value lone -> Object,
}
sig Unique in Property { }
sig Required in Property { }
sig Object { }
one sig List {
	contains: Object -> Time,
}
one sig Window_w1 extends Initial { }
one sig Window_w2 extends Form { }
one sig Action_widget_aw1 extends Trigger { }
one sig Input_widget_iw1 extends Input_widget { }
one sig Input_widget_iw2 extends Input_widget { }
one sig Action_widget_aw3 extends Ok { }
one sig Action_widget_aw4 extends Cancel { }
fact fact1{
	no t: Time| (not t = T/first) and (not t in ran[Track.op])
	no o: Operation | not o in dom [Track.op]
	all t: Time | not t = T/first => #Current_window.is_in.t = 1
	all t: Time, g: Go | Track.op.t = g => (Current_window.is_in.(T/prev[t]) = General and (not g.where = General))

}
fact fact2{
	all gw: General |  #gw.iws = 0 and #gw.aws = 0
	all iw: Input_widget | one w: Window | iw in w.iws
	all aw: Action_widget | one w: Window | aw in w.aws

}
fact fact3{
	all iw: Initial |  #iw.aws > 0 and iw.aws in Trigger and #iw.iws = 0
	all fw: Form | one ok: Ok,  cancel: Cancel | #fw.iws > 0 and fw.aws = ok+cancel
	all cw: Confirm | one ok: Ok,  cancel: Cancel | #cw.iws = 0 and cw.aws = ok+cancel
	all aw: Confirm.aws | one fw: Form | aw in Cancel => aw.goes = fw
	#Window = #Form + #Initial + #Confirm + #General
	all aw: Ok+Cancel+Trigger | not (#aw.goes  > 0 and aw.goes in aws.aw)

}
fact fact4{
	#Property = #Input_widget

}
fact Window_w1_aws{
	Window_w1.aws = Action_widget_aw1
	Action_widget_aw1.goes = Window_w2

}
fact Window_w2_iws{
	Window_w2.iws = Input_widget_iw1 + Input_widget_iw2

}
fact Window_w2_aws{
	Window_w2.aws = Action_widget_aw3 + Action_widget_aw4
	Action_widget_aw4.goes = Window_w1

}
pred transition [t, t': Time] {
	(one aw: Action_widget, c: Click | click [aw, t, t', c]) or
	(one iw: Input_widget, f: Fill, v: Value | fill [iw, t, t', v, f]) or
	(one w: Window, g: Go | go [w, t, t', g])
}
pred System [] {
	///	init [T/first]
	all t: Time - T/last | transition [t, T/next[t]]
}
pred click [aw: Action_widget, t, t': Time, c: Click] {
	--- precondition ---
	aw in Current_window.is_in.t.aws
	--- effect ---
	(click_semantics  [aw, t] and #aw.goes = 1 and Current_window.is_in.t' = aw.goes and click_success_post [aw, t, t']) or
	(click_semantics  [aw, t] and not #aw.goes = 1 and Current_window.is_in.t' = General and click_success_post [aw, t, t']) or
	(not click_semantics  [aw, t] and Current_window.is_in.t' = Current_window.is_in.t and click_fail_post [aw, t, t'])
	--- operation is tracked ---
	c.clicked = aw and Track.op.t' = c
}
pred fill [iw: Input_widget, t, t': Time, v: Value, f: Fill] {
	--- precondition ---
	iw in Current_window.is_in.t.iws
	--- effect ---
	(fill_semantics  [iw, t, v] and iw.content.t' = v and fill_success_post [iw, t, t', v]) or
	(not fill_semantics  [iw, t, v] and iw.content.t' = iw.content.t and fill_fail_post [iw, t, t', v])
	--- general postcondition ---
	Current_window.is_in.t' = Current_window.is_in.t
	(Input_widget - iw).content.t' = (Input_widget - iw).content.t
	--- operation is tracked ---
	f.filled = iw and f.with = v and Track.op.t' = f
}
pred go [w: Window, t, t': Time, g: Go] {
	--- precondition ---
	General in Current_window.is_in.t
	--- effect ---
	(go_semantics [w, t] and Current_window.is_in.t' = w and 	#Input_widget.content.t' = 0 and go_success_post [w, t, t']) or
	(not go_semantics [w, t] and Current_window.is_in.t' = General and Input_widget.content.t' =  Input_widget.content.t and go_fail_post [w, t, t'])
	--- operation is tracked ---
	g.where = w and Track.op.t' = g
}
pred init [t: Time] {
	no List.contains.t
	no Track.op.t
	no Input_widget.content.t
	Current_window.is_in.t = General
}
pred go_semantics [w: Window, t: Time] {
	
}
pred go_success_post [w: Window, t, t': Time] {
	List.contains.t' = List.contains.t
}
pred go_fail_post [w: Window, t, t': Time] {
	List.contains.t' = List.contains.t
}
pred fill_semantics [iw: Input_widget, t: Time, v: Value] {
	
}
pred fill_success_post [iw: Input_widget, t, t': Time, v: Value] {
	List.contains.t' = List.contains.t
}
pred fill_fail_post [iw: Input_widget, t, t': Time, v: Value] {
	List.contains.t' = List.contains.t
}
pred click_semantics [aw: Action_widget, t: Time] {
	(aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form)) => filled_required_in_w_test [Current_window.is_in.t, t]
	(aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0) => (filled_required_test [t] and unique_test [t])
	(aw in Ok and Current_window.is_in.t  in Confirm) => (filled_required_test [t] and unique_test [t])
}
pred click_success_post [aw: Action_widget, t, t': Time] {
	aw in Cancel => (#Current_window.is_in.t.iws.content.t' = 0 and List.contains.t' = List.contains.t and (Input_widget - Current_window.is_in.t.iws).content.t' = (Input_widget - Current_window.is_in.t.iws).content.t)
	aw in Trigger => (List.contains.t' = List.contains.t and Input_widget.content.t' = Input_widget.content.t)
	(aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form)) => (List.contains.t' = List.contains.t and Input_widget.content.t' = Input_widget.content.t)
	(aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0) => (#Input_widget.content.t' = 0 and add [t, t'])
	(aw in Ok and Current_window.is_in.t  in Confirm) => (#Input_widget.content.t' = 0 and add [t, t'])
}
pred click_fail_post [aw: Action_widget, t, t': Time] {
	List.contains.t' = List.contains.t
	Input_widget.content.t' = Input_widget.content.t
}
pred add [t, t': Time] {
	one o: Object | all p: Property | p.has_value.o = p.associated_to.content.t and List.contains.t' = List.contains.t+o
}
pred filled_required_in_w_test [w: Form, t: Time] {
	all iw: w.iws | (iw in Required.associated_to) => #iw.content.t = 1
}
pred filled_required_test [t: Time] {
	all iw: Input_widget | (iw in Required.associated_to) => #iw.content.t = 1
}
pred unique_test [t: Time] {
	all p: Unique | all o2: List.contains.t | (#p.has_value.o2 = 1) => p.associated_to.content.t != p.has_value.o2
}

run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form))) and (not (filled_required_in_w_test [Current_window.is_in.t, t]))} } for 10
run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form))) and (filled_required_in_w_test [Current_window.is_in.t, t])} } for 10
run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0)) and (not (unique_test [t]) and not (filled_required_test [t]))} } for 10
run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0)) and (unique_test [t] and not (filled_required_test [t]))} } for 10
run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0)) and (not (unique_test [t]) and filled_required_test [t])} } for 10
run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0)) and (unique_test [t] and filled_required_test [t])} } for 10
run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Confirm)) and (not (unique_test [t]) and not (filled_required_test [t]))} } for 10
run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Confirm)) and (unique_test [t] and not (filled_required_test [t]))} } for 10
run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Confirm)) and (not (unique_test [t]) and filled_required_test [t])} } for 10
run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Confirm)) and (unique_test [t] and filled_required_test [t])} } for 10
