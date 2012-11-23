package com.android.game.clash_of_the_balls.menu;

import android.content.Context;
import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;

public class CreationMenu extends GameMenuBase {
	
	private String LOG_TAG="debug";
	
	private GameSettings m_settings;
	
	MenuItem m_create_button;
	MenuItem m_cancel_button;
	
	MenuItemKeyboard m_name_button;
	
	//Grp1 of Buttons: Levels
	MenuItemGreyButton m_first_lvl_button;
	MenuItemGreyButton m_second_lvl_button;
//	MenuItemGreyButton m_thrid_lvl_button;
	
	//Grp2 of Buttons: Rounds
	MenuItemGreyButton m_1round_button;
	MenuItemGreyButton m_2rounds_button;
	MenuItemGreyButton m_4rounds_button;
	MenuItemGreyButton m_10rounds_button;
	
	public CreationMenu(Font2D item_font, MenuBackground background
			, float screen_width, float screen_height,
			TextureManager m_tex_manager,GameSettings settings,Context context) {
		super(item_font, background,context);
		
		Vector pos=new Vector(0.f, 0.f);
		Vector size=new Vector(screen_width, screen_height);
		
		m_settings=settings;
		
		
		if(m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);
		
		//add menu items
		float button_width = size.x * 0.45f;
		float button_height=0.2f*button_width;
		float distanceButtons = screen_height/34.f;

		float grey_button_width = size.x*0.1f;
		float grey_button_height = grey_button_width;
		
		float offset_y = size.y*0.025f;
		
		//Name
		m_menu_items.add(m_name_button = new MenuItemKeyboard(
				new Vector(pos.x+size.x/3.f, pos.y+size.y*3.f/4.f+offset_y),
				new Vector(5*grey_button_width, grey_button_height), 
				m_item_font,
				m_tex_manager,
				m_activity_context,
				"Please Enter your Nickname:"));
		
		
		
		//Group 1
		m_menu_items.add(m_first_lvl_button = new MenuItemGreyButton(
				new Vector(pos.x+size.x/3.f, pos.y+size.y/2.f+offset_y),
				new Vector(grey_button_width, grey_button_height), 
				m_item_font,
				m_tex_manager));
		
		m_menu_items.add(m_second_lvl_button = new MenuItemGreyButton(
				new Vector(pos.x+size.x/3.f+grey_button_width+distanceButtons, 
						pos.y+size.y/2.f+offset_y),
				new Vector(grey_button_width, grey_button_height), 
				m_item_font,
				m_tex_manager));
		
		
		//Group 2
		m_menu_items.add(m_1round_button = new MenuItemGreyButton(
				new Vector(pos.x+size.x/3.f, pos.y+size.y/4.f+offset_y),
				new Vector(grey_button_width, grey_button_height), 
				m_item_font,
				m_tex_manager));
		
		m_menu_items.add(m_2rounds_button = new MenuItemGreyButton(
				new Vector(pos.x+size.x/3.f+grey_button_width+distanceButtons,
						pos.y+size.y/4.f+offset_y),
				new Vector(grey_button_width, grey_button_height), 
				m_item_font,
				m_tex_manager));
		
		m_menu_items.add(m_4rounds_button = new MenuItemGreyButton(
				new Vector(pos.x+size.x/3.f+2*(grey_button_width+distanceButtons),
						pos.y+size.y/4.f+offset_y),
				new Vector(grey_button_width, grey_button_height), 
				m_item_font,
				m_tex_manager));
		
		m_menu_items.add(m_10rounds_button = new MenuItemGreyButton(
				new Vector(pos.x+size.x/3.f+3*(grey_button_width+distanceButtons),
						pos.y+size.y/4.f+offset_y),
				new Vector(grey_button_width, grey_button_height), 
				m_item_font,
				m_tex_manager));
		
		//Last Line
		m_menu_items.add(m_cancel_button = new MenuItemButton(
				new Vector(pos.x+size.x*(1/2.f+0.025f), pos.y+offset_y),
				new Vector(button_width, button_height), 
				m_item_font,
				m_tex_manager));
		
		m_menu_items.add(m_create_button = new MenuItemButton(
				new Vector(pos.x+size.x * 0.025f, pos.y+offset_y),
				new Vector(button_width, button_height), 
				m_item_font,
				m_tex_manager));
	}

	@Override
	protected void onTouchDown(MenuItem item) {
		 if(item == m_first_lvl_button){
				m_second_lvl_button.unpress();
			}else if(item == m_second_lvl_button){
				m_first_lvl_button.unpress();
			}else if(item == m_1round_button){
				m_2rounds_button.unpress();
				m_4rounds_button.unpress();
				m_10rounds_button.unpress();
			}else if(item == m_2rounds_button){
				m_1round_button.unpress();
				m_4rounds_button.unpress();
				m_10rounds_button.unpress();
			}else if(item == m_4rounds_button){
				m_1round_button.unpress();
				m_2rounds_button.unpress();
				m_10rounds_button.unpress();
			}else if(item == m_10rounds_button){
				m_2rounds_button.unpress();
				m_4rounds_button.unpress();
				m_1round_button.unpress();
			}
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if(item == m_create_button) {
			m_settings.is_host=true;
			m_ui_change = UIChange.WAIT_MENU;
		}else if(item == m_cancel_button){
			m_settings.is_host=false;
			m_ui_change = UIChange.MAIN_MENU;
		}
	}

}
