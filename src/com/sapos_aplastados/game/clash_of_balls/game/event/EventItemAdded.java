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
