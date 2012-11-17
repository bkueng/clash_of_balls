package com.android.game.clash_of_the_balls.game;

/**
 * GameObject
 * Base class for a game object with a position
 *
 */
public abstract class GameObject implements IDrawable, IMoveable {
	protected Vector m_position; //this is the center of the object
								 //width & height of a tile are 1.f by default
	
	public Vector pos() { return m_position; }
	
	public GameObject(Vector position) {
		m_position = position;
	}
	public GameObject() {
		m_position = new Vector();
	}

}
