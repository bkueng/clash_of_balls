package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.GameHole.HoleType;
import com.android.game.clash_of_the_balls.game.GameWall.WallType;
import com.android.game.clash_of_the_balls.game.StaticGameObject.Type;

/**
 * implements a rectangular game field
 * each field has a background 
 * and can have a stationary foreground object (obstacle or hole)
 * 
 * if texture_manager is null, no textures will be used.
 *
 */
public class GameField extends GameObject {
	
	private int m_width;
	private int m_height;
	
	private TextureManager m_texture_manager;
	
	public int width() { return m_width; }
	public int height() { return m_height; }
	
	
	StaticGameObject[] m_bg_objects;
	public StaticGameObject background(int x, int y) { return m_bg_objects[y*m_width+x]; }
	StaticGameObject[] m_fg_objects;
	public StaticGameObject foreground(int x, int y) { return m_fg_objects[y*m_width+x]; }
	
	//x & y indexes of foreground fields which are null
	private int[] m_fg_empty_idx_x=null;
	private int[] m_fg_empty_idx_y=null;
	
	
	public GameField(TextureManager texture_manager) {
		m_texture_manager = texture_manager;
	}
	
	public int[] fgEmptyFieldIdxX() {
		if(m_fg_empty_idx_x==null) initEmptyIdx();
		return m_fg_empty_idx_x;
	}
	public int[] fgEmptyFieldIdxY() {
		if(m_fg_empty_idx_y==null) initEmptyIdx();
		return m_fg_empty_idx_y;
	}
	private void initEmptyIdx() {
		int empty_count = 0;
		for(int i=0; i<m_width*m_height; ++i) {
			if(m_fg_objects[i] == null) ++empty_count;
		}
		m_fg_empty_idx_x = new int[empty_count];
		m_fg_empty_idx_y = new int[empty_count];
		int i=0;
		for(int y=0; y<m_height; ++y) {
			for(int x=0; x<m_width; ++x) {
				if(m_fg_objects[y*m_width+x] == null) {
					m_fg_empty_idx_x[i] = x;
					m_fg_empty_idx_y[i] = y;
					++i;
				}
			}
		}
	}
	
	//returns the next object id to be used
	//for the first game call set next_object_id to 1
	public short init(GameLevel level, short next_object_id) {
		m_width = level.width;
		m_height = level.height;
		m_fg_empty_idx_x = m_fg_empty_idx_y = null;
		
		//background
		if(m_texture_manager != null) {
			m_bg_objects = new StaticGameObject[m_width*m_height];
			for(int y=0; y<m_height; ++y) {
				for(int x=0; x<m_width; ++x) {
					int type = level.background(x, y);
					int raw_res_id = GameLevel.rawResTexIdFromBackground(type);
					if(raw_res_id != -1) {
						m_bg_objects[y*m_width+x] = new StaticGameObject((short)-1,
								new Vector((float)x+0.5f, (float)y+0.5f),
								Type.Background,
								m_texture_manager.get(raw_res_id));
					}
				}
			}
		}
		
		//foreground
		short object_id=next_object_id;
		
		m_fg_objects = new StaticGameObject[m_width*m_height];
		for(int y=0; y<m_height; ++y) {
			for(int x=0; x<m_width; ++x) {
				int type = level.foreground(x, y);
				int raw_res_id = GameLevel.rawResTexIdFromForeground(type);
				if(raw_res_id != -1) {
					Vector pos = new Vector((float)x+0.5f, (float)y+0.5f);
					StaticGameObject obj=null;
					Texture texture=null;
					if(m_texture_manager != null && raw_res_id != -1)
						texture = m_texture_manager.get(raw_res_id);
					switch(type) {
					case GameLevel.TYPE_BOARDER_DOWN:
						obj=new GameHole(object_id++, pos,HoleType.Boarder_down, texture);
						break;
					case GameLevel.TYPE_BOARDER_UP: 
						obj=new GameHole(object_id++, pos,HoleType.Boarder_up, texture);
						break;
					case GameLevel.TYPE_BOARDER_LEFT: 
						obj=new GameHole(object_id++, pos,HoleType.Boarder_left, texture);
						break;
					case GameLevel.TYPE_BOARDER_RIGHT:
						obj=new GameHole(object_id++, pos,HoleType.Boarder_right, texture);
						break;
					case GameLevel.TYPE_CORNER_DOWN_LEFT:
						obj=new GameHole(object_id++, pos,HoleType.Corner_down_left, texture);
						break;
					case GameLevel.TYPE_CORNER_DOWN_RIGHT:
						obj=new GameHole(object_id++, pos,HoleType.Corner_down_right, texture);
						break;
					case GameLevel.TYPE_CORNER_UP_LEFT:
						obj=new GameHole(object_id++, pos,HoleType.Corner_up_left, texture);
						break;
					case GameLevel.TYPE_CORNER_UP_RIGHT:
						obj=new GameHole(object_id++, pos,HoleType.Corner_up_right, texture);
						break;
					case GameLevel.TYPE_HOLE:
						obj=new GameHole(object_id++, pos,HoleType.Hole_single, texture);
						break;
					case GameLevel.TYPE_HOLE_FULL:
						obj=new GameHole(object_id++, pos,HoleType.Hole_Full, texture);
						break;
					case GameLevel.TYPE_HOLE_CORNER_UP_RIGHT:
						obj=new GameHole(object_id++, pos,HoleType.Hole_Corner_up_right, texture);
						break;
					case GameLevel.TYPE_HOLE_CORNER_UP_LEFT:
						obj=new GameHole(object_id++, pos,HoleType.Hole_Corner_up_left, texture);
						break;
					case GameLevel.TYPE_HOLE_CORNER_DOWN_RIGHT:
						obj=new GameHole(object_id++, pos,HoleType.Hole_Corner_down_right, texture);
						break;
					case GameLevel.TYPE_HOLE_CORNER_DOWN_LEFT:
						obj=new GameHole(object_id++, pos,HoleType.Hole_Corner_down_left, texture);
						break;
					case GameLevel.TYPE_HOLE_HEAD_DOWN:
						obj=new GameHole(object_id++, pos,HoleType.Hole_Head_Down, texture);
						break;
					case GameLevel.TYPE_HOLE_HEAD_UP:
						obj=new GameHole(object_id++, pos,HoleType.Hole_Head_Up, texture);
						break;
					case GameLevel.TYPE_HOLE_HEAD_LEFT:
						obj=new GameHole(object_id++, pos,HoleType.Hole_Head_Left, texture);
						break;
					case GameLevel.TYPE_HOLE_HEAD_RIGHT:
						obj=new GameHole(object_id++, pos,HoleType.Hole_Head_Right, texture);
						break;
					case GameLevel.TYPE_WALL_HOR:
						obj=new GameWall(object_id++, pos,WallType.Wall_horizontal, texture);
						break;
					case GameLevel.TYPE_WALL_VERT:
						obj=new GameWall(object_id++, pos,WallType.Wall_vertical, texture);
						break;
					case GameLevel.TYPE_WALL_CORNER_UP_RIGHT:
						obj=new GameWall(object_id++, pos,WallType.Wall_Corner_up_right, texture);
						break;
					case GameLevel.TYPE_WALL_CORNER_UP_LEFT:
						obj=new GameWall(object_id++, pos,WallType.Wall_Corner_up_left, texture);
						break;
					case GameLevel.TYPE_WALL_CORNER_DOWN_RIGHT:
						obj=new GameWall(object_id++, pos,WallType.Wall_Corner_down_right, texture);
						break;
					case GameLevel.TYPE_WALL_CORNER_DOWN_LEFT:
						obj=new GameWall(object_id++, pos,WallType.Wall_Corner_down_left, texture);
						break;
						
					} 
					m_fg_objects[y*m_width+x] = obj;
				}
			}
		}
		return object_id;
	}
	
	public void draw(RenderHelper renderer) {
		Game.applyDefaultPosAndColor(renderer);
		//draw back- & foreground
		if(m_bg_objects != null) drawArray(m_bg_objects, renderer);
		drawArray(m_fg_objects, renderer);
	}
	
	private void drawArray(StaticGameObject[] objects, RenderHelper renderer) {
		
		for(int y=0; y<m_height; ++y) {
			for(int x=0; x<m_width; ++x) {
				StaticGameObject obj = objects[y*m_width+x];
				if(obj != null) obj.draw(renderer);
			}
		}
	}

	public void move(float dsec) {
		// nothing to do
	}

}
