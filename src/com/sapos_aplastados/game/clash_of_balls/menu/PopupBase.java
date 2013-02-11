package com.sapos_aplastados.game.clash_of_balls.menu;

import com.sapos_aplastados.game.clash_of_balls.R;
import com.sapos_aplastados.game.clash_of_balls.Texture;
import com.sapos_aplastados.game.clash_of_balls.TextureManager;
import com.sapos_aplastados.game.clash_of_balls.VertexBufferFloat;
import com.sapos_aplastados.game.clash_of_balls.game.RenderHelper;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;

import android.content.Context;
import android.opengl.GLES20;

/**
 * base class for a popup menu as an overlay over a menu/game
 * 
 * when a popup is shown over a menu, the movement of the menu is not stopped
 * but all input events only go to the popup
 * 
 * a fixed aspect ratio is used
 *
 */
public class PopupBase extends GameMenuBase {
	
	public static final float width_scaling = 0.8f; //output width = scaling * screen width
	public static final float aspect_ratio = 1.66666f; //output height = output width / aspect
	
	protected float m_color[] = new float[4];
	protected VertexBufferFloat m_position_data;
	
	private Texture m_background_texture;
	protected Vector m_position;
	protected Vector m_size;

	public PopupBase(Context context, TextureManager tex_manager
			, float screen_width, float screen_height) {
		super(null, context);
		
		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		RenderHelper.initColorArray(0xffffffff, m_color);
		
		m_background_texture = tex_manager.get(R.raw.texture_popup_bg);
		
		float width = screen_width * width_scaling;
		float height = width/aspect_ratio;
		if(height > screen_height) {
			height = screen_height;
			width = height * aspect_ratio;
		}
		m_size = new Vector(width , height);
		m_position = new Vector((screen_width-m_size.x)/2.f, (screen_height-m_size.y)/2.f);
	}
	
	public void draw(RenderHelper renderer) {
		renderer.pushModelMat();
		renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
		renderer.modelMatScale(m_size.x, m_size.y, 0.f);
		
		drawTexture(renderer, m_background_texture);
        
        renderer.popModelMat();
        
        super.draw(renderer);
	}
	
	protected void drawTexture(RenderHelper renderer, Texture texture) {
		renderer.shaderManager().activateTexture(0);
		texture.useTexture(renderer);
		// position
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
	}

	protected void onTouchDown(MenuItem item) {
	}

	protected void onTouchUp(MenuItem item) {
	}

	public void onBackButtonPressed() {
		//we do nothing here. this event is handled inside the menus
		//and not in the popup's
	}

}
