package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.Texture;

public class GameWall extends StaticGameObject {
	
	public final Rectangle[] m_wall_items; //these are used for object intersection
							//the position of these is relative to the object position
							// so within [-0.5, 0.5]
							//use m_position + m_wall_items[i].pos to get game position
	
	
	public GameWall(final short id, Vector position
			, Texture texture, Rectangle[] wall_items) {
		super(id, position, Type.Wall, texture);
		m_wall_items = wall_items;
	}
}
