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

package com.sapos_aplastados.game.clash_of_balls.menu;

import android.graphics.Color;

import com.sapos_aplastados.game.clash_of_balls.R;
import com.sapos_aplastados.game.clash_of_balls.Font2D;
import com.sapos_aplastados.game.clash_of_balls.Texture;
import com.sapos_aplastados.game.clash_of_balls.TextureManager;
import com.sapos_aplastados.game.clash_of_balls.VertexBufferFloat;
import com.sapos_aplastados.game.clash_of_balls.Font2D.Font2DSettings;
import com.sapos_aplastados.game.clash_of_balls.game.RenderHelper;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;

public class MenuItemButton extends MenuItem {

	private String LOG_TAG = "debug";

	public Font2D m_item_font;
	
	private Texture m_texture_unpressed;
	private Texture m_texture_pressed;
	private boolean m_pressed=false;
	
	private boolean m_is_disabled=false;
	private float m_color_disabled[] = new float[4];

	public MenuItemButton(Vector position, Vector size
			, Font2DSettings font_settings, String font_string
			, TextureManager tex_manager) {
		super(position, size);
		
		m_item_font = new Font2D(tex_manager, size, font_settings, (int)Math.round(size.y * 0.7));
		m_item_font.setString(font_string);
		
		m_texture_pressed=tex_manager
				.get(R.raw.texture_main_menu_pressed_button, false);
		m_texture_unpressed=tex_manager
				.get(R.raw.texture_main_menu_unpressed_button, false);
		
		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		RenderHelper.initColorArray(0xffffffff, m_color);
		
		
		//disabled button color
		RenderHelper.initColorArray(0xff888888, m_color_disabled);
	}
	
	public void setString(String str) {
		m_item_font.setString(str);
	}

	public boolean isDisabled() { return m_is_disabled; }
	public void disable() { m_is_disabled=true; }
	public void enable() { m_is_disabled=false; }
	public void enable(boolean enabled) { m_is_disabled=!enabled; }
	
	public boolean isPressed() { return m_pressed; }
	
	private float m_tmp_color[] = new float[4];

	public void draw(RenderHelper renderer) {		
		Texture texture;
		if(m_pressed && !m_is_disabled){
			texture=m_texture_pressed;
		}else{
			texture=m_texture_unpressed;
		}
		
		float[] default_color = m_color;
		if(m_is_disabled) {
			float[] font_color = m_item_font.getColor();
			for(int i=0; i<4; ++i) m_tmp_color[i] = font_color[i];
			m_item_font.setColor(m_color_disabled);
			m_color = m_color_disabled;
		}
		
		renderer.pushModelMat();
		renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
		renderer.modelMatScale(m_size.x, m_size.y, 0.f);
		
		drawTexture(renderer, texture);
        
        // Render font
        m_item_font.draw(renderer);
        
        if(m_is_disabled) {
        	m_color = default_color;
        	m_item_font.setColor(m_tmp_color);
        }
        
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
