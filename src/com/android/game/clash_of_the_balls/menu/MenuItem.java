package com.android.game.clash_of_the_balls.menu;

import android.opengl.GLES20;

import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.GameObject;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;

/**
 * defines a menu item with text and an optional background
 * can be pressed
 *
 */
public class MenuItem extends GameObject {

	protected float m_color[] = new float[4];
	protected VertexBufferFloat m_position_data;
	
	protected Vector m_size = new Vector();
	
	public Vector size() { return m_size; }
	
	
	public MenuItem(Vector position, Vector size) {
		m_position.set(position);
		m_size.set(size);
	}
	
	
	//x, y: in screen coordinates
	public boolean isInside(float x, float y) {
		return x>=m_position.x && x<=m_position.x+m_size.x
				&& y>=m_position.y && y<=m_position.y+m_size.y;
	}
	
	public void draw(RenderHelper renderer) {
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

	public void move(float dsec) {
		
	}
	
	public void onTouchDown(float x, float y) {
		select();
	}
	public void onTouchUp(float x, float y) {
		deselect();
	}
	
	public void select() {
	}
	
	public void deselect() {
	}

}
