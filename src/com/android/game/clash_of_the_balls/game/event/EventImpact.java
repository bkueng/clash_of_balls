package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.android.game.clash_of_the_balls.game.GameBase;

public class EventImpact extends Event {

	public EventImpact(DataInputStream s, int seq_num) {
		super(type_impact, seq_num);
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
