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
import com.android.game.clash_of_the_balls.game.GameItem.ItemType;
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
	
	protected GamePlayer m_own_player = null;
	public GamePlayer ownPlayer() { return m_own_player; }
	
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
	
	//add item to the game. does not generate an Event
	public GameItem addItem(short id, ItemType type, Vector position) {
		Texture texture=null;
		if(m_texture_manager!=null) {
			switch(type) {
			case IncreaseMaxSpeed: texture=m_texture_manager.get(R.raw.texture_item_speed);
				break;
			case InvertControls: texture=m_texture_manager.get(R.raw.texture_item_control_change);
				break;
			case InvisibleToOthers: texture=m_texture_manager.get(R.raw.texture_item_invisible);
				break;
			case MassAndSize: texture=m_texture_manager.get(R.raw.texture_item_mass);
				break;
			}
		}
		GameItem item = new GameItem(this, id, position, texture, type);
		m_game_objects.put(id, item);
		return item;
	}
	
	//get the middle of a random game field (tile) where no foreground object is
	//and no moveable object is closer than min_object_dist (normally 1)
	//to this position
	//returns false if none is found
	protected boolean getFreeRandomField(Vector pos_out, float min_object_dist) {
		final int max_tries = 15;
		boolean ret = false;
		int empty_x[] = m_game_field.fgEmptyFieldIdxX();
		int empty_y[] = m_game_field.fgEmptyFieldIdxY();
		for(int i=0; i<max_tries && !ret; ++i) {
			int idx = (int)(Math.random() * empty_x.length);
			pos_out.x = (float)empty_x[idx] + 0.5f;
			pos_out.y = (float)empty_y[idx] + 0.5f;
			ret = true;
			//check if no moveable object is too close
			for (Map.Entry<Short, DynamicGameObject> entry : m_game_objects.entrySet()) {
				if(entry.getValue().pos().distSquared(pos_out) < min_object_dist*min_object_dist) {
					ret=false;
				}
			}
		}
		return ret;
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
			addEvent(new EventItemRemoved(obj.m_id));
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
				
				//there can be multiple collisions in a frame. we take only the 
				//new speed from the last detected collisions (which is the closest
				//one to the current position of obj)
				Vector new_speed = new Vector(obj.speed());
				
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

								case Wall:
								{
									GamePlayer player = ((GamePlayer) obj);
									GameWall wall = ((GameWall) field_obj);  
									
									// Temporary vectors that are necessary for the collision detection computation
									Vector normal = new Vector();
									Vector isect_p1 = new Vector(); // first intersection point
									Vector isect_p2 = new Vector(); // second intersection point (can be identical)
									Vector isect_middle = new Vector();
									
									for (Rectangle rect : wall.m_wall_items) {
										
										// rectangle center of current rectangle
										Vector rect_center = new Vector(wall.m_position.x + rect.pos.x + rect.size.x/2.0f, wall.m_position.y + rect.pos.y + rect.size.y/2.0f);
										
										/*
										 *  d----c
										 *  |    |
										 *  |    |
										 *  a----b
										 */
										
										// absolute positions of rectangle corners
										float rect_half_width = rect.width()/2.0f;
										float rect_half_height = rect.height()/2.0f;
										
										float ax = rect_center.x - rect_half_width;
										float ay = rect_center.y - rect_half_height;
										float bx = rect_center.x + rect_half_width;
										float by = rect_center.y - rect_half_height;
										float cx = rect_center.x + rect_half_width;
										float cy = rect_center.y + rect_half_height;
										float dx = rect_center.x - rect_half_width;
										float dy = rect_center.y + rect_half_height;
										
										// check left side of rectangle (including corners)
										if (player.pos().x <= rect_center.x + rect_half_width) {
											// players pos is left to the wall

											// check intersection with left edge of the wall
											if (lineCircleIntersection(ax, ay, dx, dy, player.newPosition(), player.m_radius, isect_p1, isect_p2)) {
												
												/*
												 * player intersects left edge
												 */
												if ((isect_p1.y >= ay && isect_p2.y <= ay) || (isect_p2.y >= ay && isect_p1.y <= ay)) {
													
													// check collision for lower left corner
													Log.d(TAG, "hit lower left corner");
													
													/* calculate the intersection between corner circle with radius: player.m_radius
													 * and the direction vector of newPosition() - pos() to find the position of intersection
													 * of the player and the corner
													 */
													if (lineCircleIntersection(player.pos().x, player.pos().y, player.newPosition().x, player.newPosition().y, new Vector(ax, ay), player.m_radius, isect_p1, isect_p2)) {
														
														/* 
														 * take the point of intersection which lies close to the player.pos() and
														 * compute normal vector which is the vector pointing from the corner to 
														 * the intersection point isect_p
														 */
														if (isect_p1.distSquared(player.pos()) < isect_p2.distSquared(player.pos())) {															
															isect_p1.sub(ax, ay);
															normal.set(isect_p1);
														} else {
															isect_p2.sub(ax, ay);
															normal.set(isect_p2);
														}
														normal.normalize();
														// Calculate new position and speed
														setSpeedAndPosition(player, wall, normal, new Vector(ax, ay), new_speed);
													}
													
												} else if ((isect_p1.y >= dy && isect_p2.y <= dy) || (isect_p2.y >= dy && isect_p1.y <= dy)) {
													
													// check collision for upper left corner
													Log.d(TAG, "hit upper left corner");
													
													/* calculate the intersection between corner circle with radius: player.m_radius
													 * and the direction vector of newPosition() - pos() to find the position of intersection
													 * of the player and the corner
													 */
													if (lineCircleIntersection(player.pos().x, player.pos().y, player.newPosition().x, player.newPosition().y, new Vector(dx, dy), player.m_radius, isect_p1, isect_p2)) {
														
														/* 
														 * take the point of intersection which lies close to the player.pos() and
														 * compute normal vector which is the vector pointing from the corner to 
														 * the intersection point isect_p
														 */
														if (isect_p1.distSquared(player.pos()) < isect_p2.distSquared(player.pos())) {															
															isect_p1.sub(dx, dy);
															normal.set(isect_p1);
														} else {
															isect_p2.sub(dx, dy);
															normal.set(isect_p2);
														}
														normal.normalize();
														// Calculate new position and speed
														setSpeedAndPosition(player, wall, normal, new Vector(dx, dy), new_speed);
													}
													
												} else if (isect_p1.y <= dy && isect_p2.y <= dy && isect_p1.y >= ay && isect_p2.y >= ay) {
												
													// intersection point lies inbetween corners
													Log.d(TAG, "left edge collision");
													
													// middle point of intersection points
													isect_middle.set((isect_p1.x + isect_p2.x) / 2.0f, (isect_p1.y + isect_p2.y) / 2.0f);
													// normal vector for left edge (vertical)
													normal.set(-1.0f, 0.0f);
													// calculate and set new velocity and position of player
													setSpeedAndPosition(player, wall, normal, isect_middle, new_speed);
												
												}
												
											}
											
										} 
										
										// check right side of rectangle (including corners)
										if (player.pos().x >= rect_center.x - rect_half_width) {
											// players pos is right to the wall
											
											if (lineCircleIntersection(bx, by, cx, cy, player.newPosition(), player.m_radius, isect_p1, isect_p2)) {
												
												/*
												 * player intersects right edge
												 */
												if ((isect_p1.y >= by && isect_p2.y <= by) || (isect_p2.y >= by && isect_p1.y <= by)) {
													
													// check collision for lower right corner
													Log.d(TAG, "hit lower right corner");
													
													/* calculate the intersection between corner circle with radius: player.m_radius
													 * and the direction vector of newPosition() - pos() to find the position of intersection
													 * of the player and the corner
													 */
													if (lineCircleIntersection(player.pos().x, player.pos().y, player.newPosition().x, player.newPosition().y, new Vector(bx, by), player.m_radius, isect_p1, isect_p2)) {
														
														/* 
														 * take the point of intersection which lies close to the player.pos() and
														 * compute normal vector which is the vector pointing from the corner to 
														 * the intersection point isect_p
														 */
														if (isect_p1.distSquared(player.pos()) < isect_p2.distSquared(player.pos())) {															
															isect_p1.sub(bx, by);
															normal.set(isect_p1);
														} else {
															isect_p2.sub(bx, by);
															normal.set(isect_p2);
														}
														normal.normalize();
														// Calculate new position and speed
														setSpeedAndPosition(player, wall, normal, new Vector(bx, by), new_speed);
													}
													
												} else if ((isect_p1.y >= cy && isect_p2.y <= cy) || (isect_p2.y >= cy && isect_p1.y <= cy)) {
													
													// check collision for upper right corner
													Log.d(TAG, "hit upper right corner");
													
													/* calculate the intersection between corner circle with radius: player.m_radius
													 * and the direction vector of newPosition() - pos() to find the position of intersection
													 * of the player and the corner
													 */
													if (lineCircleIntersection(player.pos().x, player.pos().y, player.newPosition().x, player.newPosition().y, new Vector(cx, cy), player.m_radius, isect_p1, isect_p2)) {
														
														/* 
														 * take the point of intersection which lies close to the player.pos() and
														 * compute normal vector which is the vector pointing from the corner to 
														 * the intersection point isect_p
														 */
														if (isect_p1.distSquared(player.pos()) < isect_p2.distSquared(player.pos())) {															
															isect_p1.sub(cx, cy);
															normal.set(isect_p1);
														} else {
															isect_p2.sub(cx, cy);
															normal.set(isect_p2);
														}
														normal.normalize();
														// Calculate new position and speed
														setSpeedAndPosition(player, wall, normal, new Vector(cx, cy), new_speed);
													}

												} else if (isect_p1.y <= cy && isect_p2.y <= cy && isect_p1.y >= by && isect_p2.y >= by) {
													
													// intersection point lies inbetween corners
													Log.d(TAG, "right edge collision");

													// middle point of intersection points
													isect_middle.set((isect_p1.x + isect_p2.x) / 2.0f, (isect_p1.y + isect_p2.y) / 2.0f);
													// normal vector for left edge (vertical)
													normal.set(1.0f, 0.0f);
													// calculate and set new velocity and position of player
													setSpeedAndPosition(player, wall, normal, isect_middle, new_speed);
													
												}
												
											}
										}
										
										// check bottom side of rectangle (excluding corners, we already checked them)
										if (player.pos().y <= rect_center.y) {
											
											if (lineCircleIntersection(ax, ay, bx, by, player.newPosition(), player.m_radius, isect_p1, isect_p2)) {
											
												/*
												 * player intersects lower edge
												 */
												if (isect_p1.x >= ax && isect_p2.x >= ax && isect_p2.x <= bx && isect_p1.x <= bx) {
													
													Log.d(TAG, "lower edge collision");
													
													// middle point of intersection points
													isect_middle.set((isect_p1.x + isect_p2.x) / 2.0f, (isect_p1.y + isect_p2.y) / 2.0f);
													// normal vector for left edge (vertical)
													normal.set(0.0f, -1.0f);
													// calculate and set new velocity and position of player
													setSpeedAndPosition(player, wall, normal, isect_middle, new_speed);
													
												}
											}
											
										}
										
										// check top side of rectangle (excluding corners, we already checked them)
										if (player.pos().y >= rect_center.y) {
											// players pos is inbetween left and right edge and above the upper edge
											
											if (lineCircleIntersection(cx, cy, dx, dy, player.newPosition(), player.m_radius, isect_p1, isect_p2)) {
												
												/*
												 * player intersects upper edge
												 */
												if (isect_p1.x >= dx && isect_p2.x >= dx && isect_p2.x <= cx && isect_p1.x <= cx) {
													
													Log.d(TAG, "upper edge collision");

													// middle point of intersection points
													isect_middle.set((isect_p1.x + isect_p2.x) / 2.0f, (isect_p1.y + isect_p2.y) / 2.0f);
													// normal vector for left edge (vertical)
													normal.set(0.0f, 1.0f);
													// calculate and set new velocity and position of player
													setSpeedAndPosition(player, wall, normal, isect_middle, new_speed);
													
												}
											}
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
				
				obj.speed().set(new_speed);
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
									
									float alpha = player_a.pos().x - player_b.pos().x;
									float gamma = player_a.pos().y - player_b.pos().y;
									float beta = player_a.newPosition().x - player_a.pos().x - player_b.newPosition().x + player_b.pos().x;
									float delta = player_a.newPosition().y - player_a.pos().y - player_b.newPosition().y + player_b.pos().y;
									
									float rads = ((player_a.m_radius + player_b.m_radius + EPS) * (player_a.m_radius + player_b.m_radius + EPS));

									float a = (beta * beta + delta * delta);
									float b = 2.f * (alpha * beta + gamma * delta);
									float c = (gamma * gamma + alpha * alpha) - (rads * rads);
									float D = FloatMath.sqrt((b*b) - (4.f * a * c));
									
									float t1 = (-b + D) / (2.f * a);
									float t2 = (-b - D) / (2.f * a);
									float t = t1;
									if (t2 < t1) t = t2;
									
									// Set the new position of player a
									Vector new_position_a = player_a.newPosition();
									new_position_a.sub(player_a.pos());
									new_position_a.mul(t);
									new_position_a.add(player_a.pos());
									
									// Set the new position of player b
									Vector new_position_b = player_b.newPosition();
									new_position_b.sub(player_b.pos());
									new_position_b.mul(t);
									new_position_b.add(player_b.pos());
																		
									Log.d(TAG, "Player a new position, x: " + player_a.newPosition().x + "y: " + player_a.newPosition().y);
									Log.d(TAG, "Player b new position, x: " + player_b.newPosition().y + "y: " + player_b.newPosition().y);
									
									// Get new direction for player a
									// Get new direction for player b
									Vector dir_a_b = new Vector(player_b.newPosition());
									dir_a_b.sub(player_a.newPosition());
									dir_a_b.normalize();
									Vector dir_b_a = new Vector(dir_a_b);
									
									dir_a_b.mul(dir_a_b.dot(player_a.speed()));
									dir_b_a.mul(dir_b_a.dot(player_b.speed()));
									
									// Calculate epsilon (elasticity factor)
									float epsilon = (player_a.elasticFactor() 
											+ player_b.elasticFactor()) / 2.0f;
									
									Vector temp_a = new Vector(dir_a_b);
									Vector temp_b = new Vector(dir_b_a);
									temp_a.mul(player_a.m_mass - epsilon * player_b.m_mass);
									temp_b.mul(player_b.m_mass * (1.0f + epsilon));
									temp_a.add(temp_b);
									temp_a.mul(1.f/(player_a.m_mass + player_b.m_mass));
									temp_a.sub(dir_a_b);
									Log.d(TAG, "new speed direction of a, x: " + temp_a.x + "y: " + temp_a.y);
									player_a.speed().add(temp_a);
									
									temp_a.set(dir_a_b);
									temp_b.set(dir_b_a);
									temp_b.mul(player_b.m_mass - epsilon * player_a.m_mass);
									temp_a.mul(player_a.m_mass * (1.0f + epsilon));
									temp_b.add(temp_a);
									temp_b.mul(1.f/(player_b.m_mass + player_a.m_mass));
									temp_b.sub(dir_b_a);
									Log.d(TAG, "new speed direction of b, x: " + temp_b.x + "y: " + temp_b.y);
									player_b.speed().add(temp_b);
									
									if(is_server) {
										//the client will receive the update from server
										handleImpact(player_a, player_a.newPosition()
												, player_b, player_b.newPosition());
									}
									
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

	private void handleImpact(StaticGameObject obja, Vector pos_a,
			StaticGameObject objb, Vector pos_b) {
		obja.handleImpact(objb);
		objb.handleImpact(obja);
		if (generate_events) {
			addEvent(new EventImpact(obja.m_id, pos_a, objb.m_id, pos_b));
		}
	}
	
	//this is used for player collisions with static field objects
	private void setSpeedAndPosition(GamePlayer player, StaticGameObject obj_b,
			Vector normal, Vector intersect_point, Vector new_speed) {
		
		//Log.d(TAG, "speed before collision, x: " + player.speed().x + " y: " + player.speed().y);

		float epsilon = (player.elasticFactor() 
				+ obj_b.elasticFactor()) / 2.0f;
		// calculate new velocity
		Vector speed = new Vector(normal);
		speed.mul(-2.f*epsilon*normal.dot(player.speed()));
		new_speed.set(player.speed());
		new_speed.add(speed);
		
		// update player newPosition to intersection point between player and wall
		player.newPosition().set(intersect_point);
		// prepare position of player regarding distance to wall
		Vector player_pos = new Vector(normal);		
		// set new position of player
		player_pos.mul(player.m_radius + EPS);
		player.newPosition().add(player_pos);
		
		//Log.d(TAG, "speed after collision, x: " + player.speed().x + " y: " + player.speed().y);
	
	}

	private boolean lineCircleIntersection(float x1, float y1, float x2, float y2, Vector circ_center, float circ_radius, Vector isect_point1, Vector isect_point2) {
		
		//Log.d(TAG, "vector of intersection: (" + x1 + "," + y1 + ", " + x2 + "," + y2 + ")");
		
		float cx = circ_center.x;
		float cy = circ_center.y;
		float dx = x2 - x1;
		float dy = y2 - y1;
		float a = dx * dx + dy * dy;
		float b = 2.0f * (dx * (x1 - cx) + dy * (y1 - cy));
		float c = cx * cx + cy * cy;
		c += x1 * x1 + y1 * y1;
		c -= 2.0f * (cx * x1 + cy * y1);
		c -= circ_radius * circ_radius;
		float D = b*b - 4.0f*a*c;

		if (D < 0) { // Not intersecting
			return false;
		} else {
			float square_root = FloatMath.sqrt(D);
			float mu = (-b + square_root) / (2.f * a);
			float ix1 = x1 + mu * (dx);
			float iy1 = y1 + mu * (dy);
			mu = (-b - square_root) / (2.f * a);
			float ix2 = x1 + mu * (dx);
			float iy2 = y1 + mu * (dy);

			// set new position of player to impact point
			isect_point1.set(ix1, iy1);
			isect_point2.set(ix2, iy2);
			
			//Log.d(TAG, "intersection point 1, x: " + ix1 + " y: " + iy1);
			//Log.d(TAG, "intersection point 2, x: " + ix2 + " y: " + iy2);
			
			return true;
		}

	}
}
