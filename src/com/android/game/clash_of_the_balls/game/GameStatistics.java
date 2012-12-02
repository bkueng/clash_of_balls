package com.android.game.clash_of_the_balls.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * holds the statistics of the currently running game.
 * each game round has its own statistics
 *
 */
public class GameStatistics {

	public static class PlayerStats {
		public PlayerStats() {}
		public PlayerStats(PlayerStats p) {
			points = p.points;
		}
		public PlayerStats(DataInputStream s) throws IOException {
			points = s.readInt();
		}
		public void write(DataOutputStream s) throws IOException {
			s.writeInt(points);
		}
		
		public int points = 0;
	}
	
	public static class Statistic {
		//this is either for a single round or for current game
		public Map<Short, PlayerStats> stats = new TreeMap<Short, PlayerStats>();
		
		public void reset() {
			stats = new TreeMap<Short, PlayerStats>(); //key is the player id
		}
		public void setPlayerPoints(Short id, int point_count) {
			PlayerStats player = stats.get(id);
			if(player == null) {
				PlayerStats p = new PlayerStats();
				p.points = point_count;
				stats.put(id, p);
			} else {
				player.points = point_count;
				stats.put(id, player);
			}
		}
		
		public void set(Statistic s) {
			reset();
			for(Map.Entry<Short, PlayerStats> entry : s.stats.entrySet()) {
				PlayerStats p = new PlayerStats(entry.getValue());
				stats.put(entry.getKey(), p);
			}
		}
		public void read(DataInputStream s) throws IOException {
			reset();
			int count = s.readInt();
			for(int i=0; i<count; ++i) {
				short id = s.readShort();
				stats.put(id, new PlayerStats(s));
			}
		}
		public void write(DataOutputStream s) throws IOException {
			s.writeInt(stats.size());
			for(Map.Entry<Short, PlayerStats> entry : stats.entrySet()) {
				s.writeShort(entry.getKey());
				entry.getValue().write(s);
			}
		}
	}
	
	private Statistic m_game_stat = new Statistic();
	public Statistic gameStatistics() { return m_game_stat; }
	public void resetGameStatistics() { m_game_stat.reset(); }
	
	private Statistic m_cur_round_stat = new Statistic();
	public Statistic currentRoundStatistics() { return m_cur_round_stat; }
	public void resetRoundStatistics() { m_cur_round_stat.reset(); }
	
	//make a copy from another statistic
	public void set(GameStatistics statistics) {
		m_game_stat.set(statistics.gameStatistics());
		m_cur_round_stat.set(statistics.currentRoundStatistics());
	}
	
	//apply a finished round to current game statistics
	//it does not reset the current round results
	public void applyCurrentRoundStatistics() {
		//add points to game_stat
		for(Map.Entry<Short, PlayerStats> entry : m_cur_round_stat.stats.entrySet()) {
			Short key = entry.getKey();
			PlayerStats game_player = m_game_stat.stats.get(key);
			if(game_player == null) {
				PlayerStats p = new PlayerStats(entry.getValue());
				m_game_stat.stats.put(key, p);
			} else {
				game_player.points += entry.getValue().points;
				m_game_stat.stats.put(key, game_player);
			}
		}
	}
	
	public void read(DataInputStream s) throws IOException {
		m_game_stat.read(s);
		m_cur_round_stat.read(s);
	}
	public void write(DataOutputStream s) throws IOException {
		m_game_stat.write(s);
		m_cur_round_stat.write(s);
	}
	
	
}
