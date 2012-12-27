package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.util.Log;

import com.android.game.clash_of_the_balls.game.GameBase;
import com.android.game.clash_of_the_balls.game.StaticGameObject;
import com.android.game.clash_of_the_balls.game.Vector;

public class EventImpact extends Event {
	private static final String TAG = "EventImpact";
	
	//object impact between 2 objects: a & b
	private short m_id_a;
	private short m_id_b;
	
	private float m_normal_x;
	private float m_normal_y;

	public EventImpact(DataInputStream s) throws IOException {
		super(type_impact);
		m_id_a = s.readShort();
		m_id_b = s.readShort();
		
		m_normal_x = s.readFloat();
		m_normal_y = s.readFloat();
	}
	
	public EventImpact(short id_a, short id_b, Vector normal) {
		super(type_impact);
		m_id_a = id_a;
		m_id_b = id_b;
		
		m_normal_x = normal.x;
		m_normal_y = normal.y;
		
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(type);
		
		s.writeShort(m_id_a);
		s.writeShort(m_id_b);
		
		s.writeFloat(m_normal_x);
		s.writeFloat(m_normal_y);
	}

	public void apply(GameBase game) {
		//we don't need to apply the position
		StaticGameObject obj_a = game.getGameObject(m_id_a);
		StaticGameObject obj_b = game.getGameObject(m_id_b);
		if(obj_a!=null && obj_b!=null) {
			Vector normal = new Vector(m_normal_x, m_normal_y);
			obj_a.handleImpact(obj_b, normal);
			normal.mul(-1.f);
			obj_b.handleImpact(obj_a, normal);
		} else {
			Log.e(TAG, "cannot apply: at least one object is NULL!");
		}
	}
}
