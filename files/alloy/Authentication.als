open util/ordering [Time] as T
open util/ternary
open util/relation
-----------------------Utils------------------------
sig Time { }
abstract sig Operation { }
sig Click extends Operation {
	clicked: one Action_widget
}
sig Fill extends Operation {
	filled: one Input_widget,
	with: lone Value
}
sig Select extends Operation {
	wid: one Selectable_widget,
	which: one Object
}
one sig Track { 
	op: Operation lone -> Time
}
pred transition [t, t': Time]  {
	(one aw: Action_widget, c: Click | click [aw, t, t', c]) or 
	(one iw: Input_widget, v: Value, f: Fill| fill [iw, t, t', v, f]) or
	(one iw: Input_widget, f: Fill| fill [iw, t, t', none, f]) or	
	(one sw: Selectable_widget, s: Select, o: Object | select [sw, t, t', o, s]) 
}
pred System {
	init [T/first]
	all t: Time - T/last | transition [t, T/next[t]]
}
----------------Generic GUI Structure ----------------
abstract sig Window {
	aws: set Action_widget,
	iws: set Input_widget,
	sws: set Selectable_widget
}
abstract sig Action_widget {
	goes: set Window
}
sig Value { }
one sig Option_value_0 extends Value{ }
one sig Option_value_1 extends Value{ }
one sig Option_value_2 extends Value{ }
one sig Option_value_3 extends Value{ }
one sig Option_value_4 extends Value{ }
sig To_be_cleaned extends Value{ }

abstract sig Input_widget {
	content: Value lone -> Time,
	val: set Value
}
sig Object {
	appeared: one Time
}
abstract sig Selectable_widget {
	list: Object set -> Time,
	selected: Object lone ->Time
}
fact {
	all iw: Input_widget | iw in Window.iws
	all aw: Action_widget | aw in Window.aws
	all sw: Selectable_widget | sw in Window.sws
}
fact no_redundant{
	no t: Time |  #Track.op.t = 1 and Track.op.t in Fill and Track.op.t.with = Track.op.t.filled.content.(T/prev[t])
	no t: Time | #Track.op.t = 1 and Track.op.t in Click and Track.op.(T/prev[t]) in Click and Track.op.t.clicked = Track.op.(T/prev[t]).clicked	
	no t: Time | #Track.op.t = 1 and Track.op.t in Select and Track.op.(T/prev[t]) in Select and Track.op.(T/prev[t]).wid = Track.op.t.wid
	no t: Time | #Track.op.t = 1 and Track.op.t in Fill and Track.op.(T/prev[t]) in Fill and Track.op.t.filled = Track.op.(T/prev[t]).filled
}
----------------Generic GUI Semantics ---------------
one sig Current_window {
	is_in: Window one -> Time
}
pred click [aw: Action_widget, t, t': Time, c: Click] {
	--- precondition ---
	aw in Current_window.is_in.t.aws
	click_pre [aw, t]
	--- effect ---
	(click_semantics [aw, t] and click_success_post [aw, t, t']) or
	(not click_semantics  [aw, t] and Current_window.is_in.t' = Current_window.is_in.t and click_fail_post [aw, t, t'])
	--- operation is tracked ---
	c.clicked = aw and Track.op.t' = c
}
pred fill [iw: Input_widget, t, t': Time, v: Value, f: Fill] { 
	--- precondition ---
	not(v = iw.content.t) and not(v = none)  => not(v in To_be_cleaned)
	iw in Current_window.is_in.t.iws
	fill_pre [iw, t, v]
	--- effect ---
	(fill_semantics  [iw, t, v] and iw.content.t' = v and fill_success_post [iw, t, t', v]) or
	(not fill_semantics  [iw, t, v] and iw.content.t' = iw.content.t and fill_fail_post [iw, t, t', v])
	--- general postcondition ---
	Current_window.is_in.t' = Current_window.is_in.t
	all iww: (Input_widget - iw) | iww.content.t' = iww.content.t
	all sw:Selectable_widget | sw.selected.t' = sw.selected.t and sw.list.t' = sw.list.t
	--- operation is tracked ---
	f.filled = iw and f.with = v and Track.op.t' = f
}
pred select [sw: Selectable_widget, t, t': Time, o: Object, s: Select] { 
	--- precondition ---
	sw in Current_window.is_in.t.sws
	o in sw.list.t
	select_pre [sw, t, o]
	--- effect ---
	(select_semantics  [sw, t, o] and sw.selected.t' = o and select_success_post [sw, t, t', o]) or
	(not select_semantics  [sw, t, o] and sw.selected.t' = sw.selected.t and select_fail_post [sw, t, t', o])
	--- general postcondition ---
	Current_window.is_in.t' = Current_window.is_in.t
	all sww: (Selectable_widget - sw) | sww.selected.t' = sww.selected.t and sww.list.t' = sww.list.t
	sw.list.t' = sw.list.t	
	--- operation is tracked ---
	s.wid = sw and s.which = o and Track.op.t' = s
}

--------------------Initial State---------------
pred init [t: Time] {
	no Track.op.t
 	Current_window.is_in.t = aws.Login
	#List.elements.t = 0
}
---------------Generic AUTH Structure ----------
abstract sig Go, Login, Signup, Ok, Cancel, Logout extends Action_widget { }
abstract sig User, Password, User_save, Password_save, Re_password, Field extends Input_widget { }

fact {
	not(User in Property_required.requireds)
	not(Password in Property_required.requireds)	
	not(User_save in Property_required.requireds)
	not(Password_save in Property_required.requireds)
	not(Re_password in Property_required.requireds)
}
---------------Generic AUTH Semantics---------- 
one sig Property_required{
	requireds: set Input_widget
}
sig Object_inlist extends Object{
	vs: Value lone -> Input_widget
}
one sig List {
	elements: Object_inlist set -> Time
}
pred fill_semantics [iw: Input_widget, t: Time, v: Value] { }
pred fill_success_post [iw: Input_widget, t, t': Time, v: Value] {  
		List.elements.t' =  List.elements.t
}
pred fill_fail_post [iw: Input_widget, t, t': Time, v: Value] {
		List.elements.t' =  List.elements.t
}
pred fill_pre[iw: Input_widget, t: Time, v: Value] { 
	#iw.content.(T/first) = 1 => not(v = none)
}

pred select_semantics [sw: Selectable_widget, t: Time, o: Object] { }
pred select_success_post [sw: Selectable_widget, t, t': Time, o: Object] { }
pred select_fail_post [sw: Selectable_widget, t, t': Time, o: Object] { }
pred select_pre[sw: Selectable_widget, t: Time, o: Object] { }

pred click_semantics [aw: Action_widget, t: Time] {
	(aw in Login) => filled_login_test [t] and existing_test [t] 
	(aw in Ok) => filled_required_test[t] and unique_fields_test [t] and same_pass_test [t]
}
pred click_success_post [aw: Action_widget, t, t': Time] {
	Current_window.is_in.t' = aw.goes
	(aw in Ok) => add [t, t'] else List.elements.t' =  List.elements.t
	(all iw: Input_widget | iw.content.t' = iw.content.(T/first))
}
pred click_fail_post [aw: Action_widget, t, t': Time]	{
	(all iw: Input_widget | iw.content.t' = iw.content.(T/first))
	List.elements.t' =  List.elements.t
}
pred click_pre[aw: Action_widget, t: Time] { }

pred add [t, t': Time] {
	one o: Object_inlist |all iw: (User_save + Password_save + Field) | not(o in List.elements.t) and o.appeared = t' and o.vs.iw = iw.content.t and List.elements.t' =  List.elements.t+o
}
pred filled_login_test [t: Time] { 
	all iw: (User+Password)| #iw.content.t = 1
}
pred  existing_test [t: Time] { 
	one o: List.elements.t | Password.content.t =o.vs.Password_save and User.content.t =o.vs.User_save
}
pred same_pass_test [t: Time] {
	Password_save.content.t = Re_password.content.t
}
pred filled_required_test [t: Time] { 
	#User_save.content.t = 1 and #Password_save.content.t = 1 and #Re_password.content.t = 1
	all iw: Field| (iw in Property_required.requireds) => #iw.content.t = 1
}
pred  unique_fields_test [t: Time] { 
	all o: List.elements.t | (#o.vs.User_save= 1 => User_save.content.t !=o.vs.User_save)
}
