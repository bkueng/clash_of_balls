package com.android.game.clash_of_the_balls;

import org.jbox2d.common.Vec2;

import com.android.game.clash_of_the_balls.game.Vector;

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
