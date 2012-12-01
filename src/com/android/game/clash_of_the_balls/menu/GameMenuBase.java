package com.android.game.clash_of_the_balls.menu;


import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.android.game.clash_of_the_balls.UIBase;
import com.android.game.clash_of_the_balls.UIHandler;
import com.android.game.clash_of_the_balls.UIHandler.UIChange;
import com.android.game.clash_of_the_balls.game.RenderHelper;

/**
 * GameMenuBase
 * base class for a menu
 *
 */
public abstract class GameMenuBase implements UIBase {
	
	private static final String LOG_TAG = "GameMenuBase";
	
	protected ArrayList<MenuItem> m_menu_items = new ArrayList<MenuItem>();
	protected MenuBackground m_background;
	protected Context m_activity_context;
	
	protected UIHandler.UIChange m_ui_change;
	
	public GameMenuBase(MenuBackground background,Context context) {
		m_ui_change = UIChange.NO_CHANGE;
		m_background = background;
		m_activity_context=context;
	}

	public void onTouchEvent(float x, float y, int event) {
		for(int i=0; i<m_menu_items.size(); ++i) {
			MenuItem item = m_menu_items.get(i);
			if(item.isInside(x, y)) {
				if(event == MotionEvent.ACTION_DOWN) {
					Log.d(LOG_TAG, "Item pressed: Down event");
					item.onTouchDown(x, y);
					onTouchDown(item);
				} else if(event == MotionEvent.ACTION_UP) {
					Log.d(LOG_TAG, "Item pressed: Up event");
					item.onTouchUp(x, y);
					onTouchUp(item);
				}
			} else {
				if(event == MotionEvent.ACTION_UP) {
					//if touch Up is outside the item, it must be 
					//deselected here
					item.deselect();
				}
			}
		}
	}

	public void move(float dsec) {
		for(int i=0; i<m_menu_items.size(); ++i)
			m_menu_items.get(i).move(dsec);
		
		if(m_background != null) m_background.move(dsec);
	}

	public void draw(RenderHelper renderer) {
		if(m_background != null) m_background.draw(renderer);
		
		for(int i=0; i<m_menu_items.size(); ++i) 
			m_menu_items.get(i).draw(renderer);
	}
	
	//these methods are called whenever an item is touched
	//item.onTouch* is already called. these methods are for menu change events
	protected abstract void onTouchDown(MenuItem item);
	protected abstract void onTouchUp(MenuItem item);

	public UIHandler.UIChange UIChange() {
		UIHandler.UIChange ret = m_ui_change;
		m_ui_change = UIChange.NO_CHANGE;
		return ret;
	}
	
	public void onActivate() {
		// ignore it
	}

	public void onDeactivate() {
		m_ui_change = UIChange.NO_CHANGE;
	}

}
