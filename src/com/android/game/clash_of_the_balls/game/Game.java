package com.android.game.clash_of_the_balls.game;


import android.content.Context;
import android.util.Log;

import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.UIBase;
import com.android.game.clash_of_the_balls.UIHandler;


public class Game extends GameBase implements UIBase {
	
	private SensorThread m_sensor_thread;
	
	//own player
	
	public Game(Context c, GameSettings s, TextureManager texture_manager) {
		super(false, s);
		// TODO Auto-generated constructor stub
		
		
		m_sensor_thread=new SensorThread(c);
		m_sensor_thread.startThread();
	}
	
	public void onDestroy() {
		if(m_sensor_thread!=null) m_sensor_thread.stopThread();
		m_sensor_thread = null;
	}

	public void onTouchEvent(float x, float y, int event) {
		// that's not used in the game
	}

	public void move(float dsec) {
		// TODO Auto-generated method stub
		
		//get sensor values & send to server
		
	}

	public void draw(RenderHelper renderer) {
		// TODO Auto-generated method stub
		
	}

	public UIHandler.UIChange UIChange() {
		// TODO Auto-generated method stub
		
		return UIHandler.UIChange.NO_CHANGE;
	}

	public void onActivate() {
		// ignore
	}

	public void onDeactivate() {
		// ignore
	}

}
