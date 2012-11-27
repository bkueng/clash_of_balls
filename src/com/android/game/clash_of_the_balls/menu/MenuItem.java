package com.android.game.clash_of_the_balls.menu;

import com.android.game.clash_of_the_balls.Font2D;
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

	protected VertexBufferFloat m_color_data;
	protected VertexBufferFloat m_position_data;
	
	private Vector m_size = new Vector();
	
	public Vector size() { return m_size; }
	
	
	public MenuItem(Vector position, Vector size, Font2D font) {
		m_position.set(position);
		m_size.set(size);
		//TODO
		
	}
	
	
	//x, y: in screen coordinates
	public boolean isInside(float x, float y) {
		return x>=m_position.x && x<=m_position.x+m_size.x
				&& y>=m_position.y && y<=m_position.y+m_size.y;
	}
	
	public void draw(RenderHelper renderer) {
		// TODO Auto-generated method stub
		//m_menu_item_font.draw(renderer);
	}

	public void move(float dsec) {
		// TODO Auto-generated method stub
		
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
