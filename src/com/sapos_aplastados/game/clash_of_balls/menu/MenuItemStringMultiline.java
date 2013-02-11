package com.sapos_aplastados.game.clash_of_balls.menu;

import com.sapos_aplastados.game.clash_of_balls.R;
import com.sapos_aplastados.game.clash_of_balls.Font2D;
import com.sapos_aplastados.game.clash_of_balls.Texture;
import com.sapos_aplastados.game.clash_of_balls.TextureManager;
import com.sapos_aplastados.game.clash_of_balls.VertexBufferFloat;
import com.sapos_aplastados.game.clash_of_balls.Font2D.Font2DSettings;
import com.sapos_aplastados.game.clash_of_balls.game.RenderHelper;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;

/**
 * menu item to draw a multiline text without background, not selectable
 * string lines are separated by '\n' 
 * font size depends on the line count. the more lines the smaller the font size
 *
 */
public class MenuItemStringMultiline extends MenuItem {
	
	private Font2D m_item_font[];
	private Vector m_font_size;
	
	private TextureManager m_tex_manager;
	private Font2DSettings m_font_settings;
	
	public MenuItemStringMultiline(Vector position, Vector size, 
			Font2DSettings font_settings, String entry, TextureManager tex_manager) {
		
		super(position, size);
		
		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		RenderHelper.initColorArray(0xffffffff, m_color);
		
		m_font_settings = font_settings;
		m_tex_manager = tex_manager;
		
		setString(entry);
	}
	
	public void setString(String str) {
		String lines[] = str.split("\n");
		
		if(lines.length > 0) {
			m_item_font = new Font2D[lines.length];
			m_font_size = new Vector(m_size);
			m_font_size.y = m_size.y / (float)lines.length;
			
			for(int i=0; i<m_item_font.length; ++i) {
				m_item_font[i] = new Font2D(m_tex_manager, m_font_size, m_font_settings
						, (int)(m_font_size.y * 0.6f));
				m_item_font[i].setString(lines[i]);
			}
			
		} else {
			m_item_font = null;
		}
	}
	
	
	public void draw(RenderHelper renderer) {
		if(m_item_font != null) {
			float y = m_position.y;
			for(int i=0; i<m_item_font.length; ++i) {
				renderer.pushModelMat();
				renderer.modelMatTranslate(m_position.x, y, 0.f);
				renderer.modelMatScale(m_font_size.x, m_font_size.y, 0.f);

				m_item_font[m_item_font.length-i-1].draw(renderer);

				renderer.popModelMat();
				y += m_font_size.y;
			}
		}
	}
}
