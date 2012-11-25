package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.android.game.clash_of_the_balls.game.GameBase;


public class EventGameStartNow extends Event {

	public EventGameStartNow(DataInputStream s, int seq_num) {
		super(type_game_start, seq_num);
		//no additional data
	}
	public EventGameStartNow(int seq_num) {
		super(type_game_start, seq_num);
	}

	public void write(DataOutputStream s) throws IOException {
		super.write(s);
		s.writeByte(type);
	}

	public void apply(GameBase game) {
		game.gameStartNow();
	}

}
