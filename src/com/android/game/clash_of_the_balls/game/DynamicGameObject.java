package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.Texture;

/**
 * all game objects that can be dynamically added or removed must inherit from
 * this class
 *
 */
public class DynamicGameObject extends StaticGameObject {
	
	protected boolean m_bIs_dead = false;
	
	
	DynamicGameObject(final int id, Vector position, Type type, Texture texture) {
		super(id, position, type, texture);
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
	
}
