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

import android.util.Log;

import com.sapos_aplastados.game.clash_of_balls.game.IMoveable;

/**
 * FPSCounter
 * class that logs the frames/sec every 100 frames
 *
 */
public class FPSCounter implements IMoveable {

	private int frameCount;
	private long lCTM;
	public float m_fps;
	
	public void move(float dsec) {
		frameCount++;
		if (frameCount >= 100) {
			long CTM = System.currentTimeMillis();
			frameCount = 0;
			if (lCTM > 0) {
				m_fps=(100.f / ((CTM - lCTM) / 1000.f));
				Log.d("FPSCounter","fps: " + m_fps);
			}
			lCTM = CTM;
		}
	}

}
