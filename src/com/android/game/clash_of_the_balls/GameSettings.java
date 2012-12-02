package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.game.GameStatistics;
import com.android.game.clash_of_the_balls.menu.PopupBase;

/**
 * Settings
 * This stores all user settings
 *
 */
public class GameSettings {
	public static final boolean debug = true; //debugging variable
						//this allows us some special things for testing if true
						//eg starting a game without any connected clients
	
	public String user_name="";
	
	public boolean is_host=false;
	
	public int game_rounds;
	public int game_current_round; //[1-game_rounds]
	public boolean isGameFinished() { return game_current_round > game_rounds; }
	public GameLevel selected_level=null;
	
	public int m_screen_width;
	public int m_screen_height;
	
	public PopupBase popup_menu; //if an ui wants to show a popup
					// it sets this variable and returns POPUP_SHOW in UIChange()
	
	public GameStatistics game_statistics = new GameStatistics();
}
