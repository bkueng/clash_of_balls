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

package com.sapos_aplastados.game.clash_of_balls.game;

import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

import com.sapos_aplastados.game.clash_of_balls.GameLevel;
import com.sapos_aplastados.game.clash_of_balls.Texture;
import com.sapos_aplastados.game.clash_of_balls.TextureManager;
import com.sapos_aplastados.game.clash_of_balls.game.GameHole.HoleType;
import com.sapos_aplastados.game.clash_of_balls.game.GameWall.WallType;
import com.sapos_aplastados.game.clash_of_balls.game.StaticGameObject.Type;

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
	
	private final GameBase m_owner;
	
	public GameField(GameBase owner, TextureManager texture_manager) {
		m_owner = owner;
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
	public short init(GameLevel level, short next_object_id, World world) {
		m_width = level.width;
		m_height = level.height;
		m_fg_empty_idx_x = m_fg_empty_idx_y = null;
		
		BodyDef body_def = new BodyDef();
		
		//background
		if(m_texture_manager != null) {
			m_bg_objects = new StaticGameObject[m_width*m_height];
			for(int y=0; y<m_height; ++y) {
				for(int x=0; x<m_width; ++x) {
					int type = level.background(x, y);
					int raw_res_id = GameLevel.rawResTexIdFromBackground(type);
					if(raw_res_id != -1) {
						m_bg_objects[y*m_width+x] = new StaticGameObjectNoInteraction((short)-1,
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
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Boarder_down, texture, world, body_def);
						break;
					case GameLevel.TYPE_BOARDER_UP: 
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Boarder_up, texture, world, body_def);
						break;
					case GameLevel.TYPE_BOARDER_LEFT: 
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Boarder_left, texture, world, body_def);
						break;
					case GameLevel.TYPE_BOARDER_RIGHT:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Boarder_right, texture, world, body_def);
						break;
					case GameLevel.TYPE_CORNER_DOWN_LEFT:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Corner_down_left, texture, world, body_def);
						break;
					case GameLevel.TYPE_CORNER_DOWN_RIGHT:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Corner_down_right, texture, world, body_def);
						break;
					case GameLevel.TYPE_CORNER_UP_LEFT:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Corner_up_left, texture, world, body_def);
						break;
					case GameLevel.TYPE_CORNER_UP_RIGHT:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Corner_up_right, texture, world, body_def);
						break;
					case GameLevel.TYPE_HOLE:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Hole_single, texture, world, body_def);
						break;
					case GameLevel.TYPE_HOLE_FULL:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Hole_Full, texture, world, body_def);
						break;
					case GameLevel.TYPE_HOLE_CORNER_UP_RIGHT:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Hole_Corner_up_right, texture, world, body_def);
						break;
					case GameLevel.TYPE_HOLE_CORNER_UP_LEFT:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Hole_Corner_up_left, texture, world, body_def);
						break;
					case GameLevel.TYPE_HOLE_CORNER_DOWN_RIGHT:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Hole_Corner_down_right, texture, world, body_def);
						break;
					case GameLevel.TYPE_HOLE_CORNER_DOWN_LEFT:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Hole_Corner_down_left, texture, world, body_def);
						break;
					case GameLevel.TYPE_HOLE_HEAD_DOWN:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Hole_Head_Down, texture, world, body_def);
						break;
					case GameLevel.TYPE_HOLE_HEAD_UP:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Hole_Head_Up, texture, world, body_def);
						break;
					case GameLevel.TYPE_HOLE_HEAD_LEFT:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Hole_Head_Left, texture, world, body_def);
						break;
					case GameLevel.TYPE_HOLE_HEAD_RIGHT:
						obj=new GameHole(m_owner, object_id++, pos
								,HoleType.Hole_Head_Right, texture, world, body_def);
						break;
					case GameLevel.TYPE_WALL_HOR:
						obj=new GameWall(m_owner, object_id++, pos
								,WallType.Wall_horizontal, texture, world, body_def);
						break;
					case GameLevel.TYPE_WALL_VERT:
						obj=new GameWall(m_owner, object_id++, pos
								,WallType.Wall_vertical, texture, world, body_def);
						break;
					case GameLevel.TYPE_WALL_CROSS:
						obj=new GameWall(m_owner, object_id++, pos
								,WallType.Wall_cross, texture, world, body_def);
						break;
					case GameLevel.TYPE_WALL_CORNER_UP_RIGHT:
						obj=new GameWall(m_owner, object_id++, pos
								,WallType.Wall_Corner_up_right, texture, world, body_def);
						break;
					case GameLevel.TYPE_WALL_CORNER_UP_LEFT:
						obj=new GameWall(m_owner, object_id++, pos
								,WallType.Wall_Corner_up_left, texture, world, body_def);
						break;
					case GameLevel.TYPE_WALL_CORNER_DOWN_RIGHT:
						obj=new GameWall(m_owner, object_id++, pos
								,WallType.Wall_Corner_down_right, texture, world, body_def);
						break;
					case GameLevel.TYPE_WALL_CORNER_DOWN_LEFT:
						obj=new GameWall(m_owner, object_id++, pos
								,WallType.Wall_Corner_down_left, texture, world, body_def);
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
