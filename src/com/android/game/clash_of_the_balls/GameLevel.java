package com.android.game.clash_of_the_balls;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import android.content.Context;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

import com.android.game.clash_of_the_balls.helper.RawResourceReader;

/**
 * load a level from a file
 *
 * file format: csv
 * <width>, <height>, <level name>
 * 2*<width> columns with <bg>, <fg>
 * ...
 * <height> rows
 * 
 * 
 * <bg>, <fg>: int values for background & foreground object/texture
 * 
 */
public class GameLevel {
	
	private static final String LOG_TAG = "GameLevel";
	
	private Context m_context;
	
	public String name;
	public int width;
	public int height;
	
	public int player_count;
	
	
	private int[] m_background;
	public int background(int x, int y) { return m_background[y*width+x]; }
	
	private int[] m_foreground;
	public int foreground(int x, int y) { return m_foreground[y*width+x]; }
	
	
	//field specification:
	//fg
	public final int TYPE_EMPTY = 0;
	public final int TYPE_PLAYER = 1;
	public final int TYPE_FG_MAX = 1; // max field int value foreground
	//bg
	public final int TYPE_NORMAL = 0;
	public final int TYPE_BG_MAX = 0; // max field int value background
	
	
	public GameLevel(Context context) { 
		m_context = context;
	}
	
	//load from raw resource
	public void loadLevel(int raw_res_id) throws Exception {
		loadLevelImpl(RawResourceReader.readFromRawResource(
				m_context, raw_res_id));
	}
	
	//load from filesystem
	public void loadLevel(String file_name) throws Exception {
		BufferedReader fileReader = new BufferedReader(new FileReader(file_name));
		loadLevelImpl(fileReader);
	}
	
	
	private void loadLevelImpl(Reader stream) throws Exception {
		player_count = 0;
		CSVReader reader = new CSVReader(stream);
		try {
			Iterator<String[]> iter = reader.readAll().iterator();
			//read the size
			assertFormat(iter.hasNext());
			String[] line = iter.next();
			assertFormat(line.length >= 3);
			width = Integer.parseInt(line[0]);
			height = Integer.parseInt(line[1]);
			name = line[2];
			
			assertFormat(width > 0 && height > 0);
			
			Log.d(LOG_TAG, "Game Level: w="+width+", h="+height+", name="+name);
			
			m_background = new int[width*height];
			m_foreground = new int[width*height];
			
			for(int y=0; y<height; ++y) {
				assertFormat(iter.hasNext());
				line = iter.next();
				assertFormat(line.length >= 2*width);
				
				String log_line = "";
				
				for(int x=0; x<width; ++x) {
					//background
					int bg = Integer.parseInt(line[x*2]);
					assertFormat(bg >= 0 && bg <= TYPE_BG_MAX);
					m_background[(height-y-1)*width+x] = bg;
					//foreground
					int fg = Integer.parseInt(line[x*2+1]);
					assertFormat(fg >= 0 && fg <= TYPE_FG_MAX);
					if(fg==TYPE_PLAYER) ++player_count;
					m_foreground[(height-y-1)*width+x] = fg;
					
					log_line += ""+bg+", "+fg+"; ";
				}
				
				Log.v(LOG_TAG, log_line);
			}
			
			Log.d(LOG_TAG, "Players: "+player_count);
			
			
		} finally {
			reader.close();
		}
	}
	
	private void assertFormat(boolean val) throws Exception {
		if(!val) throw new Exception("level: file format error");
	}
}
