package com.android.game.clash_of_the_balls;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D.Font2DSettings;
import com.android.game.clash_of_the_balls.Font2D.TextAlign;
import com.android.game.clash_of_the_balls.MainActivity.LoadViewTask;
import com.android.game.clash_of_the_balls.game.Game;
import com.android.game.clash_of_the_balls.game.GameServer;
import com.android.game.clash_of_the_balls.game.IDrawable;
import com.android.game.clash_of_the_balls.game.IMoveable;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.event.Event;
import com.android.game.clash_of_the_balls.menu.CreationMenu;
import com.android.game.clash_of_the_balls.menu.HelpMenu;
import com.android.game.clash_of_the_balls.menu.JoinMenu;
import com.android.game.clash_of_the_balls.menu.MainMenu;
import com.android.game.clash_of_the_balls.menu.MenuBackground;
import com.android.game.clash_of_the_balls.menu.PopupBase;
import com.android.game.clash_of_the_balls.menu.ResultsMenu;
import com.android.game.clash_of_the_balls.menu.WaitMenu;
import com.android.game.clash_of_the_balls.network.NetworkClient;
import com.android.game.clash_of_the_balls.network.NetworkServer;
import com.android.game.clash_of_the_balls.network.Networking;

/**
 * UIHandler
 * this class controls which view (menu, game) is currently active and displayed
 *
 */
public class UIHandler implements IDrawable, IMoveable, ITouchInput {
	
	private static final String LOG_TAG = "UIHandler";
	
	public static final String PREFS_NAME = "clash_of_balls_prefs.7834727";
	public static final String USER_NAME_KEY = "user_name";
	
	private GameSettings m_settings;
	private Context m_activity_context;
	private IMoveable m_fps_counter;
	private TextureManager m_tex_manager;
	private LevelManager m_level_manager;
	private NetworkClient m_network_client;
	private NetworkServer m_network_server;
	
	private Font2DSettings m_font_settings;
	private Typeface m_font_typeface;
	private TextAlign m_font_align;
	private int m_font_color;
	
	private UIBase m_active_ui;
	
	private UIBase m_main_menu;
	private UIBase m_creation_menu_ui;
	private UIBase m_wait_menu_ui;
	private UIBase m_join_menu_ui;
	private UIBase m_results_menu_ui;
	private UIBase m_help_menu_ui;
	private Game m_game_ui;
	
	private MenuBackground m_main_menu_background;
	private MenuBackground m_normal_menu_background;
	
	private boolean m_back_button_pressed = false;
	
	private PopupBase m_cur_popup = null;
	
	GameServer m_game_server;
	
	public enum UIChange {
		NO_CHANGE,
		MAIN_MENU,
		CREATION_MENU,
		WAIT_MENU,
		JOIN_MENU,
		HELP_MENU,
		
		GAME_START_CLIENT,
		GAME_ROUND_END,
		GAME_END,
		GAME_START_SERVER,
		GAME_ABORT, //in case of an error -> also abort the server
		
		/* popup menus */
		POPUP_SHOW, //set the GameSettings.popup_menu variable to show a popup
		POPUP_HIDE,
		POPUP_RESULT_BUTTON1
	}
	
	public UIHandler(int screen_width, int screen_height
			, Context activity_context, LoadViewTask progress_view) {
		
		progress_view.setProgress(5);
		
		m_settings = new GameSettings();
		m_fps_counter = new FPSCounter();
		m_activity_context = activity_context;
		m_tex_manager = new TextureManager(m_activity_context);
		onSurfaceChanged(screen_width, screen_height);
		
		//load username from file
		SharedPreferences settings = m_activity_context.getSharedPreferences(PREFS_NAME, 0);
		m_settings.user_name = settings.getString(USER_NAME_KEY, "");
		
		m_level_manager = new LevelManager(m_activity_context);
		m_level_manager.loadLevels();
		
		m_network_client = new NetworkClient(Networking.getInstance());
		m_network_server = new NetworkServer(Networking.getInstance());
		
		// Initialize Font2D.Font2DSetting for all menus
		m_font_typeface = Typeface.createFromAsset(m_activity_context.getAssets(),  "alphafridgemagnets.ttf");
		m_font_color = 0xddeeeeff;
		int label_font_color = 0xddcccccc;
		m_font_align = Font2D.TextAlign.CENTER;
		m_font_settings = new Font2D.Font2DSettings(m_font_typeface, m_font_align, m_font_color);
		
		progress_view.setProgress(20);
		
		//Main Menu
		m_main_menu_background = new MenuBackground(
				m_tex_manager.get(R.raw.texture_main_menu_bg),1600.f/960.f);
		m_main_menu = new MainMenu(m_main_menu_background
				, screen_width, screen_height,m_tex_manager,m_activity_context
				, m_font_settings, m_settings);
		
		progress_view.setProgress(30);
		
		//Creation Menu
		m_normal_menu_background = new MenuBackground(
				m_tex_manager.get(R.raw.texture_bg_normal),1600.f/960.f);
		m_creation_menu_ui = new CreationMenu(m_normal_menu_background
				, screen_width, screen_height, m_tex_manager, m_settings
				, m_activity_context, m_font_settings, label_font_color
				, m_level_manager, m_network_server);
		
		progress_view.setProgress(40);
		
		//Wait Menu
		m_wait_menu_ui = new WaitMenu(m_normal_menu_background
				, screen_width, screen_height,m_tex_manager
				, m_settings,m_activity_context, m_font_settings
				, label_font_color
				, Networking.getInstance(), m_network_client);
		
		progress_view.setProgress(50);
		
		//Join Menu
		m_join_menu_ui = new JoinMenu(m_normal_menu_background
				, screen_width, screen_height,m_tex_manager
				, m_activity_context, m_font_settings, label_font_color
				, m_settings, m_network_client);
		
		progress_view.setProgress(60);
		
		//Result Menu
		m_results_menu_ui = new ResultsMenu(m_normal_menu_background
				, screen_width, screen_height,m_tex_manager
				, m_settings,m_activity_context, m_font_settings
				, Networking.getInstance(), m_network_client);
		
		progress_view.setProgress(70);
		
		//Help Menu
		m_help_menu_ui = new HelpMenu(m_normal_menu_background
				, screen_width, screen_height,m_tex_manager
				, m_activity_context, m_font_settings, label_font_color
				, m_settings, m_network_client);
		
		
		//Game
		m_game_ui = new Game(m_activity_context, m_settings, m_tex_manager
				, m_network_client, m_font_settings);
		
		m_game_server = new GameServer(m_settings, Networking.getInstance()
				, m_network_server);
		
		
		m_active_ui = m_main_menu; //show main menu
		
		progress_view.setProgress(100); //finish progress bar
	}
	
	public void onSurfaceChanged(int width, int height) {
		m_settings.m_screen_width = width;
		m_settings.m_screen_height = height;
		m_tex_manager.reloadAllTextures();
	}
	
    //handle back button (called from another thread!)
    public boolean onBackPressed() {
    	//only do minimal stuff and return immediately
    	if(m_active_ui == m_main_menu) return false;
    	m_back_button_pressed = true;
    	return true;
    }

	public void move(float dsec) {

		if(m_active_ui != null) {
			m_active_ui.move(dsec);
			
			if(m_back_button_pressed) {
				m_back_button_pressed = false;
				m_active_ui.onBackButtonPressed();
			}
			
			if(m_cur_popup != null) m_cur_popup.move(dsec);

			switch(m_active_ui.UIChange()) {
			case GAME_START_CLIENT: startGameClient();
			break;
			case GAME_ROUND_END: handleGameRoundEnded();
			break;
			case GAME_END: handleGameEnded();
			break;
			case GAME_START_SERVER: startGameServer();
			break;
			case GAME_ABORT: handleGameAbort();
			break;
			case CREATION_MENU: uiChange(m_active_ui,m_creation_menu_ui);
			break;
			case WAIT_MENU: uiChange(m_active_ui,m_wait_menu_ui);
			break;
			case JOIN_MENU: uiChange(m_active_ui,m_join_menu_ui);
			break;
			case HELP_MENU: uiChange(m_active_ui,m_help_menu_ui);
			break;
			case MAIN_MENU: uiChange(m_active_ui, m_main_menu);
			break;
			case POPUP_SHOW: showPopup();
			break;
			case POPUP_HIDE: hidePopup();
			break;
			case POPUP_RESULT_BUTTON1: assert(false); //a menu should not return this
			case NO_CHANGE: //nothing to do
			}
		}
		
		m_fps_counter.move(dsec);
	}
	
	private void uiChange(UIBase old_ui, UIBase new_ui) {
		if(old_ui != new_ui) {
			old_ui.onDeactivate();
			new_ui.onActivate();
			m_active_ui = new_ui;
			hidePopup();
		}
	}
	
	private void showPopup() {
		assert(m_settings.popup_menu!=null);
		m_cur_popup = m_settings.popup_menu;
		m_settings.popup_menu = null;
	}
	private void hidePopup() {
		m_cur_popup = null;
	}
	
	//initialize & run the server with the selected level & show the game
	private void startGameServer() {
		m_settings.game_statistics.resetRoundStatistics();
		
		if(m_settings.selected_level != null) {
			m_game_server.startThread();
			m_game_server.initGame(m_settings.selected_level);
			m_game_server.startGame();
			uiChange(m_active_ui, m_game_ui);
		} else {
			Log.e(LOG_TAG, "Trying to start server but the level is not set! cannot start server!");
		}
	}
	
	private void startGameClient() {
		m_settings.game_statistics.resetRoundStatistics();
		uiChange(m_active_ui, m_game_ui);
	}
	
	//this is called in case of a networking error
	//it can also happen before starting the game!
	private void handleGameAbort() {
		Log.i(LOG_TAG, "Game abort call");
		
		Networking networking = Networking.getInstance();
		if(m_settings.is_host) networking.stopAdvertise();
		networking.leaveSession();
		
		m_game_server.stopThread();
		networking.resetErrors();
		
		uiChange(m_active_ui, m_main_menu);
	}
	
	private void handleGameRoundEnded() {
		Log.i(LOG_TAG, "Game round "+m_settings.game_current_round+"/"+
				m_settings.game_rounds + " ended");
		
		++m_settings.game_current_round;
		
		uiChange(m_active_ui, m_results_menu_ui);
	}
	
	private void handleGameEnded() {
		
		m_game_server.stopThread();
		
		Networking networking = Networking.getInstance();
		if(m_settings.is_host) networking.stopAdvertise();
		networking.leaveSession();
		networking.resetErrors();
		
		uiChange(m_active_ui, m_main_menu);
	}
	
	public void onDestroy() {
		if(m_game_ui!=null) m_game_ui.onDestroy();
		m_game_ui=null;
		if(m_game_server != null) m_game_server.stopThread();
		
		//store username to a file
		SharedPreferences settings = m_activity_context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(USER_NAME_KEY, m_settings.user_name);
		editor.commit();

	}

	public void draw(RenderHelper renderer) {
		if(m_active_ui != null) m_active_ui.draw(renderer);
		if(m_cur_popup != null) m_cur_popup.draw(renderer);
	}

	public void onTouchEvent(float x, float y, int event) {
		Log.v(LOG_TAG, "Touch event: x="+x+", y="+y+", event="+event);
		
		if(m_cur_popup != null) {
			m_cur_popup.onTouchEvent(x, y, event);
		} else {
			if(m_active_ui != null) m_active_ui.onTouchEvent(x, y, event);
		}
		
	}
	
}
