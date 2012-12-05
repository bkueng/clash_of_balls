package com.android.game.clash_of_the_balls;

import android.content.Context;

import com.android.game.clash_of_the_balls.Font2D.TextAlign;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.menu.GameMenuBase;
import com.android.game.clash_of_the_balls.menu.MenuBackground;
import com.android.game.clash_of_the_balls.menu.MenuItem;
import com.android.game.clash_of_the_balls.menu.MenuItemImg;
import com.android.game.clash_of_the_balls.menu.MenuItemStringMultiline;
import com.android.game.clash_of_the_balls.network.NetworkClient;

public class HelpMenu extends GameMenuBase {

	private TextureManager m_tex_manager;
	
	private MenuItemStringMultiline m_first_label;
	private MenuItemImg m_first_img;
	private MenuItemStringMultiline m_second_label;
	private MenuItemImg m_second_img;
	private MenuItemStringMultiline m_third_label;
	private MenuItemImg m_third_img;
	
	public HelpMenu(MenuBackground background
			, float screen_width, float screen_height
			, TextureManager tex_manager, Context context
			, Font2D.Font2DSettings font_settings, int label_font_color
			, GameSettings settings
			, NetworkClient network_client) {
		super(background,context);
		
		m_tex_manager = tex_manager;
		
		Vector pos=new Vector(0.f, 0.f);
		Vector size=new Vector(screen_width, screen_height);
		
		Font2D.Font2DSettings label_font_settings 
		= new Font2D.Font2DSettings(font_settings.m_typeface,
			TextAlign.RIGHT, label_font_color);
		
		//add menu items
		float label_width = size.x * 0.35f;
		float button_height=0.25f*label_width;
		float label_height = 2.f*button_height;
		
		
		float offset_x = size.y*0.1f;
		float offset_y = offset_x*0.5f;
		
		m_menu_items.add(m_first_label = new MenuItemStringMultiline(
				new Vector(pos.x + offset_x
						, pos.y + size.y - label_height-offset_y),
				new Vector(1.2f*label_width, label_height),
				label_font_settings, " Push the others\ninto the holes", m_tex_manager));
		
		m_menu_items.add(m_first_img = new MenuItemImg(
				new Vector(pos.x + offset_x+label_width+3*offset_x
						, pos.y + size.y - label_height-offset_y),
				new Vector(label_width, label_height),
				m_tex_manager, R.raw.texture_ball_up));
		
		m_menu_items.add(m_second_label = new MenuItemStringMultiline(
				new Vector(pos.x + offset_x+label_width+offset_x
						, pos.y + size.y -2*(label_height)-offset_y),
				new Vector(1.5f*label_width, label_height),
				 new Font2D.Font2DSettings(font_settings.m_typeface,
							TextAlign.LEFT, label_font_color), "Control your ball\n moving your phone!", m_tex_manager));
		
		m_menu_items.add(m_second_img = new MenuItemImg(
				new Vector(pos.x + offset_x
						, pos.y + size.y - 2*(label_height)-offset_y),
				new Vector(label_width, label_height),
				m_tex_manager, R.raw.texture_ball_up));
		
		m_menu_items.add(m_third_label = new MenuItemStringMultiline(
				new Vector(pos.x + 2*offset_x
						, pos.y + size.y -3*(label_height)-offset_y),
				new Vector(label_width, label_height),
				label_font_settings, " Watch out\nfor Walls", m_tex_manager));
		
		m_menu_items.add(m_third_img = new MenuItemImg(
				new Vector(pos.x + offset_x+label_width+3*offset_x
						, pos.y + size.y - 3*(label_height)-offset_y),
				new Vector(label_width, label_height),
				m_tex_manager, R.raw.texture_ball_up));
	}

	@Override
	protected void onTouchDown(MenuItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		// TODO Auto-generated method stub
		
	}


}
