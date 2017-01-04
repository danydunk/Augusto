open util/ordering [Time] as T
open util/ordering [Input_widget] as IW
open util/ternary
open util/relation
-----------------------Utils------------------------

one sig Option_value_0 extends Value{ }
one sig Option_value_1 extends Value{ }
one sig Option_value_2 extends Value{ }
one sig Option_value_3 extends Value{ }
one sig Option_value_4 extends Value{ }
one sig Option_value_5 extends Value{ }
one sig Option_value_6 extends Value{ }
one sig Option_value_7 extends Value{ }
one sig Option_value_8 extends Value{ }
one sig Option_value_9 extends Value{ }

sig Time { }
abstract sig Operation { }
sig Click extends Operation {
	clicked: one Action_widget
}
sig Object {
	appeared: one Time
}
sig Fill extends Operation {
	filled: one Input_widget,
	with: one Value
}
one sig Track { 
	op: Operation lone -> Time
}
pred transition [t, t': Time]  {
	(one aw: Action_widget, c: Click | click [aw, t, t', c]) or 
	(one iw: Input_widget, f: Fill, v: Value | fill [iw, t, t', v, f]) 
}
pred System {
///	init [T/first]
	all t: Time - T/last | transition [t, T/next[t]]
}
fact{
	no t: Time| (not t = T/first) and (not t in ran[Track.op])
	no o: Operation | not o in dom [Track.op]
	all t: Time | not t = T/first => #Current_window.is_in.t = 1
}
----------------Generic GUI Structure ----------------
abstract sig Window {
	aws: set Action_widget,
	iws: set Input_widget,
	sws: set Selectable_widget
}
abstract sig Action_widget {
	goes: lone Window
}
sig Value { }
sig Invalid in Value{}
abstract sig Input_widget {
	content: Value lone -> Time
}
abstract sig Selectable_widget {}

fact {
	all iw: Input_widget | one w: Window | iw in w.iws
	all aw: Action_widget | one w: Window | aw in w.aws
}
----------------Generic GUI Semantics ---------------
one sig Current_window {
	is_in: Window one -> Time
}
pred click [aw: Action_widget, t, t': Time, c: Click] {
	--- precondition ---
	aw in Current_window.is_in.t.aws
	--- effect ---
	(click_semantics  [aw, t] and #aw.goes = 1 and Current_window.is_in.t' = aw.goes and click_success_post [aw, t, t']) or
	(click_semantics  [aw, t] and not #aw.goes = 1  and click_success_post [aw, t, t']) or
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

pred select  [iw: Input_widget, t, t': Time, v: Value, f: Fill] {}


run {}
--------------------Initial State---------------
pred init [t: Time] {
	no List.contains.t
	no Track.op.t
	no Input_widget.content.t
 	Current_window.is_in.t = Initial
}
---------------Generic ADD Structure ----------
abstract sig Ok, Cancel extends Action_widget { }
abstract sig Trigger extends Action_widget { }

abstract sig Form extends Window { }
abstract sig Initial extends Window { }
abstract sig Confirm extends Window { }

fact {
	all iw: Initial |  #iw.aws > 0 and iw.aws in Trigger and #iw.iws = 0
	all fw: Form | one ok: Ok,  cancel: Cancel | #fw.iws > 0 and fw.aws = ok+cancel
	all cw: Confirm | one ok: Ok,  cancel: Cancel | #cw.iws = 0 and cw.aws = ok+cancel
	all aw: Confirm.aws | one fw: Form | aw in Cancel => aw.goes = fw
	#Window = #Form + #Initial + #Confirm 
	all aw: Ok+Cancel+Trigger | not (#aw.goes  > 0 and aw.goes in aws.aw)
}

---------------Generic ADD Semantics----------
one sig Property_unique{
	uniques: set Input_widget
} 
one sig Property_required{
	requireds: set Input_widget
}
sig Object_inlist extends Object{
	vs: Value lone ->Input_widget
}
one sig List { 
	contains: Object_inlist set -> Time
}
fact {
	all o: Object_inlist| #o.vs <= #Form.iws
	//all t: Time | List.contains.t = Selectable_widget.list.t
	//all t: Time | (Current_window.is_in.t in Initial) <=> #Current_crud_op.operation.t = 0
}
pred go_semantics [w: Window, t: Time] { }
pred go_success_post [w: Window, t, t': Time] {
	List.contains.t' = List.contains.t
}
pred go_fail_post [w: Window, t, t': Time] {
	List.contains.t' = List.contains.t
}

pred fill_semantics [iw: Input_widget, t: Time, v: Value] { }
pred select_semantics [iw: Input_widget, t: Time, v: Value] { }

pred fill_success_post [iw: Input_widget, t, t': Time, v: Value] { 
	List.contains.t' = List.contains.t
}
pred fill_fail_post [iw: Input_widget, t, t': Time, v: Value] { 
	List.contains.t' = List.contains.t
}

pred click_semantics [aw: Action_widget, t: Time] {
	(aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form)) => filled_required_test [Current_window.is_in.t, t] 
	(aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0) => (filled_required_test [Current_window.is_in.t,t] and unique_test [Current_window.is_in.t,t])
	(aw in Ok and Current_window.is_in.t  in Confirm) => (filled_required_test [Current_window.is_in.t,t] and unique_test [Current_window.is_in.t,t])
}
pred click_success_post [aw: Action_widget, t, t': Time] {
	aw in Cancel => (#Current_window.is_in.t.iws.content.t' = 0 and List.contains.t' = List.contains.t and (Input_widget - Current_window.is_in.t.iws).content.t' = (Input_widget - Current_window.is_in.t.iws).content.t)
	aw in Trigger => (List.contains.t' = List.contains.t and Input_widget.content.t' = Input_widget.content.t)
	(aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form)) => (List.contains.t' = List.contains.t and Input_widget.content.t' = Input_widget.content.t)
	(aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0) => (#Input_widget.content.t' = 0 and add [t, t'])
	(aw in Ok and Current_window.is_in.t  in Confirm) => (#Input_widget.content.t' = 0 and add [t, t'])
}
pred click_fail_post [aw: Action_widget, t, t': Time]	{
	List.contains.t' = List.contains.t
	Input_widget.content.t' = Input_widget.content.t
}
pred add [t, t': Time] {
	one o: Object_inlist |all iw: Form.iws | not(o in List.contains.t) and o.appeared = t' and o.vs.iw = iw.content.t and List.contains.t' = List.contains.t+o
}
pred filled_required_test [w: Form, t: Time] { 
	all iw: w.iws | (iw in Property_required.requireds) => #iw.content.t = 1
}
pred  unique_test [w: Form, t: Time] { 
	all iw: w.iws | all o: List.contains.t | (iw in Property_unique.uniques and (#o.vs.iw= 1)) => iw.content.t !=o.vs.iw //and ((#p.has_value.o2 = 0) => #p.associated_to.content.t = 1)
}