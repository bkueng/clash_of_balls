package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.game.RenderHelper;

import android.content.Context;

/**
 * OpenGL texture with coordinates for a sprite
 *
 */
public class Texture extends TextureBase {
	private VertexBufferFloat m_tex_buffer;
	
	
	public Texture(Context activity_context, int raw_res_id) {
		super(activity_context, raw_res_id);
		
		init(null);
	}
	
	//tex_coords can be null to use default tex coords
	public Texture(TextureBase t, float[] tex_coords) {
		super(t);
		
		init(tex_coords);
	}
	
	public Texture(Context activity_context, int raw_res_id
			, float[] tex_coords) {
		super(activity_context, raw_res_id);
		
		init(tex_coords);
	}
	
	
	private void init(float[] tex_coords) {
		if(tex_coords == null) {
			//use default sprite tex coords
			tex_coords = VertexBufferFloat.sprite_tex_coords;
		}
		m_tex_buffer = new VertexBufferFloat(tex_coords, 2);
	}
	
	@Override
	public void useTexture(RenderHelper renderer) {
        super.useTexture(renderer);
        int tex_coord_handle=renderer.shaderManager().a_TexCoordinate_handle;
        if(tex_coord_handle != -1)
        	m_tex_buffer.apply(tex_coord_handle);
        
	}

}
