package com.android.game.clash_of_the_balls.game;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Collision;
import org.jbox2d.collision.Collision.PointState;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import android.util.FloatMath;
import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.Maths;
import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.GameItem.ItemType;
import com.android.game.clash_of_the_balls.game.StaticGameObject.Type;
import com.android.game.clash_of_the_balls.game.event.Event;
import com.android.game.clash_of_the_balls.game.event.EventImpact;
import com.android.game.clash_of_the_balls.game.event.EventItemRemoved;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo.PlayerInfo;
import com.android.game.clash_of_the_balls.game.event.EventItemUpdate;

/**
 * GameBase
 * base class for client & server game class
 * most of the game logic is implemented here
 */
public abstract class GameBase implements ContactListener {
	private static final String TAG = "GameBase";
	
	public static final float EPS = 0.00001f;
	
	protected final static int wait_to_start_game = 5; //[sec]
					//time between press of play and actual game start
					//in the last second of this, the sensor calibration will be done
	
	protected final static int max_items_count = 8;
					//how many items there should be maximally on the game
					//field at once
	
	protected GameSettings m_settings;
	protected final TextureManager m_texture_manager;
	
	protected GameField m_game_field;
	protected int m_initial_player_count;
	protected int m_current_player_count;
	protected int m_items_count;
	protected GameLevel m_level;
	
	/* box 2d */
	public World m_world;
	private float m_time_accumulator;
	private static final float step_in_seconds = 30.f / 1000.0f;
	private static final int velocityIterations = 10;
	private static final int positionIterations = 5;
	protected BodyDef m_body_def = new BodyDef();
	
	
	protected boolean m_bIs_game_running = false;
	public boolean isRunning() { return m_bIs_game_running; }
	
	protected GamePlayer m_own_player = null;
	public GamePlayer ownPlayer() { return m_own_player; }
	protected Font2D m_overlay_times[] = null;
	
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
		
		for(int i=0; i<m_impacts.length; ++i) 
			m_impacts[i] = new Impact();
	}
	
	public void initGame(GameLevel level) {
		if(m_bIs_game_running)
			Log.e(TAG, "initGame called while game is running! this must NOT happend");
		
		
		Vec2 gravity = new Vec2(0.0f, 0.0f);
		boolean doSleep = true;
		m_world = new World(gravity, doSleep);
		m_world.setContactListener(this);
		
		m_game_objects = new TreeMap<Short, DynamicGameObject>();
		m_game_field = new GameField(this, m_texture_manager);
		m_next_object_id = m_game_field.init(level, (short) 1, m_world);
		m_level = level;
		m_bIs_game_running = false;
		m_time_accumulator = 0.f;
		m_items_count = 0;
		
		System.gc();
	}
	
	public GameLevel level() { return m_level; }
	
	
	public void addEvent(Event e) {
		assert(generate_events); //don't add events if generate_events==false!
		m_events.add(e);
	}
	
	//initGame must be called before this!
	public void initPlayers(PlayerInfo[] players) {
		m_initial_player_count = players.length;
		m_current_player_count = m_initial_player_count;
		for(int i=0; i<players.length; ++i) {
			Texture texture=null;
			Texture texture_overlay=null;
			Texture texture_glow = null;
			if(m_texture_manager != null) {
				texture = m_texture_manager.get(R.raw.texture_ball_base);
				texture_overlay = m_texture_manager.get(R.raw.texture_ball_up);
				texture_glow = m_texture_manager.get(R.raw.texture_ball_hover);
			}
			GamePlayer p = new GamePlayer(players[i], this, texture
					, texture_overlay, texture_glow, m_overlay_times
					, m_world, m_body_def);
			m_game_objects.put(players[i].id, p);
			if(players[i].id >= m_next_object_id)
				m_next_object_id = (short) (players[i].id + 1);
		}
	}
	
	//add item to the game. does not generate an Event
	public GameItem addItem(short id, ItemType type, Vector position) {
		GameItem item=createItem(id, type, position);
		m_game_objects.put(id, item);
		++m_items_count;
		return item;
	}
	
	//only creates an item, does not add it to the game objects
	public GameItem createItem(short id, ItemType type, Vector position) {
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
			case DontFall: texture=m_texture_manager.get(R.raw.texture_item_dont_fall);
				break;
			}
		}
		return new GameItem(this, id, position, texture, type, m_world, m_body_def);
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
				if(Maths.distSquared(new Vector(entry.getValue().pos()), pos_out) < min_object_dist*min_object_dist) {
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
		    	m_world.destroyBody(entry.getValue().m_body);
		    	entry.getValue().m_body = null;
		        i.remove();  
		    }
		}  
	}
	protected void handleObjectDied(DynamicGameObject obj) {
		if(generate_events) {
			addEvent(new EventItemRemoved(obj.m_id));
		}
		if(obj.type == Type.Player) --m_current_player_count;
		else if(obj.type == Type.Item) --m_items_count;
		if(obj.m_body!=null) {
			while(obj.m_body.getFixtureList() != null) {
				obj.m_body.destroyFixture(obj.m_body.getFixtureList());
			}
		}
		
	}
	
	
	public void move(float dsec) {
		m_game_field.move(dsec);
		//box2d
		m_time_accumulator += dsec;
		while (m_time_accumulator >= step_in_seconds) {
			for (DynamicGameObject obj : m_game_objects.values()) {
				obj.move(step_in_seconds);
			}
			m_world.step(step_in_seconds, velocityIterations, positionIterations);
			m_time_accumulator -= step_in_seconds;
		}
		//position event updates
		if(generate_events) {
			for (DynamicGameObject obj : m_game_objects.values()) {
				if(obj.hasMoved()) {
					addEvent(new EventItemUpdate(obj));
				}
			}
		}
		//collision handling
		for(int i=0; i<m_impact_count; ++i) {
			handleImpact(m_impacts[i].obja, m_impacts[i].objb, 
					m_impacts[i].impact_point, m_impacts[i].normal);
		}
		m_impact_count = 0;
	}
	
	public void moveClient(float dsec) {
		m_game_field.move(dsec);
		for (Map.Entry<Short, DynamicGameObject> entry : m_game_objects.entrySet()) {
			entry.getValue().moveClient(dsec);
		}
	}

	//normal points from obja to objb
	protected void handleImpact(StaticGameObject obja,
			StaticGameObject objb, Vector impact_point, Vector normal) {
	}
	
	/* box2d */
	private static class Impact {
		public StaticGameObject obja;
		public StaticGameObject objb;
		public Vector impact_point = new Vector();
		public Vector normal = new Vector(); //points from obja to objb, normalized
		
		public void copyTo(Impact dest) {
			dest.obja = obja;
			dest.objb = objb;
			dest.impact_point.set(impact_point);
			dest.normal.set(normal);
		}
	}
	private Impact[] m_impacts=new Impact[10];
	private int m_impact_count = 0;
	
	public void beginContact(Contact contact) {
		//do not change world inside here (add/remove objects)
	}
	
	public void endContact(Contact contact) {
	}
	
	private WorldManifold m_tmp_world_manifold = new WorldManifold();
	private PointState m_tmp_state1[]=new PointState[2];
	private PointState m_tmp_state2[]=new PointState[2];
	
	public void preSolve(Contact contact, Manifold oldManifold) {
		//do not change world inside here (add/remove objects)
		
		StaticGameObject obja =(StaticGameObject)contact.m_fixtureA.m_body.m_userData;
		StaticGameObject objb =(StaticGameObject)contact.m_fixtureB.m_body.m_userData;
		
		//whether we must handle this impact, even if it was not newly added
		//in this step. setting handle_impact always to true would not cause
		//problems but would only lead to increased network traffic
		boolean handle_impact = false;
		
		if(obja.type == Type.Hole || objb.type == Type.Hole) {
			GamePlayer player = null;
			Fixture player_fixture = null;
			if(obja.type == Type.Player) {
				player = (GamePlayer) obja;
				player_fixture = contact.m_fixtureA;
			} else if(objb.type == Type.Player) {
				player = (GamePlayer) objb;
				player_fixture = contact.m_fixtureB;
			}
			if(player == null || player.m_item_type != ItemType.DontFall) {
				contact.setEnabled(false);
				if(player_fixture != null && player_fixture.m_userData != null) {
					//at this point we must handle the impact even if this conact
					//was not newly added. because the player had the DontFall
					//item, but not anymore.
					handle_impact = true;
					player_fixture.m_userData = null;
				}
			} else {
				//we have a player with the DontFall item: we need a flag to
				//remember this: simply use m_userData and set it to !=null
				player_fixture.m_userData = player_fixture;
			}
		} else if(obja.type == Type.Item || objb.type == Type.Item) {
			contact.setEnabled(false);
		}
		
		contact.getWorldManifold(m_tmp_world_manifold);
		Collision.getPointStates(m_tmp_state1, m_tmp_state2, oldManifold, contact.m_manifold);
		if (m_tmp_state2[0] == PointState.ADD_STATE || handle_impact) {
			
			if(m_impact_count >= m_impacts.length) { //need to resize
				Impact[] new_impacts = new Impact[m_impacts.length*2];
				for(int i=0; i<new_impacts.length; ++i) 
					new_impacts[i] = new Impact();
				for(int i=0; i<m_impact_count; ++i)
					m_impacts[i].copyTo(new_impacts[i]);
				m_impacts = new_impacts;
			}

			Vec2 impact_point = m_tmp_world_manifold.points[0];
			Vec2 normal = m_tmp_world_manifold.normal;
			
			m_impacts[m_impact_count].obja =(StaticGameObject)contact.m_fixtureA.m_body.m_userData;
			m_impacts[m_impact_count].objb =(StaticGameObject)contact.m_fixtureB.m_body.m_userData;
			m_impacts[m_impact_count].impact_point.set(impact_point.x, impact_point.y);
			m_impacts[m_impact_count].normal.set(normal.x, normal.y);
			++m_impact_count;
			
		}
	}
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}
}
