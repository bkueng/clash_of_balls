package com.android.game.clash_of_the_balls.game;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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
	
	protected final static int wait_to_start_game = 3; //[sec]
					//time between press of play and actual game start
	
	protected GameSettings m_settings;
	protected final TextureManager m_texture_manager;
	
	protected GameField m_game_field;
	protected int m_player_count;
	protected GameLevel m_level;
	
	protected boolean m_bIs_game_running = false;
	public boolean isRunning() { return m_bIs_game_running; }
	
	public final boolean is_server;
	
	public boolean generate_events = false;
	
	//the moveable game objects: key is the object id
	public Map<Short, DynamicGameObject> m_game_objects;
	protected short m_next_object_id;
	
	protected Queue<Event> m_events = new LinkedList<Event>();
	
	public DynamicGameObject getGameObject(short id) {
		return m_game_objects.get(id); //can return null!
	}
	
	public GameBase(boolean is_server, GameSettings s, TextureManager texture_manager) {
		this.is_server = is_server;
		m_settings = s;
		m_texture_manager = texture_manager;
		m_game_objects = new TreeMap<Short, DynamicGameObject>();
	}
	
	public void initGame(GameLevel level) {
		m_game_objects = new TreeMap<Short, DynamicGameObject>();
		m_game_field = new GameField(m_texture_manager);
		m_next_object_id = m_game_field.init(level, (short) 1);
		m_level = level;
		m_bIs_game_running = false;
	}
	
	public GameLevel level() { return m_level; }
	
	
	public void addEvent(Event e) {
		assert(generate_events); //don't add events if generate_events==false!
		//TODO timestamp ?
		
		m_events.add(e);
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
	
	protected short getNextItemId() {
		return m_next_object_id++;
	}
	
	public abstract int getNextSequenceNum();

	public void gameEnd() {
		m_bIs_game_running = false;
	}

	public void gameStartNow() {
		m_bIs_game_running = true;
	}
	
	public abstract String getUniqueNameFromPlayerId(short player_id);
}
