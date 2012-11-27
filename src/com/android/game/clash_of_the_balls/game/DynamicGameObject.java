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
	
	protected boolean m_bIs_dead = false;
	
	public Vector m_new_pos=new Vector();
	public boolean m_has_moved = false;
	
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
		if(!m_bIs_dead) {
			super.draw(renderer);
		}
	}

	@Override
	public void move(float dsec) {
		// TODO Auto-generated method stub
			//-> set m_has_moved & m_new_pos
	}
	
	public void handleImpact(DynamicGameObject other) {
		++m_impact_count;
	}
	public int impactCount() { return m_impact_count; }
	
	
	public void die() {
		//TODO: dying effect ?
		
		m_bIs_dead = true;
		
		//generate event ?
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
