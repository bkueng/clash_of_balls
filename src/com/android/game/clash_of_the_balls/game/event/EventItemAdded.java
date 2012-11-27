package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.android.game.clash_of_the_balls.game.GameBase;
import com.android.game.clash_of_the_balls.game.StaticGameObject;
import com.android.game.clash_of_the_balls.game.StaticGameObject.Type;

public class EventItemAdded extends Event {
	
	private Type m_object_type;
	private short m_id;
	private float m_pos_x;
	private float m_pos_y;
	//TODO: additional type specific data ?

	public EventItemAdded(DataInputStream s, int seq_num) throws IOException {
		super(type_item_added, seq_num);
		m_object_type = Type.values()[s.readByte()];
		m_id = s.readShort();
		m_pos_x = s.readFloat();
		m_pos_y = s.readFloat();
		//additional data?
	}
	
	public EventItemAdded(GameBase game, int seq_num, StaticGameObject obj) {
		super(type_item_added, seq_num);
		m_object_type = obj.type;
		m_id = obj.m_id;
		m_pos_x = obj.pos().x;
		m_pos_y = obj.pos().y;
		//additional data?
	}

	public void write(DataOutputStream s) throws IOException {
		super.write(s);
		s.writeByte(type);
		s.writeByte(m_object_type.ordinal());
		s.writeShort(m_id);
		s.writeFloat(m_pos_x);
		s.writeFloat(m_pos_y);
		//additional data?
	}

	public void apply(GameBase game) {
		// TODO create the object & add to game
		//  -> add GameBase method create[object]
		
	}
}
