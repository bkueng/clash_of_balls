package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.TextureManager;
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
	
	public int width() { return m_width; }
	public int height() { return m_height; }
	
	
	StaticGameObject[] m_bg_objects;
	public StaticGameObject background(int x, int y) { return m_bg_objects[y*m_width+x]; }
	StaticGameObject[] m_fg_objects;
	public StaticGameObject foreground(int x, int y) { return m_fg_objects[y*m_width+x]; }
	
	
	public GameField(GameLevel level, TextureManager texture_manager) {
		m_width = level.width;
		m_height = level.height;
		
		//background
		if(texture_manager != null) {
			m_bg_objects = new StaticGameObject[m_width*m_height];
			for(int y=0; y<m_height; ++y) {
				for(int x=0; x<m_width; ++x) {
					int type = level.background(x, y);
					int raw_res_id = GameLevel.rawResTexIdFromBackground(type);
					if(raw_res_id != -1) {
						m_bg_objects[y*m_width+x] = new StaticGameObject(
								new Vector((float)x+0.5f, (float)y+0.5f),
								Type.Background,
								texture_manager.get(raw_res_id));
					}
				}
			}
		}
		
		//foreground
		m_fg_objects = new StaticGameObject[m_width*m_height];
		for(int y=0; y<m_height; ++y) {
			for(int x=0; x<m_width; ++x) {
				int type = level.foreground(x, y);
				int raw_res_id = GameLevel.rawResTexIdFromForeground(type);
				if(raw_res_id != -1) {
					Vector pos = new Vector((float)x+0.5f, (float)y+0.5f);
					StaticGameObject obj=null;
					/* TODO
					switch(type) {
					case GameLevel.TYPE_HOLE: 
						obj=new GameHole(pos, texture_manager.get(raw_res_id));
						break;
					} */
					m_fg_objects[y*m_width+x] = obj;
				}
			}
		}
	}
	
	@Override
	public void draw(RenderHelper renderer) {
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

	@Override
	public void move(float dsec) {
		// nothing to do
	}

}