package com.android.game.clash_of_the_balls.menu;

import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;

public class MenuItemGreyButton extends MenuItem {

	private String LOG_TAG = "debug";
		
	private Texture m_texture_unpressed;
	private Texture m_texture_pressed;
	private boolean m_pressed=false;
	
	private MenuItemStringMultiline m_label;

	public MenuItemGreyButton(Vector position, Vector size
			, TextureManager tex_manager, String label, 
			Font2D.Font2DSettings font_settings) {
		
		super(position, size);
		
		m_texture_pressed=tex_manager
				.get(R.raw.texture_grey_pressed_button);
		m_texture_unpressed=tex_manager
				.get(R.raw.texture_grey_unpressed_button);
		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		RenderHelper.initColorArray(0xffffffff, m_color);
		
		m_label = new MenuItemStringMultiline(
				m_position, m_size,
				font_settings, label, tex_manager);
	}
	
	public void setLabel(String label) {
		m_label.setString(label);
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
        
        m_label.draw(renderer);
	}

	public void select() {
		m_pressed =true;
	}
	
	public void deselect(){
		
	}

	public boolean isPressed(){return m_pressed;}

	public void remain_unpressed(){
		m_pressed=false;
	}
	
}
