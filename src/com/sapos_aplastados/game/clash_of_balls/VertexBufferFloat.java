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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

/**
 * Contains an array of floats to be used as shader input
 *
 */

public class VertexBufferFloat {
	private FloatBuffer m_data;
	private static final int bytes_per_float = 4;
	private final int m_num_components_per_item;
	
	public static final float[] sprite_position_data = new float[] 
			{
			0.0f, 0.0f, 0.0f, // triangle strip: bottom left start, clockwise
			0.0f, 1.0f, 0.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 1.0f, 0.0f
			};
	
	public static final float[] sprite_tex_coords = new float[] 
			{ 
			0.0f, 0.0f, // triangle strip: bottom left start, clockwise
			0.0f, 1.0f,
			1.0f, 0.0f,
			1.0f, 1.0f
			};
	
	public VertexBufferFloat(float[] data, int num_components_per_item) {
		m_num_components_per_item = num_components_per_item;
		m_data = ByteBuffer.allocateDirect(
				data.length * bytes_per_float)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		m_data.put(data).position(0);
	}
	
	public FloatBuffer data() { return m_data; }
	
	
	//send data to shader
	public void apply(int shader_attr_handle) {
    	// Pass in the texture coordinate information
		m_data.position(0);
    	GLES20.glVertexAttribPointer(shader_attr_handle
    			, m_num_components_per_item, GLES20.GL_FLOAT, false, 
    			0, m_data);

    	GLES20.glEnableVertexAttribArray(shader_attr_handle);
	}
}
