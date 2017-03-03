--------------------Initial State---------------
pred init [t: Time] {
	no Selectable_widget.list.t
	no Track.op.t
	no Selectable_widget.selected.t
 	Current_window.is_in.t = sws.For_selecting
	#Create_trigger = 0 =>Current_crud_op.operation.t = CREATE else #Current_crud_op.operation.t = 0
}
---------------Generic CRUD Structure ----------
abstract sig Ok, Cancel extends Action_widget { }
abstract sig Create_trigger extends Action_widget { }
abstract sig Update_trigger extends Action_widget { }
abstract sig Delete_trigger extends Action_widget { }
abstract sig For_inputing extends Input_widget { }

abstract sig For_selecting extends Selectable_widget{ }

fact{
	no t: Time |  #Track.op.t = 1 and Track.op.t in Fill and Track.op.t.with = Track.op.t.filled.content.(T/prev[t])
}

fact {
	#Ok < 2
	#For_selecting = 1 and #Selectable_widget = 1
	all iw: For_inputing | #iw.content.(T/first) =1 => not(iw in Property_required.requireds)
}
---------------Generic CRUD Semantics---------- 
abstract sig Crud_op {}
one sig CREATE, UPDATE extends Crud_op {}
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
	(aw in Ok and Current_crud_op.operation.t in CREATE) => filled_required_test [Current_window.is_in.t, t] and unique_test [Current_window.is_in.t, t] 
	(aw in Ok and Current_crud_op.operation.t in UPDATE) => filled_required_test [Current_window.is_in.t, t] and unique_for_update_test [Current_window.is_in.t, t] 
}
pred click_success_post [aw: Action_widget, t, t': Time] {
	Current_window.is_in.t' = aw.goes
	(aw in Create_trigger) => (Current_crud_op.operation.t' = CREATE and For_selecting.list.t' = For_selecting.list.t and (all iw: Input_widget | iw.content.t' = iw.content.(T/first)) and #For_selecting.selected.t' = 0)
	(aw in Update_trigger) => (Current_crud_op.operation.t' = UPDATE and For_selecting.list.t' = For_selecting.list.t and load_form[For_selecting.selected.t, t']  and For_selecting.selected.t' = For_selecting.selected.t)
	(aw in Delete_trigger) => (#For_selecting.selected.t' = 0 and delete [t, t'])
	(aw in Delete_trigger and #Create_trigger = 0) => (Current_crud_op.operation.t' = CREATE  and (all iw: Input_widget | iw.content.t' = iw.content.(T/first)))
	(aw in Delete_trigger and #Create_trigger > 0) => (#Current_crud_op.operation.t' = 0  and (all iw: Input_widget | iw.content.t' = iw.content.(T/first)))
	
	(aw in Cancel and #Create_trigger > 0) => (#Current_crud_op.operation.t' = 0 and For_selecting.list.t' = For_selecting.list.t and #For_selecting.selected.t' = 0)
	(aw in Cancel and #Create_trigger = 0) => (Current_crud_op.operation.t' = CREATE and For_selecting.list.t' = For_selecting.list.t and #For_selecting.selected.t' = 0  and (all iw: Input_widget | iw.content.t' = iw.content.(T/first)))
	
	(aw in Ok and Current_crud_op.operation.t in CREATE) => (#For_selecting.selected.t' = 0 and add [t, t'])
	(aw in Ok and Current_crud_op.operation.t in UPDATE) => (#For_selecting.selected.t' = 0 and update [t, t'])
	(aw in Ok and #Create_trigger > 0) => (#Current_crud_op.operation.t' =0)
	(aw in Ok and #Create_trigger = 0) => (Current_crud_op.operation.t' =CREATE  and (all iw: Input_widget | iw.content.t' = iw.content.(T/first)) and #For_selecting.selected.t' = 0)
}
pred click_fail_post [aw: Action_widget, t, t': Time]	{
	For_selecting.list.t' = For_selecting.list.t
	(all iw:Input_widget | iw.content.t' = iw.content.t)
	For_selecting.selected.t' = For_selecting.selected.t
	Current_crud_op.operation.t' = Current_crud_op.operation.t
}
pred click_pre[aw: Action_widget, t: Time] {
	(aw in Update_trigger) => #For_selecting.selected.t = 1
	(aw in Delete_trigger) => #For_selecting.selected.t = 1
}

pred add [t, t': Time] {
	one o: Object_inlist |all iw: For_inputing | not(o in For_selecting.list.t) and o.appeared = t' and o.vs.iw = iw.content.t and For_selecting.list.t' = For_selecting.list.t+o
}
pred filled_required_test [w: Window, t: Time] { 
	all iw: (w.iws & For_inputing)| (iw in Property_required.requireds) => #iw.content.t = 1
}
pred  unique_test [w: Window, t: Time] { 
	all iw: (w.iws & For_inputing) | all o: For_selecting.list.t | (iw in Property_unique.uniques and (#o.vs.iw= 1)) => iw.content.t !=o.vs.iw //and ((#p.has_value.o2 = 0) => #p.associated_to.content.t = 1)
}
pred  unique_for_update_test [w: Window, t: Time] {
	all iw: (w.iws & For_inputing) | all o: (For_selecting.list.t-For_selecting.selected.t) | (iw in Property_unique.uniques and (#o.vs.iw= 1)) => iw.content.t !=o.vs.iw //and ((#p.has_value.o2 = 0) => #p.associated_to.content.t = 1)
}
pred load_form [o: Object, t': Time] {
	all iw: For_inputing | iw.content.t' = o.vs.iw
}
pred update [t, t': Time] {
	one o: Object | all iw: For_inputing | not(o in For_selecting.list.t) and o.appeared = For_selecting.selected.t.appeared and o.vs.iw = iw.content.t and For_selecting.list.t' = (For_selecting.list.t - Selectable_widget.selected.t)+o
}
pred delete [t, t': Time] {
	For_selecting.list.t' = For_selecting.list.t - For_selecting.selected.t
}
