package com.android.game.clash_of_the_balls.game;

import java.util.Map;
import java.util.TreeMap;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.event.Event;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo.PlayerInfo;

/**
 * GameBase
 * base class for client & server game class
 * most of the game logic is implemented here
 */
public abstract class GameBase {
	private static final String TAG = "GameBase";
	
	protected GameSettings m_settings;
	protected final TextureManager m_texture_manager;
	
	protected GameField m_game_field;
	protected int m_player_count;
	protected GameLevel m_level;
	
	public final boolean is_server;
	
	public boolean generate_events = false;
	
	//the moveable game objects: key is the object id
	public Map<Short, DynamicGameObject> m_game_objects;
	protected short m_next_object_id;
	
	
	public GameBase(boolean is_server, GameSettings s, TextureManager texture_manager) {
		this.is_server = is_server;
		m_settings = s;
		m_texture_manager = texture_manager;
	}
	
	public void initGame(GameLevel level) {
		m_game_objects = new TreeMap<Short, DynamicGameObject>();
		m_game_field = new GameField(m_texture_manager);
		m_next_object_id = m_game_field.init(level, (short) 1);
		m_level = level;
	}
	
	public GameLevel level() { return m_level; }
	
	
	public void addEvent(Event e) {
		assert(generate_events); //don't add events if generate_events==false!
		//TODO: a queue, timestamp ?
		
	}
	
	//initGame must be called before this!
	public void initPlayers(PlayerInfo[] players) {
		m_player_count = players.length;
		for(int i=0; i<players.length; ++i) {
			Texture texture=null;
			if(m_texture_manager != null) 
				texture = m_texture_manager.get(R.raw.texture_grey_pressed_button);
			GamePlayer p = new GamePlayer(players[i], this, texture);
			m_game_objects.put(players[i].id, p);
			if(players[i].id >= m_next_object_id)
				m_next_object_id = (short) (players[i].id + 1);
		}
	}
	
	public int playerCount() {
		return m_player_count;
	}
	
	protected int getNextItemId() {
		return m_next_object_id++;
	}
	
	public abstract int getNextSequenceNum();

	public void gameEnd() {
		// TODO Auto-generated method stub
		
	}

	public void gameStartNow() {
		// TODO Auto-generated method stub
		
	}
	
	public abstract String getUniqueNameFromPlayerId(short player_id);
}
