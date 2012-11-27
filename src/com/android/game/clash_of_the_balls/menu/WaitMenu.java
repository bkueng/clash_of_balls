package com.android.game.clash_of_the_balls.menu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.Font2D.TextAlign;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;

public class WaitMenu extends GameMenuBase {

	private String LOG_TAG = "debug";

	private GameSettings m_settings;

	MenuItem m_start_button;
	MenuItem m_cancel_button;

	// Grp1 of Buttons: Levels
	MenuItemGreyButton m_first_lvl_button;
	MenuItemGreyButton m_second_lvl_button;
	// MenuItemGreyButton m_thrid_lvl_button;

	// Grp2 of Buttons: Rounds
	MenuItemGreyButton m_1round_button;
	MenuItemGreyButton m_2rounds_button;
	MenuItemGreyButton m_4rounds_button;
	MenuItemGreyButton m_10rounds_button;

	public WaitMenu(MenuBackground background,
			float screen_width, float screen_height,
			TextureManager m_tex_manager, GameSettings settings,Context context) {
		super(background, context);

		Vector pos = new Vector(0.f, 0.f);
		Vector size = new Vector(screen_width, screen_height);

		if (m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);

		m_settings = settings;

		// add menu items
		float button_width = size.x * 0.25f;
		float button_height = 0.5f * button_width;
		float distanceButtons = screen_height / 34.f;

		float grey_button_width = size.x * 0.1f;
		float grey_button_height = grey_button_width;

		float offset_y = size.y * 0.025f;

		// Prepare fonts
		// color constants
		int font_color = Color.WHITE;
		int font_size = (int)Math.round(0.5 * button_height);
		Typeface font_typeface = Typeface.createFromAsset(m_tex_manager.m_activity_context.getAssets(), "alphafridgemagnets.ttf");
		
		Font2D start_font = new Font2D(m_tex_manager, font_typeface, "Start", font_size, TextAlign.CENTER, new Vector(button_width, button_height), font_color);
		Font2D cancel_font = new Font2D(m_tex_manager, font_typeface, "Cancel", font_size, TextAlign.CENTER, new Vector(button_width, button_height), font_color);		
		
		// right Column
		m_menu_items.add(m_start_button = new MenuItemButton(new Vector(pos.x
				+ size.x * (0.025f + 2.f / 3.f), pos.y + size.y * 2.f / 4.f
				+ offset_y), new Vector(button_width, button_height),
				start_font, m_tex_manager));

		m_menu_items.add(m_cancel_button = new MenuItemButton(new Vector(pos.x
				+ size.x * (0.025f + 2.f / 3.f), pos.y + offset_y + size.y
				* 1.f / 4.f), new Vector(button_width, button_height),
				cancel_font, m_tex_manager));

	}

	@Override
	protected void onTouchDown(MenuItem item) {
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if (item == m_start_button) {
			// m_ui_change = UIChange.MAIN_MENU;
		} else if (item == m_cancel_button) {
			if (m_settings.is_host) {
				m_settings.is_host = false;
				m_ui_change = UIChange.CREATION_MENU;
			} else {
				m_settings.is_host = false;
				m_ui_change = UIChange.JOIN_MENU;
			}
		}
	}

}
