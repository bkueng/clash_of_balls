package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.android.game.clash_of_the_balls.game.GameBase;

public class EventItemUpdate extends Event{

	public EventItemUpdate(DataInputStream s) {
		super(type_item_update, -1);
		//no additional data
	}

	public void write(DataOutputStream s) throws IOException {
		super.write(s);
		s.writeByte(type);
	}

	public void apply(GameBase game) {
		// TODO Auto-generated method stub
		
	}
}
