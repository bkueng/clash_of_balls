package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.Texture;

public class GameWall extends StaticGameObject {
	
	public final Rectangle[] m_wall_items; //these are used for object intersection
							//the position of these is relative to the object position
							// so within [-0.5, 0.5]
							//use m_position + m_wall_items[i].pos to get game position
	
	public enum WallType {
		Wall_horizontal,
		Wall_vertical,
		Wall_Corner_up_right,
		Wall_Corner_up_left,
		Wall_Corner_down_right,
		Wall_Corner_down_left
	}
	private final WallType m_wall_type;

	public GameWall(short id, Vector pos, WallType wall_type, Texture texture) {
		super(id, pos, Type.Wall, texture);		
		m_wall_type = wall_type;
		
		float angle = (float) Math.PI / 2;

		Rectangle r1 = new Rectangle(-0.11f,0.23f,0.22f,0.22f);
		Rectangle r2 = new Rectangle(-0.45f,-0.11f,0.22f,0.22f);
		Rectangle r3 = new Rectangle(-0.11f,-0.11f,0.22f,0.22f);
		Rectangle r4 = new Rectangle(0.22f,-0.11f,0.22f,0.22f);
		Rectangle r5 = new Rectangle(-0.11f,-0.44f,0.22f,0.22f);
		
		Rectangle r6 = new Rectangle(-0.11f, -0.5f, 0.22f, 1.0f);
		Rectangle r7 = new Rectangle(-0.5f, -0.11f, 1.0f, 0.22f);
		
		Rectangle[] rects = null;
		
		if (m_wall_type == WallType.Wall_vertical) {
			// DO Nothing
			rects = new Rectangle[]{r6};
		} else if (m_wall_type ==WallType.Wall_horizontal) {
			setRotation(angle);
			rects = new Rectangle[]{r7};
		} else if (m_wall_type ==WallType.Wall_Corner_up_right) {
			rects = new Rectangle[]{r1,r3,r4};
		} else if (m_wall_type ==WallType.Wall_Corner_up_left) {
			setRotation(angle);
			rects = new Rectangle[]{r1,r3,r2};
		} else if (m_wall_type ==WallType.Wall_Corner_down_left) {
			setRotation(2*angle);
			rects = new Rectangle[]{r2,r3,r5};
		} else if (m_wall_type ==WallType.Wall_Corner_down_right) {
			setRotation(3*angle);
			rects = new Rectangle[]{r5,r3,r4};
		}
		
		m_wall_items= rects;
		
	}
}
