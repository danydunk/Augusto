one sig Window_initial_w extends Initial {}
one sig Window_form_w extends Form {}

one sig Trigger_aw extends Trigger {}
one sig Ok_aw extends Ok {}
one sig Cancel_aw extends Cancel {}

one sig Input_iw extends Input_widget {}

fact {
	Trigger_aw.goes = Window_form_w
	Ok_aw.goes = Window_initial_w
	Cancel_aw.goes =  Window_initial_w
	Window_form_w.iws = Input_iw
	Window_form_w.aws = Ok_aw+Cancel_aw
	Window_initial_w.aws = Trigger_aw
	#Input_widget = #Input_iw
	#Action_widget = #Ok_aw + #Cancel_aw + #Trigger_aw
	#Window = #Window_form_w + #Window_initial_w
}
