package com.android.game.clash_of_the_balls.menu;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.Font2D.Font2DSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;


/**
 * simple menu item which draws a string and can be selected
 *
 */
public class MenuItemString extends MenuItem {

	private boolean m_is_selected=false;
	private Font2D m_item_font;
	
	private Texture m_normal_texture;
	private Texture m_selected_texture;
	
	public Object obj; //additional data connected with this object
	
	public MenuItemString(Vector position, Vector size, 
			Font2DSettings font_settings, String entry, TextureManager tex_manager) {
		
		super(position, size);
		m_item_font = new Font2D(tex_manager, m_size, font_settings
				, (int)(m_size.y * 0.5f));
		m_item_font.setString(entry);
		
		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		m_color_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_color_data_white, 4);
		
		m_normal_texture = tex_manager.get(R.raw.texture_grey_unpressed_button);
		m_selected_texture = tex_manager.get(R.raw.texture_grey_pressed_button);
		
	}
	
	
	public void setString(String str) { m_item_font.setString(str); }
	public String getString() { return m_item_font.getString(); }
	
	
	public void draw(RenderHelper renderer) {
		renderer.pushModelMat();
		renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
		renderer.modelMatScale(m_size.x, m_size.y, 0.f);
		
		if(m_is_selected)
			drawTexture(renderer, m_selected_texture);
		else
			drawTexture(renderer, m_normal_texture);
		
		m_item_font.draw(renderer);
		
        renderer.popModelMat();
	}
	
	public void select() {
		m_is_selected = true;
	}
	public void deselect() {
		m_is_selected = false;
	}
}
