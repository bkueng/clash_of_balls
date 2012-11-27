package com.android.game.clash_of_the_balls.game;

import android.util.Log;

import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo.PlayerInfo;

/**
 * this represents a player of the game. ie a ball 
 *
 */
public class GamePlayer extends DynamicGameObject {
	
	private int m_color; //ARGB
	
	public int color() { return m_color; }
	
	private Vector m_acceleration = new Vector();
	public Vector acceleration() { return m_acceleration; }
	
	//TODO: add overlay texture
	

	public GamePlayer(GameBase owner, short id, Vector position
			, int color, Texture texture) {
		super(owner, id, position, Type.Player, texture);
		m_color = color;
	}
	
	public GamePlayer(PlayerInfo info, GameBase owner, Texture texture) {
		super(owner, info.id, new Vector(info.pos_x, info.pos_y), Type.Player, texture);
		m_color = info.color;
	}
	
	
	public void move(float dsec) {
		//update position
		m_new_pos.x = m_position.x + (m_speed.x + dsec * m_acceleration.x / 2.f) * dsec;
		m_new_pos.y = m_position.y + (m_speed.y + dsec * m_acceleration.y / 2.f) * dsec;
		//update speed
		m_speed.x += dsec * m_acceleration.x;
		m_speed.y += dsec * m_acceleration.y;
		
		m_has_moved = true;
		
	}
	
	public void draw(RenderHelper renderer) {
		if(!m_bIs_dead) {
			super.draw(renderer);
			//TODO
			
		}
	}

}
