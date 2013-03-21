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
package com.sapos_aplastados.game.clash_of_balls;

import java.util.ArrayList;

import com.sapos_aplastados.game.clash_of_balls.R;

import android.content.Context;
import android.util.Log;


public class LevelManager {
	private static final String LOG_TAG = "LevelManager";
	
	private Context m_context;
	
	private ArrayList<GameLevel> m_levels;
	
	public GameLevel level(int idx) { return m_levels.get(idx); }
	public int levelCount() { return m_levels.size(); }

	public LevelManager(Context context) {
		m_context = context;
		m_levels = new ArrayList<GameLevel>();
	}
	
	public void loadLevels() {
		loadLevel(R.raw.level_1);
		loadLevel(R.raw.level_2);
		loadLevel(R.raw.level_3);
		loadLevel(R.raw.level_test1);
		loadLevel(R.raw.level_obstacles);
		loadLevel(R.raw.level_walls);
		loadLevel(R.raw.level_walls2);
		//TODO: other levels, also from file system?
		
	}
	
	private void loadLevel(int raw_res_id) {
		try {
			GameLevel l = new GameLevel(m_context);
			l.loadLevel(raw_res_id);
			m_levels.add(l);
		} catch (Exception e) {
			Log.w(LOG_TAG, "Failed to load level with raw res id="+raw_res_id
					+" ("+e.getMessage()+")");
			
		}
		
	}
}
