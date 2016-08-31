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
	with: one Value
}
sig Go extends Operation {
	where: one Window
}
sig Select extends Operation {
	wid: one Selectable_widget,
	selected: one Object
}
one sig Track { 
	op: Operation lone -> Time
}
pred transition [t, t': Time]  {
	(one aw: Action_widget, c: Click | click [aw, t, t', c]) or 
	(one iw: Input_widget, f: Fill, v: Value | fill [iw, t, t', v, f]) or
	(one sw: Selectable_widget, s: Select, o: Object | select [sw, t, t', o, s]) or
	(one w: Window, g: Go | go [w, t, t', g])
}
pred System {
	init [T/first]
	all t: Time - T/last | transition [t, T/next[t]]
}
fact{
	no t: Time| (not t = T/first) and (not t in ran[Track.op])
	no o: Operation | not o in dom [Track.op]
	all t: Time | not t = T/first => #Current_window.is_in.t = 1
	all t: Time, g: Go | Track.op.t = g => (Current_window.is_in.(T/prev[t]) = General and (not g.where in General))
	all t,t':Time| no f, f': Fill | t' = T/next[t] and Track.op.t = f and Track.op.t' =f' and f.filled = (f').filled and f.with = (f').with
	all t,t':Time| no s, s': Select | t' = T/next[t] and Track.op.t = s and Track.op.t' =s' and s.selected = (s').selected and s.wid = (s').wid
}
----------------Generic GUI Structure ----------------
abstract sig Window {
	aws: set Action_widget,
	iws: set Input_widget,
	sws: set Selectable_widget
}
one sig General extends Window { }
abstract sig Action_widget {
	goes: lone Window
}
sig Value { }
sig Invalid in Value { }
abstract sig Input_widget {
	content: Value lone -> Time
}
sig Object {
	appeared: one Time
}
abstract sig Selectable_widget {
	list: Object set -> Time,
	selected: Object lone ->Time
}
fact {
	all gw: General |  #gw.iws = 0 and #gw.aws = 0 and #gw.sws = 0
	all iw: Input_widget | one w: Window | iw in w.iws
	all aw: Action_widget | one w: Window | aw in w.aws and #aw.goes < 2
	all sw: Selectable_widget | one w: Window | sw in w.sws
	all o: Object | some t: Time | o in Selectable_widget.list.t
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
	(click_semantics [aw, t] and Current_window.is_in.t' = aw.goes and click_success_post [aw, t, t']) or
	//(click_semantics  [aw, t] and not #aw.goes = 1 and Current_window.is_in.t' = General and click_success_post [aw, t, t']) or
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
	s.wid = sw and s.selected = o and Track.op.t' = s
}
pred go [w: Window, t, t': Time, g: Go] {
	--- precondition ---
	General in Current_window.is_in.t
	go_pre [w, t]
	--- effect ---
	(go_semantics [w, t] and Current_window.is_in.t' = w and 	#Input_widget.content.t' = 0 and #Selectable_widget.selected.t' = 0 and go_success_post [w, t, t']) or
	(not go_semantics [w, t] and Current_window.is_in.t' = General and (all iw: Input_widget | iw.content.t' = iw.content.t) and Selectable_widget.selected.t' = Selectable_widget.selected.t and go_fail_post [w, t, t'])
	--- operation is tracked ---
	g.where = w and Track.op.t' = g
}