package com.android.game.clash_of_the_balls.game;


import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.UIBase;
import com.android.game.clash_of_the_balls.UIHandler;
import com.android.game.clash_of_the_balls.network.NetworkClient;


public class Game extends GameBase implements UIBase {
	
	private SensorThread m_sensor_thread;
	private GameView m_view;
	
	private NetworkClient m_network_client;
	
	/*
	TODO 
	own player -> if set: set m_view.setObjectToTrack
	
	*/
	
	public Game(Context c, GameSettings s, TextureManager texture_manager, 
			NetworkClient network_client) {
		super(false, s, texture_manager);
		
		m_sensor_thread=new SensorThread(c);
		m_sensor_thread.startThread();
		m_network_client = network_client;
	}
	
	public void initGame(GameLevel level) {
		super.initGame(level);
		
		//view: save & restore scaling if it exists
		float scaling = -1.f;
		if(m_view != null) scaling = m_view.getZoomToTileSize();
		m_view = new GameView(m_settings.m_screen_width, m_settings.m_screen_height, 
				null, (float)level.width, (float)level.height);
		if(scaling > 0.f) m_view.setZoomToTileSize(scaling);
		
	}
	
	public void onDestroy() {
		if(m_sensor_thread!=null) m_sensor_thread.stopThread();
		m_sensor_thread = null;
	}

	public void onTouchEvent(float x, float y, int event) {
		// that's not used in the game
	}

	public void move(float dsec) {
		
		//get sensor values & send to server
		
		//receive server updates
		Vector sensor_vec = m_sensor_thread.getCurrentVector();
		
		//receive network errors
		
		//has updates? -> apply them
		//else: move normally
		m_game_field.move(dsec);
		
		
		m_view.move(dsec);
	}

	public void draw(RenderHelper renderer) {
		
		m_view.applyView(renderer);
		
		m_game_field.draw(renderer);
		for (Map.Entry<Short, DynamicGameObject> entry : m_game_objects.entrySet()) {
			entry.getValue().draw(renderer);
		}
		
		m_view.resetView(renderer);
	}
	
	public void gameStartNow() {
		//TODO
	}
	public void gameEnd() {
		//TODO
	}
	
	public int getNextSequenceNum() {
		return -1; //only the server generates sequence numbers
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
	
	public String getUniqueNameFromPlayerId(short player_id) {
		throw new RuntimeException("getUniqueNameFromPlayerId should not be called inside Game object");
	}
}
