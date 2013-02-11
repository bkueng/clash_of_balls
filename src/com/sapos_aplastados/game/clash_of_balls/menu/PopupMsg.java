package com.sapos_aplastados.game.clash_of_balls.menu;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.sapos_aplastados.game.clash_of_balls.TextureManager;
import com.sapos_aplastados.game.clash_of_balls.Font2D.Font2DSettings;
import com.sapos_aplastados.game.clash_of_balls.Font2D.TextAlign;
import com.sapos_aplastados.game.clash_of_balls.UIHandler.UIChange;
import com.sapos_aplastados.game.clash_of_balls.game.RenderHelper;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;

/**
 * this popup shows a (multiline) message with an ok button
 * 
 * a long message should be split into multiple lines using '\n'
 * there should be at most 5 lines
 * 
 * the creator of this object must check for button pressed events:
 * call UIChange()
 *
 */

public class PopupMsg extends PopupBase {
	
	private MenuItemButton m_ok_button;
	
	protected MenuItemStringMultiline m_title;
	protected MenuItemStringMultiline m_msg;
	

	public PopupMsg(Context context, TextureManager tex_manager,
			float screen_width, float screen_height, Typeface font_typeface, 
			int button_font_color, String title, String msg, String ok_button_text) {
		super(context, tex_manager, screen_width, screen_height);
		
		final float border_offset = 0.06f;
		
		final int font_color = 0xff888888;
		final float button_width = m_size.x / 2.f;
		final float button_height = 0.18f * button_width;
		
		Font2DSettings font_settings = new Font2DSettings(font_typeface
				, TextAlign.CENTER, font_color);
		
		//title
		Vector title_size = new Vector(m_size.x, button_height*0.95f);
		m_menu_items.add(m_title = new MenuItemStringMultiline(
			new Vector(m_position.x, m_position.y + m_size.y*(1.f-border_offset)-title_size.y),
			title_size,
			font_settings, title, tex_manager));
		
		//button
		font_settings.m_color = button_font_color;
		m_menu_items.add(m_ok_button = new MenuItemButton(
			new Vector(m_position.x+(m_size.x-button_width)/2.f
					, m_position.y + m_size.y*border_offset), 
			new Vector(button_width, button_height), 
			font_settings, ok_button_text, tex_manager));
		
		//message
		String msg_split[] = msg.split("\n");
		int len = msg_split.length;
		while(len < 4) {
			++len;
			msg+="\n ";
		}
		font_settings.m_color = font_color;
		Vector msg_pos = new Vector(m_position.x, 
				m_ok_button.pos().y + m_ok_button.size().y + m_size.y*(border_offset));
		m_menu_items.add(m_msg = new MenuItemStringMultiline(
				msg_pos,
				new Vector(m_size.x, m_title.pos().y - msg_pos.y - m_size.y*border_offset),
				font_settings, msg, tex_manager));
		
	}
	
	
	protected void onTouchUp(MenuItem item) {
		if(item == m_ok_button) {
			if(!m_ok_button.isDisabled()) {
				m_ui_change = UIChange.POPUP_RESULT_BUTTON1;
			}
		}
	}
	

}
