package com.android.game.clash_of_the_balls;


import android.util.Log;

import com.android.game.clash_of_the_balls.game.IDrawable;
import com.android.game.clash_of_the_balls.game.IMoveable;
import com.android.game.clash_of_the_balls.game.MatrixStack;

/**
 * UIHandler
 * this class controls which view (menu, game) is currently active and displayed
 *
 */
public class UIHandler implements IDrawable, IMoveable, ITouchInput {
	
	private static final String LOG_TAG = "UIHandler";
	
	
	private GameSettings m_settings;
	private IMoveable m_fps_counter;
	
	private UIBase m_active_ui;
	private int m_screen_width;
	private int m_screen_height;
	
	public UIHandler(int screen_width, int screen_height) {
		m_screen_width = screen_width;
		m_screen_height = screen_height;
		m_settings = new GameSettings();
		m_fps_counter = new FPSCounter();
	}

	@Override
	public void move(float dsec) {
		// TODO Auto-generated method stub
		
		m_fps_counter.move(dsec);
	}

	@Override
	public void draw(MatrixStack stack) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTouchEvent(float x, float y, int event) {
		Log.v(LOG_TAG, "Touch event: x="+x+", y="+y+", event="+event);
		
		if(m_active_ui != null) m_active_ui.onTouchEvent(x, y, event);
		
	}
	
}
