package com.android.game.clash_of_the_balls.game;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import android.util.FloatMath;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.StaticGameObject.Type;
import com.android.game.clash_of_the_balls.game.event.Event;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo.PlayerInfo;

/**
 * GameBase
 * base class for client & server game class
 * most of the game logic is implemented here
 */
public abstract class GameBase {
	private static final String TAG = "GameBase";
	
	protected final static int wait_to_start_game = 5; //[sec]
					//time between press of play and actual game start
					//in the last second of this, the sensor calibration will be done
	
	protected GameSettings m_settings;
	protected final TextureManager m_texture_manager;
	
	protected GameField m_game_field;
	protected int m_initial_player_count;
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
		if(m_bIs_game_running)
			throw new RuntimeException("initGame called while game is running! this must NOT happend");
		
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
		m_initial_player_count = players.length;
		for(int i=0; i<players.length; ++i) {
			Texture texture=null;
			Texture texture_overlay=null;
			if(m_texture_manager != null) {
				texture = m_texture_manager.get(R.raw.texture_grey_unpressed_button);
				texture_overlay = m_texture_manager.get(R.raw.texture_grey_pressed_button);
			}
			GamePlayer p = new GamePlayer(players[i], this, texture, texture_overlay);
			m_game_objects.put(players[i].id, p);
			if(players[i].id >= m_next_object_id)
				m_next_object_id = (short) (players[i].id + 1);
		}
	}
	
	//this is the initial player count
	public int initialPlayerCount() {
		return m_initial_player_count;
	}
	
	public int currentPlayerCount() {
		int ret=0;
		for(DynamicGameObject item : m_game_objects.values()) {
			if(item.type == Type.Player && !item.isDead()) ++ret;
		}
		return ret;
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
	
	
	protected void doCollisionHandling() {
		
		//dynamic objects <--> static game objects
		for(DynamicGameObject obj : m_game_objects.values()) {
			
			//assume that the game objects are not larger than 1.0 (one sprite)
			
			if(obj.hasMoved()) {
				int x_start = (int)(obj.pos().x) - 1;
				if(x_start < 0) x_start = 0;
				int x_end = x_start + 2;
				if(x_end >= m_game_field.width()) x_end = m_game_field.width()-1;
				
				int y_start = (int)(obj.pos().y) - 1;
				if(y_start < 0) y_start = 0;
				int y_end = y_start + 2;
				if(y_end >= m_game_field.height()) y_end = m_game_field.height()-1;
				
				for(int x=x_start; x<=x_end; ++x) {
					for(int y=y_start; y<=y_end; ++y) {
						StaticGameObject field_obj = m_game_field.foreground(x, y);
						if(field_obj != null) {
							//check intersection between field_obj & obj
							switch(obj.type) {
							case Player:
								
								switch(field_obj.type) {
								case Hole:
									
									if ( eucDist(obj.pos(), field_obj.pos()) < ((GamePlayer) obj).m_radius ) {
										// player falls down hole and dies
										((GamePlayer) obj).die();
									}
									
									break;
									
								case Wall:
									
									//!!! TODO: Adjust minimum distance, regarding the size of the obstacle!!!

									
									break;
								default: throw new RuntimeException("collision detection for type "+
										field_obj.type+" not implemented!");
								}
								
								break;
							case Item:
								
								//an item does not move (or does it??)
								
								break;
								default: throw new RuntimeException("collision detection for type "+
										obj.type+" not implemented!");
							}
						}
					}
				}
			}
			
		}
		
		//dynamic objects <--> dynamic objects
		for(DynamicGameObject obja : m_game_objects.values()) {
			if(!obja.isDead()) {
				Iterator<DynamicGameObject> iter = m_game_objects.values().iterator();
				//stupid java forces us to do stupid things...
				while(iter.hasNext() && iter.next() != obja) { }
				
				while(iter.hasNext()) {
					DynamicGameObject objb = iter.next();
					if(!objb.isDead() && (obja.hasMoved() || objb.hasMoved())) {
						//check intersection between obja & objb
						if(objb.type.ordinal() < objb.type.ordinal()) {
							DynamicGameObject tmp = obja;
							obja = objb;
							objb = tmp;
						}
						//now: obja.type <= objb.type
						
						switch(obja.type) {
						case Player:
							
							switch(objb.type) {
							case Player:
								//TODO
								if ( eucDist(obja.pos(), objb.pos()) < ((GamePlayer) obja).m_radius + ((GamePlayer) objb).m_radius ) {
									
									// Calculate collision angle phi
									float phi = (float) Math.atan2(obja.pos().y - objb.pos().y, obja.pos().x - objb.pos().x);
									
									// Derive x/y velocity in rotated coordinate system
									float u1 = obja.speed().length();
									float u2 = objb.speed().length();
									
									// object a
									float v1x = u1 * (float) Math.cos(obja.speed().angle());
									float v1y = u1 * (float) Math.cos(obja.speed().angle());
									
									// object b
									float v2x = u2 * (float) Math.cos(objb.speed().angle());
									float v2y = u2 * (float) Math.cos(objb.speed().angle());
									
									// 1D collision detection
									float m1 = ((GamePlayer) obja).m_mass;
									float m2 = ((GamePlayer) objb).m_mass;
									
									float f1x = v1x * (m1 - m2) + 2 * m2 * v2x;
									float f2x = v2x * (m1 - m2) + 2 * m2 * v1x;
									
									// rotate everything back to normal coordinate system
									float v1 = (float) Math.sqrt(f1x * f1x );
								}
								
								break;
							case Item:
								//TODO: take the item
								
								break;
							default: throw new RuntimeException("collision detection for type "+
									objb.type+" not implemented!");
							}
							
							break;
						case Item:
							
							//an item does not move (or does it??)
							switch(objb.type) {
							case Item:
								//nothing to do if items cannot move
								break;
							}
							
							
							break;
							default: throw new RuntimeException("collision detection for type "+
									obja.type+" not implemented!");
						}
					}
				}
			}
		}
	}
	
	private float eucDist(Vector v1, Vector v2) {
		return (float) FloatMath.sqrt((v1.x - v2.x) * (v1.x - v2.x) + (v1.y - v2.y) * (v1.y - v2.y));
	}
	
}
