package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.game.RenderHelper;

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
