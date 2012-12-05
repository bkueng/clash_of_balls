package com.android.game.clash_of_the_balls.game;


import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.UIBase;
import com.android.game.clash_of_the_balls.UIHandler;
import com.android.game.clash_of_the_balls.Font2D.Font2DSettings;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;
import com.android.game.clash_of_the_balls.game.event.Event;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo.PlayerInfo;
import com.android.game.clash_of_the_balls.menu.PopupBase;
import com.android.game.clash_of_the_balls.menu.PopupGameStart;
import com.android.game.clash_of_the_balls.menu.PopupMsg;
import com.android.game.clash_of_the_balls.network.NetworkClient;
import com.android.game.clash_of_the_balls.network.Networking;
import com.android.game.clash_of_the_balls.network.Networking.AllJoynError;
import com.android.game.clash_of_the_balls.network.Networking.AllJoynErrorData;
import com.android.game.clash_of_the_balls.network.Networking.ConnectedClient;


public class Game extends GameBase implements UIBase {
	private static final String TAG_GAME = "Game";
	
	private SensorThread m_sensor_thread;
	private GameView m_view;
	
	private UIHandler.UIChange m_ui_change;
	private PopupBase m_error_popup = null;
	private Context m_activity_context;
	private Font2DSettings m_font_settings;
	
	private NetworkClient m_network_client;
	
	private float m_calibration_timeout=0.f; //[sec]
	
	
	public Game(Context c, GameSettings s, TextureManager texture_manager, 
			NetworkClient network_client, Font2DSettings font_settings) {
		super(false, s, texture_manager);
		
		m_sensor_thread=new SensorThread(c);
		m_activity_context = c;
		m_font_settings = font_settings;
		m_sensor_thread.startThread();
		m_network_client = network_client;
		m_ui_change = UIHandler.UIChange.NO_CHANGE;
	}
	
	public void initGame(GameLevel level) {
		super.initGame(level);
		
		//after this call (and initPlayers) we should have wait_to_start_game 
		//seconds until the game starts
		
		//view: save & restore scaling if it exists
		float scaling = -1.f;
		float old_width=0.f, old_height=0.f;
		if(m_view != null) {
			scaling = m_view.getZoomToTileSize();
			old_width = m_view.levelWidth();
			old_height = m_view.levelHeight();
		}
		m_view = new GameView(m_settings.m_screen_width, m_settings.m_screen_height, 
				null, (float)level.width, (float)level.height);
		if(scaling > 0.f && old_width==m_view.levelWidth() && old_height==m_view.levelHeight())
			m_view.setZoomToTileSize(scaling);
		
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
				//set network client id
				for(int k=0; k<m_network_client.getConnectedClientCount(); ++k) {
					ConnectedClient client=m_network_client.getConnectedClient(k);
					if(client!=null && client.unique_id.equals(players[i].unique_name)) {
						client.id = players[i].id;
					}
				}
			}
		}
		
		if(m_own_player == null) {
			Log.e(TAG_GAME, "could not find own player in players list! This is very bad!");
		}
		
		m_view.setObjectToTrack(m_own_player);
		
		//start calibration
		m_calibration_timeout = (float)wait_to_start_game - 1.f;
		
		//show game start popup
		if(m_error_popup == null) {
			m_settings.popup_menu = new PopupGameStart(m_activity_context
					, m_texture_manager, m_settings.m_screen_width, m_settings.m_screen_height
					, (float)wait_to_start_game, m_own_player.color()
					, m_font_settings.m_typeface);
			m_ui_change = UIChange.POPUP_SHOW;
		}
	}
	
	public void onDestroy() {
		if(m_sensor_thread!=null) m_sensor_thread.stopThread();
		m_sensor_thread = null;
	}

	public void onTouchEvent(float x, float y, int event) {
		// that's not used in the game
	}
	
	private boolean m_bReceived_events = false; //send new sensor data if true
	private float m_time_since_last_data_receive=0.f; //[sec]
	private float m_last_rtt; //[sec] round time trip in sec
	private static final float network_receive_timeout = 4.f; //[sec]
	private Vector m_last_sensor_update;

	public void move(float dsec) {
		if(m_error_popup != null) {
			//check for button pressed
			if(m_error_popup.UIChange() == UIChange.POPUP_RESULT_BUTTON1) {
				gameAbort();
				m_error_popup = null;
			}
		} else {
			//calibration
			if(m_calibration_timeout > 0.f) {
				m_calibration_timeout -= dsec;
				if(m_calibration_timeout <= 0.f) {
					m_sensor_thread.calibrate();
				}
			}

			if(m_bIs_game_running) {

				//get sensor values & send to server
				Vector sensor_vec = m_sensor_thread.getCurrentVector();
				if(m_bReceived_events) {
					m_network_client.sensorUpdate(sensor_vec);
					m_last_sensor_update = sensor_vec;
				}
				//TODO: apply sensor values to own player
				
				handleNetworkError(m_network_client.getNetworkError());

				m_network_client.handleReceive();
				boolean has_network_events = m_network_client.hasEvents();
				m_bReceived_events = has_network_events;
				if(has_network_events) {
					
					//we have a synchronization update from the server
					//we assume that all predicted events (game moves) 
					//can simply be overwritten by the server update

					generate_events = false;
					//go back 1/2 RTT
					super.move(-m_last_rtt/2.f);
					
					//apply the updates from the server
					applyIncomingEvents();
					
					//move forward 1/2 RTT
					//the incoming events happend about 1/2 RTT in the past
					//so we move the game foreward by 1/2 RTT to be at the 
					//correct time again
					super.move(m_last_rtt/2.f);
					doCollisionHandling();
					applyMove();
					
					removeDeadObjects();
					
					m_last_rtt = m_time_since_last_data_receive;
					m_time_since_last_data_receive = 0.f;
				}
				
				//do a predicted move
				generate_events = false;
				
				/* 
				 * Undo Predicted Events
				 * ---------------------
				 * for the future if we want to be able to undo predicted events:
				 * - add member boolean m_is_predicting
				 * - use a separate Event queue & check in addEvent if m_is_predicting
				 * - add an undo method in Event class
				 * - before applying network events: undo all Events in the queue
				 * - add here:
					generate_events = true;
					m_is_predicting = true;
				 */
				
				//move the client stuff like animation
				//this can be done in any case, because this does not to be
				//strictly synchronized with the server
				moveClient(dsec);
				
				if(!has_network_events) {
					super.move(dsec);
					doCollisionHandling();
					super.applyMove();
				}
				
				
				m_game_field.move(dsec);


				m_view.move(dsec);
				m_game_field.move(dsec);
				
				//we expect the sensor update to be received by the server
				//1/2 RTT after sending, so we try to apply the sensor update
				//at the same time as the server
				if(m_time_since_last_data_receive < m_last_rtt/2.f
						&& m_time_since_last_data_receive+dsec >= m_last_rtt/2.f)
					m_own_player.applySensorVector(m_last_sensor_update);
				
				
				//check for receive timeout
				if((m_time_since_last_data_receive+=dsec) > network_receive_timeout) {
					AllJoynErrorData error = new AllJoynErrorData();
					error.error_string = "";
					error.error = AllJoynError.RECEIVE_TIMEOUT;
					handleNetworkError(error);
				}

			} else {
				m_network_client.handleReceive();
				applyIncomingEvents();
			}
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
			//this is bad: here it's very difficult to recover, so we 
			//show a message to the user and abort the game
			
			switch(data.error) {
			case CONNECT_ERROR:
			case JOIN_SESSION_ERROR:
			case SEND_ERROR:
			case BUS_EXCEPTION:
			case RECEIVE_TIMEOUT:
				m_settings.popup_menu = m_error_popup = new PopupMsg(m_activity_context 
						, m_texture_manager, m_settings.m_screen_width
						, m_settings.m_screen_height 
						, m_font_settings.m_typeface, m_font_settings.m_color
						, "Error", Networking.getErrorMsgMultiline(data.error), "Ok");
				m_ui_change = UIChange.POPUP_SHOW;
			}
			
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
		m_time_since_last_data_receive = 0.f;
		m_sensor_thread.stopCalibrate();
		m_ui_change = UIChange.POPUP_HIDE;
	}
	public void gameEnd() {
		super.gameEnd();
		m_ui_change = UIHandler.UIChange.GAME_ROUND_END;
	}
	
	public int getNextSequenceNum() {
		return -1; //only the server generates sequence numbers
	}

	public UIHandler.UIChange UIChange() {
		UIHandler.UIChange ret = m_ui_change;
		m_ui_change = UIChange.NO_CHANGE;
		return ret;
	}

	public void onActivate() {
		// ignore
	}

	public void onDeactivate() {
		gameEnd();
		m_ui_change = UIHandler.UIChange.NO_CHANGE;
	}
	
	public String getUniqueNameFromPlayerId(short player_id) {
		throw new RuntimeException("getUniqueNameFromPlayerId should not be called inside Game object");
	}
	
	private void gameAbort() {
		m_bIs_game_running = false;
		m_ui_change = UIChange.GAME_ABORT;
	}
	
	public void onBackButtonPressed() {
		//ok the user wants it this way: abort the game
		gameAbort();
	}
}
