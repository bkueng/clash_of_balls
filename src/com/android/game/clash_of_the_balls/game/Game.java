package com.android.game.clash_of_the_balls.game;


import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.UIBase;
import com.android.game.clash_of_the_balls.UIHandler;
import com.android.game.clash_of_the_balls.game.event.Event;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo.PlayerInfo;
import com.android.game.clash_of_the_balls.network.NetworkClient;
import com.android.game.clash_of_the_balls.network.Networking.AllJoynErrorData;


public class Game extends GameBase implements UIBase {
	private static final String TAG_GAME = "Game";
	
	private SensorThread m_sensor_thread;
	private GameView m_view;
	
	private NetworkClient m_network_client;
	
	private GamePlayer m_own_player;
	
	
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
		
		//TODO: start calibration
		
	}
	
	public void initPlayers(PlayerInfo[] players) {
		super.initPlayers(players);
		
		String own_unique_name = m_network_client.getOwnUniqueName();
		if(own_unique_name != null) {
			for(int i=0; i<players.length; ++i) {
				if(own_unique_name.equals(players[i].unique_name)) {
					m_own_player = (GamePlayer)m_game_objects.get(players[i].id);
					
					Log.d(TAG_GAME, "we got our player at x="+m_own_player.pos().x
							+", y="+m_own_player.pos().y);
				}
			}
		}
		
		if(m_own_player == null) {
			Log.e(TAG_GAME, "could not find own player in players list! This is very bad!");
		}
		
		m_view.setObjectToTrack(m_own_player);
	}
	
	public void onDestroy() {
		if(m_sensor_thread!=null) m_sensor_thread.stopThread();
		m_sensor_thread = null;
	}

	public void onTouchEvent(float x, float y, int event) {
		// that's not used in the game
	}
	
	private boolean m_bReceived_events = false; //send new sensor data if true

	public void move(float dsec) {
		if(m_bIs_game_running) {

			//get sensor values & send to server
			Vector sensor_vec = m_sensor_thread.getCurrentVector();
			if(m_bReceived_events) 
				m_network_client.sensorUpdate(sensor_vec);
			//TODO: apply sensor values to own player

			handleNetworkError(m_network_client.getNetworkError());

			m_network_client.handleReceive();
			if(m_network_client.hasEvents()) {
				//TODO: undo prediction...
				
				//apply the updates from the server
				applyIncomingEvents();
				
				m_bReceived_events = true;
			} else {
				//TODO: do predicted move
				
				m_bReceived_events = false;
			}
			m_game_field.move(dsec);


			m_view.move(dsec);
			m_game_field.move(dsec);
			
		} else {
			m_network_client.handleReceive();
			applyIncomingEvents();
		}
	}
	
	private void applyIncomingEvents() {
		Event e;
		while((e=m_network_client.getNextEvent()) != null) {
			e.apply(this);
		}
	}
	
	private void handleNetworkError(AllJoynErrorData data) {
		if(data != null) {
			//TODO

		}
	}

	public void draw(RenderHelper renderer) {
		
		if(m_view != null && m_game_field != null) {
			m_view.applyView(renderer);

			m_game_field.draw(renderer);
			for (Map.Entry<Short, DynamicGameObject> entry : m_game_objects.entrySet()) {
				entry.getValue().draw(renderer);
			}

			m_view.resetView(renderer);
		}
	}
	
	public void gameStartNow() {
		super.gameStartNow();
		m_bReceived_events = true;
		//TODO
	}
	public void gameEnd() {
		super.gameEnd();
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
