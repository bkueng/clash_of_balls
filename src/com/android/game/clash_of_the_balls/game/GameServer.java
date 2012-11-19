package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.GameSettings;

/**
 * GameServer
 * this implements the server of the game: it does the network communication
 * and runs independantly in a thread
 *
 */
public class GameServer extends GameBase implements Runnable {

	public GameServer(GameSettings s) {
		super(true, s);
		// TODO Auto-generated constructor stub
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}

}
