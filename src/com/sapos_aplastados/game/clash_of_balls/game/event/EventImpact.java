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

import com.sapos_aplastados.game.clash_of_balls.GameLevel;
import com.sapos_aplastados.game.clash_of_balls.game.GameBase;
import com.sapos_aplastados.game.clash_of_balls.game.StaticGameObject;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;
import com.sapos_aplastados.game.clash_of_balls.game.event.EventGameInfo.PlayerInfo;

public class EventImpact extends Event {
	private static final String TAG = "EventImpact";
	
	//object impact between 2 objects: a & b
	private short m_id_a;
	private short m_id_b;
	
	private float m_normal_x;
	private float m_normal_y;

	public EventImpact() {
		super(type_impact);
	}
	
	public void init(short id_a, short id_b, Vector normal) {
		m_id_a = id_a;
		m_id_b = id_b;
		
		m_normal_x = normal.x;
		m_normal_y = normal.y;
		
	}
	public void init(DataInputStream s) throws IOException {
		m_id_a = s.readShort();
		m_id_b = s.readShort();
		
		m_normal_x = s.readFloat();
		m_normal_y = s.readFloat();
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
