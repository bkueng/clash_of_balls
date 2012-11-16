package com.android.game.clash_of_the_balls;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;


public class LevelManager {
	private static final String LOG_TAG = "LevelManager";
	
	private Context m_context;
	
	public ArrayList<GameLevel> m_levels;

	public LevelManager(Context context) {
		m_context = context;
		m_levels = new ArrayList<GameLevel>();
	}
	
	public void loadLevels() {
		loadLevel(R.raw.level_test);
		//TODO: other levels, also from file system?
		
	}
	
	private void loadLevel(int raw_res_id) {
		try {
			GameLevel l = new GameLevel(m_context);
			l.loadLevel(raw_res_id);
			m_levels.add(l);
		} catch (Exception e) {
			Log.w(LOG_TAG, "Failed to load level with raw res id="+raw_res_id);
		}
		
	}
}
