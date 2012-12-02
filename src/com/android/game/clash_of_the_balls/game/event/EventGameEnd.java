package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.android.game.clash_of_the_balls.game.GameBase;
import com.android.game.clash_of_the_balls.game.GameStatistics;

public class EventGameEnd extends Event {
	
	private GameStatistics m_statistics=new GameStatistics();

	public EventGameEnd(DataInputStream s, int seq_num) throws IOException {
		super(type_game_end, seq_num);
		m_statistics.read(s);
	}
	
	public EventGameEnd(int seq_num, GameStatistics statistics) {
		super(type_game_end, seq_num);
		m_statistics.set(statistics);
	}

	public void write(DataOutputStream s) throws IOException {
		super.write(s);
		s.writeByte(type);
		m_statistics.write(s);
	}

	public void apply(GameBase game) {
		game.gameEnd();
		game.statistics().set(m_statistics);
	}
}
