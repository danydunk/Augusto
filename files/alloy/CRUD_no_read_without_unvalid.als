--------------------Initial State---------------
pred init [t: Time] {
	no Selectable_widget.list.t
	no Track.op.t
	no Selectable_widget.selected.t
 	Current_window.is_in.t = sws.Selectable_widget
	#Create_trigger = 0 =>Current_crud_op.operation.t = CREATE else #Current_crud_op.operation.t = 0
}
---------------Generic CRUD Structure ----------
abstract sig Ok, Cancel extends Action_widget { }
abstract sig Create_trigger extends Action_widget { }
abstract sig Update_trigger extends Action_widget { }
abstract sig Delete_trigger extends Action_widget { }

fact {
	#Ok < 2
	#Selectable_widget = 1
	all iw: Input_widget | #iw.content.(T/first) = 1 => not(iw in Property_semantic.requireds) 
}
---------------Generic CRUD Semantics---------- 
abstract sig Crud_op {}
one sig CREATE, UPDATE extends Crud_op {}
one sig Current_crud_op {
	operation: Crud_op lone -> Time
}
one sig Property_semantic{
	uniques: set Input_widget,
	requireds: set Input_widget
} 

sig Object_inlist extends Object{
	vs: Value lone ->Input_widget
}

pred fill_semantics [iw: Input_widget, t: Time, v: Value] { }
pred fill_success_post [iw: Input_widget, t, t': Time, v: Value] { 
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred fill_fail_post [iw: Input_widget, t, t': Time, v: Value] { 
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred fill_pre[iw: Input_widget, t: Time, v: Value] { 
	#iw.content.(T/first) = 1 => not(v = none)
}

pred select_semantics [sw: Selectable_widget, t: Time, o: Object] { }
pred select_fail_post [sw: Selectable_widget, t, t': Time, o: Object] {
	Current_crud_op.operation.t' = Current_crud_op.operation.t
	all iw: Input_widget | iw.content.t' = iw.content.t
}
pred select_success_post [sw: Selectable_widget, t, t': Time, o: Object] {
	#Create_trigger = 0 => (Current_crud_op.operation.t' = UPDATE and load_form[o, t'])  else (#Current_crud_op.operation.t' = 0 and all iw: Input_widget | iw.content.t' = iw.content.t)
}
pred select_pre[sw: Selectable_widget, t: Time, o: Object] { 
	o in sw.list.t
}

pred click_semantics [aw: Action_widget, t: Time] {
	(aw in Ok and Current_crud_op.operation.t in CREATE) => filled_required_test [t] and unique_test [t] 
	(aw in Ok and Current_crud_op.operation.t in UPDATE) => filled_required_test [t] and unique_for_update_test [t]
}
pred click_success_post [aw: Action_widget, t, t': Time] {
	Current_window.is_in.t' = aw.goes
	(aw in Create_trigger) => (Current_crud_op.operation.t' = CREATE and Selectable_widget.list.t' = Selectable_widget.list.t and (all iw: Input_widget | iw.content.t' = iw.content.(T/first)) and #Selectable_widget.selected.t' = 0)
	(aw in Update_trigger) => (Current_crud_op.operation.t' = UPDATE and Selectable_widget.list.t' = Selectable_widget.list.t and load_form[Selectable_widget.selected.t, t']  and Selectable_widget.selected.t' = Selectable_widget.selected.t)
	(aw in Delete_trigger) => (#Selectable_widget.selected.t' = 0 and delete [t, t'])
	(aw in Delete_trigger and #Create_trigger = 0) => (Current_crud_op.operation.t' = CREATE  and (all iw: Input_widget | iw.content.t' = iw.content.(T/first)))
	(aw in Delete_trigger and #Create_trigger > 0) => (#Current_crud_op.operation.t' = 0  and (all iw: Input_widget | iw.content.t' = iw.content.(T/first)))
	
	(aw in Cancel and #Create_trigger > 0) => (#Current_crud_op.operation.t' = 0 and Selectable_widget.list.t' = Selectable_widget.list.t and #Selectable_widget.selected.t' = 0)
	(aw in Cancel and #Create_trigger = 0) => (Current_crud_op.operation.t' = CREATE and Selectable_widget.list.t' = Selectable_widget.list.t and #Selectable_widget.selected.t' = 0  and (all iw: Input_widget | iw.content.t' = iw.content.(T/first)))
	
	(aw in Ok and Current_crud_op.operation.t in CREATE) => (#Selectable_widget.selected.t' = 0 and add [t, t'])
	(aw in Ok and Current_crud_op.operation.t in UPDATE) => (#Selectable_widget.selected.t' = 0 and update [t, t'])
	(aw in Ok and #Create_trigger > 0) => (#Current_crud_op.operation.t' =0)
	(aw in Ok and #Create_trigger = 0) => (Current_crud_op.operation.t' =CREATE  and (all iw: Input_widget | iw.content.t' = iw.content.(T/first)) and #Selectable_widget.selected.t' = 0)
}
pred click_fail_post [aw: Action_widget, t, t': Time]	{
	Selectable_widget.list.t' = Selectable_widget.list.t
	(all iw:Input_widget | iw.content.t' = iw.content.t)
	Selectable_widget.selected.t' = Selectable_widget.selected.t
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred click_pre[aw: Action_widget, t: Time] {
	(aw in Update_trigger) => #Selectable_widget.selected.t = 1
	(aw in Delete_trigger) => #Selectable_widget.selected.t = 1
}

pred add [t, t': Time] {
	one o: Object_inlist |all iw: Input_widget | not(o in Selectable_widget.list.t) and o.appeared = t' and o.vs.iw = iw.content.t and Selectable_widget.list.t' = Selectable_widget.list.t+o
}
pred filled_required_test [t: Time] { 
	all iw: Property_semantic.requireds| #iw.content.t = 1
}
pred  unique_test [t: Time] { 
	all iw: Property_semantic.uniques | all o: Selectable_widget.list.t | (#o.vs.iw= 1) => iw.content.t !=o.vs.iw
}
pred  unique_for_update_test [t: Time] {
	all iw: Property_semantic.uniques | all o: (Selectable_widget.list.t-Selectable_widget.selected.t) | (#o.vs.iw= 1) => iw.content.t !=o.vs.iw
}
pred load_form [o: Object, t': Time] {
	all iw: Input_widget | iw.content.t' = o.vs.iw
}
pred update [t, t': Time] {
	one o: Object | all iw: Input_widget | not(o in Selectable_widget.list.t) and o.appeared = Selectable_widget.selected.t.appeared and o.vs.iw = iw.content.t and Selectable_widget.list.t' = (Selectable_widget.list.t - Selectable_widget.selected.t)+o
}
pred delete [t, t': Time] {
	Selectable_widget.list.t' = Selectable_widget.list.t - Selectable_widget.selected.t
}