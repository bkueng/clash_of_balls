package com.sapos_aplastados.game.clash_of_balls.menu;

import android.opengl.GLES20;

import com.sapos_aplastados.game.clash_of_balls.R;
import com.sapos_aplastados.game.clash_of_balls.Texture;
import com.sapos_aplastados.game.clash_of_balls.TextureManager;
import com.sapos_aplastados.game.clash_of_balls.VertexBufferFloat;
import com.sapos_aplastados.game.clash_of_balls.game.RenderHelper;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;

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
		RenderHelper.initColorArray(0xffffffff, m_color);
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
		
		renderer.pushModelMat();
		renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
		renderer.modelMatScale(m_size.x, m_size.y, 0.f);
		
		drawTexture(renderer, texture);
		
        renderer.popModelMat();
	}

}
