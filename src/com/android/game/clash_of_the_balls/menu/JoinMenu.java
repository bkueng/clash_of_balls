package com.android.game.clash_of_the_balls.menu;

import android.content.Context;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.Font2D.Font2DSettings;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;

public class JoinMenu extends GameMenuBase {
	
	private String LOG_TAG="JoinMenu";
	
	private MenuItemButton m_join_button;
	private MenuItemButton m_cancel_button;
	
	private MenuItemList m_game_list;
	private float m_item_height;
	
	private TextureManager m_tex_manager;
	
	private Font2DSettings m_list_item_font_settings;
	
	
	public JoinMenu(MenuBackground background
			, float screen_width, float screen_height
			, TextureManager tex_manager, Context context
			, Font2D.Font2DSettings font_settings) {
		super(background,context);
		
		Vector pos=new Vector(0.f, 0.f);
		Vector size=new Vector(screen_width, screen_height);
		
		m_tex_manager = tex_manager;
		
		if(m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);

		//add menu items
		float button_width = size.x * 0.25f;
		float button_height=0.4f*button_width;
		
		float offset_y = size.y*0.025f;
		
		//game list
		m_item_height = button_height * 0.8f;
		m_list_item_font_settings = new Font2DSettings(font_settings.m_typeface
				, font_settings.m_align, font_settings.m_color);
		m_menu_items.add(m_game_list = new MenuItemList(
				new Vector(pos.x + offset_y, pos.y + offset_y), 
				new Vector(size.x * (2.f/3.f-0.025f) - offset_y, size.y - 2.f*offset_y), 
				new Vector(m_item_height*1.5f, m_item_height), 
				tex_manager));
		

		//right Column
		m_menu_items.add(m_cancel_button = new MenuItemButton(
				new Vector(pos.x+size.x* (0.025f+2.f/3.f), pos.y+offset_y),
				new Vector(button_width, button_height), 
				font_settings, "Cancel", tex_manager));
		m_menu_items.add(m_join_button = new MenuItemButton(
				new Vector(m_cancel_button.pos().x, 
						m_cancel_button.pos().y+button_height+offset_y),
				new Vector(button_width, button_height), 
				font_settings, "Join", tex_manager));

	}
	
	public void move(float dsec) {
		//update available games
		
		
		m_join_button.enable(m_game_list.getSelectedItem() != null);
	}
	
	private void addItem(String str) {
		m_game_list.addItem(new MenuItemString(new Vector()
			, new Vector(m_game_list.size().x, m_item_height)
			, m_list_item_font_settings, str, m_tex_manager));
	}

	@Override
	protected void onTouchDown(MenuItem item) {
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if(item == m_join_button) {
			if(!m_join_button.isDisabled()) {
				//selected entry

				m_ui_change = UIChange.WAIT_MENU;
			}
		}else if(item == m_cancel_button){
			m_ui_change = UIChange.MAIN_MENU;
		}
	}
	
	public void onDeactivate() {
		super.onDeactivate();
		//clear the list
		while(m_game_list.itemCount() > 0) m_game_list.removeItem(0);
	}

}
