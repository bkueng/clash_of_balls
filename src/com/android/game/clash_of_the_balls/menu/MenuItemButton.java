package com.android.game.clash_of_the_balls.menu;

import android.graphics.Color;

import com.android.game.clash_of_the_balls.Font2D.Font2DSettings;
import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;

public class MenuItemButton extends MenuItem {

	private String LOG_TAG = "debug";

	public Font2D m_item_font;
	
	private Texture m_texture_unpressed;
	private Texture m_texture_pressed;
	private boolean m_pressed=false;
	
	private boolean m_is_disabled=false;
	private VertexBufferFloat m_color_data_disabled;

	public MenuItemButton(Vector position, Vector size
			, Font2DSettings font_settings, String font_string
			, TextureManager tex_manager) {
		super(position, size);
		
		m_item_font = new Font2D(tex_manager, size, font_settings, (int)Math.round(size.y * 0.7));
		m_item_font.setString(font_string);
		
		m_texture_pressed=tex_manager
				.get(R.raw.texture_main_menu_pressed_button);
		m_texture_unpressed=tex_manager
				.get(R.raw.texture_main_menu_unpressed_button);
		
		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		m_color_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_color_data_white, 4);
		
		
		//disabled button color
		final int color = 0xff888888;
		float color_data[] = new float[4*4];
		for(int i=0; i<4; ++i) {
			color_data[i*4 + 0] = (float)Color.red(color) / 255.f;
			color_data[i*4 + 1] = (float)Color.green(color) / 255.f;
			color_data[i*4 + 2] = (float)Color.blue(color) / 255.f;
			color_data[i*4 + 3] = (float)Color.alpha(color) / 255.f;
		}
		m_color_data_disabled = new VertexBufferFloat(color_data, 4);
	}
	
	public void setString(String str) {
		m_item_font.setString(str);
	}

	public boolean isDisabled() { return m_is_disabled; }
	public void disable() { m_is_disabled=true; }
	public void enable() { m_is_disabled=false; }
	public void enable(boolean enabled) { m_is_disabled=!enabled; }
	
	public boolean isPressed(){return m_pressed;}

	public void draw(RenderHelper renderer) {		
		Texture texture;
		if(m_pressed && !m_is_disabled){
			texture=m_texture_pressed;
		}else{
			texture=m_texture_unpressed;
		}
		
		VertexBufferFloat color_data_tmp = m_color_data;
		VertexBufferFloat color_data_tmp_font = m_item_font.colorData();
		if(m_is_disabled) {
			m_color_data = m_color_data_disabled;
			m_item_font.setColorData(m_color_data_disabled);
		}
		
		renderer.pushModelMat();
		renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
		renderer.modelMatScale(m_size.x, m_size.y, 0.f);
		
		drawTexture(renderer, texture);
        
        // Render font
        m_item_font.draw(renderer);
        
        m_color_data = color_data_tmp;
        m_item_font.setColorData(color_data_tmp_font);
        
        renderer.popModelMat();
	}

	public void move(float dsec) {
		// nothing to do
	}
	public void select() {
		m_pressed =true;
	}
	
	public void deselect(){
		m_pressed =false;
	}

}
