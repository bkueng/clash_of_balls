package com.android.game.clash_of_the_balls.game;

public class Rectangle {
	public Vector pos;
	public Vector size;
	
	public Rectangle() {
		pos = new Vector();
		size = new Vector();
	}
	public Rectangle(Vector vpos, Vector vsize) {
		pos = vpos;
		size = vsize;
	}
	public Rectangle(float x, float y, float width, float height) {
		pos = new Vector(x, y);
		size = new Vector(width, height);
	}
	
	public float x() { return pos.x; }
	public float y() { return pos.y; }
	public float width() { return size.x; }
	public float height() { return size.y; }
}
