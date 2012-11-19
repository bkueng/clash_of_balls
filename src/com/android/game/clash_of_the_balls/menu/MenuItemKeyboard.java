package com.android.game.clash_of_the_balls.menu;

import android.content.Context;
import android.support.v4.content.Loader.ForceLoadContentObserver;
import android.view.inputmethod.InputMethodManager;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.game.Vector;

/**
 * Menu item with keyboard input
 *
 */
public class MenuItemKeyboard extends MenuItem {

	private Context m_activity_context;
	
	public MenuItemKeyboard(Vector position, Vector size, Font2D font
			, Context activity_context) {
		super(position, size, font);
		m_activity_context = activity_context;
	}
	
	
	public void onTouchDown(float x, float y) {
		// do nothing
	}
	public void onTouchUp(float x, float y) {
		//TODO: show soft keyboard & grab inputs
		/*
		InputMethodManager imm = (InputMethodManager)m_activity_context
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		*/
		//or like this:
		/*
		//Show soft-keyboard: getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

  		//hide keyboard : getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		 */

		/*
		 to receive key events: 
		 	in the activity: pass the event via queueEvent (like touch event) to renderer
				, then to UIHandler then to menu ...
				-> add dispatchKeyEvent func to ITouchInput
			do in the activity:

			@Override
			public boolean dispatchKeyEvent(KeyEvent KEvent) 
			{
				int keyaction = KEvent.getAction();

				if(keyaction == KeyEvent.ACTION_DOWN)
				{
					int keycode = KEvent.getKeyCode();
					int keyunicode = KEvent.getUnicodeChar(KEvent.getMetaState() );
					char character = (char) keyunicode;

					System.out.println("DEBUG MESSAGE KEY=" + character + " KEYCODE=" +  keycode);
				}


				return super.dispatchKeyEvent(KEvent);
			}

		*/
	}

}
