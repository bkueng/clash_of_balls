package com.android.game.clash_of_the_balls;

import android.view.MotionEvent;

public interface ITouchInput {
	/* x, y are screen coordinates ([0,0] is the bottom left display corner) */
	public void onTouchEvent(float x, float y, MotionEvent event);
}
