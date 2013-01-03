package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.game.GameBase;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo.PlayerInfo;


public class EventGameStartNow extends Event {

	public EventGameStartNow() {
		super(type_game_start);
	}
	public void init(DataInputStream s) throws IOException {
		//no additional data
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(type);
	}

	public void apply(GameBase game) {
		game.gameStartNow();
	}

}
