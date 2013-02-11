package com.sapos_aplastados.game.clash_of_balls;

import com.sapos_aplastados.game.clash_of_balls.UIHandler.UIChange;
import com.sapos_aplastados.game.clash_of_balls.game.IDrawable;
import com.sapos_aplastados.game.clash_of_balls.game.IMoveable;

public interface UIBase extends ITouchInput, IMoveable, IDrawable {
	
	/* this asks after move if an ui change is requested */
	public UIChange UIChange();
	
	/* called when ui is changed */
	public void onActivate();
	public void onDeactivate();
	
	public void onBackButtonPressed();
}
