package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.android.game.clash_of_the_balls.game.GameBase;


public class EventGameStartNow extends Event {

	public EventGameStartNow(DataInputStream s) {
		super(type_game_start);
		//no additional data
	}
	public EventGameStartNow() {
		super(type_game_start);
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(type);
	}

	public void apply(GameBase game) {
		game.gameStartNow();
	}

}
