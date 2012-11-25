package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.util.Log;

import com.android.game.clash_of_the_balls.game.DynamicGameObject;
import com.android.game.clash_of_the_balls.game.GameBase;

public class EventItemRemoved extends Event {
	private static final String TAG = "EventItemRemoved";
	
	private short m_id;

	public EventItemRemoved(DataInputStream s, int seq_num) throws IOException {
		super(type_item_removed, seq_num);
		m_id = s.readShort();
	}
	
	public EventItemRemoved(int seq_num, short item_id) {
		super(type_item_removed, seq_num);
		m_id = item_id;
	}

	public void write(DataOutputStream s) throws IOException {
		super.write(s);
		s.writeByte(type);
		s.writeShort(m_id);
	}

	public void apply(GameBase game) {
		DynamicGameObject obj = game.getGameObject(m_id);
		if(obj!=null) {
			obj.die();
		} else {
			Log.e(TAG, "cannot apply: obj is null!");
		}
	}
}
