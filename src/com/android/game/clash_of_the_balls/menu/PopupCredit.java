package com.android.game.clash_of_the_balls.menu;

import android.content.Context;
import android.graphics.Typeface;

import com.android.game.clash_of_the_balls.TextureManager;

public class PopupCredit extends PopupMsg {

	public static final String COPYRIGHT  = "\u00a9";
	
	public PopupCredit(Context context, TextureManager tex_manager,
			float screen_width, float screen_height, Typeface font_typeface,
			int button_font_color) {
		super(context, tex_manager, screen_width, screen_height, font_typeface,
				button_font_color, 
				"Credits", 
				COPYRIGHT+" Sapos Aplastados Team\n"+
				"ETHZ Distributed Systems HS2012\n"+
				"Lead Programmer and Project Director: Beat Kueng\n"+
				"Lead Physics Engine and Programmer: Andrin Jenal\n"+
				"Lead Designer and Programmer: Hans Hardmeier", 
				"Thx Guys!!");
	}
}
