package com.android.game.clash_of_the_balls.menu;

import android.graphics.Color;
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

public class MenuItemImg extends MenuItem {

	private String LOG_TAG = "debug";

	public Font2D m_item_font;
	
	private Texture m_texture;

	public MenuItemImg(Vector position, Vector size
			, TextureManager tex_manager,int raw_id) {
		super(position, size);

		m_texture=tex_manager
				.get(raw_id);

		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		m_color_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_color_data_white, 4);
	}

	public void draw(RenderHelper renderer) {		
		int model_mat_pos = renderer.pushModelMat();
		float model_mat[] = renderer.modelMat();
		Matrix.translateM(model_mat, model_mat_pos, m_position.x, m_position.y, 0.f);
		Matrix.scaleM(model_mat, model_mat_pos, this.size().x, this.size().y, 0.f);
		
		drawTexture(renderer, m_texture);

        renderer.popModelMat();
	}

	public void move(float dsec) {
		// nothing to do
	}
	public void select() {
	}
	
	public void deselect(){
	}

}
