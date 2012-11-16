package com.android.game.clash_of_the_balls.menu;


import java.util.ArrayList;

import android.util.Log;
import android.view.MotionEvent;

import com.android.game.clash_of_the_balls.Font2D;
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
	protected Font2D m_item_font;
	
	protected UIHandler.UIChange m_ui_change;
	
	public GameMenuBase(Font2D item_font, MenuBackground background) {
		m_item_font = item_font;
		m_ui_change = UIChange.NO_CHANGE;
		m_background = background;
	}

	@Override
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
					//if touch Up is outside the item, it must be deselected here
					item.deselect();
				}
			}
		}
	}

	@Override
	public void move(float dsec) {
		for(int i=0; i<m_menu_items.size(); ++i)
			m_menu_items.get(i).move(dsec);
		
		if(m_background != null) m_background.move(dsec);
	}

	@Override
	public void draw(RenderHelper renderer) {
		for(int i=0; i<m_menu_items.size(); ++i)
			m_menu_items.get(i).draw(renderer);
		
		if(m_background != null) m_background.draw(renderer);
	}
	
	//these methods are called whenever an item is touched
	//item.onTouch* is already called. these methods are for menu change events
	protected abstract void onTouchDown(MenuItem item);
	protected abstract void onTouchUp(MenuItem item);

	@Override
	public UIHandler.UIChange UIChange() {
		return m_ui_change;
	}
	
	@Override
	public void onActivate() {
		// ignore it
	}

	@Override
	public void onDeactivate() {
		m_ui_change = UIChange.NO_CHANGE;
	}

}
