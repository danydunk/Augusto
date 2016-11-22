one sig Window_w1 extends Initial {}
one sig Window_w2 extends Form {}

one sig Action_widget_aw1 extends Create_trigger {}
one sig Action_widget_aw2 extends Delete_trigger {}
one sig Action_widget_aw3 extends Read_trigger {}
one sig Action_widget_aw4 extends Update_trigger {}
one sig Action_widget_aw5 extends Ok {}
one sig Action_widget_aw6 extends Cancel {}
one sig Selectable_widget_sw1 extends Selectable_widget{}

one sig Input_widget_iw1 extends Input_widget {}

fact {
	Window_w1.sws = Selectable_widget_sw1 
	Action_widget_aw1.goes = Window_w2
	Action_widget_aw5.goes = Window_w1
	Action_widget_aw6 .goes =  Window_w1
	Window_w2.iws = Input_widget_iw1
	Window_w2.aws = Action_widget_aw5 +Action_widget_aw6
	Window_w1.aws = (Action_widget_aw1+Action_widget_aw2+Action_widget_aw3+Action_widget_aw4)
	#Action_widget_aw2.goes = 0
	#Action_widget_aw3.goes = 0
	#Action_widget_aw4.goes = 0
	#Action_widget = 6
	#Window = 2
}
