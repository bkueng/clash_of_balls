package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.android.game.clash_of_the_balls.game.GameBase;

import android.util.Log;

/**
 * all game changes can be stored as an Event
 * these Event's will be collected at the server and then sent to the clients
 *
 */
public abstract class Event {
	private static final String TAG = "Event";
	
	public static final byte type_seq_num = 's';
	
	public static final byte type_game_start = 'S';
	public static final byte type_game_end = 'E';
	public static final byte type_game_info = 'G';
	public static final byte type_item_removed = 'R';
	public static final byte type_item_added = 'A';
	public static final byte type_item_update = 'U'; //also used for players
	public static final byte type_impact = 'I';
	
	public final byte type;
	public final int sequence_num;
	
	public Event(byte type, int seq_num) {
		this.type = type;
		sequence_num = seq_num;
	}
	
	//Network conversion
	//returns new Event or null if end of stream
	public static Event read(DataInputStream s) {
		try {
			byte b=s.readByte();
			int seq_num = -1;
			if(b == type_seq_num) {
				seq_num = s.readInt();
				b = s.readByte();
			}
			switch(b) {
			case type_game_start: return new EventGameStartNow(seq_num);
			case type_game_end: return new EventGameEnd(seq_num);
			case type_item_removed: return new EventItemRemoved(s, seq_num);
			case type_item_added: return new EventItemAdded(s, seq_num);
			case type_item_update: return new EventItemUpdate(s);
			case type_impact: return new EventImpact(s, seq_num);
			default: throw new IOException();
			}
			
		} catch (IOException e) {
			Log.e(TAG, "Failed to read event ("+e.getMessage()+")");
		}
		
		return null;
	}
	public void write(DataOutputStream s) throws IOException {
		if(sequence_num != -1) {
			s.writeByte(type_seq_num);
			s.writeInt(sequence_num);
		}
	}
	
	//apply this event to the game
	public abstract void apply(GameBase game);
	
	//TODO: add undo method ???
}
