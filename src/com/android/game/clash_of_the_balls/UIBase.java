package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.UIHandler.UIChange;
import com.android.game.clash_of_the_balls.game.IDrawable;
import com.android.game.clash_of_the_balls.game.IMoveable;

public interface UIBase extends ITouchInput, IMoveable, IDrawable {
	
	/* this asks after move if an ui change is requested */
	public UIChange UIChange();
	
	/* called when ui is changed */
	public void onActivate();
	public void onDeactivate();
}
