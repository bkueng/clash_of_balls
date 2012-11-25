package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.util.Log;

import com.android.game.clash_of_the_balls.game.DynamicGameObject;
import com.android.game.clash_of_the_balls.game.GameBase;

public class EventItemUpdate extends Event {
	private static final String TAG = "EventItemUpdate";
	
	private short m_id;
	private float m_pos_x;
	private float m_pos_y;
	private float m_speed_x;
	private float m_speed_y;

	public EventItemUpdate(DataInputStream s) throws IOException {
		super(type_item_update, -1);
		m_id = s.readShort();
		m_pos_x = s.readFloat();
		m_pos_y = s.readFloat();
		m_speed_x = s.readFloat();
		m_speed_y = s.readFloat();
	}
	
	public EventItemUpdate(DynamicGameObject obj) {
		super(type_item_update, -1);
		m_id = obj.m_id;
		m_pos_x = obj.pos().x;
		m_pos_y = obj.pos().y;
		m_speed_x = obj.speed().x;
		m_speed_y = obj.speed().y;
	}

	public void write(DataOutputStream s) throws IOException {
		super.write(s);
		s.writeByte(type);
		s.writeShort(m_id);
		s.writeFloat(m_pos_x);
		s.writeFloat(m_pos_y);
		s.writeFloat(m_speed_x);
		s.writeFloat(m_speed_y);
	}

	public void apply(GameBase game) {
		DynamicGameObject obj = game.getGameObject(m_id);
		if(obj!=null) {
			obj.pos().set(m_pos_x, m_pos_y);
			obj.speed().set(m_speed_x, m_speed_y);
		} else {
			Log.e(TAG, "cannot apply: obj is null!");
		}
	}
}
