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


public interface ITouchInput {
	/* x, y are screen coordinates ([0,0] is the bottom left display corner) 
	 * event is one of: 
	 *  MotionEvent.ACTION_DOWN
	 *  MotionEvent.ACTION_MOVE
	 *  MotionEvent.ACTION_UP
	 */
	public void onTouchEvent(float x, float y, int event);
}
