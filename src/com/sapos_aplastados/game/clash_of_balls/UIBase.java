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
package com.sapos_aplastados.game.clash_of_balls;

import com.sapos_aplastados.game.clash_of_balls.UIHandler.UIChange;
import com.sapos_aplastados.game.clash_of_balls.game.IDrawable;
import com.sapos_aplastados.game.clash_of_balls.game.IMoveable;

public interface UIBase extends ITouchInput, IMoveable, IDrawable {
	
	/* this asks after move if an ui change is requested */
	public UIChange UIChange();
	
	/* called when ui is changed */
	public void onActivate();
	public void onDeactivate();
	
	public void onBackButtonPressed();
}
