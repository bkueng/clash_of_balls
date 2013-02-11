package com.sapos_aplastados.game.clash_of_balls.menu;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.sapos_aplastados.game.clash_of_balls.R;
import com.sapos_aplastados.game.clash_of_balls.Font2D;
import com.sapos_aplastados.game.clash_of_balls.GameSettings;
import com.sapos_aplastados.game.clash_of_balls.TextureManager;
import com.sapos_aplastados.game.clash_of_balls.Font2D.TextAlign;
import com.sapos_aplastados.game.clash_of_balls.UIHandler.UIChange;
import com.sapos_aplastados.game.clash_of_balls.game.RenderHelper;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;
import com.sapos_aplastados.game.clash_of_balls.menu.MenuItemArrow.ArrowType;
import com.sapos_aplastados.game.clash_of_balls.network.NetworkClient;

public class HelpMenu extends GameMenuBase {

	private TextureManager m_tex_manager;
	private Font2D.Font2DSettings m_label_font_settings;

	float m_screen_width;
	float m_screen_height;

	private int m_curr_page = 0;
	private int m_num_pages = 0;

	private MenuItemArrow m_button_next;
	private MenuItemArrow m_button_prev;
	private MenuItemGreyButton m_button_back;

	private boolean m_left_visible = false;
	private boolean m_right_visible = false;
	private boolean m_back_visible = false;

	private Vector pos;
	private Vector size;

	private float img_width;
	private float img_height;

	private float text_width;
	private float text_height;

	private float offset_x;
	private float offset_y;

	ArrayList<ArrayList<MenuItem>> m_pages = new ArrayList<ArrayList<MenuItem>>();

	public HelpMenu(MenuBackground background, float screen_width,
			float screen_height, TextureManager tex_manager, Context context,
			Font2D.Font2DSettings font_settings, int label_font_color,
			GameSettings settings, NetworkClient network_client) {
		super(background, context);

		m_screen_height = screen_height;
		m_screen_width = screen_width;
		
		pos = new Vector(0.f, 0.f);
		size = new Vector(m_screen_width, m_screen_height);
		
		img_width = size.x * 0.6f;
		img_height = .5f * size.y;
		
		text_height = 0.4f * size.y;
		text_width = 0.8f * size.x;
		
		offset_x = size.x * 0.025f;
		offset_y = offset_x * 0.5f;

		m_tex_manager = tex_manager;

		m_label_font_settings = new Font2D.Font2DSettings(
				font_settings.m_typeface, TextAlign.CENTER, label_font_color);

		// Add Pages
		addPage("Push the others into\n the holes", R.raw.img_first);

		addPage("Control your ball\n balancing your phone!", R.raw.img_second);

		addPage("Watch out\nfor Walls", R.raw.img_third);

		addPage("At the beginning, keep your\n phone still to choose\n your equilibrium state",
				R.raw.img_four);
		
		addPage("There are good\n...and bad items", R.raw.img_fifth);

		m_button_next = new MenuItemArrow(new Vector(pos.x + size.x / 2
				+ img_width / 2+offset_x, 
				pos.y + offset_y),
				new Vector(img_height / 2,
				img_height / 2), m_tex_manager, ArrowType.RIGHT);

		m_button_prev = new MenuItemArrow(new Vector(pos.x +size.x/2 -img_width/2-offset_x-img_height/2, pos.y
				+ offset_y), new Vector(img_height / 2, img_height / 2),
				m_tex_manager, ArrowType.LEFT);
		
		m_button_back = new MenuItemGreyButton(new Vector(pos.x + size.x / 2
				                + img_width / 2+offset_x/2, 
				                pos.y +  offset_y),
				                new Vector(img_height / 1.5f,
				                img_height / 2.2f), m_tex_manager,"Menu",font_settings);

		handlePageChanged();
	}

	private void handlePageChanged() {
		
		if (m_curr_page > 0) {
			m_left_visible = true;
		} else {
			m_left_visible = false;
		}
		if (m_curr_page < m_num_pages - 1) {
			m_right_visible = true;
			m_back_visible=false;
		} else {
			m_right_visible = false;
			m_back_visible=true;
		}
		m_menu_items = m_pages.get(m_curr_page);

		Log.d("debug", "Current Page:" + String.valueOf(m_curr_page));
	}

	private void addPage(String text, int raw_image) {
		ArrayList<MenuItem> tmp_page = new ArrayList<MenuItem>();

		tmp_page.add(new MenuItemStringMultiline(new Vector(pos.x + size.x / 2
				- text_width / 2, pos.y + size.y - text_height - offset_y),
				new Vector(text_width, text_height), m_label_font_settings,
				text, m_tex_manager));

		tmp_page.add(new MenuItemImg(new Vector(pos.x + size.x / 2 - img_width
				/ 2, pos.y + offset_y), new Vector(img_width, img_height),
				m_tex_manager, raw_image));

		m_pages.add(tmp_page);
		m_num_pages++;

	}

	public void draw(RenderHelper renderer) {
		if (m_background != null)
			m_background.draw(renderer);
		for (int i = 0; i < m_menu_items.size(); ++i)
			m_menu_items.get(i).draw(renderer);
		if (m_left_visible) m_button_prev.draw(renderer);
		if (m_right_visible) m_button_next.draw(renderer);
		if (m_back_visible) m_button_back.draw(renderer);
		
	}

	@Override
	public void onTouchEvent(float x, float y, int event) {
		super.onTouchEvent(x, y, event);
		if (event == MotionEvent.ACTION_DOWN) {
			if (m_button_prev.isInside(x, y) && m_left_visible) {
				m_button_prev.onTouchDown(x, y);
				onTouchDown(m_button_prev);
			}else if (m_button_next.isInside(x, y) && m_right_visible) {
				m_button_next.onTouchDown(x, y);
				onTouchDown(m_button_next);
			}else if (m_button_back.isInside(x, y) && m_back_visible) {
				m_button_back.onTouchDown(x, y);
				onTouchDown(m_button_back);
			}
		} else if (event == MotionEvent.ACTION_UP) {
			if (m_button_prev.isInside(x, y) && m_left_visible) {
				m_button_prev.onTouchUp(x, y);
				onTouchUp(m_button_prev);
			}else if (m_button_next.isInside(x, y) && m_right_visible) {
				m_button_next.onTouchUp(x, y);
				onTouchUp(m_button_next);
			}else if (m_button_back.isInside(x, y) && m_back_visible) {
				m_button_back.onTouchUp(x, y);
				onTouchUp(m_button_back);
			}
		}
	}

	@Override
	protected void onTouchDown(MenuItem item) {
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if (item == m_button_next) {
			nextPage();
		} else if (item == m_button_prev) {
			previousPage();
		} else if (item == m_button_back){
			m_curr_page=0;
			m_button_back.remain_unpressed();
			m_ui_change = UIChange.MAIN_MENU;
			handlePageChanged();
		}

	}

	public void previousPage() {
		m_curr_page = (--m_curr_page) % m_num_pages;
		handlePageChanged();
	}

	public void nextPage() {
		m_curr_page = (++m_curr_page) % m_num_pages;
		handlePageChanged();
	}

}
