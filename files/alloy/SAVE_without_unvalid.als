--------------------Initial State---------------
pred init [t: Time] {
	no Track.op.t
 	Current_window.is_in.t = aws.New
	#Saving_list.list.t = 0
}
---------------Generic SAVE Structure ----------
abstract sig New, Open, Save, Saveas, Saves, Cancelsave, Openo, Cancelopen, Encryptb, Backe, Decryptb, Backb, Yes, No, Replace, Noreplace extends Action_widget { }
abstract sig Filename, Password, Repassword, Depassword extends Input_widget { }
abstract sig Saving_list, Opening_list extends Selectable_widget { }

fact {
	Saving_list.list = Opening_list.list
}
---------------Generic SAVE Semantics---------- 
one sig Auxiliary{
	pwd: Object lone -> Value,
	names: Object one -> Value,
	saved: Time lone -> Value
}

pred fill_semantics [iw: Input_widget, t: Time, v: Value] { }
pred fill_success_post [iw: Input_widget, t, t': Time, v: Value] {  
		Saving_list.list.t' =  Saving_list.list.t
		t'.(Auxiliary.saved) = 	t.(Auxiliary.saved)
}
pred fill_fail_post [iw: Input_widget, t, t': Time, v: Value] {
		Saving_list.list.t' =  Saving_list.list.t
		t'.(Auxiliary.saved) = 	t.(Auxiliary.saved)
}
pred fill_pre[iw: Input_widget, t: Time, v: Value] { }

pred select_semantics [sw: Selectable_widget, t: Time, o: Object] { }
pred select_success_post [sw: Selectable_widget, t, t': Time, o: Object] { 
		Saving_list.list.t' =  Saving_list.list.t
		t'.(Auxiliary.saved) = 	t.(Auxiliary.saved)
}
pred select_fail_post [sw: Selectable_widget, t, t': Time, o: Object] { 
		Saving_list.list.t' =  Saving_list.list.t
		t'.(Auxiliary.saved) = 	t.(Auxiliary.saved)
}
pred select_pre[sw: Selectable_widget, t: Time, o: Object] { }

pred click_semantics [aw: Action_widget, t: Time] {
	(aw in Save)        => #t.(Auxiliary.saved) = 0
	(aw in Openo)     => #Opening_list.selected.t = 1
	(aw in Saves)      => #Filename.content.t = 1
	(aw in Encryptb)   => #Repassword.content.t = 1 and Password.content.t = Repassword.content.t
	(aw in Decryptb) => #Depassword.content.t = 1 and Depassword.content.t = (Opening_list.selected.t).(Auxiliary.pwd)
}
pred click_success_post [aw: Action_widget, t, t': Time] {
	Current_window.is_in.t' = aws.New => #Input_widget.content.t' = 0 and #Selectable_widget.selected.t' = 0 else all iw:  Input_widget | iw.content.t' = iw.content.t and all sw: Selectable_widget | sw.selected.t' = sw.selected.t
	not(aw in Saves or aw in Openo) => Current_window.is_in.t' = aw.goes
	(aw in New) => new[t,t']
	(aw in Saves and exisit[t, Filename.content.t]) => (Current_window.is_in.t' = aws.Yes and same[t, t'])
	(aw in Saves and not(exisit[t, Filename.content.t])) => (#Encryptb = 1 => Current_window.is_in.t' = aws.Encryptb and same[t,t'] else  save[t,t', none,Filename.content.t])
	(aw in Yes) => (#Encryptb = 1 => Current_window.is_in.t' = aws.Encryptb and same[t,t'] else save[t,t', none,Filename.content.t])
	(aw in Encryptb) => save[t,t', Password.content.t, Filename.content.t]
	(aw in Decryptb) => openo[t, t']
	(aw in Openo) => (#(Opening_list.selected.t).(Auxiliary.pwd) = 1) => Current_window.is_in.t' = aws.Decryptb and same[t,t'] else Current_window.is_in.t' = aws.New and 	openo[t,t'] 
}
pred click_fail_post [aw: Action_widget, t, t': Time]	{
	(all iw: Input_widget | iw.content.t' = iw.content.(T/first))
	Saving_list.list.t' =  Saving_list.list.t
	t'.(Auxiliary.saved) = 	t.(Auxiliary.saved)
}
pred click_pre[aw: Action_widget, t': Time] { }

pred save [t,t': Time, password: Value, filename: Value] {
	(filename in (Saving_list.list.t).(Auxiliary.names)) => (one o: Object | not(o in Saving_list.list.t) and o.appeared = Auxiliary.names.filename.appeared and o.(Auxiliary.pwd) = password and 	o.(Auxiliary.names) = filename and Saving_list.list.t' = (Saving_list.list.t - (Auxiliary.names).filename)+o) else (one o: Object | not(o in Saving_list.list.t) and o.(Auxiliary.pwd) = password and	o.(Auxiliary.names) = filename and o.appeared = t' and	Saving_list.list.t' = Saving_list.list.t + o)
	#t'.(Auxiliary.saved) = 1
	Current_window.is_in.t' = aws.New
}
pred new [t,t': Time] {
	#t'.(Auxiliary.saved) = 0
	Saving_list.list.t' =  Saving_list.list.t
}
pred openo [t,t': Time] {
	#t'.(Auxiliary.saved) = 0
	Saving_list.list.t' =  Saving_list.list.t
}
pred exisit[t: Time, name: Value]{
	name in (Saving_list.list.t).(Auxiliary.names)
}
pred same[t,t': Time]{
		Saving_list.list.t' =  Saving_list.list.t
		t'.(Auxiliary.saved) = 	t.(Auxiliary.saved)
}