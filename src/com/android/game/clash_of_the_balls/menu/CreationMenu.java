package com.android.game.clash_of_the_balls.menu;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.LevelManager;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.menu.MenuItemArrow.ArrowType;
import com.android.game.clash_of_the_balls.Font2D.TextAlign;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;

public class CreationMenu extends GameMenuBase {

	private String LOG_TAG = "debug";

	private GameSettings m_settings;
	private TextureManager m_tex_manager;

	private Font2D.Font2DSettings m_font_settings;
	
	MenuItem m_create_button;
	MenuItem m_cancel_button;

	MenuItemKeyboard m_name_button;
	
	MenuItemGreyButton m_round_buttons[];
	
	private MenuItemList m_level_list;

	public CreationMenu(MenuBackground background,
			float screen_width, float screen_height
			, TextureManager tex_manager, GameSettings settings, Context context
			, Font2D.Font2DSettings font_settings, LevelManager level_manager) {
		super(background, context);

		Vector pos = new Vector(0.f, 0.f);
		Vector size = new Vector(screen_width, screen_height);

		m_settings = settings;
		m_tex_manager = tex_manager;
		
		// bind font setting
		m_font_settings = font_settings;

		if (m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);

		// add menu items
		float button_width = size.x * 0.45f;
		float button_height = 0.2f * button_width;
		float distanceButtons = screen_height / 34.f;

		float grey_button_width = size.x * 0.1f;
		float grey_button_height = grey_button_width;

		float offset_y = size.y * 0.025f;
		
		
		//levels
		float level_item_width = button_width;
		m_menu_items.add(m_level_list = new MenuItemList(
				new Vector(pos.x+size.x * 0.025f, pos.y+size.y/5.f),
				new Vector(level_item_width, 
					size.y * 3.f/4.f + offset_y + grey_button_height - size.y/5.f),
				new Vector(1.5f*button_height*0.6f, button_height*0.6f)
				, m_tex_manager));
		
		for(int i=0; i<level_manager.levelCount(); ++i) {
			m_level_list.addItem(new MenuItemLevel(new Vector()
				, new Vector(level_item_width, size.y/2.f/3.f)
				, level_manager.level(i), tex_manager));
		}
		m_level_list.selectItem(0);
		
		
		// Name
		m_menu_items.add(m_name_button = new MenuItemKeyboard(
				new Vector(pos.x + size.x / 2.f
						, pos.y + size.y * 3.f / 4.f + offset_y),
				new Vector(4 * grey_button_width, grey_button_height),
				m_font_settings, m_tex_manager, m_activity_context,
				"Please Enter your Nickname:"));

		
		// rounds
		m_round_buttons = new MenuItemGreyButton[3];
		for(int i=0; i<m_round_buttons.length; ++i) {
			m_round_buttons[i] = new MenuItemGreyButton(
					new Vector(pos.x + size.x / 2.f 
							+ (grey_button_width + distanceButtons) * i
							, pos.y + size.y / 2.f + offset_y), 
					new Vector(grey_button_width, grey_button_height)
					, m_tex_manager);
		}
		m_round_buttons[0].select();


		// Last Line
		// Buttons
		m_menu_items.add(m_cancel_button = new MenuItemButton(new Vector(pos.x
				+ size.x * (1 / 2.f + 0.025f), pos.y + offset_y), new Vector(
				button_width, button_height), m_font_settings, "Cancel"
				, m_tex_manager));

		m_menu_items.add(m_create_button = new MenuItemButton(new Vector(pos.x
				+ size.x * 0.025f, pos.y + offset_y), new Vector(button_width,
				button_height), m_font_settings, "Create", m_tex_manager));
	}

	@Override
	protected void onTouchDown(MenuItem item) {
		boolean is_round_button=false;
		for(int i=0; i<m_round_buttons.length; ++i) {
			if(item == m_round_buttons[i]) is_round_button = true;
		}
		if(is_round_button) {
			for(int i=0; i<m_round_buttons.length; ++i) {
				if(item != m_round_buttons[i])
					m_round_buttons[i].remain_unpressed();
				else m_round_buttons[i].select();
			}
		}
	}
	
	public void onTouchEvent(float x, float y, int event) {
		super.onTouchEvent(x, y, event);
		
		if(event == MotionEvent.ACTION_DOWN) {
			int round_button=-1;
			for(int i=0; i<m_round_buttons.length; ++i) {
				if(m_round_buttons[i].isInside(x, y)) round_button = i;
			}
			if(round_button != -1) {
				for(int i=0; i<m_round_buttons.length; ++i) {
					if(i != round_button)
						m_round_buttons[i].remain_unpressed();
					else m_round_buttons[i].select();
				}
			}
		}
	}
	
	
	
	public void draw(RenderHelper renderer) {
		super.draw(renderer);
		for(int i=0; i<m_round_buttons.length; ++i) 
			m_round_buttons[i].draw(renderer);
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if (item == m_create_button) {
			m_settings.is_host = true;
			//1->1 Round, 2->2 Round, 3-> 4 Rounds, 4-> 10 Rounds?? TODO
			int idx= 0;
			for(int i=0; i<m_round_buttons.length; ++i)
				if(m_round_buttons[i].isPressed()) idx = i;
			Log.d(LOG_TAG, "Game Creation: selected rounds: "+idx);
			m_settings.game_rounds=idx;
			
			MenuItemLevel item_level = (MenuItemLevel)m_level_list.getSelectedItem();
			m_settings.selected_level = item_level.level();
			
			m_settings.user_name = m_name_button.getString();
			m_ui_change = UIChange.WAIT_MENU;
		} else if (item == m_cancel_button) {
			m_settings.is_host = false;
			m_ui_change = UIChange.MAIN_MENU;
		}
	}

}
