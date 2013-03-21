/*
 * Copyright (C) 2012-2013 Hans Hardmeier <hanshardmeier@gmail.com>
 * Copyright (C) 2012-2013 Andrin Jenal
 * Copyright (C) 2012-2013 Beat Küng <beat-kueng@gmx.net>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

package com.sapos_aplastados.game.clash_of_balls.game;

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
