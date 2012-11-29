package com.android.game.clash_of_the_balls.menu;

import android.content.Context;
import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.network.Networking;
import com.android.game.clash_of_the_balls.network.Networking.ConnectedClient;
import com.android.game.clash_of_the_balls.Font2D.Font2DSettings;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;

public class WaitMenu extends GameMenuBase {

	private static final String LOG_TAG = "WaitMenu";

	private GameSettings m_settings;
	private TextureManager m_tex_manager;
	
	private Font2DSettings m_list_item_font_settings;

	private MenuItemButton m_start_button;
	private MenuItemButton m_cancel_button;
	
	private MenuItemList m_client_list;
	private float m_item_height;
	
	private Networking m_networking;
	

	public WaitMenu(MenuBackground background,
			float screen_width, float screen_height
			, TextureManager tex_manager, GameSettings settings
			, Context context, Font2D.Font2DSettings font_settings
			, Networking networking) {
		super(background, context);

		Vector pos = new Vector(0.f, 0.f);
		Vector size = new Vector(screen_width, screen_height);

		if (m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);

		m_settings = settings;
		m_tex_manager = tex_manager;
		m_networking = networking;

		// add menu items
		float button_width = size.x * 0.35f;
		float button_height=0.25f*button_width;

		float offset_y = size.y * 0.025f;
		float offset_x = offset_y;
		
		//client list
		m_item_height = button_height * 0.8f;
		m_list_item_font_settings = new Font2DSettings(font_settings.m_typeface
				, font_settings.m_align, font_settings.m_color);
		m_menu_items.add(m_client_list = new MenuItemList(
				new Vector(pos.x + offset_x, pos.y + offset_y), 
				new Vector(size.x - offset_x*3.f - button_width, size.y - 2.f*offset_y), 
				new Vector(m_item_height*1.5f, m_item_height), 
				tex_manager));
		
		
		//right Column
		m_menu_items.add(m_cancel_button = new MenuItemButton(
				new Vector(pos.x+size.x - offset_x - button_width, pos.y+offset_y),
				new Vector(button_width, button_height), 
				font_settings, "Cancel", tex_manager));
		m_menu_items.add(m_start_button = new MenuItemButton(
				new Vector(m_cancel_button.pos().x, 
						m_cancel_button.pos().y+button_height+offset_y),
				new Vector(button_width, button_height), 
				font_settings, "Start", tex_manager));

	}
	
	public void move(float dsec) {
		//update the connected clients
		//remove old ones
		for(int i=0; i<m_client_list.itemCount(); ++i) {
			MenuItemString item = (MenuItemString)m_client_list.item(i);
			String server_id = (String)item.obj;
			boolean found=false;
			for(int k=0; k<m_networking.connectedClientCount(); ++k) {
				ConnectedClient c = m_networking.connectedClient(k);
				if(c != null && c.well_known_name != null) {
					if(server_id.equals(c.well_known_name))
						found = true;
				}
			}
			if(!found)
				m_client_list.removeItem(i--);
		}
		//add new ones
		for(int k=0; k<m_networking.connectedClientCount(); ++k) {
			boolean found=false;
			ConnectedClient c = m_networking.connectedClient(k);
			if(c != null && c.well_known_name != null) {
				for(int i=0; i<m_client_list.itemCount(); ++i) {
					MenuItemString item = (MenuItemString)m_client_list.item(i);
					String server_id = (String)item.obj;
					if(server_id.equals(c.well_known_name))
						found = true;
				}
				if(!found)
					addListItem(Networking.getNameFromServerId(c.well_known_name)
							, c.well_known_name);
			}
		}
		
		
		m_start_button.enable(m_settings.is_host
				&& (m_client_list.itemCount() > 0 || GameSettings.debug));
	}
	
	private void addListItem(String str_display, Object additional) {
		MenuItemString item = new MenuItemString(new Vector()
			, new Vector(m_client_list.size().x, m_item_height)
			, m_list_item_font_settings, str_display, m_tex_manager);
		item.obj = additional;
		m_client_list.addItem(item);
		
	}

	@Override
	protected void onTouchDown(MenuItem item) {
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if (item == m_start_button) {
			//TODO: start the game
			
			
			// m_ui_change = UIChange.MAIN_MENU;
		} else if (item == m_cancel_button) {
			if (m_settings.is_host) {
				m_settings.is_host = false;
				
				
				m_networking.stopAdvertise();
				
				m_ui_change = UIChange.CREATION_MENU;
			} else {
				m_settings.is_host = false;
				m_ui_change = UIChange.JOIN_MENU;
			}
		}
	}
	
	public void onActivate() {
		super.onActivate();
		m_start_button.enable(m_settings.is_host);
	}

}
