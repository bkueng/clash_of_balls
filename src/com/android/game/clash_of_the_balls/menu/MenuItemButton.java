package com.android.game.clash_of_the_balls.menu;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.android.game.clash_of_the_balls.Font2D.Font2DSettings;
import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;

public class MenuItemButton extends MenuItem {

	private String LOG_TAG = "debug";

	public Font2D m_item_font;
	
	private Texture m_texture_unpressed;
	private Texture m_texture_pressed;
	private boolean m_pressed=false;

	public MenuItemButton(Vector position, Vector size
			, Font2DSettings font_settings, String font_string
			, TextureManager m_tex_manager) {
		super(position, size);
		
		m_item_font = new Font2D(m_tex_manager, size, font_settings, (int)Math.round(size.y * 0.7));
		m_item_font.setString(font_string);
		
		m_texture_pressed=m_tex_manager
				.get(R.raw.texture_main_menu_pressed_button);
		m_texture_unpressed=m_tex_manager
				.get(R.raw.texture_main_menu_unpressed_button);
		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		m_color_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_color_data_white, 4);
	}

	public boolean isPressed(){return m_pressed;}

	public void draw(RenderHelper renderer) {		
		Texture texture;
		if(m_pressed){
			texture=m_texture_pressed;
		}else{
			texture=m_texture_unpressed;
		}
		
		int model_mat_pos = renderer.pushModelMat();
		float model_mat[] = renderer.modelMat();
		Matrix.translateM(model_mat, model_mat_pos, m_position.x, m_position.y, 0.f);
		Matrix.scaleM(model_mat, model_mat_pos, this.size().x, this.size().y, 0.f);
		
		drawTexture(renderer, texture);
        
        // Render font
        m_item_font.draw(renderer);
        
        renderer.popModelMat();
	}

	public void move(float dsec) {
		// nothing to do
	}
	public void select() {
		m_pressed =true;
	}
	
	public void deselect(){
		m_pressed =false;
	}

}
