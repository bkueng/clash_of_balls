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

import com.sapos_aplastados.game.clash_of_balls.game.GameBase;
import com.sapos_aplastados.game.clash_of_balls.game.GameItem;
import com.sapos_aplastados.game.clash_of_balls.game.StaticGameObject;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;
import com.sapos_aplastados.game.clash_of_balls.game.GameItem.ItemType;
import com.sapos_aplastados.game.clash_of_balls.game.StaticGameObject.Type;

public class EventItemAdded extends Event {
	
	private Type m_object_type;
	private short m_id;
	private float m_pos_x;
	private float m_pos_y;
	//item specific stuff
	private ItemType m_item_type;

	public EventItemAdded() {
		super(type_item_added);
	}
	
	public void init(GameBase game, StaticGameObject obj) {
		m_object_type = obj.type;
		m_id = obj.m_id;
		m_pos_x = obj.pos().x;
		m_pos_y = obj.pos().y;
		
		switch(obj.type) {
		case Item:
			m_item_type = ((GameItem)obj).itemType();
			break;
		default:
			throw new RuntimeException("unsupported item type ("+m_object_type+") for EventItemAdd");
		}
	}
	public void init(DataInputStream s) throws IOException {
		m_object_type = Type.values()[s.readByte()];
		m_id = s.readShort();
		m_pos_x = s.readFloat();
		m_pos_y = s.readFloat();
		
		switch(m_object_type) {
		case Item:
			m_item_type = ItemType.values()[s.readByte()];
			break;
		default:
		}
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(type);
		s.writeByte(m_object_type.ordinal());
		s.writeShort(m_id);
		s.writeFloat(m_pos_x);
		s.writeFloat(m_pos_y);
		
		switch(m_object_type) {
		case Item:
			s.writeByte(m_item_type.ordinal());
			break;
		default:
		}
	}

	public void apply(GameBase game) {
		switch(m_object_type) {
		case Item:
			game.addItem(m_id, m_item_type, new Vector(m_pos_x, m_pos_y));
			break;
		default:
		}
	}
}
