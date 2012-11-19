package com.android.game.clash_of_the_balls.menu;

import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;
import com.android.game.clash_of_the_balls.game.Vector;

public class MainMenu extends GameMenuBase {
	
	MenuItem m_test_button;
	
	public MainMenu(Font2D item_font, MenuBackground background
			, float screen_width, float screen_height) {
		super(item_font, background);
		
		Vector pos=new Vector(0.f, 0.f);
		Vector size=new Vector(screen_width, screen_height);
		
		if(m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);
		
		
		//add menu items
		m_menu_items.add(m_test_button = new MenuItem(
				new Vector(pos.x+size.x / 2.f, pos.y),
				new Vector(size.x / 2.f, size.y / 2.f), 
				m_item_font));
		
		
	}

	@Override
	protected void onTouchDown(MenuItem item) {
		// do nothing
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if(item == m_test_button) {
			//m_ui_change = UIChange.new_menu;
		}
	}

}
