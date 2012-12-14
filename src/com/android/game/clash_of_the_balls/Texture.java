package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.game.RenderHelper;

/**
 * OpenGL texture with coordinates for a sprite
 *
 */
public class Texture {
	private TextureBase m_texture;
	private VertexBufferFloat m_tex_buffer;
	
	public int textureHandle() { return m_texture.textureHandle(); }
	
	//tex_coords can be null to use default tex coords
	public Texture(TextureBase t, float[] tex_coords) {
		m_texture = t;
		
		initCoords(tex_coords);
	}
	
	private void initCoords(float[] tex_coords) {
		if(tex_coords == null) {
			//use default sprite tex coords
			tex_coords = VertexBufferFloat.sprite_tex_coords;
		}
		m_tex_buffer = new VertexBufferFloat(tex_coords, 2);
	}
	
	public void useTexture(RenderHelper renderer) {
        m_texture.useTexture(renderer);
        int tex_coord_handle=renderer.shaderManager().a_TexCoordinate_handle;
        if(tex_coord_handle != -1)
        	m_tex_buffer.apply(tex_coord_handle);
        
	}

}
