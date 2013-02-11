package com.sapos_aplastados.game.clash_of_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.sapos_aplastados.game.clash_of_balls.GameLevel;
import com.sapos_aplastados.game.clash_of_balls.game.GameBase;
import com.sapos_aplastados.game.clash_of_balls.game.event.EventGameInfo.PlayerInfo;


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
