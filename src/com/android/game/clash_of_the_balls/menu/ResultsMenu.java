package com.android.game.clash_of_the_balls.menu;

import android.content.Context;
import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.Font2D.Font2DSettings;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;
import com.android.game.clash_of_the_balls.game.GameMenuInGame;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.game.GameStatistics.PlayerStats;
import com.android.game.clash_of_the_balls.network.NetworkClient;
import com.android.game.clash_of_the_balls.network.Networking;
import com.android.game.clash_of_the_balls.network.Networking.ConnectedClient;

public class ResultsMenu extends GameMenuInGame {

	private static final String LOG_TAG = "ResultsMenu";
	
	private Font2DSettings m_list_item_font_settings;

	private MenuItemButton m_start_button;
	
	private MenuItemList m_result_list;
	private float m_item_height;
	
	private MenuItemStringMultiline m_title;
	
	

	public ResultsMenu(MenuBackground background,
			float screen_width, float screen_height
			, TextureManager tex_manager, GameSettings settings
			, Context context, Font2D.Font2DSettings font_settings
			, Networking networking, NetworkClient network_client) {
		
		super(background, context, settings, networking
				, network_client, tex_manager, font_settings);

		Vector pos = new Vector(0.f, 0.f);
		Vector size = new Vector(screen_width, screen_height);
		
		if (m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);

		// add menu items
		float button_width = size.x * 0.45f;
		float button_height = 0.2f * button_width;

		float offset_y = size.y * 0.025f;
		float offset_x = offset_y;
		
		m_menu_items.add(m_title = new MenuItemStringMultiline(
				new Vector(pos.x, pos.y + size.y - offset_y - button_height),
				new Vector(size.x, button_height),
				m_font_settings, "", m_tex_manager));
		
		m_menu_items.add(m_start_button = new MenuItemButton(new Vector(pos.x
			+ (size.x - button_width)/2.f, pos.y + offset_y), new Vector(button_width,
			button_height), m_font_settings, "", m_tex_manager));
		
		
		//player list
		m_item_height = button_height * 0.56f;
		m_list_item_font_settings = new Font2DSettings(font_settings.m_typeface
				, font_settings.m_align, font_settings.m_color);
		Vector list_size = new Vector(size.x * 0.75f
				, m_title.pos().y - m_start_button.pos().y 
				- m_start_button.size().y - offset_y);
		m_menu_items.add(m_result_list = new MenuItemList(
			new Vector(pos.x + (size.x-list_size.x)/2.f, 
				m_start_button.pos().y + m_start_button.size().y + offset_y), 
			list_size, 
			new Vector(m_item_height*1.5f, m_item_height), 
			tex_manager));
		
	}
	
	public void move(float dsec) {
		
		super.move(dsec);
		
		if(m_error_popup == null) {
			//nothing to do
		}
	}
	
	
	private void addListItem(int rank, String name, short id) {
		PlayerStats game_stats = m_settings.game_statistics.gameStatistics().get(id);
		PlayerStats round_stats = m_settings.game_statistics.currentRoundStatistics().get(id);
		
		MenuItemResultEntry item = new MenuItemResultEntry(new Vector()
			, new Vector(m_result_list.size().x, m_item_height)
			, m_list_item_font_settings, rank, name, game_stats, round_stats
			, m_tex_manager);
		
		m_result_list.addItem(item);
		
	}

	@Override
	protected void onTouchDown(MenuItem item) {
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if (item == m_start_button) {
			if(!m_start_button.isDisabled()) {
				
				if(m_settings.isGameFinished()) {
					m_ui_change = UIChange.GAME_END;
				} else {
					if(m_settings.is_host)
						m_ui_change = UIChange.GAME_START_SERVER;
				}
			}
		}
	}
	
	public void onActivate() {
		super.onActivate();
		m_start_button.enable(m_settings.is_host || m_settings.isGameFinished());
		String winner = "";
		//load statistics
		short id=-1;
		int counter = 0;
		int rank = 0;
		int last_points = -1;
		while((id=m_settings.game_statistics.nextPlayer(id)) != -1) {
			//get name of the player
			String name="";
			for(int i=0; i<m_networking.connectedClientCount(); ++i) {
				ConnectedClient client = m_networking.connectedClient(i);
				if(client!=null && client.id == id) {
					name = Networking.toDisplayableName(Networking.getNameFromServerId(
							client.well_known_name));
				}
			}
			if(name.length() == 0)
				Log.e(LOG_TAG, "Failed to find user name from id "+id);
			
			PlayerStats game_stats = m_settings.game_statistics.gameStatistics().get(id);
			int point_count = 0;
			if(game_stats != null) point_count = game_stats.points;
			
			if(point_count != last_points) ++rank;
			last_points = point_count;
			++counter;
			
			addListItem(rank, name, id);
			
			if(counter == 1) winner = name;
			else if(rank == 1) winner=""; //multiple winners
		}
		
		if(m_settings.isGameFinished()) {
			m_start_button.setString("main menu");
			if(winner.length() > 0)
				m_title.setString("Results: "+winner+" wins");
			else
				m_title.setString("Results");
		} else {
			m_start_button.setString("next round");
			m_title.setString("Results: Round "+(m_settings.game_current_round-1) + 
					" of "+m_settings.game_rounds);
		}
	}
	public void onDeactivate() {
		super.onDeactivate();
		//clear the list
		while(m_result_list.itemCount() > 0) m_result_list.removeItem(0);
	}
}
