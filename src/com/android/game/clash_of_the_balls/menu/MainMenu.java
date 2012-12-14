package com.android.game.clash_of_the_balls.menu;

import android.content.Context;
import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.Font2D.Font2DSettings;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;
import com.android.game.clash_of_the_balls.game.Vector;

public class MainMenu extends GameMenuBase {

	private String LOG_TAG = "debug";

	GameSettings m_settings;
	Font2DSettings m_font_settings;
	TextureManager m_tex_manager;
	PopupCredit m_credits;
	
	MenuItem m_host_button;
	MenuItem m_join_button;
	MenuItem m_help_button;
	MenuItem m_credits_button;

	public MainMenu(MenuBackground background, float screen_width,
			float screen_height, TextureManager tex_manager, Context context,
			Font2D.Font2DSettings font_settings, GameSettings s) {
		super(background, context);
		m_settings = s;
		m_font_settings = font_settings;
		m_tex_manager = tex_manager;
		Vector pos = new Vector(0.f, 0.f);
		Vector size = new Vector(screen_width, screen_height);

		if (m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);

		// add menu items
		float button_width = size.x * 0.45f;
		float button_height = 0.2f * button_width;
		float distanceButtons = screen_height / 34.f;

		m_menu_items.add(m_host_button = new MenuItemButton(new Vector(pos.x
				+ size.x / 2.f, pos.y + size.y * 3.f / 5.f), new Vector(
				button_width, button_height), font_settings, "New Game",
				m_tex_manager));

		m_menu_items.add(m_join_button = new MenuItemButton(new Vector(pos.x
				+ size.x / 2.f, pos.y + size.y * 3.f / 5.f
				- (button_height + distanceButtons)), new Vector(button_width,
				button_height), font_settings, "Join Game", m_tex_manager));

		m_menu_items.add(m_help_button = new MenuItemButton(new Vector(pos.x
				+ size.x / 2.f, pos.y + size.y * 3.f / 5.f - 2
				* (button_height + distanceButtons)), new Vector(button_width,
				button_height), font_settings, "Help", m_tex_manager));

		m_menu_items.add(m_credits_button = new MenuItemButton(new Vector(pos.x
				+ size.x / 2.f, pos.y + size.y * 3.f / 5.f - 3
				* (button_height + distanceButtons)), new Vector(button_width,
				button_height), font_settings, "Credits", m_tex_manager));

		// Credits PopUp
		m_credits = new PopupCredit(m_activity_context, m_tex_manager,
				m_settings.m_screen_width, m_settings.m_screen_height,
				m_font_settings.m_typeface, m_font_settings.m_color);
	}

	@Override
	protected void onTouchDown(MenuItem item) {
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if (item == m_host_button) {
			m_ui_change = UIChange.CREATION_MENU;
		} else if (item == m_join_button) {
			m_ui_change = UIChange.JOIN_MENU;
		} else if (item == m_help_button) {
			m_ui_change = UIChange.HELP_MENU;
		} else if (item == m_credits_button) {
			m_settings.popup_menu = m_credits;
			m_ui_change = UIChange.POPUP_SHOW;
		} else {
			Log.d(LOG_TAG, "No button pressed");
		}
	}

	public void move(float dsec) {
			//check for button pressed
			if(m_credits.UIChange() == UIChange.POPUP_RESULT_BUTTON1) {
				m_ui_change = UIChange.POPUP_HIDE;
			}
		}
	
	
}
