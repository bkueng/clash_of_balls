/*
 * Copyright (C) 2012-2013 Hans Hardmeier <hanshardmeier@gmail.com>
 * Copyright (C) 2012-2013 Andrin Jenal
 * Copyright (C) 2012-2013 Beat Küng <beat-kueng@gmx.net>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

package com.sapos_aplastados.game.clash_of_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.sapos_aplastados.game.clash_of_balls.game.GameBase;
import com.sapos_aplastados.game.clash_of_balls.game.GameStatistics;

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
