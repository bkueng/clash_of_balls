package com.android.game.clash_of_the_balls;

import android.util.Log;

import com.android.game.clash_of_the_balls.game.IMoveable;

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
