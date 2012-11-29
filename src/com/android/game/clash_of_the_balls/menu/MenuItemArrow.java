package com.android.game.clash_of_the_balls.menu;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.Toast;

import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;

public class MenuItemArrow extends MenuItem {

	private static final String LOG_TAG = "MenuItemArrow";
	
	private Texture m_texture_unpressed;
	private Texture m_texture_pressed;
	private boolean m_pressed=false;
	
	public enum ArrowType {
		RIGHT,
		LEFT
	}
	
	
	public MenuItemArrow(Vector position, Vector size
			,TextureManager m_tex_manager,ArrowType type) {
		super(position, size);

		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		m_color_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_color_data_white, 4);
		switch(type){
		case RIGHT:
			m_texture_pressed=m_tex_manager
				.get(R.raw.texture_presiright_pressed_button);
			m_texture_unpressed=m_tex_manager
				.get(R.raw.texture_presiright_unpressed_button);
		break;
		case LEFT:
			m_texture_pressed=m_tex_manager
				.get(R.raw.texture_presileft_pressed_button);
			m_texture_unpressed=m_tex_manager
				.get(R.raw.texture_presileft_unpressed_button);
		break;
		}

	}

	public void select() {
		m_pressed =true;
	}
	
	public void deselect(){
		m_pressed=false;
	}
	
	
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
		
        renderer.popModelMat();
	}

}
