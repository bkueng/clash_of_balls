package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.game.StaticGameObject.Type;

public class GameHole extends StaticGameObject {

	public enum HoleType {
		Boarder_left,
		Boarder_up,
		Boarder_right,
		Boarder_down,
		
		Corner_up_right,
		Corner_up_left,
		Corner_down_right,
		Corner_down_left,
		
		Hole_single,
		Hole_Full, //Completely empty
		Hole_Corner_up_right,
		Hole_Corner_up_left,
		Hole_Corner_down_right,
		Hole_Corner_down_left,
		Hole_Head_Down,
		Hole_Head_Up,
		Hole_Head_Left,
		Hole_Head_Right,
	}
	private final HoleType m_hole_type;
	
	GameHole(short id, Vector position, HoleType hole_type, Texture texture) {
		super(id, position, Type.Hole, texture);
		float angle = (float) Math.PI / 2;
		
		m_hole_type = hole_type;

		switch(hole_type) {
		case Hole_Corner_up_right:
		case Hole_Head_Up:
		case Hole_single:
		case Hole_Full:
		case Boarder_left:
		case Corner_up_right:
		break;
		case Corner_down_right:
		case Hole_Head_Right:
		case Boarder_up:
		case Corner_up_left:
			setRotation(3 * angle);
			break;
		case Hole_Corner_down_left:
		case Hole_Head_Down:
		case Boarder_right:
		case Corner_down_left:
			setRotation(2 * angle);
			break;
		case Hole_Corner_up_left:
		case Hole_Head_Left:
		case Boarder_down:
		case Hole_Corner_down_right:
			setRotation(angle);
			break;
		}
	}

}
