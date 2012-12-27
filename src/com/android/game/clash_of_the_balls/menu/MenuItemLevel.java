package com.android.game.clash_of_the_balls.menu;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.Font2D.TextAlign;
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
	
	private MenuItemStringMultiline m_player; //max player count
	
	Texture m_sel_texture;
	boolean m_is_selected=false;
	
	public GameLevel level() { return m_level; }

	public MenuItemLevel(Vector position, Vector size, GameLevel level
			, TextureManager texture_manager, Font2D.Font2DSettings font_settings) {
		super(position, size);
		
		Font2D.Font2DSettings label_font_settings = new Font2D.Font2DSettings(
				font_settings.m_typeface, TextAlign.LEFT, font_settings.m_color);
		
		Vector player_size = new Vector(size.x/3.f,size.y*0.8f);
		m_player = new MenuItemStringMultiline(new Vector(size.x*2.f/3.f,
					(size.y - player_size.y)/2.f),
				player_size, label_font_settings, 
				"max. "+level.player_count+"\nPlayers", texture_manager);
		
		m_level = level;
		m_game_field = new GameField(null, texture_manager);
		World world = new World(new Vec2(0.f, 0.f), true);
		m_game_field.init(m_level, (short)1, world);
		m_game_view = new GameView(size.x - m_player.size().x, size.y, null
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
		
		m_player.draw(renderer);
		renderer.popModelMat();
	}
	
	public void select() {
		m_is_selected = true;
		
	}
	public void deselect() {
		m_is_selected = false;
	}
	

}
