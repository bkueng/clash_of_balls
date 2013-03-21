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

import android.opengl.GLES20;

import com.sapos_aplastados.game.clash_of_balls.Texture;
import com.sapos_aplastados.game.clash_of_balls.VertexBufferFloat;
import com.sapos_aplastados.game.clash_of_balls.game.GameObject;
import com.sapos_aplastados.game.clash_of_balls.game.RenderHelper;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;

/**
 * Background of a Menu: spans a texture over the screen
 *
 */
public class MenuBackground extends GameObject {
	
	private Texture m_texture;
	private float m_aspect_ratio;
	private Vector m_size=new Vector();
	
	public void setAspect(float aspect) { m_aspect_ratio = aspect; }
	public float aspect() { return m_aspect_ratio; }
	
	private float m_color[] = new float[4];
	private VertexBufferFloat m_position_data;
	
	//aspect_ratio is texture width/height
	public MenuBackground(Texture t, float aspect_ratio) {
		m_texture = t;
		m_aspect_ratio = aspect_ratio;
		
		m_position_data = new VertexBufferFloat(VertexBufferFloat.sprite_position_data, 3);
		RenderHelper.initColorArray(0xffffffff, m_color);
	}

	public void draw(RenderHelper renderer) {
		//texture
		renderer.shaderManager().activateTexture(0);
		m_texture.useTexture(renderer);
		
		//position & size
		getViewport(renderer.m_screen_width, renderer.m_screen_height
				, m_position, m_size);
		
		//translate & scale to fit screen
		renderer.pushModelMat();
		renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
		renderer.modelMatScale(m_size.x, m_size.y, 0.f);
		
		int position_handle = renderer.shaderManager().a_Position_handle;
		if(position_handle != -1)
			m_position_data.apply(position_handle);
		
        
        // color
		int color_handle = renderer.shaderManager().u_Color_handle;
		if(color_handle != -1)
			GLES20.glUniform4fv(color_handle, 1, m_color, 0);
        
		
		renderer.apply();
		
        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);                               
        
        renderer.popModelMat();
	}
	
	//get menu viewport: this depends on screen aspect ratio
	public void getViewport(float screen_width, float screen_height
			, Vector pos, Vector size) {
		
		//touch screen border from inside
		float screen_aspect = screen_width / screen_height;
		if(screen_aspect > m_aspect_ratio) {
			size.y = screen_height;
			pos.y = 0.f;
			size.x = m_aspect_ratio * screen_height;
			pos.x = (screen_width-size.x) / 2.f;
		} else {
			size.x = screen_width;
			pos.x = 0.f;
			size.y = screen_width / m_aspect_ratio;
			pos.y = (screen_height-size.y) / 2.f;
		}
	}

	public void move(float dsec) {
		// nothing to do
	}
}
