package com.android.game.clash_of_the_balls.menu;

import java.util.ArrayList;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.Toast;

import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;

public class MenuItemPresi extends MenuItem {

	private static final String LOG_TAG = "MenuItemPresi";
	
	private ArrayList<MenuItemGreyButton> m_list;
	private int curr_pos;
	private int m_size;
	
	public MenuItemPresi(Vector position, Vector size
			, ArrayList<MenuItemGreyButton> list) {
		super(position, size);
		
		m_list=list;
		if(m_list!=null && m_list.size()>0){
			curr_pos=0;
			m_size = m_list.size();
		}else{
			Log.e(LOG_TAG,"Empty ArrayList in MenuItemPresi");
		}
	}

	public void draw(RenderHelper renderer) {		
		m_list.get(curr_pos).draw(renderer);
	}

	
	public void next(){
		curr_pos = (++curr_pos)%m_size; 
		Log.d(LOG_TAG,"Next Curr Item: "+curr_pos);
	}
	
	public void previous(){
		--curr_pos;
		if(curr_pos<0)curr_pos+=m_size; 
		Log.d(LOG_TAG,"Previous Curr Item: "+curr_pos);
	}
	
	public void move(float dsec) {
		// nothing to do
	}
	
	public void select(){
		for(int i = 0;i<m_size;++i){
			m_list.get(i).remain_unpressed();
		}
		m_list.get(curr_pos).select();
		Log.d(LOG_TAG,"Select MenuItemPresu");
	}
	
	public int getPos(){
		return curr_pos+1;
	}
	
}
