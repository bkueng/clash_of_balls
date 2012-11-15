package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.GameSettings;

/**
 * GameBase
 * base class for client & server game class
 * most of the game logic is implemented here
 */
public class GameBase {
	protected GameSettings m_settings;
	
	public GameBase(GameSettings s) {
		m_settings = s;
	}
	
	public void init() {
		//TODO: level, ...
	}
	//TODO: game logic
	
}
