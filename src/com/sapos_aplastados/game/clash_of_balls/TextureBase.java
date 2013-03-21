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

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.sapos_aplastados.game.clash_of_balls.game.RenderHelper;
import com.sapos_aplastados.game.clash_of_balls.helper.TextureHelper;

/**
 * OpenGL texture without coordinates
 *
 */
public class TextureBase {
	protected int m_tex_handle;
	private Context m_activity_context;
	private int m_raw_res_id;
	private boolean m_use_mipmapping;
	
	public int textureHandle() { return m_tex_handle; }
	
	public TextureBase(Context activity_context, int raw_res_id, boolean use_mipmapping) {
		m_activity_context = activity_context;
		m_use_mipmapping = use_mipmapping;
		m_tex_handle = loadTexture(raw_res_id);
		m_raw_res_id = raw_res_id;
	}
	
	public TextureBase(Bitmap bitmap, boolean use_mipmapping) {
		m_use_mipmapping = use_mipmapping;
		m_tex_handle = TextureHelper.loadTextureFromBitmap(bitmap, use_mipmapping);
	}
	
	public void reloadTexture() {
		m_tex_handle = loadTexture(m_raw_res_id);
	}
	
	private int loadTexture(int raw_res_id) {
        return TextureHelper.loadTexture(m_activity_context, raw_res_id, m_use_mipmapping);
	}
	
	public void useTexture(RenderHelper renderer) {
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_tex_handle);
	}
	
}
