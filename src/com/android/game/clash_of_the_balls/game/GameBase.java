package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;

/**
 * GameBase
 * base class for client & server game class
 * most of the game logic is implemented here
 */
public class GameBase {
	protected GameSettings m_settings;
	
	protected GameView m_game_view;
	
	//map with dynamicGameObject (key is id)
	
	
	public GameBase(GameSettings s) {
		m_settings = s;
	}
	
	protected void initGame(GameLevel level, TextureManager texture_manager) {
		//TODO: level, ...
	}
	//TODO: game logic
	
}
