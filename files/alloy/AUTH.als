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
	not(User in Property_unique.uniques)
	not(Password in Property_unique.uniques)
	
	not(User_save in Property_required.requireds)
	not(Password_save in Property_required.requireds)
	not(Re_password in Property_required.requireds)
	not(User_save in Property_unique.uniques)
	not(Password_save in Property_unique.uniques)
	not(Re_password in Property_unique.uniques)
}
---------------Generic AUTH Semantics---------- 
one sig Property_unique{
	uniques: set Input_widget
} 
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
pred select_success_post [sw: Selectable_widget, t, t': Time, o: Object] { 
		//List.elements.t' =  List.elements.t
}
pred select_fail_post [sw: Selectable_widget, t, t': Time, o: Object] { 
		//List.elements.t' =  List.elements.t
}
pred select_pre[sw: Selectable_widget, t: Time, o: Object] { }

pred click_semantics [aw: Action_widget, t: Time] {
	(aw in Login) => filled_login_test [t] and existing_test [t] 
	(aw in Ok) => filled_required_test[t] and unique_fields_test [t] and same_pass_test [t] and valid_data_test [Current_window.is_in.t, t]
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
	all o: List.elements.t | (#o.vs.User_save= 1 => User_save.content.t !=o.vs.User_save) and (#o.vs.Password_save= 1 => Password_save.content.t !=o.vs.Password_save) 
	all iw: Field | all o: List.elements.t | (iw in Property_unique.uniques and (#o.vs.iw= 1)) => iw.content.t !=o.vs.iw 
}
pred valid_data_test [w: Window, t: Time] {
	all iw: w.iws | (#iw.invalid > 0 and #iw.content.t > 0) => not(iw.content.t in iw.invalid)
}