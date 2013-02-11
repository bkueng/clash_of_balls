package com.sapos_aplastados.game.clash_of_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.util.Log;

import com.sapos_aplastados.game.clash_of_balls.game.DynamicGameObject;
import com.sapos_aplastados.game.clash_of_balls.game.GameBase;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;

public class EventItemUpdate extends Event {
	private static final String TAG = "EventItemUpdate";
	
	private short m_id;
	private Vector m_pos=new Vector();
	private Vector m_speed=new Vector();

	public EventItemUpdate() {
		super(type_item_update);
	}
	
	public void init(DynamicGameObject obj) {
		m_id = obj.m_id;
		m_pos.set(obj.pos().x, obj.pos().y);
		m_speed.set(obj.speed().x, obj.speed().y);
	}
	public void init(DataInputStream s) throws IOException {
		m_id = s.readShort();
		float x = s.readFloat();
		float y = s.readFloat();
		m_pos.set(x, y);
		x = s.readFloat();
		y = s.readFloat();
		m_speed.set(x, y);
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(type);
		s.writeShort(m_id);
		s.writeFloat(m_pos.x);
		s.writeFloat(m_pos.y);
		s.writeFloat(m_speed.x);
		s.writeFloat(m_speed.y);
	}

	public void apply(GameBase game) {
		DynamicGameObject obj = game.getMoveableGameObject(m_id);
		if(obj!=null) {
			obj.applyVectorData(m_pos, m_speed);
		} else {
			Log.e(TAG, "cannot apply: obj is null!");
		}
	}
}
