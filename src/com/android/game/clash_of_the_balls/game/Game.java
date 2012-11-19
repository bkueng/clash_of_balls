package com.android.game.clash_of_the_balls.game;


import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.UIBase;
import com.android.game.clash_of_the_balls.UIHandler;


public class Game extends GameBase implements UIBase {
	
	//own player
	
	public Game(GameSettings s, TextureManager texture_manager) {
		super(false, s);
		// TODO Auto-generated constructor stub
		
		//TODO: use gravity sensor
	}

	public void onTouchEvent(float x, float y, int event) {
		// that's not used in the game
	}

	public void move(float dsec) {
		// TODO Auto-generated method stub
		
		//get sensor values & send to server
		
	}

	public void draw(RenderHelper renderer) {
		// TODO Auto-generated method stub
		
	}

	public UIHandler.UIChange UIChange() {
		// TODO Auto-generated method stub
		
		return UIHandler.UIChange.NO_CHANGE;
	}

	public void onActivate() {
		// ignore
	}

	public void onDeactivate() {
		// ignore
	}

}
