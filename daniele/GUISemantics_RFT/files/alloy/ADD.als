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
sig Field {
	associated_to: one Input_widget,
	has_value: Value lone -> Object
}
sig Property_unique, Property_required in Field { }
one sig List { 
	contains: Object set -> Time
}
fact {
	#Field = #Input_widget
	all f: Field |  #f.associated_to = 1
	all iw: Input_widget | #associated_to.iw = 1
}

pred go_semantics [w: Window, t: Time] { }
pred go_success_post [w: Window, t, t': Time] {
	List.contains.t' = List.contains.t
}
pred go_fail_post [w: Window, t, t': Time] {
	List.contains.t' = List.contains.t
}
pred go_pre[w: Window, t: Time] {}

pred fill_semantics [iw: Input_widget, t: Time, v: Value] { }
pred fill_success_post [iw: Input_widget, t, t': Time, v: Value] { 
	List.contains.t' = List.contains.t
}
pred fill_fail_post [iw: Input_widget, t, t': Time, v: Value] { 
	List.contains.t' = List.contains.t
}
pred fill_pre[iw: Input_widget, t: Time, v: Value] { }

pred click_semantics [aw: Action_widget, t: Time] {
	(aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form)) => filled_required_in_w_test [Current_window.is_in.t, t] 
	(aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0) => (filled_required_test [t] and unique_test [t])
	(aw in Ok and Current_window.is_in.t  in Confirm) => (filled_required_test [t] and unique_test [t])
}
pred click_success_post [aw: Action_widget, t, t': Time] {
	aw in Cancel => (#Current_window.is_in.t.iws.content.t' = 0 and List.contains.t' = List.contains.t and (Input_widget - Current_window.is_in.t.iws).content.t' = (Input_widget - Current_window.is_in.t.iws).content.t) 
	aw in Trigger => (List.contains.t' = List.contains.t and (all iww: Input_widget | iww.content.t' = iww.content.t))
	(aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form)) => (List.contains.t' = List.contains.t and (all iww: Input_widget | iww.content.t' = iww.content.t))
	(aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0) => (#Input_widget.content.t' = 0 and add [t, t'])
	(aw in Ok and Current_window.is_in.t  in Confirm) => (#Input_widget.content.t' = 0 and add [t, t'])
}
pred click_fail_post [aw: Action_widget, t, t': Time]	{
	List.contains.t' = List.contains.t
	all iw:Input_widget | iw.content.t' = iw.content.t
}
pred click_pre[aw: Action_widget, t: Time] {}

pred select_semantics [sw: Selectable_widget, t: Time, o: Object] { }
pred select_success_post [sw: Selectable_widget, t, t': Time, o: Object] { }
pred select_fail_post [sw: Selectable_widget, t, t': Time, o: Object] { }
pred select_pre[sw: Selectable_widget, t: Time, o: Object] { }

pred add [t, t': Time] {
	one o: Object | all f: Field | not (o in List.contains.t) and f.has_value.o = f.associated_to.content.t and List.contains.t' = List.contains.t+o
}
pred filled_required_in_w_test [w: Form, t: Time] { 
	all iw: w.iws | (iw in Property_required.associated_to) => #iw.content.t = 1
}
pred  filled_required_test [t: Time] { 
	all iw: Input_widget | (iw in Property_required.associated_to) => #iw.content.t = 1
}
pred  unique_test [t: Time] { 
	all p: Property_unique | all o2: List.contains.t | (#p.has_value.o2 = 1) => p.associated_to.content.t != p.has_value.o2
}
fact{
#Select = 0
all t:Time|Selectable_widget.list.t = List.contains.t 
all t:Time|(#List.contains.t = 1 => Selectable_widget.selected.t = List.contains.t) and (#List.contains.t = 0 => #Selectable_widget.selected.t = 0)
}