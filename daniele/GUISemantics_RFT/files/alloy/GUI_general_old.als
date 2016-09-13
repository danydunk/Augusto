open util/ordering [Time] as T
open util/ordering [Input_widget] as IW
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
one sig Track { 
	op: Operation lone -> Time
}
pred transition [t, t': Time]  {
	(one aw: Action_widget, c: Click | click [aw, t, t', c]) or 
	(one iw: Input_widget, f: Fill, v: Value | fill [iw, t, t', v, f]) or
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
	all t: Time, g: Go | Track.op.t = g => (Current_window.is_in.(T/prev[t]) = General and (not g.where = General))
}
----------------Generic GUI Structure ----------------
abstract sig Window {
	aws: set Action_widget,
	iws: set Input_widget,
}
one sig General extends Window { }
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
	all gw: General |  #gw.iws = 0 and #gw.aws = 0
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
	(click_semantics  [aw, t] and not #aw.goes = 1 and Current_window.is_in.t' = General and click_success_post [aw, t, t']) or
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
	all iw:(Input_widget - iw)| iw.content.t' = iw.content.t
	--- operation is tracked ---
	f.filled = iw and f.with = v and Track.op.t' = f
}
pred select [sw: Selectable_widget, t, t': Time, o: Object, s: Fill] { }
pred go [w: Window, t, t': Time, g: Go] {
	--- precondition ---
	General in Current_window.is_in.t
	--- effect ---
	(go_semantics [w, t] and Current_window.is_in.t' = w and 	#Input_widget.content.t' = 0 and go_success_post [w, t, t']) or
	(not go_semantics [w, t] and Current_window.is_in.t' = General and Input_widget.content.t' =  Input_widget.content.t and go_fail_post [w, t, t'])
	--- operation is tracked ---
	g.where = w and Track.op.t' = g
}