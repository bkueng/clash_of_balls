package com.android.game.clash_of_the_balls.game;

import org.jbox2d.common.Vec2;

import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.Texture;

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
	
	private int m_impact_count = 0;
	
	protected GameBase m_owner;
	
	DynamicGameObject(GameBase owner, final short id, Type type
			, Texture texture) {
		super(id, type, texture);
		m_owner = owner;
	}

	//can be used for shadow drawing etc...
	public void drawBackground(RenderHelper renderer) {
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
	
	private Vec2 m_tmp_vec = new Vec2();
	
	public void applyVectorData(Vector new_pos, Vector new_speed) {
		m_tmp_vec.set(new_speed.x, new_speed.y);
		m_body.setLinearVelocity(m_tmp_vec);
		
		if(GameSettings.client_prediction) {
			//apply position smoothly
			//we cannot simply change m_position or m_new_pos because we would need
			//to do collision handling
			m_server_translation.set(new_pos.x - m_body.getPosition().x
					, new_pos.y - m_body.getPosition().y);
		} else {
			m_tmp_vec.set(new_pos.x, new_pos.y);
			m_body.setTransform(m_tmp_vec, m_body.getAngle());
		}
		
	}

	//handle everything in here updated by the server. like position & speed
	//all these operations must be undoable or overwritable by a server update!
	@Override
	public void move(float dsec) {
		//check for client-side smoothing of server position update
		if(Math.abs(m_server_translation.x) > GameBase.EPS
				|| Math.abs(m_server_translation.y) > GameBase.EPS) {
			if(m_server_translation.length() < 0.02f) {
				m_tmp_vec.set(m_body.getPosition().x + m_server_translation.x
						, m_body.getPosition().y + m_server_translation.y);
				m_body.setTransform(m_tmp_vec, m_body.getAngle());
				
				m_server_translation.set(0.f, 0.f);
			} else {
				float dx = m_server_translation.x * SERVER_POS_SMOOTHING;
				float dy = m_server_translation.y * SERVER_POS_SMOOTHING;
				
				m_tmp_vec.set(m_body.getPosition().x + dx
						, m_body.getPosition().y + dy);
				m_body.setTransform(m_tmp_vec, m_body.getAngle());
				
				m_server_translation.sub(dx, dy);
			}
		}
	}
	
	//every change that does not need to be updated by the server can be handled
	//in here
	//this can also be called by the server & is used to move less important
	//stuff like animation or item timeout's
	//do NOT generate Events in here
	public void moveClient(float dsec) {
		
	}
	
	public void handleImpact(StaticGameObject other, Vector normal) {
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
	
}
