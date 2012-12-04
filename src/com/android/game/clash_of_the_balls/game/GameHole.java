package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.Texture;

public class GameHole extends StaticGameObject {

	GameHole(Vector position, Type type, Texture texture) {
		super((short) -1, position, type, texture);
		float angle = (float) Math.PI / 2;

		if (type == Type.Hole_Corner_up_right) {
			//DO Nothing
		} else if (type == Type.Hole_Corner_down_right) {
			setRotation(3*angle);
		} else if (type == Type.Hole_Corner_down_left) {
			setRotation(2 * angle);
		} else if (type == Type.Hole_Corner_up_left) {
			setRotation(angle);
		}
	}

}
