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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;

import com.sapos_aplastados.game.clash_of_balls.TextureManager;

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
