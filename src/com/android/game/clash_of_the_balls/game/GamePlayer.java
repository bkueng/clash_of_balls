package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo.PlayerInfo;

/**
 * this represents a player of the game. ie a ball 
 *
 */
public class GamePlayer extends DynamicGameObject {
	
	private int m_color; //ARGB
	
	public int color() { return m_color; }
	
	private Vector m_speed = new Vector();
	private Vector m_acceleration = new Vector();
	
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
		//TODO
		
	}
	
	public void draw(RenderHelper renderer) {
		//TODO
	}

}
