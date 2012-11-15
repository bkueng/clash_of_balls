package com.android.game.clash_of_the_balls.game;

/**
 * GameView
 * this is used by the Game to determine the rectangle of the current view
 * if the game level is big, the view is moved around depending on the users
 * position. it manipulates the model view matrix
 *
 */
public class GameView extends GameObject {
	
	private Vector m_size = new Vector();
	//TODO: use user, GameSettings for screen size, scaling

	@Override
	public void draw(MatrixStack stack) {
		// nothing to do
	}

	@Override
	public void move(float dsec) {
		// TODO Auto-generated method stub
		
	}
	
	//applyView(MatrixStack stack)
}
