package com.sapos_aplastados.game.clash_of_balls;


public interface ITouchInput {
	/* x, y are screen coordinates ([0,0] is the bottom left display corner) 
	 * event is one of: 
	 *  MotionEvent.ACTION_DOWN
	 *  MotionEvent.ACTION_MOVE
	 *  MotionEvent.ACTION_UP
	 */
	public void onTouchEvent(float x, float y, int event);
}
