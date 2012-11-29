package com.android.game.clash_of_the_balls;

/**
 * Settings
 * This stores all user settings
 *
 */
public class GameSettings {
	public static final boolean debug = false; //debugging variable
						//this allows us some special things for testing if true
						//eg starting a game without any connected clients
	
	public String user_name="";
	
	public boolean is_host=false;
	
	public int game_rounds;
	public GameLevel selected_level=null;
	
	public int m_screen_width;
	public int m_screen_height;
}
