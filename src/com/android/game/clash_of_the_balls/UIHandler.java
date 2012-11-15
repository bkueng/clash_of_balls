package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.game.IDrawable;
import com.android.game.clash_of_the_balls.game.IMoveable;
import com.android.game.clash_of_the_balls.game.MatrixStack;

/**
 * UIHandler
 * this class controls which view (menu, game) is currently active and displayed
 *
 */
public class UIHandler implements IDrawable, IMoveable {
	
	private GameSettings m_settings;
	
	private int m_screen_width;
	private int m_screen_height;
	
	public UIHandler(int screen_width, int screen_height) {
		m_screen_width = screen_width;
		m_screen_height = screen_height;
		m_settings = new GameSettings();
	}

	@Override
	public void move(float dsec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(MatrixStack stack) {
		// TODO Auto-generated method stub
		
	}
	
}
