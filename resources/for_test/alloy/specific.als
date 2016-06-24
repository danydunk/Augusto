one sig Initial_w extends Initial {}
one sig Form_w extends Form {}

one sig Trigger_aw extends Trigger {}
one sig Ok_aw extends Ok {}
one sig Cancel_aw extends Cancel {}

one sig Input_iw extends Input_widget {}

fact {
	Trigger_aw.goes = Form_w
	#Ok_aw.goes = 0
	Cancel_aw.goes = Initial_w
	Form_w.iws = Input_iw
	Form_w.aws = Ok_aw+Cancel_aw
	Initial_w.aws = Trigger_aw
	#Input_widget = #Input_iw
	#Action_widget = #Ok_aw + #Cancel_aw + #Trigger_aw
	#Window = #Form_w + #Initial_w
}
