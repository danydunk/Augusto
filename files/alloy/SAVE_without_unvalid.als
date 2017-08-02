--------------------Initial State---------------
pred init [t: Time] {
	no Track.op.t
	Current_window.is_in.t = aws.New
	#Opening_list.list.t = 0
	#(Auxiliary.saved.t) = 1
	no Selectable_widget.selected.t
	#To_be_cleaned = 1
	all iw: Input_widget | #iw.content.(T/first) > 0
}
---------------Generic SAVE Structure ----------
abstract sig New, Open, Save, Saveas, Saves, Cancelsave, Openo, Cancelopen, Encryptb, Backe, Decryptb, Backd, Yes, No, Replace, Noreplace extends Action_widget { }
abstract sig Filename, Password, Repassword, Depassword extends Input_widget { }
abstract sig Opening_list extends Selectable_widget { }

---------------Generic SAVE Semantics---------- 
one sig Auxiliary{
	pwd: Object lone -> Value,
	haspwd: one Object,
	names: Object one -> Value,
	saved: Value lone -> Time
}

pred fill_semantics [iw: Input_widget, t: Time, v: Value] {
	
}
pred fill_success_post [iw: Input_widget, t, t': Time, v: Value] {
	Opening_list.list.t' =  Opening_list.list.t
	(Auxiliary.saved.t') = 	(Auxiliary.saved.t)
}
pred fill_fail_post [iw: Input_widget, t, t': Time, v: Value] {
	Opening_list.list.t' =  Opening_list.list.t
	(Auxiliary.saved.t') = 	(Auxiliary.saved.t)
}
pred fill_pre [iw: Input_widget, t: Time, v: Value] {
	
}
pred select_semantics [sw: Selectable_widget, t: Time, o: Object] {
	
}
pred select_success_post [sw: Selectable_widget, t, t': Time, o: Object] {
	Opening_list.list.t' =  Opening_list.list.t
	(Auxiliary.saved.t') = 	(Auxiliary.saved.t)
	(all iw: Input_widget | iw.content.t' = iw.content.t)
}
pred select_fail_post [sw: Selectable_widget, t, t': Time, o: Object] {
	Opening_list.list.t' =  Opening_list.list.t
	(Auxiliary.saved.t') = 	(Auxiliary.saved.t)
	(all iw: Input_widget | iw.content.t' = iw.content.t)
}
pred select_pre [sw: Selectable_widget, t: Time, o: Object] {
	
}
pred click_semantics [aw: Action_widget, t: Time] {
	(aw in Save)        => #(Auxiliary.saved.t) = 0
	(aw in Openo)     => #Opening_list.selected.t = 1
	(aw in Saves)      => #Filename.content.t = 1
	(aw in Encryptb)   => Password.content.t = Repassword.content.t 
	(aw in Decryptb) => Depassword.content.t = (Opening_list.selected.t).(Auxiliary.pwd)
}
pred click_success_post [aw: Action_widget, t, t': Time] {
	(aw in New and #aw.goes = 0) => new[t,t']
	(aw in New and #aw.goes > 0) => same[t,t'] and Current_window.is_in.t' = aw.goes
	(aw in Open) => same[t,t'] and Current_window.is_in.t' = aw.goes
	(aw in Save) => same[t,t'] and Current_window.is_in.t' = aw.goes
	(aw in Saveas) => same[t,t'] and Current_window.is_in.t' = aw.goes
	(aw in Saves and exisit[t, Filename.content.t]) => ((#Replace = 1) => (Current_window.is_in.t' = aws.Replace and same[t,t']) else ((#Encryptb = 1 or #Yes = 1) => (same[t,t'] and (#Yes = 1 => Current_window.is_in.t' = aws.Yes else Current_window.is_in.t' = aws.Encryptb)) else (savenp[t,t', Filename.content.t])))
	(aw in Saves and not(exisit[t, Filename.content.t])) => ((#Encryptb = 1 or #Yes = 1) => (same[t,t'] and (#Yes = 1 => Current_window.is_in.t' = aws.Yes else Current_window.is_in.t' = aws.Encryptb)) else (savenp[t,t', Filename.content.t]))
	(aw in Cancelsave) => returned[t, t']
	(aw in Openo) => ((Opening_list.selected.t) in (Auxiliary.haspwd)) => (Current_window.is_in.t' = aws.Decryptb and same[t,t']) else (openo[t,t'])
	(aw in Cancelopen) => returned[t, t']
	(aw in Encryptb) => save[t,t', Password.content.t, Filename.content.t]
	(aw in Backe) => (#Yes = 1) => savenp[t,t', Filename.content.t] else((aw.goes in aws.New) => returned[t,t'] else (same2[t,t'] and Current_window.is_in.t' = aw.goes))
	(aw in Decryptb) => openo[t, t']
	(aw in Backd) => ((aw.goes in aws.New) => returned[t,t'] else (same2[t,t'] and Current_window.is_in.t' = aw.goes))
	(aw in Yes) => same[t,t'] and Current_window.is_in.t' = aw.goes
	(aw in No) => savenp[t,t', Filename.content.t]
	(aw in Replace) => ((#Encryptb = 1 or #Yes = 1) => (same[t,t'] and (#Yes = 1 => Current_window.is_in.t' = aws.Yes else Current_window.is_in.t' = aws.Encryptb)) else (savenp[t,t', Filename.content.t]))
	(aw in Noreplace) => ((aw.goes in aws.New) => returned[t,t'] else (same2[t,t'] and Current_window.is_in.t' = aw.goes))
}
pred click_fail_post [aw: Action_widget, t, t': Time] {
	(aw in (Encryptb+Decryptb)) => 	(all iw: Input_widget | iw.content.t' = iw.content.(T/first)) else (all iw: Input_widget | iw.content.t' = iw.content.t)
	Opening_list.list.t' =  Opening_list.list.t
	Opening_list.selected.t' =  Opening_list.selected.t
	(Auxiliary.saved.t') = 	(Auxiliary.saved.t)
}
pred click_pre [aw: Action_widget, t': Time] {
	
}
pred save [t, t': Time, password, filename: Value] {
	(filename in (Opening_list.list.t).(Auxiliary.names)) => (one o: Object | o in Auxiliary.haspwd and not(o in Opening_list.list.t) and o.appeared = Auxiliary.names.filename.appeared and o.(Auxiliary.pwd) = password and o.(Auxiliary.names) = filename and Opening_list.list.t' = (Opening_list.list.t - (Auxiliary.names).filename)+o) else (one o: Object | o in Auxiliary.haspwd and not(o in Opening_list.list.t) and o.(Auxiliary.pwd) = password and o.(Auxiliary.names) = filename and o.appeared = t' and Opening_list.list.t' = Opening_list.list.t + o)
	#(Auxiliary.saved.t') = 1
	Current_window.is_in.t' = aws.New
	#Opening_list.selected.t' = 0
	(all iw: Input_widget | iw.content.t' = iw.content.(T/first))
}
pred savenp [t, t': Time, filename: Value] {
	(filename in (Opening_list.list.t).(Auxiliary.names)) => (one o: Object | not(o in Auxiliary.haspwd) and not(o in Opening_list.list.t) and o.appeared = Auxiliary.names.filename.appeared and o.(Auxiliary.pwd) = none and o.(Auxiliary.names) = filename and Opening_list.list.t' = (Opening_list.list.t - (Auxiliary.names).filename)+o) else (one o: Object | not(o in Auxiliary.haspwd) and not(o in Opening_list.list.t) and o.(Auxiliary.pwd) = none and o.(Auxiliary.names) = filename and o.appeared = t' and Opening_list.list.t' = Opening_list.list.t + o)
	#(Auxiliary.saved.t') = 1
	Current_window.is_in.t' = aws.New
	#Opening_list.selected.t' = 0
	(all iw: Input_widget | iw.content.t' = iw.content.(T/first))
}
pred new [t, t': Time] {
	#(Auxiliary.saved.t') = 0
	(all iw: Input_widget | iw.content.t' = iw.content.(T/first))
	Opening_list.list.t' =Opening_list.list.t
	#Opening_list.selected.t' = 0
	Current_window.is_in.t' = aws.New
}
pred openo [t, t': Time] {
	#(Auxiliary.saved.t') = 1
	(all iw: Input_widget | iw.content.t' = iw.content.(T/first))
	Opening_list.list.t' =  Opening_list.list.t
	Current_window.is_in.t' = aws.New
	#Opening_list.selected.t' = 0
}
pred exisit [t: Time, name: Value] {
	name in (Opening_list.list.t).(Auxiliary.names)
}
pred returned [t, t': Time] {
	(Auxiliary.saved.t') = 	(Auxiliary.saved.t)
	(all iw: Input_widget | iw.content.t' = iw.content.(T/first))
	#Opening_list.selected.t' = 0
	Opening_list.list.t' = Opening_list.list.t
	Current_window.is_in.t' = aws.New
}

pred same [t, t': Time] {
	(Auxiliary.saved.t') = 	(Auxiliary.saved.t)
	(all iw:  Input_widget | iw.content.t' = iw.content.t)
	Opening_list.selected.t' = Opening_list.selected.t
	Opening_list.list.t' = Opening_list.list.t
}
pred same2 [t, t': Time] {
	(Auxiliary.saved.t') = 	(Auxiliary.saved.t)
	(all iw: Input_widget | iw.content.t' = iw.content.(T/first))
	Opening_list.selected.t' = Opening_list.selected.t
	Opening_list.list.t' = Opening_list.list.t
}
