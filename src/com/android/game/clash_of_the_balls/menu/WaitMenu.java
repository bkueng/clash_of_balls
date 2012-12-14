package com.android.game.clash_of_the_balls.menu;

import android.content.Context;
import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.GameMenuInGame;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.network.NetworkClient;
import com.android.game.clash_of_the_balls.network.Networking;
import com.android.game.clash_of_the_balls.network.Networking.ConnectedClient;
import com.android.game.clash_of_the_balls.Font2D.Font2DSettings;
import com.android.game.clash_of_the_balls.Font2D.TextAlign;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;

public class WaitMenu extends GameMenuInGame {

	private static final String LOG_TAG = "WaitMenu";
	
	private Font2DSettings m_list_item_font_settings;

	private MenuItemButton m_start_button;
	private MenuItemButton m_cancel_button;
	
	private MenuItemStringMultiline m_client_label;
	private MenuItemList m_client_list;
	private float m_item_height;
	
	private int m_max_player_count;


	public WaitMenu(MenuBackground background,
			float screen_width, float screen_height
			, TextureManager tex_manager, GameSettings settings
			, Context context, Font2D.Font2DSettings font_settings
			, int label_font_color
			, Networking networking, NetworkClient network_client) {
		
		super(background, context, settings, networking
				, network_client, tex_manager, font_settings);

		Vector pos = new Vector(0.f, 0.f);
		Vector size = new Vector(screen_width, screen_height);
		
		Font2D.Font2DSettings label_font_settings 
			= new Font2D.Font2DSettings(font_settings.m_typeface,
				TextAlign.LEFT, label_font_color);

		if (m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);

		// add menu items
		float button_width = size.x * 0.35f;
		float button_height=0.25f*button_width;
		float label_height = 0.75f*button_height;

		float offset_y = size.y * 0.025f;
		float offset_x = offset_y;
		
		//client list
		m_item_height = button_height * 0.8f;
		m_list_item_font_settings = new Font2DSettings(font_settings.m_typeface
				, font_settings.m_align, font_settings.m_color);
		m_menu_items.add(m_client_label = new MenuItemStringMultiline(
				new Vector(pos.x + offset_x
						, pos.y + size.y - label_height),
				new Vector(size.x, label_height),
				label_font_settings, "", m_tex_manager));
		m_menu_items.add(m_client_list = new MenuItemList(
				new Vector(pos.x + offset_x, pos.y + offset_y), 
				new Vector(size.x - offset_x*3.f - button_width, size.y 
						- offset_y - label_height), 
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
		
		super.move(dsec);
		
		if(m_error_popup == null) {
			int old_list_size = m_client_list.itemCount();
			//update the connected clients
			boolean is_self_connected=false; //is our own name on the list?
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
						addListItem(Networking.toDisplayableName(
								Networking.getNameFromServerId(c.well_known_name))
								, c.well_known_name);
					if(c.unique_id.equals(m_networking.getUniqueName())) 
							is_self_connected = true;
				}
			}
			if(old_list_size != m_client_list.itemCount())
				updateLabel();
			
			m_start_button.enable(m_settings.is_host
					&& (m_client_list.itemCount() > 1 || GameSettings.debug)
					&& is_self_connected);
			
		}
	}
	
	private void updateLabel() {
		if(m_settings.is_host && m_max_player_count > 0) {
			m_client_label.setString(" Connected Players: "
					+m_client_list.itemCount() + " of max "
					+m_max_player_count);
		} else {
			m_client_label.setString(" Connected Players: "
					+m_client_list.itemCount());
		}
	}
	
	protected void onGameStart() {
		m_settings.game_statistics.resetGameStatistics();
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
			if (m_settings.is_host && !m_start_button.isDisabled()) {
				
				onGameStart();
				m_networking.setClientsCanJoin(false);
				m_ui_change = UIChange.GAME_START_SERVER;
				
			}
		} else if (item == m_cancel_button) {
			onCancelPressed();
		}
	}
	
	private void onCancelPressed() {
		if (m_settings.is_host) {
			
			m_networking.stopAdvertise();
			
			m_ui_change = UIChange.CREATION_MENU;
		} else {
			
			m_ui_change = UIChange.JOIN_MENU;
		}
		m_settings.is_host = false;
		m_networking.leaveSession();
	}
	
	public void onActivate() {
		super.onActivate();
		m_start_button.enable(m_settings.is_host);
		if(m_settings.selected_level!=null) 
			m_max_player_count = m_settings.selected_level.player_count;
		else m_max_player_count = 0;
		updateLabel();
	}
	public void onDeactivate() {
		super.onDeactivate();
		//clear the list
		while(m_client_list.itemCount() > 0) m_client_list.removeItem(0);
	}
	
	public void onBackButtonPressed() {
		onCancelPressed();
	}

}
