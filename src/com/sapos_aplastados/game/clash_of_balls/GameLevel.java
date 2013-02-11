package com.sapos_aplastados.game.clash_of_balls;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import android.content.Context;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

import com.sapos_aplastados.game.clash_of_balls.R;
import com.sapos_aplastados.game.clash_of_balls.helper.RawResourceReader;

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
	
	
	//field specification: all must be <128 !
	//fg
	//when objects are added change GameField to load correct object
	public static final int TYPE_EMPTY = 0;
	public static final int TYPE_PLAYER = 1;
	//Boarder of field
	public static final int TYPE_BOARDER_LEFT = 2;
	public static final int TYPE_BOARDER_RIGHT = 3;
	public static final int TYPE_BOARDER_UP = 4;
	public static final int TYPE_BOARDER_DOWN = 5;
	//Corners of field: these are also holes
	public static final int TYPE_CORNER_UP_RIGHT = 6;
	public static final int TYPE_CORNER_DOWN_RIGHT = 7;
	public static final int TYPE_CORNER_DOWN_LEFT = 8;
	public static final int TYPE_CORNER_UP_LEFT = 9;
	//Holes 
	public static final int TYPE_HOLE = 10; //single round hole, only 1 tile
	public static final int TYPE_HOLE_FULL = 11;
	public static final int TYPE_HOLE_CORNER_UP_RIGHT = 12;
	public static final int TYPE_HOLE_CORNER_DOWN_RIGHT = 13;
	public static final int TYPE_HOLE_CORNER_DOWN_LEFT = 14;
	public static final int TYPE_HOLE_CORNER_UP_LEFT = 15;
	
	public static final int TYPE_HOLE_HEAD_UP = 16;
	public static final int TYPE_HOLE_HEAD_DOWN = 17;
	public static final int TYPE_HOLE_HEAD_LEFT = 18;
	public static final int TYPE_HOLE_HEAD_RIGHT = 19;
	//Wall
	public static final int TYPE_WALL_HOR = 20;
	public static final int TYPE_WALL_VERT = 21;
	public static final int TYPE_WALL_CROSS = 26;
	
	public static final int TYPE_WALL_CORNER_UP_RIGHT = 22;
	public static final int TYPE_WALL_CORNER_DOWN_RIGHT = 23;
	public static final int TYPE_WALL_CORNER_DOWN_LEFT = 24;
	public static final int TYPE_WALL_CORNER_UP_LEFT = 25;
	
	public static final int TYPE_FG_MAX = 26; // max field int value foreground
	
	public static int rawResTexIdFromForeground(int fg_field) {
		switch(fg_field) {
		case TYPE_PLAYER: return -1;
		
		case TYPE_BOARDER_LEFT:;
		case TYPE_BOARDER_RIGHT:;
		case TYPE_BOARDER_DOWN:;
		case TYPE_BOARDER_UP: return R.raw.texture_border;
		
		case TYPE_CORNER_UP_RIGHT:;
		case TYPE_CORNER_DOWN_RIGHT:;
		case TYPE_CORNER_UP_LEFT:;
		case TYPE_CORNER_DOWN_LEFT: return R.raw.texture_corner;
		
		case TYPE_HOLE: return R.raw.texture_hole;
		case TYPE_HOLE_FULL: return R.raw.texture_hole_full;
		
		case TYPE_HOLE_CORNER_DOWN_LEFT:;
		case TYPE_HOLE_CORNER_DOWN_RIGHT:;
		case TYPE_HOLE_CORNER_UP_LEFT:;
		case TYPE_HOLE_CORNER_UP_RIGHT:return R.raw.texture_hole_corner;
		
		case TYPE_HOLE_HEAD_LEFT:;
		case TYPE_HOLE_HEAD_RIGHT:;
		case TYPE_HOLE_HEAD_UP:;
		case TYPE_HOLE_HEAD_DOWN:return R.raw.texture_hole_head;
		
		case TYPE_WALL_HOR:;
		case TYPE_WALL_VERT: return R.raw.texture_wall;
		
		case TYPE_WALL_CROSS: return R.raw.texture_wallcross;
		
		case TYPE_WALL_CORNER_DOWN_LEFT:;
		case TYPE_WALL_CORNER_DOWN_RIGHT:;
		case TYPE_WALL_CORNER_UP_LEFT:;
		case TYPE_WALL_CORNER_UP_RIGHT:return R.raw.texture_wallcorner;
		
		}
		return -1;
	}
	
	//bg
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_BG_BRICK = 1;
	public static final int TYPE_BG_STONE = 2;
	public static final int TYPE_BG_MAX = 2; // max field int value background
	
	public static int rawResTexIdFromBackground(int bg_field) {
		switch(bg_field) {
		case TYPE_NORMAL: return R.raw.texture_game_bg;
		case TYPE_BG_BRICK: return R.raw.texture_game_bg_brick;
		case TYPE_BG_STONE: return R.raw.texture_game_bg_stone;
		}
		return -1;
	}
	
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
	
	//load from network buffer
	public void loadLevel(DataInputStream s) throws IOException {
		width = s.readInt();
		height = s.readInt();
		player_count = (int)s.readShort();
		m_background = new int[width*height];
		m_foreground = new int[width*height];
		for(int i=0; i<width*height; ++i) {
			m_background[i] = (int)s.readByte();
			m_foreground[i] = (int)s.readByte();
		}
	}
	//write to network buffer
	public void write(DataOutputStream s) throws IOException {
		s.writeInt(width);
		s.writeInt(height);
		s.writeShort((short)player_count);
		for(int i=0; i<width*height; ++i) {
			s.writeByte(m_background[i]);
			s.writeByte(m_foreground[i]);
		}
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
					int bg = Integer.parseInt(line[x*2].trim());
					assertFormat(bg >= 0 && bg <= TYPE_BG_MAX);
					m_background[(height-y-1)*width+x] = bg;
					//foreground
					int fg = Integer.parseInt(line[x*2+1].trim());
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
