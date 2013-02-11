package com.sapos_aplastados.game.clash_of_balls.game;

import org.jbox2d.common.Vec2;

import android.util.FloatMath;


public class Vector {
	
	// 2D vector
	public float x;
	public float y;

	public Vector() {
		x = 0.0f;
		y = 0.0f;
	}
	public Vector(Vec2 v) {
		x = v.x;
		y = v.y;
	}

	public Vector(float fx, float fy) {
		x = fx;
		y = fy;
	}

	public Vector(Vector src) {
		x = src.x;
		y = src.y;
	}

	public void set(Vector src) {
		x = src.x;
		y = src.y;
	}

	public void set(float fx, float fy) {
		x = fx;
		y = fy;
	}

	
	public float length() { 
		return FloatMath.sqrt((x * x) + (y * y));
	}
	
	public float lengthSquared() { 
		return (x * x) + (y * y);
	}

	public void normalize() {
		float len = length();

		if (len != 0.0f) {
			x /= len;
			y /= len;
		} else {
			x = 0.0f;
			y = 0.0f;
		}
	}
	
	public float distSquared(Vector p) {
		return (x-p.x) * (x-p.x) + (y-p.y) * (y-p.y);
	}
	public float dist(Vector p) {
		return FloatMath.sqrt((x-p.x) * (x-p.x) + (y-p.y) * (y-p.y));
	}

	public float angle() {
		return (float) Math.atan2(y, x);
	}
	
	public void add(Vector vector) {
		x += vector.x;
		y += vector.y;
	}
	public void add(float fx, float fy) {
		x+=fx;
		y+=fy;
	}

	public void sub(Vector vector) {
		x -= vector.x;
		y -= vector.y;
	}
	public void sub(float fx, float fy) {
		x-=fx;
		y-=fy;
	}


	public void mul(Vector vector) {
		x *= vector.x;
		y *= vector.y; 
	}
	 
	public void mul(float scalar) {
		x *= scalar;
		y *= scalar;
	}

	public float dot(Vector vector) {
		return (x * vector.x) + (y * vector.y);
	}

	public void rotate(float dAlfa_rad) {
		float nCos = FloatMath.cos(dAlfa_rad);
		float nSin = FloatMath.sin(dAlfa_rad);
		
		float iX = x * nCos - y * nSin;
		float iY = y * nCos + x * nSin;

		x = iX;
		y = iY;
	}

}
