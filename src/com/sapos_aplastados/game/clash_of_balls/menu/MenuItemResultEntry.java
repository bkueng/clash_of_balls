/*
 * Copyright (C) 2012-2013 Hans Hardmeier <hanshardmeier@gmail.com>
 * Copyright (C) 2012-2013 Andrin Jenal
 * Copyright (C) 2012-2013 Beat Küng <beat-kueng@gmx.net>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

package com.sapos_aplastados.game.clash_of_balls.menu;

import com.sapos_aplastados.game.clash_of_balls.R;
import com.sapos_aplastados.game.clash_of_balls.Font2D;
import com.sapos_aplastados.game.clash_of_balls.Texture;
import com.sapos_aplastados.game.clash_of_balls.TextureManager;
import com.sapos_aplastados.game.clash_of_balls.VertexBufferFloat;
import com.sapos_aplastados.game.clash_of_balls.Font2D.Font2DSettings;
import com.sapos_aplastados.game.clash_of_balls.Font2D.TextAlign;
import com.sapos_aplastados.game.clash_of_balls.game.RenderHelper;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;
import com.sapos_aplastados.game.clash_of_balls.game.GameStatistics.PlayerStats;

/**
 * game result entry for one player
 *
 */
public class MenuItemResultEntry extends MenuItem {
	
	private Font2D m_rank_font;
	private Font2D m_name_font;
	private Font2D m_points_font;
	
	private Font2DSettings m_font_settings;
	
	private Texture m_texture;
	
	
	public MenuItemResultEntry(Vector position, Vector size, 
			Font2DSettings font_settings, int rank, String name,
			PlayerStats game_stats, PlayerStats cur_round_stats, 
			TextureManager tex_manager) {
		
		super(position, size);
		m_font_settings = new Font2DSettings(font_settings.m_typeface
				, font_settings.m_align, font_settings.m_color);
		
		int font_size = (int)(m_size.y * 0.5f);
		
		//Rank
		m_font_settings.m_align = TextAlign.LEFT;
		m_rank_font = new Font2D(tex_manager, m_size, m_font_settings , font_size);
		m_rank_font.setString("     "+rank);
		
		//Name
		m_font_settings.m_align = TextAlign.CENTER;
		m_name_font = new Font2D(tex_manager, m_size, m_font_settings , font_size);
		m_name_font.setString(name);
		
		//Points
		int round_points = 0;
		if(cur_round_stats != null) round_points = cur_round_stats.points;
		int game_points = 0;
		if(game_stats != null) game_points = game_stats.points;
		m_font_settings.m_align = TextAlign.RIGHT;
		m_points_font = new Font2D(tex_manager, m_size, m_font_settings , font_size);
		m_points_font.setString(""+round_points+" / "+game_points+"     ");
		
		
		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		RenderHelper.initColorArray(0xffffffff, m_color);
		
		m_texture = tex_manager.get(R.raw.texture_grey_unpressed_button);
		
	}
	
	
	public void draw(RenderHelper renderer) {
		renderer.pushModelMat();
		renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
		renderer.modelMatScale(m_size.x, m_size.y, 0.f);
		
		drawTexture(renderer, m_texture);
		
		m_rank_font.draw(renderer);
		m_name_font.draw(renderer);
		m_points_font.draw(renderer);
		
        renderer.popModelMat();
	}
}
