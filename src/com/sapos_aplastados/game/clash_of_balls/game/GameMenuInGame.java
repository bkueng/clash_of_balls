package com.sapos_aplastados.game.clash_of_balls.game;

import android.content.Context;
import android.util.Log;

import com.sapos_aplastados.game.clash_of_balls.Font2D;
import com.sapos_aplastados.game.clash_of_balls.GameSettings;
import com.sapos_aplastados.game.clash_of_balls.TextureManager;
import com.sapos_aplastados.game.clash_of_balls.Font2D.Font2DSettings;
import com.sapos_aplastados.game.clash_of_balls.UIHandler.UIChange;
import com.sapos_aplastados.game.clash_of_balls.game.event.Event;
import com.sapos_aplastados.game.clash_of_balls.menu.GameMenuBase;
import com.sapos_aplastados.game.clash_of_balls.menu.MenuBackground;
import com.sapos_aplastados.game.clash_of_balls.menu.PopupMsg;
import com.sapos_aplastados.game.clash_of_balls.network.NetworkClient;
import com.sapos_aplastados.game.clash_of_balls.network.Networking;
import com.sapos_aplastados.game.clash_of_balls.network.Networking.AllJoynErrorData;

/**
 * base class for a menu that is shown during a game is running
 * it's single purpose is to increase code sharing
 * it shows a popup message when a networking error happened
 *
 */
public abstract class GameMenuInGame extends GameMenuBase {
	
	private static final String LOG_TAG = "GameMenuInGame";
	
	protected PopupMsg m_error_popup = null;
	
	protected TextureManager m_tex_manager;
	
	protected Networking m_networking;
	protected NetworkClient m_network_client;
	
	protected GameSettings m_settings;
	protected Font2DSettings m_font_settings;
	

	public GameMenuInGame(MenuBackground background
			, Context context, GameSettings game_settings, Networking networking
			, NetworkClient network_client, TextureManager tex_manager
			, Font2D.Font2DSettings font_settings) {
		super(background, context);

		m_networking = networking;
		m_network_client = network_client;
		m_settings = game_settings;
		m_tex_manager = tex_manager;
		
		m_font_settings = font_settings;
	}
	
	public void move(float dsec) {
		if(m_error_popup != null) {
			//check for button pressed
			if(m_error_popup.UIChange() == UIChange.POPUP_RESULT_BUTTON1) {
				m_ui_change = UIChange.GAME_ABORT;
				m_error_popup = null;
			}
		} else {
			
			//check for game start signal if we are not the game creator
			if(!m_settings.is_host) {
				m_network_client.handleReceive();
				Event e;
				while((e = m_network_client.peekNextEvent()) != null) {
					if(e.type == Event.type_game_info) {
						onGameStart();
						m_ui_change = UIChange.GAME_START_CLIENT;
						break;
					} else {
						Log.w(LOG_TAG, "we received an unwanted game event from the server (type="
							+ (int)e.type+"). We ignore it");
						m_network_client.getNextEvent(); //discard event
					}
				}
			}
			
			handleNetworkError(m_network_client.getNetworkError());
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
				m_settings.popup_menu = m_error_popup = new PopupMsg(m_activity_context 
						, m_tex_manager, m_settings.m_screen_width
						, m_settings.m_screen_height 
						, m_font_settings.m_typeface, m_font_settings.m_color
						, "Error", Networking.getErrorMsgMultiline(data.error), "Ok");
				m_ui_change = UIChange.POPUP_SHOW;
			}
			
		}
	}
	
	protected void onGameStart() {
		//nothing to do
	}
	
}
