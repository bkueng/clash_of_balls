package com.android.game.clash_of_the_balls.game;

import com.android.game.clash_of_the_balls.Texture;

public class GameBoarder extends StaticGameObject {

	public GameBoarder(Vector pos,Type type, Texture texture) {
		super((short)-1,pos,type, texture);
		
		float angle = (float)Math.PI/2;
		
		if(type==Type.Boarder_down){
			setRotation(angle);
		}else if(type==Type.Boarder_up){
			setRotation(3*angle);
		}else if(type==Type.Boarder_right){
			setRotation(2*angle);
		}else if(type==Type.Boarder_left){
			//DO NOTHING
		}
	}

	
	
}
