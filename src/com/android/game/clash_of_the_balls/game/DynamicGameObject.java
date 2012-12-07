package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.game.event.EventItemUpdate;

/**
 * all game objects that can be dynamically added or removed must inherit from
 * this class
 * 
 * move() must not change m_position, but m_new_pos instead
 * this is then used in the game for object collision detection
 *
 */
public class DynamicGameObject extends StaticGameObject {
	
	/* death of an object:
	 * m_bIs_dead==true: means that this object cannot interact with others anymore
	 * m_bIs_dying==true: implies m_bIs_dead==true. means the object is still in the
	 * 					object list, but cannot move anymore. used for dying effect
	 * m_bIs_dead && !m_bIs_dying: means the object is not used anymore
	 */
	protected boolean m_bIs_dead = false;
	public boolean isDead() { return m_bIs_dead; }
	protected boolean m_bIs_dying = false;
	public boolean isReallyDead() { return m_bIs_dead && !m_bIs_dying; }
	
	protected Vector m_new_pos=new Vector();
	protected boolean m_has_moved = false;
	
	public Vector newPosition() { return m_new_pos; }
	public boolean hasMoved() { return m_has_moved; }
	
	protected Vector m_speed = new Vector();
	public Vector speed() { return m_speed; }
	
	private int m_impact_count = 0;
	
	protected GameBase m_owner;
	
	DynamicGameObject(GameBase owner, final short id, Vector position, Type type
			, Texture texture) {
		super(id, position, type, texture);
		m_owner = owner;
	}

	@Override
	public void draw(RenderHelper renderer) {
		if(!isReallyDead()) {
			super.draw(renderer);
		}
	}
	
	private Vector m_server_translation = new Vector(0.f, 0.f);
	private static final float SERVER_POS_SMOOTHING = 1.f/5.f;
						//this defines how smoothly the server position update
						//is applied. a bigger value means less smoothly but 
						//more accruate with the server (1 means apply in one step)
						//it takes ln(0.01)/ln(1-SERVER_POS_SMOOTHING) frames (steps)
						//to get the error (distance of positions) below 0.01
						//for SERVER_POS_SMOOTHING = 1/5 it's about 20 frames
	
	
	public void applyVectorData(Vector new_pos, Vector new_speed) {
		m_speed.set(new_speed);
		
		//apply position smoothly
		//we cannot simply change m_position or m_new_pos because we would need
		//to do collision handling
		m_server_translation.set(new_pos.x - m_new_pos.x, new_pos.y - m_new_pos.y);
		
	}

	//handle everything in here updated by the server. like position & speed
	//all these operations must be undoable or overwritable by a server update!
	//Note that dsec can be negative to move back in time!
	@Override
	public void move(float dsec) {
		m_new_pos.set(m_position);
		
		if(m_owner.generate_events) {
			//check for client-side smoothing of server position update
			if(Math.abs(m_server_translation.x) > GameBase.EPS
					|| Math.abs(m_server_translation.y) > GameBase.EPS) {
				if(m_server_translation.length() < 0.02f) {
					m_new_pos.add(m_server_translation);
					m_server_translation.set(0.f, 0.f);
				} else {
					float dx = m_server_translation.x * SERVER_POS_SMOOTHING;
					float dy = m_server_translation.y * SERVER_POS_SMOOTHING;
					m_new_pos.add(dx, dy);
					m_server_translation.sub(dx, dy);
				}
				m_has_moved = true;
			}
		}
		
		//-> set m_has_moved & m_new_pos
	}
	
	//every change that does not need to be updated by the server can be handled
	//in here
	//this can also be called by the server & is used to move less important
	//stuff like animation or item timeout's
	//do NOT generate Events in here
	public void moveClient(float dsec) {
		
	}
	
	public void handleImpact(StaticGameObject other) {
		++m_impact_count;
	}
	public int impactCount() { return m_impact_count; }
	
	
	public void die() {
		if(!m_bIs_dead) {
			m_bIs_dead = true;
			m_bIs_dying = false;
			m_owner.handleObjectDied(this);
		}
	}
	
	
	public void applyMove() {
		if(m_has_moved) {
			m_position.set(m_new_pos);
			m_has_moved = false;
			if(m_owner.generate_events) {
				m_owner.addEvent(new EventItemUpdate(this));
			}
		}
	}
	
}
