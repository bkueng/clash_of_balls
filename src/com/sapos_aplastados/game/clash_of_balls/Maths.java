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

import org.jbox2d.common.Vec2;

import com.sapos_aplastados.game.clash_of_balls.game.Vector;

import android.util.FloatMath;

/**
 * some math functions for (2D) vectors
 *
 */
public class Maths {
	
	public static float distSquared(Vector a, Vector b) {
		return (a.x-b.x) * (a.x-b.x) + (a.y-b.y) * (a.y-b.y);
	}
	public static float dist(Vector a, Vector b) {
		return FloatMath.sqrt((a.x-b.x) * (a.x-b.x) + (a.y-b.y) * (a.y-b.y));
	}
	
}
