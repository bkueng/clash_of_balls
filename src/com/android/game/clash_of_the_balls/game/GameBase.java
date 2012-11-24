package com.android.game.clash_of_the_balls.game;

import java.util.Map;
import java.util.TreeMap;

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
	
	protected GameField m_game_field;
	
	public final boolean is_server;
	
	//the moveable game objects: key is the object id
	protected Map<Integer, DynamicGameObject> m_game_objects =
			new TreeMap<Integer, DynamicGameObject>();
	protected int m_next_object_id;
	
	
	public GameBase(boolean is_server, GameSettings s) {
		this.is_server = is_server;
		m_settings = s;
	}
	
	public void initGame(GameLevel level, TextureManager texture_manager) {
		m_game_field = new GameField(texture_manager);
		m_next_object_id = m_game_field.init(level, 1);
		//TODO
		//players & id
	}
	//TODO: game logic
	
}
