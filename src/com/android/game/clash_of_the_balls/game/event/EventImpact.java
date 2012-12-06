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
	private float m_a_x; //object position a at time of impact
	private float m_a_y;
	
	private short m_id_b;
	private float m_b_x;
	private float m_b_y;

	public EventImpact(DataInputStream s, int seq_num) throws IOException {
		super(type_impact, seq_num);
		m_id_a = s.readShort();
		m_a_x = s.readFloat();
		m_a_y = s.readFloat();
		
		m_id_b = s.readShort();
		m_b_x = s.readFloat();
		m_b_y = s.readFloat();
	}
	
	public EventImpact(int seq_num, short id_a, Vector pos_a, short id_b, Vector pos_b) {
		super(type_impact, seq_num);
		m_id_a = id_a;
		m_a_x = pos_a.x;
		m_a_y = pos_a.y;
		
		m_id_b = id_b;
		m_b_x = pos_b.x;
		m_b_y = pos_b.y;
		
	}

	public void write(DataOutputStream s) throws IOException {
		super.write(s);
		s.writeByte(type);
		
		s.writeShort(m_id_a);
		s.writeFloat(m_a_x);
		s.writeFloat(m_a_y);
		
		s.writeShort(m_id_b);
		s.writeFloat(m_b_x);
		s.writeFloat(m_b_y);
	}

	public void apply(GameBase game) {
		//we don't need to apply the position
		StaticGameObject obj_a = game.getGameObject(m_id_a);
		StaticGameObject obj_b = game.getGameObject(m_id_b);
		if(obj_a!=null && obj_b!=null) {
			obj_a.handleImpact(obj_b);
			obj_b.handleImpact(obj_a);
		} else {
			Log.e(TAG, "cannot apply: at least one object is NULL!");
		}
	}
}
