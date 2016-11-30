open util/ordering [Time] as T
open orderingFIXED [Input_widget] as IW
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
	with: one Value
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
	(one iw: Input_widget, f: Fill, v: Value | fill [iw, t, t', v, f]) or
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
abstract sig Input_widget {
	content: Value lone -> Time,
	invalid: set Value
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
	//all o: Object | some t: Time | o in Selectable_widget.list.t
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
	(click_semantics [aw, t] and #aw.goes < 2 and Current_window.is_in.t' = aw.goes and click_success_post [aw, t, t']) or
	(click_semantics [aw, t]  and not(#aw.goes < 2) and click_success_post [aw, t, t']) or
	(not click_semantics  [aw, t] and Current_window.is_in.t' = Current_window.is_in.t and click_fail_post [aw, t, t'])
	--- operation is tracked ---
	c.clicked = aw and Track.op.t' = c
}
pred fill [iw: Input_widget, t, t': Time, v: Value, f: Fill] { 
	--- precondition ---
	iw in Current_window.is_in.t.iws
	fill_pre [iw, t, v]
	--- effect ---
	(fill_semantics  [iw, t, v] and iw.content.t' = v and fill_success_post [iw, t, t', v]) or
	(not fill_semantics  [iw, t, v] and iw.content.t' = iw.content.t and fill_fail_post [iw, t, t', v])
	--- general postcondition ---
	Current_window.is_in.t' = Current_window.is_in.t
	all iww: (Input_widget - iw) | iww.content.t' = iww.content.t
	all sw:Selectable_widget | sw.selected.t' = sw.selected.t
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
	all iw: Input_widget | iw.content.t' = iw.content.t
	(Selectable_widget - sw).selected.t' = (Selectable_widget - sw).selected.t
	--- operation is tracked ---
	s.wid = sw and s.which = o and Track.op.t' = s
}