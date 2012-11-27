package com.android.game.clash_of_the_balls;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.helper.TextureHelper;

/**
 * OpenGL texture without coordinates
 *
 */
public class TextureBase {
	protected int m_tex_handle;
	private Context m_activity_context;
	private int m_raw_res_id;
	
	public int textureHandle() { return m_tex_handle; }
	
	public TextureBase(Context activity_context, int raw_res_id) {
		m_activity_context = activity_context;
		m_tex_handle = loadTexture(raw_res_id);
		m_raw_res_id = raw_res_id;
	}
	
	public TextureBase(Bitmap bitmap) {
		m_tex_handle = TextureHelper.loadTextureFromBitmap(bitmap);
	}
	
	public void reloadTexture() {
		m_tex_handle = loadTexture(m_raw_res_id);
	}
	
	private int loadTexture(int raw_res_id) {
        return TextureHelper.loadTexture(m_activity_context, raw_res_id);
	}
	
	public void useTexture(RenderHelper renderer) {
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_tex_handle);
	}
	
}
