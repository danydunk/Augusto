--------------------Initial State---------------
pred init [t: Time] {
	no List.contains.t
	no Track.op.t
	no Input_widget.content.t
 	Current_window.is_in.t = General
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
	#Window = #Form + #Initial + #Confirm + #General
	all aw: Ok+Cancel+Trigger | not (#aw.goes  > 0 and aw.goes in aws.aw)
}
fact {
	all g: Go | g.where = Initial
}
---------------Generic ADD Semantics----------
sig Property {
	associated_to: one Input_widget,
	has_value: Value lone -> Object
}
sig Unique, Required in Property { }
sig Object { }
one sig List { 
	contains: Object set -> Time
}
fact {
	#Property = #Input_widget
}

pred go_semantics [w: Window, t: Time] { }
pred go_success_post [w: Window, t, t': Time] {
	List.contains.t' = List.contains.t
}
pred go_fail_post [w: Window, t, t': Time] {
	List.contains.t' = List.contains.t
}

pred fill_semantics [iw: Input_widget, t: Time, v: Value] { }
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
pred click_fail_post [aw: Action_widget, t, t': Time]	{
	List.contains.t' = List.contains.t
	Input_widget.content.t' = Input_widget.content.t
}
pred add [t, t': Time] {
	one o: Object | all p: Property | p.has_value.o = p.associated_to.content.t and List.contains.t' = List.contains.t+o
}
pred filled_required_in_w_test [w: Form, t: Time] { 
	all iw: w.iws | (iw in Required.associated_to) => #iw.content.t = 1
}
pred  filled_required_test [t: Time] { 
	all iw: Input_widget | (iw in Required.associated_to) => #iw.content.t = 1
}
pred  unique_test [t: Time] { 
	all p: Unique | all o2: List.contains.t | (#p.has_value.o2 = 1) => p.associated_to.content.t != p.has_value.o2
}
