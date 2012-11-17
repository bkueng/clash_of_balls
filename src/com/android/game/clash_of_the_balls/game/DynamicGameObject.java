package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.Texture;

/**
 * all game objects that can be dynamically added or removed must inherit from
 * this class
 *
 */
public class DynamicGameObject extends StaticGameObject {
	//id, death, ..
	
	
	DynamicGameObject(Vector position, Type type, Texture texture) {
		super(position, type, texture);
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
