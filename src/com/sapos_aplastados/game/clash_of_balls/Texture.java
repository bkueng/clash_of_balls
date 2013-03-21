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
package com.sapos_aplastados.game.clash_of_balls;

import com.sapos_aplastados.game.clash_of_balls.game.RenderHelper;

/**
 * OpenGL texture with coordinates for a sprite
 *
 */
public class Texture {
	private TextureBase m_texture;
	
	public int textureHandle() { return m_texture.textureHandle(); }
	
	//tex_coords can be null to use default tex coords
	public Texture(TextureBase t) {
		m_texture = t;
	}
	
	public void useTexture(RenderHelper renderer) {
        m_texture.useTexture(renderer);
        //here would the texture coords be applied, but we use the same for 
        //all textures so the default coordinates are applied in ShaderManager
        //when changing the shader
	}

}
