package com.android.game.clash_of_the_balls.menu;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.GameObject;
import com.android.game.clash_of_the_balls.game.RenderHelper;

/**
 * Background of a Menu: spans a texture over the screen
 *
 */
public class MenuBackground extends GameObject {
	
	private Texture m_texture;
	private float m_aspect_ratio;
	
	public void setAspect(float aspect) { m_aspect_ratio = aspect; }
	
	private VertexBufferFloat m_color_data;
	private VertexBufferFloat m_position_data;
	
	//aspect_ratio is texture width/height
	public MenuBackground(Texture t, float aspect_ratio) {
		m_texture = t;
		m_aspect_ratio = aspect_ratio;
		
		m_position_data = new VertexBufferFloat(VertexBufferFloat.sprite_position_data, 3);
		m_color_data = new VertexBufferFloat(VertexBufferFloat.sprite_color_data_white, 4);
	}

	@Override
	public void draw(RenderHelper renderer) {
		//texture
		renderer.shaderManager().activateTexture(0);
		m_texture.useTexture(renderer);
		
		//position & size
		//touch screen border from inside
		float screen_aspect = renderer.screenWidth() / renderer.screenHeight();
		float width, height, x, y;
		if(screen_aspect > m_aspect_ratio) {
			height = renderer.screenHeight();
			y = 0.f;
			width = m_aspect_ratio * renderer.screenHeight();
			x = (renderer.screenWidth()-width) / 2.f;
		} else {
			width = renderer.screenWidth();
			x = 0.f;
			height = renderer.screenWidth() / m_aspect_ratio;
			y = (renderer.screenHeight()-height) / 2.f;
		}
		
		//translate & scale to fit screen
		int model_mat_pos = renderer.pushModelMat();
		float model_mat[] = renderer.modelMat();
		Matrix.setIdentityM(model_mat, model_mat_pos);
		Matrix.translateM(model_mat, model_mat_pos, x, y, 0.f);
		Matrix.scaleM(model_mat, model_mat_pos, width, height, 0.f);
		
		
		int position_handle = renderer.shaderManager().a_Position_handle;
		if(position_handle != -1)
			m_position_data.apply(position_handle);
		
        
        // color
		int color_handle = renderer.shaderManager().a_Color_handle;
		if(color_handle != -1)
			m_color_data.apply(color_handle);      
        
		
		renderer.apply();
		
        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 2*3);                               
        
        renderer.popModelMat();
	}

	@Override
	public void move(float dsec) {
		// nothing to do
	}
}
