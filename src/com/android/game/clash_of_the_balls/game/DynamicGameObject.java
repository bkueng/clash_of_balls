package com.android.game.clash_of_the_balls.game;

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
	
	protected boolean m_bIs_dead = false;
	
	public Vector m_new_pos=new Vector();
	public boolean m_has_moved = false;
	
	private Vector m_speed = new Vector();
	public Vector speed() { return m_speed; }
	
	protected GameBase m_owner;
	
	DynamicGameObject(GameBase owner, final short id, Vector position, Type type
			, Texture texture) {
		super(id, position, type, texture);
		m_owner = owner;
	}

	@Override
	public void draw(RenderHelper renderer) {
		// TODO Auto-generated method stub
		//only if not dead... -> call super.draw()
	}

	@Override
	public void move(float dsec) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void applyMove() {
		if(m_has_moved) {
			m_position.set(m_new_pos);
			m_has_moved = false;
		}
	}
	
}
