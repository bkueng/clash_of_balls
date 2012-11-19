package com.android.game.clash_of_the_balls;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.game.clash_of_the_balls.game.IDrawable;
import com.android.game.clash_of_the_balls.game.IMoveable;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.menu.MainMenu;
import com.android.game.clash_of_the_balls.menu.MenuBackground;

/**
 * UIHandler
 * this class controls which view (menu, game) is currently active and displayed
 *
 */
public class UIHandler implements IDrawable, IMoveable, ITouchInput {
	
	private static final String LOG_TAG = "UIHandler";
	
	
	private GameSettings m_settings;
	private Context m_activity_context;
	private IMoveable m_fps_counter;
	private TextureManager m_tex_manager;
	private LevelManager m_level_manager;
	
	private UIBase m_active_ui;
	
	private UIBase m_main_menu;
	private UIBase m_game_ui;
	
	private Font2D m_menu_item_font;
	private MenuBackground m_main_menu_background;
	
	public enum UIChange {
		NO_CHANGE,
		MAIN_MENU,
		
		GAME
	}
	
	public UIHandler(int screen_width, int screen_height
			, Context activity_context) {
		
		m_settings = new GameSettings();
		m_fps_counter = new FPSCounter();
		m_activity_context = activity_context;
		m_tex_manager = new TextureManager(m_activity_context);
		m_menu_item_font = new Font2D();
		
		m_level_manager = new LevelManager(m_activity_context);
		m_level_manager.loadLevels();
		
		m_main_menu_background = new MenuBackground(
				m_tex_manager.get(R.raw.texture_main_menu_bg), (float)screen_width/(float)screen_height);
		m_main_menu = new MainMenu(m_menu_item_font, m_main_menu_background
				, screen_width, screen_height,m_tex_manager);
		
		//TODO: init menu's , game
		
		
		onSurfaceChanged(screen_width, screen_height);
		m_active_ui = m_main_menu; //show main menu
	}
	
	public void onSurfaceChanged(int width, int height) {
		m_settings.m_screen_width = width;
		m_settings.m_screen_height = height;
		m_tex_manager.reloadAllTextures();
	}

	public void move(float dsec) {
		if(m_active_ui != null) {
			m_active_ui.move(dsec);

			switch(m_active_ui.UIChange()) {
			case GAME: uiChange(m_active_ui, m_game_ui);
			break;
			case MAIN_MENU: uiChange(m_active_ui, m_main_menu);
			break;
			case NO_CHANGE: //nothing to do
			}
		}
		
		m_fps_counter.move(dsec);
	}
	
	private void uiChange(UIBase old_ui, UIBase new_ui) {
		if(old_ui != new_ui) {
			old_ui.onDeactivate();
			new_ui.onActivate();
			m_active_ui = new_ui;
		}
	}

	public void draw(RenderHelper renderer) {
		if(m_active_ui != null) m_active_ui.draw(renderer);
	}

	public void onTouchEvent(float x, float y, int event) {
		Log.v(LOG_TAG, "Touch event: x="+x+", y="+y+", event="+event);
		
		if(m_active_ui != null) m_active_ui.onTouchEvent(x, y, event);
		
	}
	
}
