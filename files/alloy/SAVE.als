--------------------Initial State---------------
pred init [t: Time] {
	no Track.op.t
	Current_window.is_in.t = aws.New
	#Opening_list.list.t = 0
	#(Auxiliary.saved.t) = 1
	no Selectable_widget.selected.t
}
---------------Generic SAVE Structure ----------
abstract sig New, Open, Save, Saveas, Saves, Cancelsave, Openo, Cancelopen, Encryptb, Backe, Decryptb, Backd, Yes, No, Replace, Noreplace extends Action_widget { }
abstract sig Filename, Password, Repassword, Depassword extends Input_widget { }
abstract sig Opening_list extends Selectable_widget { }

---------------Generic SAVE Semantics---------- 
one sig Auxiliary{
	pwd: Object lone -> Value,
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
}
pred select_fail_post [sw: Selectable_widget, t, t': Time, o: Object] {
	Opening_list.list.t' =  Opening_list.list.t
	(Auxiliary.saved.t') = 	(Auxiliary.saved.t)
}
pred select_pre [sw: Selectable_widget, t: Time, o: Object] {
	
}
pred click_semantics [aw: Action_widget, t: Time] {
	(aw in Save)        => #(Auxiliary.saved.t) = 0
	(aw in Openo)     => #Opening_list.selected.t = 1
	(aw in Saves)      => #Filename.content.t = 1
	(aw in Encryptb)   => #Repassword.content.t = 1 and Password.content.t = Repassword.content.t and valid_data[t]
	(aw in Decryptb) => #Depassword.content.t = 1 and Depassword.content.t = (Opening_list.selected.t).(Auxiliary.pwd)
}
pred click_success_post [aw: Action_widget, t, t': Time] {
	Current_window.is_in.t' = aws.New => (#Input_widget.content.t' = 0 and #Opening_list.selected.t' = 0) 
	not(aw in Saves or aw in Openo) => Current_window.is_in.t' = aw.goes
	(aw in New and aw.goes = aws.New) => new[t,t'] 
	(aw in New and not(aw.goes = aws.New)) => same[t,t']
	(aw in Saves and exisit[t, Filename.content.t]) => (Current_window.is_in.t' = aws.Yes) and same[t,t']
	(aw in Saves and not(exisit[t, Filename.content.t])) => ((#Encryptb = 1) => (Current_window.is_in.t' = aws.Encryptb and same[t,t']) else (save[t,t', none,Filename.content.t]))
	(aw in Yes) => (#Encryptb = 1 => Current_window.is_in.t' = aws.Encryptb and same[t,t'] else save[t,t', none,Filename.content.t])
	(aw in Encryptb) => save[t,t', Password.content.t, Filename.content.t]
	(aw in Decryptb) => openo[t, t']
	(aw in Openo) => (#(Opening_list.selected.t).(Auxiliary.pwd) = 1) => (Current_window.is_in.t' = aws.Decryptb and same[t,t']) else (Current_window.is_in.t' = aws.New and openo[t,t'])
	not(aw in (New+Saves+Openo+Encryptb+Decryptb)) => same[t,t']
}
pred click_fail_post [aw: Action_widget, t, t': Time] {
	(all iw: Input_widget | iw.content.t' = iw.content.(T/first))
	Opening_list.list.t' =  Opening_list.list.t
	Opening_list.selected.t' =  Opening_list.selected.t
	(Auxiliary.saved.t') = 	(Auxiliary.saved.t)
}
pred click_pre [aw: Action_widget, t': Time] {
	
}
pred save [t, t': Time, password, filename: Value] {
	(filename in (Opening_list.list.t).(Auxiliary.names)) => (one o: Object | not(o in Opening_list.list.t) and o.appeared = Auxiliary.names.filename.appeared and o.(Auxiliary.pwd) = password and 	o.(Auxiliary.names) = filename and Opening_list.list.t' = (Opening_list.list.t - (Auxiliary.names).filename)+o) else (one o: Object | not(o in Opening_list.list.t) and o.(Auxiliary.pwd) = password and	o.(Auxiliary.names) = filename and o.appeared = t' and	Opening_list.list.t' = Opening_list.list.t + o)
	#(Auxiliary.saved.t') = 1
	Current_window.is_in.t' = aws.New
}
pred new [t, t': Time] {
	#(Auxiliary.saved.t') = 0
	Opening_list.list.t' =Opening_list.list.t
	Current_window.is_in.t' = aws.New
}
pred openo [t, t': Time] {
	#(Auxiliary.saved.t') = 1
	Opening_list.list.t' =  Opening_list.list.t
	Current_window.is_in.t' = aws.New
}
pred exisit [t: Time, name: Value] {
	name in (Opening_list.list.t).(Auxiliary.names)
}
pred same[t,t': Time]{
	(Auxiliary.saved.t') = 	(Auxiliary.saved.t)
	(all iw:  Input_widget | iw.content.t' = iw.content.t)
	Opening_list.selected.t' = Opening_list.selected.t
	Opening_list.list.t' = Opening_list.list.t
}
pred valid_data [t: Time] {
	(#Password.invalid > 0 and #Password.content.t = 1) => (not(Password.content.t in Password.invalid))
}