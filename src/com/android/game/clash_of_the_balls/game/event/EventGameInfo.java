package com.android.game.clash_of_the_balls.game.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.game.DynamicGameObject;
import com.android.game.clash_of_the_balls.game.GameBase;
import com.android.game.clash_of_the_balls.game.GamePlayer;
import com.android.game.clash_of_the_balls.game.StaticGameObject.Type;

public class EventGameInfo extends Event {
	
	//initial player info
	public static class PlayerInfo {
		public float pos_x;
		public float pos_y;
		public int color;
		public short id;
		public String unique_name;
	}
	private int m_player_count;
	private PlayerInfo[] m_players;
	private GameLevel m_level;

	public EventGameInfo(DataInputStream s, int seq_num) throws IOException {
		super(type_game_info, seq_num);
		//players
		m_player_count = s.readInt();
		m_players = new PlayerInfo[m_player_count];
		for(int i=0; i<m_player_count; ++i) {
			m_players[i] = new PlayerInfo();
			m_players[i].pos_x = s.readFloat();
			m_players[i].pos_y = s.readFloat();
			m_players[i].color = s.readInt();
			m_players[i].id = s.readShort();
			short name_len = s.readShort();
			byte[] buffer = new byte[name_len];
			if(s.read(buffer) < name_len) throw new IOException();
			m_players[i].unique_name = new String(buffer);
		}
		//game level
		m_level = new GameLevel(null);
		m_level.loadLevel(s);
	}
	public EventGameInfo(GameBase game, int seq_num) {
		super(type_game_info, seq_num);
		//players
		m_player_count = game.playerCount();
		m_players = new PlayerInfo[m_player_count];
		int i=0;
		for (Map.Entry<Short, DynamicGameObject> entry : game.m_game_objects.entrySet()) {
			DynamicGameObject obj = entry.getValue();
			if(obj.m_type == Type.Player) {
				GamePlayer player = (GamePlayer) obj;
				m_players[i]=new PlayerInfo();
				m_players[i].pos_x = player.pos().x;
				m_players[i].pos_y = player.pos().y;
				m_players[i].color = player.color();
				m_players[i].id = player.m_id;
				m_players[i].unique_name = game.getUniqueNameFromPlayerId(player.m_id);
				
				++i;
			}
		}
		assert(i == m_player_count);
		//level
		m_level = game.level();
	}

	public void write(DataOutputStream s) throws IOException {
		super.write(s);
		s.writeByte(type);
		//players
		s.writeInt(m_player_count);
		for(int i=0; i<m_player_count; ++i) {
			s.writeFloat(m_players[i].pos_x);
			s.writeFloat(m_players[i].pos_y);
			s.writeInt(m_players[i].color);
			s.writeShort(m_players[i].id);
			byte[] unique_name=m_players[i].unique_name.getBytes();
			s.writeShort((short)unique_name.length);
			s.write(unique_name);
		}
		//level
		m_level.write(s);
	}

	public void apply(GameBase game) {
		game.initGame(m_level);
		game.initPlayers(m_players);
	}
}
