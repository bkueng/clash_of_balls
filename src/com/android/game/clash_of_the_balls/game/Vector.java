package com.android.game.clash_of_the_balls.game;


public class Vector {
	
	// 2D vector
	public float x;
	public float y;

	public Vector() {
		x = 0.0f;
		y = 0.0f;
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
		return (float) Math.sqrt((x * x) + (y * y));
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

	public void add(Vector vector) {
		x += vector.x;
		y += vector.y;
	}

	public void sub(Vector vector) {
		x -= vector.x;
		y -= vector.y;
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
		float nCos = (float) Math.cos(dAlfa_rad);
		float nSin = (float) Math.sin(dAlfa_rad);

		float iX = x * nCos - y * nSin;
		float iY = y * nCos + x * nSin;

		x = iX;
		x = iY;
	}

}
