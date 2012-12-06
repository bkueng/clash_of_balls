package com.android.game.clash_of_the_balls.game;

import android.util.Log;

import com.android.game.clash_of_the_balls.Texture;

public class GameHole extends StaticGameObject {

	public enum HoleType {
		Boarder_left,
		Boarder_up,
		Boarder_right,
		Boarder_down,
		
		Corner_up_right,
		Corner_up_left,
		Corner_down_right,
		Corner_down_left,
		
		Hole_single,
		Hole_Full, //Completely empty
		Hole_Corner_up_right,
		Hole_Corner_up_left,
		Hole_Corner_down_right,
		Hole_Corner_down_left,
		Hole_Head_Down,
		Hole_Head_Up,
		Hole_Head_Left,
		Hole_Head_Right,
	}
	private final HoleType m_hole_type;
	
	public HoleType holeType() { return m_hole_type; }
	
	
	GameHole(short id, Vector position, HoleType hole_type, Texture texture) {
		super(id, position, Type.Hole, texture);
		float angle = (float) Math.PI / 2;
		
		m_hole_type = hole_type;

		switch(hole_type) {
		case Hole_Corner_up_right:
		case Hole_Head_Up:
		case Hole_single:
		case Hole_Full:
		case Boarder_left:
		case Corner_up_right:
		break;
		case Hole_Corner_down_right:
		case Hole_Head_Right:
		case Boarder_up:
		case Corner_up_left:
			setRotation(3.f * angle);
			break;
		case Hole_Corner_down_left:
		case Hole_Head_Down:
		case Boarder_right:
		case Corner_down_left:
			setRotation(2.f * angle);
			break;
		case Hole_Corner_up_left:
		case Hole_Head_Left:
		case Boarder_down:
		case Corner_down_right:
			setRotation(angle);
			break;
		}
	}
	
	Vector m_tmp_v = new Vector(); //minimize object creation & deletion
	Vector m_tmp_p = new Vector();
	Vector m_tmp_dir = new Vector();
	
	//check if a game position vector is on the hole, ie falling down
	//all points between p_start & p_end are checked
	//normal_out is an output parameter & will be set if return is true
	// it points to the middle of the hole, and should be rectangular to the
	// border of the hole
	public boolean isInside(Vector p_start, Vector p_end, Vector normal_out) {
		
		final float MAX_STEP = 0.2f;
		//test points between p_start & p_end with a max distance of MAX_STEP
		m_tmp_p.set(p_start);
		m_tmp_dir.set(p_end);
		m_tmp_dir.sub(p_start);
		float dist = m_tmp_dir.length();
		int count = (int)(dist/MAX_STEP) + 1;
		if(dist==0.f) m_tmp_dir.set(0.f, 0.f);
		else m_tmp_dir.mul(1.f/(float)count);
		
		boolean ret = false;
		boolean rotate_normal = true;
		
		for(int i=0; i<count && !ret; ++i) {
			m_tmp_p.add(m_tmp_dir);
			
			m_tmp_v.set(m_tmp_p.x - m_position.x, m_tmp_p.y - m_position.y);
			//rotate to avoid having to check all 4 different rotations differently
			m_tmp_v.rotate(-m_rotation_angle);
			m_tmp_v.add(0.5f, 0.5f);
			
			if(!isInsideTile(m_tmp_v.x, m_tmp_v.y)) return false;
			//m_tmp_v is now within [0,1]x[0,1]
			
			//rotate the point back
			switch(m_hole_type) {
			case Hole_Corner_up_right:
			case Hole_Corner_down_right:
			case Hole_Corner_down_left:
			case Hole_Corner_up_left:
				if(m_tmp_v.x > 0.5f && m_tmp_v.y > 0.5f) {
					ret = isInsideHoleSingle(m_tmp_p, normal_out);
					rotate_normal=false;
				} else {
					getNormalForFull(m_tmp_v, normal_out);
					ret = true;
				}
				break;
			case Hole_Head_Up:
			case Hole_Head_Right:
			case Hole_Head_Down:
			case Hole_Head_Left:
				if(m_tmp_v.y > 0.5f) {
					ret = isInsideHoleSingle(m_tmp_p, normal_out);
					rotate_normal=false;
				} else {
					getNormalForFull(m_tmp_v, normal_out);
					ret = true;
				}
				break;
			case Hole_single:
				ret = isInsideHoleSingle(m_tmp_p, normal_out);
				rotate_normal=false;
				break;
			case Hole_Full:
				getNormalForFull(m_tmp_v, normal_out);
				ret=true;
				break;
			case Boarder_left:
			case Boarder_up:
			case Boarder_right:
			case Boarder_down:
				if(m_tmp_v.x < 0.21f) {
					normal_out.set(-1.f, 0.f);
					ret=true;
				}
				break;
			case Corner_up_right:
			case Corner_up_left:
			case Corner_down_left:
			case Corner_down_right:
				if(m_tmp_v.x < 0.21f) {
					normal_out.set(-1.f, 0.f);
					ret=true;
				} else if(m_tmp_v.y < 0.21f) {
					normal_out.set(0.f, -1.f);
					ret=true;
				}
				break;
			}
			if(rotate_normal) {
				normal_out.rotate(m_rotation_angle);
			}
		}
		return ret;
	}
	
	
	private boolean isInsideTile(float x, float y) {
		return x>=0.f && x<=1.f && y>=0.f && y<=1.f;
	}
	
	private void getNormalForFull(Vector pos, Vector normal_out) {
		if(pos.y > pos.x) {
			if(pos.y > 1.f - pos.x) {
				normal_out.set(0.f, -1.f);
			} else {
				normal_out.set(1.f, 0.f);
			}
		} else {
			if(pos.y > 1.f - pos.x) {
				normal_out.set(-1.f, 0.f);
			} else {
				normal_out.set(0.f, 1.f);
			}
		}
	}
	
	private boolean isInsideHoleSingle(Vector p, Vector normal_out) {
		if(p.distSquared(m_position) < 0.5f * 0.5f) {
			normal_out.set(m_position);
			normal_out.sub(p);
			normal_out.normalize();
			return true;
		}
		return false;
	}

}
