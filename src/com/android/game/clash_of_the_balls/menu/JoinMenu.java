package com.android.game.clash_of_the_balls.menu;

import android.content.Context;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.network.NetworkClient;
import com.android.game.clash_of_the_balls.network.Networking;
import com.android.game.clash_of_the_balls.Font2D.Font2DSettings;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;

public class JoinMenu extends GameMenuBase {
	
	private String LOG_TAG="JoinMenu";
	
	private MenuItemButton m_join_button;
	private MenuItemButton m_cancel_button;
	private MenuItemKeyboard m_name_button;
	
	private MenuItemList m_game_list;
	private float m_item_height;
	
	private TextureManager m_tex_manager;
	
	private Font2DSettings m_list_item_font_settings;
	
	private GameSettings m_settings;
	private NetworkClient m_network_client;
	
	
	public JoinMenu(MenuBackground background
			, float screen_width, float screen_height
			, TextureManager tex_manager, Context context
			, Font2D.Font2DSettings font_settings
			, GameSettings settings
			, NetworkClient network_client) {
		super(background,context);
		
		Vector pos=new Vector(0.f, 0.f);
		Vector size=new Vector(screen_width, screen_height);
		
		m_tex_manager = tex_manager;
		m_network_client = network_client;
		m_settings = settings;
		
		if(m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);

		//add menu items
		float button_width = size.x * 0.35f;
		float button_height=0.25f*button_width;
		
		float offset_y = size.y*0.025f;
		float offset_x = offset_y;
		
		//game list
		m_item_height = button_height * 0.8f;
		m_list_item_font_settings = new Font2DSettings(font_settings.m_typeface
				, font_settings.m_align, font_settings.m_color);
		m_menu_items.add(m_game_list = new MenuItemList(
				new Vector(pos.x + offset_x, pos.y + offset_y), 
				new Vector(size.x - offset_x*3.f - button_width, size.y - 2.f*offset_y), 
				new Vector(m_item_height*1.5f, m_item_height), 
				tex_manager));
		
		// Name
		m_menu_items.add(m_name_button = new MenuItemKeyboard(
				new Vector(pos.x+size.x - offset_x - button_width
						, pos.y + size.y * 3.f / 4.f + offset_y),
				new Vector(button_width, button_height),
				font_settings, m_tex_manager, m_activity_context,
				"Please Enter your Nickname:"));

		//right Column
		m_menu_items.add(m_cancel_button = new MenuItemButton(
				new Vector(pos.x+size.x - offset_x - button_width, pos.y+offset_y),
				new Vector(button_width, button_height), 
				font_settings, "Cancel", tex_manager));
		m_menu_items.add(m_join_button = new MenuItemButton(
				new Vector(m_cancel_button.pos().x, 
						m_cancel_button.pos().y+button_height+offset_y),
				new Vector(button_width, button_height), 
				font_settings, "Join", tex_manager));

	}
	
	public void move(float dsec) {
		super.move(dsec);
		
		//update available games
		m_network_client.handleReceive();
		//remove old ones
		for(int i=0; i<m_game_list.itemCount(); ++i) {
			MenuItemString item = (MenuItemString)m_game_list.item(i);
			String server_id = (String)item.obj;
			boolean found=false;
			for(int k=0; k<m_network_client.serverIdCount(); ++k) {
				if(server_id.equals(m_network_client.serverId(k)))
					found = true;
			}
			if(!found)
				m_game_list.removeItem(i--);
		}
		//add new ones
		for(int k=0; k<m_network_client.serverIdCount(); ++k) {
			boolean found=false;
			for(int i=0; i<m_game_list.itemCount(); ++i) {
				MenuItemString item = (MenuItemString)m_game_list.item(i);
				String server_id = (String)item.obj;
				if(server_id.equals(m_network_client.serverId(k)))
					found = true;
			}
			if(!found)
				addListItem(Networking.toDisplayableName(
						Networking.getNameFromServerId(m_network_client.serverId(k)))
						, m_network_client.serverId(k));
		}
		
		String name = Networking.fromDisplayableName(m_name_button.getString());
		m_join_button.enable(m_game_list.getSelectedItem() != null 
				&& name.length() > 0);
		if(name.length() > 0) m_settings.user_name = new String(name);
	}
	
	private void addListItem(String str_display, Object additional) {
		MenuItemString item = new MenuItemString(new Vector()
			, new Vector(m_game_list.size().x, m_item_height)
			, m_list_item_font_settings, str_display, m_tex_manager);
		item.obj = additional;
		m_game_list.addItem(item);
		
	}

	@Override
	protected void onTouchDown(MenuItem item) {
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if(item == m_join_button) {
			if(!m_join_button.isDisabled()) {
				//get the server to join
				MenuItem sel_item = m_game_list.getSelectedItem();
				if(sel_item!=null) {
					Object obj = ((MenuItemString)sel_item).obj;
					if(obj != null) {
						String server_id = (String)obj;
						String user_name = Networking.fromDisplayableName(
								m_name_button.getString());
						m_settings.user_name = user_name;
						m_network_client.setOwnName(user_name);
						m_network_client.connectToServer(server_id);
						m_ui_change = UIChange.WAIT_MENU;
					}
				}
			}
		}else if(item == m_cancel_button){
			m_ui_change = UIChange.MAIN_MENU;
		}
	}
	
	public void onActivate() {
		super.onActivate();
		m_network_client.startDiscovery();
		
		String name =  Networking.toDisplayableName(m_settings.user_name);
		if(name.length() > 0) m_name_button.setString(name);
	}
	
	public void onDeactivate() {
		super.onDeactivate();
		//clear the list
		while(m_game_list.itemCount() > 0) m_game_list.removeItem(0);
		m_network_client.stopDiscovery();
	}

}
