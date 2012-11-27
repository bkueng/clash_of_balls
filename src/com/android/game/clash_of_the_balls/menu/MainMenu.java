package com.android.game.clash_of_the_balls.menu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.Font2D.TextAlign;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;
import com.android.game.clash_of_the_balls.game.Vector;

public class MainMenu extends GameMenuBase {
	
	private String LOG_TAG="debug";
	
	MenuItem m_host_button;
	MenuItem m_join_button;
	MenuItem m_help_button;
	MenuItem m_credits_button;
	
	public MainMenu(MenuBackground background
			, float screen_width, float screen_height,
			TextureManager m_tex_manager,Context context) {
		super(background, context);
		
		Vector pos=new Vector(0.f, 0.f);
		Vector size=new Vector(screen_width, screen_height);
		
		if(m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);
		
		
		//add menu items
		float button_width = size.x * 0.45f;
		float button_height=0.2f*button_width;
		float distanceButtons = screen_height/34.f;
		
		// prepare fonts
		// font constants
		int font_color = Color.WHITE;
		int font_size = (int)Math.round(0.5 * button_height);
		Typeface font_typeface = Typeface.createFromAsset(m_tex_manager.m_activity_context.getAssets(), "arial.ttf");
		
		Font2D host_font = new Font2D(m_tex_manager, font_typeface, "Host", font_size, TextAlign.CENTER, new Vector(button_width, button_height), font_color);
		
		m_menu_items.add(m_host_button = new MenuItemButton(
				new Vector(pos.x+size.x/2.f, pos.y+size.y*3.f/5.f),
				new Vector(button_width, button_height), 
				host_font,
				m_tex_manager));
		
		m_menu_items.add(m_join_button = new MenuItemButton(
				new Vector(pos.x+size.x/2.f,
						pos.y+size.y*3.f/5.f-(button_height+distanceButtons)),
				new Vector(button_width, button_height), 
				host_font,
				m_tex_manager));
		
		m_menu_items.add(m_help_button = new MenuItemButton(
				new Vector(pos.x+size.x/2.f, 
						pos.y+size.y*3.f/5.f-2*(button_height+distanceButtons)),
				new Vector(button_width, button_height), 
				host_font,
				m_tex_manager));
		
		m_menu_items.add(m_credits_button = new MenuItemButton(
				new Vector(pos.x+size.x/2.f, 
						pos.y+size.y*3.f/5.f-3*(button_height+distanceButtons)),
				new Vector(button_width, button_height), 
				host_font,
				m_tex_manager));
	}

	@Override
	protected void onTouchDown(MenuItem item) {
		// do nothing
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if(item == m_host_button) {
			m_ui_change = UIChange.CREATION_MENU;
		}else if(item == m_join_button){
			m_ui_change = UIChange.JOIN_MENU;
		}else if(item == m_help_button){
			
		}else if(item == m_credits_button){
			
		}else{
			Log.d(LOG_TAG,"No button pressed");
		}
	}

}
