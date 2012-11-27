package com.android.game.clash_of_the_balls.menu;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.menu.MenuItemPresiArrow.PresiArrow;
import com.android.game.clash_of_the_balls.Font2D.TextAlign;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;

public class CreationMenu extends GameMenuBase {

	private String LOG_TAG = "debug";

	private GameSettings m_settings;
	private TextureManager m_tex_manager;

	// TODO REMOVE!!!
	private Font2D m_font;
	
	MenuItem m_create_button;
	MenuItem m_cancel_button;

	MenuItemKeyboard m_name_button;

	// Grp1 of Buttons: Levels
	MenuItemGreyButton m_first_lvl_button;
	MenuItemGreyButton m_second_lvl_button;
	// MenuItemGreyButton m_thrid_lvl_button;

	// Grp2 of Buttons: Rounds
	MenuItem m_presiLeft_button;
	MenuItem m_presiRight_button;
	MenuItemPresi m_presi_rounds;

	MenuItemGreyButton m_1round_button;
	MenuItemGreyButton m_2rounds_button;
	MenuItemGreyButton m_4rounds_button;
	MenuItemGreyButton m_10rounds_button;

	public CreationMenu(MenuBackground background,
			float screen_width, float screen_height,
			TextureManager tex_manager, GameSettings settings, Context context) {
		super(background, context);

		Vector pos = new Vector(0.f, 0.f);
		Vector size = new Vector(screen_width, screen_height);

		m_settings = settings;
		m_tex_manager = tex_manager;

		if (m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);

		// add menu items
		float button_width = size.x * 0.45f;
		float button_height = 0.2f * button_width;
		float distanceButtons = screen_height / 34.f;

		float grey_button_width = size.x * 0.1f;
		float grey_button_height = grey_button_width;

		float offset_y = size.y * 0.025f;

		
		// Prepare fonts
		// color constants
		int font_color = Color.WHITE;
		int font_size = (int)Math.round(0.5 * button_height);
		Typeface font_typeface = Typeface.createFromAsset(m_tex_manager.m_activity_context.getAssets(), "alphafridgemagnets.ttf");
		
		Font2D name_font = new Font2D(m_tex_manager, font_typeface, "Name", font_size, TextAlign.CENTER, new Vector(button_width, button_height), font_color);
		Font2D first_font = new Font2D(m_tex_manager, font_typeface, "1", font_size, TextAlign.CENTER, new Vector(button_width, button_height), font_color);
		Font2D second_font = new Font2D(m_tex_manager, font_typeface, "2", font_size, TextAlign.CENTER, new Vector(button_width, button_height), font_color);
		Font2D left_arrow_font = new Font2D(m_tex_manager, font_typeface, "Left", font_size, TextAlign.CENTER, new Vector(button_width, button_height), font_color);

		m_font = name_font;
		
		// Name
		m_menu_items.add(m_name_button = new MenuItemKeyboard(new Vector(pos.x
				+ size.x / 3.f, pos.y + size.y * 3.f / 4.f + offset_y),
				new Vector(5 * grey_button_width, grey_button_height),
				name_font, m_tex_manager, m_activity_context,
				"Please Enter your Nickname:"));

		// Group 1
		m_menu_items.add(m_first_lvl_button = new MenuItemGreyButton(
				new Vector(pos.x + size.x / 3.f, pos.y + size.y / 2.f
						+ offset_y), new Vector(grey_button_width,
						grey_button_height), first_font, m_tex_manager));

		m_menu_items.add(m_second_lvl_button = new MenuItemGreyButton(
				new Vector(pos.x + size.x / 3.f + grey_button_width
						+ distanceButtons, pos.y + size.y / 2.f + offset_y),
				new Vector(grey_button_width, grey_button_height), second_font,
				m_tex_manager));

		// Group 2
		m_menu_items.add(m_presiLeft_button = new MenuItemPresiArrow(
				new Vector(pos.x + size.x / 3.f, pos.y + size.y / 4.f
						+ offset_y), new Vector(grey_button_width,
						grey_button_height), left_arrow_font, m_tex_manager,
				PresiArrow.LEFT));

		// Create List for PresiView---

		Vector list_pos = new Vector(pos.x + size.x / 3.f + grey_button_width
				+ distanceButtons, pos.y + size.y / 4.f + offset_y);
		Vector list_size = new Vector(grey_button_width, grey_button_height);

		ArrayList<MenuItemGreyButton> presi_round_list = createRoundList(list_pos,
				list_size);
		//--------------------------------
		m_menu_items.add(m_presi_rounds = new MenuItemPresi(list_pos,
				list_size, name_font, presi_round_list));

		m_menu_items.add(m_presiRight_button = new MenuItemPresiArrow(
				new Vector(pos.x + size.x / 3.f + 2
						* (grey_button_width + distanceButtons), pos.y + size.y
						/ 4.f + offset_y), new Vector(grey_button_width,
						grey_button_height), name_font, m_tex_manager,
				PresiArrow.RIGHT));

		// Last Line
		// Fonts
		Font2D cancel_font = new Font2D(m_tex_manager, font_typeface, "Cancel", font_size, TextAlign.CENTER, new Vector(button_width, button_height), font_color);
		Font2D create_font = new Font2D(m_tex_manager, font_typeface, "Create", font_size, TextAlign.CENTER, new Vector(button_width, button_height), font_color);

		// Buttons
		m_menu_items.add(m_cancel_button = new MenuItemButton(new Vector(pos.x
				+ size.x * (1 / 2.f + 0.025f), pos.y + offset_y), new Vector(
				button_width, button_height), cancel_font, m_tex_manager));

		m_menu_items.add(m_create_button = new MenuItemButton(new Vector(pos.x
				+ size.x * 0.025f, pos.y + offset_y), new Vector(button_width,
				button_height), create_font, m_tex_manager));
	}

	@Override
	protected void onTouchDown(MenuItem item) {
		if (item == m_first_lvl_button) {
			m_second_lvl_button.remain_unpressed();
		} else if (item == m_second_lvl_button) {
			m_first_lvl_button.remain_unpressed();
		} else if (item == m_presiRight_button) {
			m_presi_rounds.next();
		} else if (item == m_presiLeft_button) {
			m_presi_rounds.previous();
		} else if (item==m_presi_rounds){
			Log.d(LOG_TAG,"PresiRoungs pressed");
			
		}

	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if (item == m_create_button) {
			m_settings.is_host = true;
			//1->1 Round, 2->2 Round, 3-> 4 Rounds, 4-> 10 Rounds?? TODO
			m_settings.rounds_idx=m_presi_rounds.getPos();
			
			m_settings.user_name = m_name_button.getString();
			m_ui_change = UIChange.WAIT_MENU;
		} else if (item == m_cancel_button) {
			m_settings.is_host = false;
			m_ui_change = UIChange.MAIN_MENU;
		}
	}

	private ArrayList<MenuItemGreyButton> createRoundList(Vector pos, Vector size) {

		// Initialize list
		ArrayList<MenuItemGreyButton> presi_round_list = new ArrayList<MenuItemGreyButton>();
		presi_round_list.add(m_1round_button = new MenuItemGreyButton(pos,
				size, m_font, m_tex_manager));

		presi_round_list.add(m_2rounds_button = new MenuItemGreyButton(pos,
				size, m_font, m_tex_manager));

		presi_round_list.add(m_4rounds_button = new MenuItemGreyButton(pos,
				size, m_font, m_tex_manager));

		presi_round_list.add(m_10rounds_button = new MenuItemGreyButton(pos,
				size, m_font, m_tex_manager));
		return presi_round_list;

	}

}
