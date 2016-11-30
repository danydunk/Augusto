--------------------Initial State---------------
pred init [t: Time] {
	no Selectable_widget.list.t
	no Track.op.t
	no Selectable_widget.selected.t
 	Current_window.is_in.t = Initial
	no Current_crud_op.operation.t
}
---------------Generic CRUD Structure ----------
abstract sig Ok, Cancel extends Action_widget { }
abstract sig Create_trigger extends Action_widget { }
abstract sig Read_trigger extends Action_widget { }
abstract sig Update_trigger extends Action_widget { }
abstract sig Delete_trigger extends Action_widget { }

abstract sig Form extends Window { }
abstract sig View extends Window { 
	mapping: Input_widget set-> Input_widget
}
abstract sig Initial extends Window { }

fact {
	#View.sws = 0 and View.aws in Ok
	#View = 1 => #View.iws = #Form.iws
	#View = 1 => all iw: View.iws | one iww: Form.iws | View.mapping.iw = iww and #View.mapping.iw = 1
	#View = 1 => all iww: Form.iws | one iw: View.iws | View.mapping.iw = iww
	#View = 1 => no iww, iww2: Form.iws | one iw, iw2: View.iws | IW/lt[iww,iww2] and IW/lt[iw2,iw] and View.mapping.iw = iww and View.mapping.iw2 = iww2
	Initial.aws = (Create_trigger+Read_trigger+Update_trigger+Delete_trigger) and #Initial.iws = 0 and #Initial.sws = 1
	#Form.iws > 0 and #Form.aws = 2 and #Form.sws = 0
	#Window = #Form + #Initial + #View
	#Ok.goes < 2
	#Create_trigger.goes < 2
	#Read_trigger.goes < 2
	#Update_trigger.goes < 2
	#Delete_trigger.goes < 2
	//all iw: (Input_widget-Form.iws) | not(iw in Property_unique.uniques) and not(iw in Property_required.requireds)
	all iw: Form.iws | #iw.content.(T/first) =1 => not(iw in Property_required.requireds)
}
---------------Generic CRUD Semantics---------- 
abstract sig Crud_op {}
one sig CREATE, READ, UPDATE extends Crud_op {}
one sig Current_crud_op {
	operation: Crud_op lone -> Time
}
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

pred fill_semantics [iw: Input_widget, t: Time, v: Value] { }
pred fill_success_post [iw: Input_widget, t, t': Time, v: Value] { 
	Selectable_widget.list.t' = Selectable_widget.list.t
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred fill_fail_post [iw: Input_widget, t, t': Time, v: Value] { 
	Selectable_widget.list.t' = Selectable_widget.list.t
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred fill_pre[iw: Input_widget, t: Time, v: Value] { 
	not Current_crud_op.operation.t = READ
}

pred select_semantics [sw: Selectable_widget, t: Time, o: Object] { }
pred select_success_post [sw: Selectable_widget, t, t': Time, o: Object] { 
	Selectable_widget.list.t' = Selectable_widget.list.t
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred select_fail_post [sw: Selectable_widget, t, t': Time, o: Object] { 
	Selectable_widget.list.t' = Selectable_widget.list.t
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred select_pre[sw: Selectable_widget, t: Time, o: Object] { 
	o in sw.list.t
}

pred click_semantics [aw: Action_widget, t: Time] {
	(aw in Ok and Current_window.is_in.t  in Form and Current_crud_op.operation.t in CREATE) => filled_required_test [Current_window.is_in.t, t] and unique_test [Current_window.is_in.t, t]
	(aw in Ok and Current_window.is_in.t  in Form and Current_crud_op.operation.t in UPDATE) => filled_required_test [Current_window.is_in.t, t] and unique_for_update_test [Current_window.is_in.t, t]
	(aw in Ok and Current_window.is_in.t  in Form and Current_crud_op.operation.t in READ) => (2=(1+1))
	(aw in Delete_trigger) => (2=(1+1))
}
pred click_success_post [aw: Action_widget, t, t': Time] {
	(aw in Create_trigger) => (Current_crud_op.operation.t' = CREATE and Selectable_widget.list.t' = Selectable_widget.list.t and (all iw: Input_widget | iw.content.t' = iw.content.(T/first)) and #Selectable_widget.selected.t' = 0)
	(aw in Read_trigger) => (Current_crud_op.operation.t' = READ and Selectable_widget.list.t' = Selectable_widget.list.t and load_form[Selectable_widget.selected.t, t'] and Selectable_widget.selected.t' = Selectable_widget.selected.t)
	(aw in Update_trigger) => (Current_crud_op.operation.t' = UPDATE and Selectable_widget.list.t' = Selectable_widget.list.t and load_form[Selectable_widget.selected.t, t']  and Selectable_widget.selected.t' = Selectable_widget.selected.t)
	(aw in Delete_trigger) => (#Selectable_widget.selected.t' = 0 and delete [t, t'] and #Current_crud_op.operation.t' = 0)
	
	(aw in Cancel and Current_window.is_in.t  in Form) => (#Current_crud_op.operation.t' =0 and Selectable_widget.list.t' = Selectable_widget.list.t and #Selectable_widget.selected.t' = 0)
	
	(aw in Ok and Current_crud_op.operation.t in CREATE) => (#Selectable_widget.selected.t' = 0 and add [t, t'] and #Current_crud_op.operation.t' = 0)
	(aw in Ok and Current_crud_op.operation.t in UPDATE) => (#Selectable_widget.selected.t' = 0 and update [t, t'] and #Current_crud_op.operation.t' = 0)
	(aw in Ok and Current_window.is_in.t  in View) => (#Selectable_widget.selected.t' = 0  and #Current_crud_op.operation.t' = 0 and Selectable_widget.list.t' = Selectable_widget.list.t)
}
pred click_fail_post [aw: Action_widget, t, t': Time]	{
	Selectable_widget.list.t' = Selectable_widget.list.t
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
	one o: Object_inlist |all iw: Form.iws | not(o in Selectable_widget.list.t) and o.appeared = t' and o.vs.iw = iw.content.t and Selectable_widget.list.t' = Selectable_widget.list.t+o
}
pred filled_required_test [w: Form, t: Time] { 
	all iw: w.iws | (iw in Property_required.requireds) => #iw.content.t = 1
}
pred  unique_test [w: Form, t: Time] { 
	all iw: w.iws | all o: Selectable_widget.list.t | (iw in Property_unique.uniques and (#o.vs.iw= 1)) => iw.content.t !=o.vs.iw //and ((#p.has_value.o2 = 0) => #p.associated_to.content.t = 1)
}
pred  unique_for_update_test [w: Form, t: Time] {
	all iw: w.iws | all o: (Selectable_widget.list.t-Selectable_widget.selected.t) | (iw in Property_unique.uniques and (#o.vs.iw= 1)) => iw.content.t !=o.vs.iw //and ((#p.has_value.o2 = 0) => #p.associated_to.content.t = 1)
}
pred load_form [o: Object, t': Time] {
	all iw: Form.iws | iw.content.t' = o.vs.iw
	all iw: View.iws | iw.content.t' = View.mapping.iw.content.t'
}
pred update [t, t': Time] {
	one o: Object | all iw: Form.iws | not(o in Selectable_widget.list.t) and o.appeared = Selectable_widget.selected.t.appeared and o.vs.iw = iw.content.t and Selectable_widget.list.t' = (Selectable_widget.list.t - Selectable_widget.selected.t)+o
}
pred delete [t, t': Time] {
	Selectable_widget.list.t' = Selectable_widget.list.t - Selectable_widget.selected.t
}
