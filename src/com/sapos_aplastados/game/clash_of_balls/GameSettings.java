/*
 * Copyright (C) 2012-2013 Hans Hardmeier <hanshardmeier@gmail.com>
 * Copyright (C) 2012-2013 Andrin Jenal
 * Copyright (C) 2012-2013 Beat Küng <beat-kueng@gmx.net>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
package com.sapos_aplastados.game.clash_of_balls;

import com.sapos_aplastados.game.clash_of_balls.game.GameStatistics;
import com.sapos_aplastados.game.clash_of_balls.menu.PopupBase;

/**
 * Settings
 * This stores all user settings
 *
 */
public class GameSettings {
	public static final boolean debug = false; //debugging variable
						//this allows us some special things for testing if true
						//eg starting a game without any connected clients
	
	public static final boolean place_items = true; //whether there should be
						//items placed randomly on the field from time to time
	
	public static final boolean client_prediction = true;
						//whether the client should predict the game
						//between 2 server updates
						//if false, the client will only apply the server updates
						//and only call moveClient for local animation updates
	
	
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
