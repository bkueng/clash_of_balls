/*
 * Copyright (C) 2012-2013 Hans Hardmeier <hanshardmeier@gmail.com>
 * Copyright (C) 2012-2013 Andrin Jenal
 * Copyright (C) 2012-2013 Beat Küng <beat-kueng@gmx.net>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

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
