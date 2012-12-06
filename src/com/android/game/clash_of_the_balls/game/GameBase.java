package com.android.game.clash_of_the_balls.game;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import android.util.FloatMath;
import android.util.Log;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.StaticGameObject.Type;
import com.android.game.clash_of_the_balls.game.event.Event;
import com.android.game.clash_of_the_balls.game.event.EventImpact;
import com.android.game.clash_of_the_balls.game.event.EventItemRemoved;
import com.android.game.clash_of_the_balls.game.event.EventItemUpdate;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo.PlayerInfo;

/**
 * GameBase
 * base class for client & server game class
 * most of the game logic is implemented here
 */
public abstract class GameBase {
	private static final String TAG = "GameBase";
	
	public static final float EPS = 0.00001f;
	
	protected final static int wait_to_start_game = 5; //[sec]
					//time between press of play and actual game start
					//in the last second of this, the sensor calibration will be done
	
	protected GameSettings m_settings;
	protected final TextureManager m_texture_manager;
	
	protected GameField m_game_field;
	protected int m_initial_player_count;
	protected int m_current_player_count;
	protected GameLevel m_level;
	
	protected boolean m_bIs_game_running = false;
	public boolean isRunning() { return m_bIs_game_running; }
	
	public final boolean is_server;
	
	public boolean generate_events = false;
	
	//the moveable game objects: key is the object id
	public Map<Short, DynamicGameObject> m_game_objects;
	protected short m_next_object_id;
	
	protected Queue<Event> m_events = new LinkedList<Event>();
	
	public StaticGameObject getGameObject(short id) {
		StaticGameObject obj = m_game_objects.get(id);
		if(obj == null) {
			//check field
			for(int x=0; x<m_game_field.width() && obj==null; ++x) {
				for(int y=0; y<m_game_field.height() && obj==null; ++y) {
					StaticGameObject field_obj=m_game_field.foreground(x, y);
					if(field_obj!=null && field_obj.m_id == id) obj=field_obj;
				}
			}
		}
		return obj;
	}
	public DynamicGameObject getMoveableGameObject(short id) {
		return m_game_objects.get(id); //can return null!
	}
	
	public GameStatistics statistics() { return m_settings.game_statistics; }
	public GameSettings settings() { return m_settings; }
	
	public GameBase(boolean is_server, GameSettings s, TextureManager texture_manager) {
		this.is_server = is_server;
		m_settings = s;
		m_texture_manager = texture_manager;
		m_game_objects = new TreeMap<Short, DynamicGameObject>();
	}
	
	public void initGame(GameLevel level) {
		if(m_bIs_game_running)
			Log.e(TAG, "initGame called while game is running! this must NOT happend");
		
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
		m_current_player_count = m_initial_player_count;
		for(int i=0; i<players.length; ++i) {
			Texture texture=null;
			Texture texture_overlay=null;
			if(m_texture_manager != null) {
				texture = m_texture_manager.get(R.raw.texture_ball_base);
				texture_overlay = m_texture_manager.get(R.raw.texture_ball_up);
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
		return m_current_player_count;
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
	
	protected void removeDeadObjects() {
		for (Iterator<Map.Entry<Short, DynamicGameObject>> i 
				= m_game_objects.entrySet().iterator(); i.hasNext(); ) {  
			
		    Map.Entry<Short, DynamicGameObject> entry = i.next();  
		    if (entry.getValue().isReallyDead()) {
		        i.remove();  
		    }  
		}  
	}
	protected void handleObjectDied(DynamicGameObject obj) {
		if(generate_events) {
			addEvent(new EventItemRemoved(getNextSequenceNum(), obj.m_id));
		}
		if(obj.type == Type.Player) --m_current_player_count;
	}
	
	
	public void move(float dsec) {
		m_game_field.move(dsec);
		for (Map.Entry<Short, DynamicGameObject> entry : m_game_objects.entrySet()) {
			entry.getValue().move(dsec);
		}
	}
	
	public void applyMove() {
		for (Map.Entry<Short, DynamicGameObject> entry : m_game_objects.entrySet()) {
			entry.getValue().applyMove();
		}
	}
	
	protected void doCollisionHandling() {
		
		//dynamic objects <--> static game objects
		for(DynamicGameObject obj : m_game_objects.values()) {
			
			//assume that the game objects are not larger than 1.0 (one sprite)
			
			if(obj.hasMoved() && !obj.isDead()) {
				int x_start = (int)(obj.newPosition().x) - 1;
				if(x_start < 0) x_start = 0;
				int x_end = x_start + 2;
				if(x_end >= m_game_field.width()) x_end = m_game_field.width()-1;
				
				int y_start = (int)(obj.newPosition().y) - 1;
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
									GameHole hole = (GameHole) field_obj;
									Vector n=new Vector();
									if(hole.isInside(obj.pos(), obj.newPosition(), n)) {
										//obj falls down
										if(is_server) { 
											//the client will receive the update from server
											handleImpact(obj, obj.newPosition()
													, field_obj, field_obj.pos());
											//apply speed
											obj.speed().set(n);
											
											obj.die();
											
										}
									}
									
									break;
								case Wall_horizontal: //TODO: change to Wall
								{
									GamePlayer player = ((GamePlayer) obj);
									GameWall wall = ((GameWall) field_obj);  
									
									for (Rectangle rect : wall.m_wall_items) {
										if (rect.intersectCircle(player.newPosition(), player.m_radius)) {
											// TODO: 
											Log.d(TAG, "Player - Wall collided");

										}
									}
									
									break;
								}
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
							{								
								GamePlayer player_a = ((GamePlayer) obja);
								GamePlayer player_b = ((GamePlayer) objb);
								
								if ( obja.newPosition().dist(objb.newPosition()) <= (player_a.m_radius + player_b.m_radius) ) {
									
									Log.d(TAG, "Players collide");
									
									// Players collide
									float eps = 0.0f; // eps distance between players
									
									float alpha = player_a.pos().x - player_b.pos().x;
									float gamma = player_a.pos().y - player_b.pos().y;
									float beta = player_a.newPosition().x - player_a.pos().x + player_b.newPosition().x - player_b.pos().x;
									float delta = player_a.newPosition().y - player_a.pos().y + player_b.newPosition().y - player_b.pos().y;
									
									float rads = ((player_a.m_radius + player_b.m_radius + eps) * (player_a.m_radius + player_b.m_radius + eps));

									float a = (beta * beta + delta * delta);
									float b = 2.f * (alpha * beta + gamma * delta);
									float c = (gamma * gamma + alpha * alpha) - (rads * rads);
									float D = (b*b) - (4.f * a * c);
									D = FloatMath.sqrt(D);
									
									float t1 = (-b + D) / (2.f * a);
									float t2 = (-b - D) / (2.f * a);
									float t = t1;
									
									if (Math.abs(t2) < Math.abs(t1))
										t = t2;
									
									// Set the new position of player a
									Vector new_position_a = new Vector(player_a.newPosition());
									new_position_a.sub(player_a.pos());
									new_position_a.mul(t);
									player_a.pos().add(new_position_a);
									player_a.newPosition().set(player_a.pos());
									
									// Set the new position of player b
									Vector new_position_b = new Vector(player_b.newPosition());
									new_position_b.sub(player_b.pos());
									new_position_b.mul(t);
									player_b.pos().add(new_position_b);
									player_b.newPosition().set(player_b.pos());
																		
									Log.d(TAG, "Player a new position, x: " + player_a.newPosition().x + "y: " + player_a.newPosition().y);
									Log.d(TAG, "Player b new position, x: " + player_b.newPosition().y + "y: " + player_b.newPosition().y);
									
									// Get new direction for player a
									// Get new direction for player b
									Vector dir_a_b = new Vector(player_b.newPosition());
									dir_a_b.sub(player_a.newPosition());
									dir_a_b.normalize();
									Vector dir_b_a = new Vector(dir_a_b);
									
									Vector speed_a = new Vector(player_a.speed());
									speed_a.mul(1/dir_a_b.lengthSquared());
									dir_a_b.mul(dir_a_b.dot(speed_a));
									
									Vector speed_b = new Vector(player_b.speed());
									speed_b.mul(1/dir_b_a.lengthSquared());
									dir_b_a.mul(dir_b_a.dot(speed_b));
									
									// Calculate epsilon (elasticity factor)
									float epsilon = (player_a.elasticFactor() 
											+ player_b.elasticFactor()) / 2.0f;
									
									Vector temp_a = new Vector(speed_a);
									Vector temp_b = new Vector(speed_b);
									temp_a.mul(player_a.m_mass - epsilon * player_b.m_mass);
									temp_b.mul(player_b.m_mass * (1.0f + epsilon));
									temp_a.add(temp_b);
									temp_a.mul(1/(player_a.m_mass + player_b.m_mass));
									temp_a.sub(speed_a);
									Log.d(TAG, "new speed direction of a, x: " + temp_a.x + "y: " + temp_a.y);
									player_a.speed().add(temp_a);
									
									temp_a.set(speed_a);
									temp_b.set(speed_b);
									temp_b.mul(player_b.m_mass - epsilon * player_a.m_mass);
									temp_a.mul(player_a.m_mass * (1.0f + epsilon));
									temp_b.add(temp_a);
									temp_b.mul(1/(player_b.m_mass + player_a.m_mass));
									temp_b.sub(speed_b);
									Log.d(TAG, "new speed direction of b, x: " + temp_b.x + "y: " + temp_b.y);
									player_b.speed().add(temp_b);
									
								}
								
								break;
							}
							case Item:
								Log.d(TAG, "Player - Item collision");

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
	
	private void handleImpact(StaticGameObject obja, Vector pos_a
			, StaticGameObject objb, Vector pos_b) {
		obja.handleImpact(objb);
		objb.handleImpact(obja);
		if(generate_events) {
			addEvent(new EventImpact(getNextSequenceNum(), obja.m_id
					, pos_a, objb.m_id, pos_b));
		}
	}
	
}
