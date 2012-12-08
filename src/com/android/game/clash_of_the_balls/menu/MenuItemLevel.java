package com.android.game.clash_of_the_balls.menu;

import android.opengl.Matrix;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.GameField;
import com.android.game.clash_of_the_balls.game.GameView;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;

/**
 * this shows a GameLevel in a MenuItem
 *
 */
public class MenuItemLevel extends MenuItem {
	
	private GameLevel m_level;
	private GameView m_game_view;
	private GameField m_game_field;
	
	Texture m_sel_texture;
	boolean m_is_selected=false;
	
	public GameLevel level() { return m_level; }

	public MenuItemLevel(Vector position, Vector size, GameLevel level
			, TextureManager texture_manager) {
		super(position, size);
		m_level = level;
		m_game_field = new GameField(texture_manager);
		m_game_field.init(m_level, (short)1);
		m_game_view = new GameView(size.x, size.y, null
				, m_level.width, m_level.height);
		
		
		m_sel_texture=texture_manager.get(R.raw.texture_selected_level);
		
		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		m_color_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_color_data_white, 4);
		
	}
	
	
	public void draw(RenderHelper renderer) {
		
		renderer.pushModelMat();
		renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
		
		m_game_view.applyView(renderer);
		m_game_field.draw(renderer);
		if(m_is_selected) {
			renderer.modelMatScale((float)m_level.width, 
					(float)m_level.height, 0.f);
			drawTexture(renderer, m_sel_texture);
		}
		m_game_view.resetView(renderer);
		
		renderer.popModelMat();
	}
	
	public void select() {
		m_is_selected = true;
		
	}
	public void deselect() {
		m_is_selected = false;
	}
	

}
