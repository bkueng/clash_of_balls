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

public class EventItemRemoved extends Event {
	private static final String TAG = "EventItemRemoved";
	
	private short m_id;

	public EventItemRemoved() {
		super(type_item_removed);
	}
	
	public void init(short item_id) {
		m_id = item_id;
	}
	public void init(DataInputStream s) throws IOException {
		m_id = s.readShort();
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(type);
		s.writeShort(m_id);
	}

	public void apply(GameBase game) {
		DynamicGameObject obj = game.getMoveableGameObject(m_id);
		if(obj!=null) {
			obj.die();
		} else {
			Log.e(TAG, "cannot apply: obj is null!");
		}
	}
}
