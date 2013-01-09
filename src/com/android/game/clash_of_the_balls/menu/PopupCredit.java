package com.android.game.clash_of_the_balls.menu;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;

import com.android.game.clash_of_the_balls.TextureManager;

public class PopupCredit extends PopupMsg {

	public static final String COPYRIGHT  = "\u00a9";
	
	public PopupCredit(Context context, TextureManager tex_manager,
			float screen_width, float screen_height, Typeface font_typeface,
			int button_font_color) {
		super(context, tex_manager, screen_width, screen_height, font_typeface,
				button_font_color, 
				"",
				COPYRIGHT+" Sapos Aplastados Team\n"+
				"ETHZ Distributed Systems HS2012\n"+
				"Lead Programmer and Project Director: Beat Kueng\n"+
				"Lead Physics Engine and Programmer: Andrin Jenal\n"+
				"Lead Designer and Programmer: Hans Hardmeier", 
				"Thx Guys!!");
		m_title.setString("Credits (" + getVersionInfo(context) + ")");
	}
	
	private String getVersionInfo(Context context) {
		PackageManager pm = context.getPackageManager();
		String ret="";
        try {
			PackageInfo package_info = pm.getPackageInfo(context.getPackageName(), 0);
			ret = "version "+package_info.versionName;
		} catch (NameNotFoundException e) {
		}
		return ret;
	}
}
