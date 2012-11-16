package com.android.game.clash_of_the_balls.menu;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.GameObject;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;

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
		getViewport(renderer.screenWidth(), renderer.screenHeight()
				, m_position, m_size);
		
		//translate & scale to fit screen
		int model_mat_pos = renderer.pushModelMat();
		float model_mat[] = renderer.modelMat();
		Matrix.setIdentityM(model_mat, model_mat_pos);
		Matrix.translateM(model_mat, model_mat_pos, m_position.x, m_position.y, 0.f);
		Matrix.scaleM(model_mat, model_mat_pos, m_size.x, m_size.y, 0.f);
		
		
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

	@Override
	public void move(float dsec) {
		// nothing to do
	}
}
