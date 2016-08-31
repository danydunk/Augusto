--------------------Initial State---------------
pred init [t: Time] {
	no List.contains.t
	no Track.op.t
	no Input_widget.content.t
	no Selectable_widget.selected.t
 	Current_window.is_in.t = General
	no Current_crud_op.operation.t
}
---------------Generic ADD Structure ----------
abstract sig Ok, Cancel extends Action_widget { }
abstract sig Create_trigger extends Action_widget { }
abstract sig Read_trigger extends Action_widget { }
abstract sig Update_trigger extends Action_widget { }
abstract sig Delete_trigger extends Action_widget { }

abstract sig Form extends Window { }
abstract sig View extends Window { 
	mapping: Input_widget one -> Input_widget
}
abstract sig Initial extends Window { }
abstract sig Confirm extends Window { }
abstract sig Confirm_delete extends Window { }

fact {
	#Selectable_widget = 1
	#Create_trigger > 0
	#Read_trigger > 0
	#Update_trigger > 0
	#Delete_trigger > 0
	//#View = 1
	#Form > 0
	#Initial = 1
	all vw: View | one ok: Ok | #vw.sws = 0 and vw.aws = ok
	#View = 1 => #View.iws = #Form.iws
	#View = 1 => all iw: View.iws | one iww: Form.iws | View.mapping.iw = iww and #View.mapping.iw = 1
	#View = 1 => 	all iww: Form.iws | one iw: View.iws | View.mapping.iw = iww
	//all t: Time, iw: View.iws | iw.content.t = View.mapping.iw.content.t
	all iw: Initial | iw.aws = (Create_trigger+Read_trigger+Update_trigger+Delete_trigger) and #iw.iws = 0 and #iw.sws = 1
	all fw: Form | one ok: Ok,  cancel: Cancel | #fw.iws > 0 and fw.aws = ok+cancel and #fw.sws = 0 and ok.goes in Initial+Form and cancel.goes in Initial+Form
	all cw: Confirm | one ok: Ok,  cancel: Cancel | #cw.iws = 0 and #cw.sws = 0 and cw.aws = ok+cancel and ok.goes in Initial+Form and cancel.goes in Initial+Form
	all cw: Confirm_delete | one ok: Ok,  cancel: Cancel | #cw.iws = 0 and #cw.sws = 0 and cw.aws = ok+cancel and ok.goes in Initial and cancel.goes in Initial
	#Window = #Form + #Initial + #Confirm + #Confirm_delete + #General + #View
}
---------------Generic ADD Semantics----------
abstract sig Crud_op {}
one sig CREATE, READ, UPDATE, DELETE extends Crud_op {}
one sig Current_crud_op {
	operation: Crud_op lone -> Time
}
sig Field {
	associated_to: one Input_widget,
	has_value: Value lone -> Object
}
sig Property_unique, Property_required in Field { }
one sig List { 
	contains: Object set -> Time
}
fact {
	#Field = #Form.iws
	all t: Time | List.contains.t = Selectable_widget.list.t
	all f: Field | #f.associated_to = 1
	all iw: Input_widget | iw in Form.iws => #associated_to.iw = 1
	all t: Time | (Current_window.is_in.t in Initial or Current_window.is_in.t in General) <=> #Current_crud_op.operation.t = 0
}

pred go_semantics [w: Window, t: Time] { }
pred go_success_post [w: Window, t, t': Time] {
	List.contains.t' = List.contains.t
	#Current_crud_op.operation.t' = 0
}
pred go_fail_post [w: Window, t, t': Time] {
	List.contains.t' = List.contains.t
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred go_pre[w: Window, t: Time] {
	w in Initial
}

pred fill_semantics [iw: Input_widget, t: Time, v: Value] { }
pred fill_success_post [iw: Input_widget, t, t': Time, v: Value] { 
	List.contains.t' = List.contains.t
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred fill_fail_post [iw: Input_widget, t, t': Time, v: Value] { 
	List.contains.t' = List.contains.t
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred fill_pre[iw: Input_widget, t: Time, v: Value] { 
	not Current_crud_op.operation.t = READ
}

pred select_semantics [sw: Selectable_widget, t: Time, o: Object] { }
pred select_success_post [sw: Selectable_widget, t, t': Time, o: Object] { 
	List.contains.t' = List.contains.t
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred select_fail_post [sw: Selectable_widget, t, t': Time, o: Object] { 
	List.contains.t' = List.contains.t
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred select_pre[sw: Selectable_widget, t: Time, o: Object] { 
	o in sw.list.t
}

pred click_semantics [aw: Action_widget, t: Time] {
	(aw in Ok and Current_window.is_in.t  in Form and Current_crud_op.operation.t = CREATE) => filled_required_test [Current_window.is_in.t, t] and unique_test [Current_window.is_in.t, t] and valid_data_test [Current_window.is_in.t, t]
	(aw in Ok and Current_window.is_in.t  in Form and Current_crud_op.operation.t = UPDATE) => filled_required_test [Current_window.is_in.t, t] and unique_for_update_test [Current_window.is_in.t, t] and valid_data_test [Current_window.is_in.t, t]
	(aw in Ok and Current_window.is_in.t  in Confirm and Current_crud_op.operation.t = CREATE) => filled_required_test [Current_window.is_in.t, t] and unique_test [Current_window.is_in.t, t] and valid_data_test [Current_window.is_in.t, t]
	(aw in Ok and Current_window.is_in.t  in Confirm and Current_crud_op.operation.t = UPDATE) => filled_required_test [Current_window.is_in.t, t] and unique_for_update_test [Current_window.is_in.t, t] and valid_data_test [Current_window.is_in.t, t]
	(aw in Ok and Current_window.is_in.t  in Confirm_delete and Current_crud_op.operation.t = DELETE) => #Selectable_widget.selected.t = 1
	//(aw in Ok and Current_window.is_in.t  in Form and aw.goes in Initial) => (filled_required_test [t] and unique_test [t])
	//(aw in Ok and Current_window.is_in.t  in Confirm) => (filled_required_test [t] and unique_test [t])
}
pred click_success_post [aw: Action_widget, t, t': Time] {
	(aw in Create_trigger) => (Current_crud_op.operation.t' = CREATE and List.contains.t' = List.contains.t and #Input_widget.content.t' = 0 and #Selectable_widget.selected.t' = 0)
	(aw in Read_trigger) => (Current_crud_op.operation.t' = READ and List.contains.t' = List.contains.t and load_form[Selectable_widget.selected.t, t'] and (all sw: Selectable_widget |sw.selected.t' = sw.selected.t))
	(aw in Update_trigger) => (Current_crud_op.operation.t' = UPDATE and List.contains.t' = List.contains.t and load_form[Selectable_widget.selected.t, t']  and (all sw: Selectable_widget |sw.selected.t' = sw.selected.t))
	(aw in Delete_trigger and aw.goes in Confirm_delete) => (Current_crud_op.operation.t' = DELETE and List.contains.t' = List.contains.t  and (all sw: Selectable_widget |sw.selected.t' = sw.selected.t))
	
	(aw in Cancel and  Current_crud_op.operation.t = CREATE) => (List.contains.t' = List.contains.t and #Current_window.is_in.t.iws.content.t' = 0 and (all iww: (Input_widget - Current_window.is_in.t.iws) | iww.content.t' = iww.content.t) and Selectable_widget.selected.t' = Selectable_widget.selected.t)
	(aw in Cancel and Current_crud_op.operation.t in (UPDATE+READ) and aw.goes in Form) => (List.contains.t' = List.contains.t and load_form[Selectable_widget.selected.t, t']  and Selectable_widget.selected.t' = Selectable_widget.selected.t)
	(aw in Cancel and aw.goes in Initial) => (List.contains.t' = List.contains.t and #Input_widget.content.t' = 0 and Selectable_widget.selected.t' = Selectable_widget.selected.t)
	
	(aw in Ok and aw.goes in Form) => (List.contains.t' = List.contains.t and (all iw:Input_widget | iw.content.t' = iw.content.t) and Current_crud_op.operation.t' = Current_crud_op.operation.t and Selectable_widget.selected.t' = Selectable_widget.selected.t)
	(aw in Ok and Current_crud_op.operation.t = CREATE and aw.goes = Initial) => (#Input_widget.content.t' = 0 and add [t, t'] and #Current_crud_op.operation.t' = 0)
	(aw in Ok and Current_crud_op.operation.t = UPDATE and aw.goes = Initial) => (#Input_widget.content.t' = 0 and update [t, t'] and #Current_crud_op.operation.t' = 0)
	(aw in Ok and Current_crud_op.operation.t = DELETE and aw.goes = Initial) => (#Selectable_widget.selected.t' = 0 and #Input_widget.content.t' = 0 and delete [t, t'] and #Current_crud_op.operation.t' = 0)
	(aw in Ok and Current_crud_op.operation.t = READ and aw.goes = Initial) => (Selectable_widget.selected.t' = Selectable_widget.selected.t and #Input_widget.content.t' = 0 and #Current_crud_op.operation.t' = 0 and List.contains.t' = List.contains.t)
	(aw in Delete_trigger and aw.goes in Initial) => (#Selectable_widget.selected.t' = 0 and #Input_widget.content.t' = 0 and delete [t, t'] and #Current_crud_op.operation.t' = 0)
}
pred click_fail_post [aw: Action_widget, t, t': Time]	{
	List.contains.t' = List.contains.t
	(all iw:Input_widget | iw.content.t' = iw.content.t)
	(all sw:Selectable_widget | sw.selected.t' = sw.selected.t)
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred click_pre[aw: Action_widget, t: Time] {
	(aw in Read_trigger) => #Selectable_widget.selected.t = 1
	(aw in Update_trigger) => #Selectable_widget.selected.t = 1
	(aw in Delete_trigger) => #Selectable_widget.selected.t = 1
}

pred add [t, t': Time] {
	one o: Object |all f: Field | not(o in List.contains.t) and o.appeared = t' and f.has_value.o = f.associated_to.content.t and List.contains.t' = List.contains.t+o and Selectable_widget.selected.t' = o
}
pred filled_required_test [w: Form, t: Time] { 
	all iw: w.iws | (iw in Property_required.associated_to) => #iw.content.t = 1
}
pred  unique_test [w: Form, t: Time] { 
	all p: Property_unique | all o2: List.contains.t | (p.associated_to in w.iws and #p.has_value.o2 = 1) => p.associated_to.content.t != p.has_value.o2
}
pred valid_data_test [w: Form, t: Time] {
	all v: w.iws.content.t | not(v in Invalid)
}
pred  unique_for_update_test [w: Form, t: Time] { 
	all p: Property_unique | all o2: (List.contains.t-Selectable_widget.selected.t) | (p.associated_to in w.iws and #p.has_value.o2 = 1) => p.associated_to.content.t != p.has_value.o2
}
pred load_form [o: Object, t': Time] {
	all f: Field | f.associated_to.content.t' = f.has_value.o
	all iw: View.iws | iw.content.t' = View.mapping.iw.content.t'
}
pred update [t, t': Time] {
	one o: Object | all f: Field | not(o in List.contains.t) and o.appeared = Selectable_widget.selected.t.appeared and f.has_value.o = f.associated_to.content.t and List.contains.t' = (List.contains.t - Selectable_widget.selected.t)+o and Selectable_widget.selected.t' = o
}
pred delete [t, t': Time] {
	List.contains.t' = List.contains.t - Selectable_widget.selected.t
}