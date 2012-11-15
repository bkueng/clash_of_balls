package com.android.game.clash_of_the_balls.game;

/**
 * GameObject
 * Base class for a game object with a position
 *
 */
public abstract class GameObject implements IDrawable, IMoveable {
	protected Vector m_position = new Vector();
	
	public Vector pos() { return m_position; }

}
