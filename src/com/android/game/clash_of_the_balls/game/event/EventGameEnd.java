package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.android.game.clash_of_the_balls.game.GameBase;
import com.android.game.clash_of_the_balls.game.GameStatistics;

public class EventGameEnd extends Event {
	
	private GameStatistics m_statistics=new GameStatistics();

	public EventGameEnd() {
		super(type_game_end);
	}
	
	public void init(GameStatistics statistics) {
		m_statistics.set(statistics);
	}
	public void init(DataInputStream s) throws IOException {
		m_statistics.read(s);
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(type);
		m_statistics.write(s);
	}

	public void apply(GameBase game) {
		game.gameEnd();
		game.statistics().set(m_statistics);
	}
}
