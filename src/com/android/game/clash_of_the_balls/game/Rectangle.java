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
	
	// checks whether circle (Player) intersects with rectangle (Wall)
	public boolean intersectCircle(Vector rect_offset, Vector circle_position, float circle_radius) {
		float rect_center_x = rect_offset.x + pos.x + size.x / 2.f;
		float rect_center_y = rect_offset.y + pos.y + size.y / 2.f;
		
		float dist_x = Math.abs(circle_position.x - rect_center_x);
		float dist_y = Math.abs(circle_position.y - rect_center_y);
		
		// check for rectangle circle intersection
		if (dist_x > size.x / 2.f + circle_radius) { return false; }
		if (dist_y > size.y / 2.f + circle_radius) { return false; }
		
		if (dist_x <= (size.x / 2.f)) { return true; }
		if (dist_y <= (size.y / 2.f)) { return true; }
		
		// check for corner collision
		float corner_dist = (dist_x - size.x / 2.f) * (dist_x - size.x / 2.f) +
				(dist_y - size.y / 2.f) * (dist_y - size.y / 2.f);
		
		return (corner_dist <= circle_radius * circle_radius);
	}
}
