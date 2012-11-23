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
	private static final String TAG = "GameBase";
	
	protected GameSettings m_settings;
	
	protected GameView m_game_view;
	
	public final boolean is_server;
	
	//map with dynamicGameObject (key is id)
	protected int m_next_object_id;
	
	
	public GameBase(boolean is_server, GameSettings s) {
		this.is_server = is_server;
		m_settings = s;
	}
	
	protected void initGame(GameLevel level, TextureManager texture_manager) {
		//TODO: level, ...
		//players
		//m_next_object_id
	}
	//TODO: game logic
	
}
