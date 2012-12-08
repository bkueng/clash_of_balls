package com.android.game.clash_of_the_balls.menu;

import java.util.ArrayList;
import java.util.List;

import android.opengl.Matrix;

import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.menu.MenuItemArrow.ArrowType;

/**
 * this list can hold multiple MenuItem's in a list
 * scrolling through the list is done through 2 buttons
 * 
 * Note: the added MenuItems must have the same width as the list!
 * the position will be set by this class
 *
 */
public class MenuItemList extends MenuItem {
	
	private static final String LOG_TAG = "MenuItemList";
	
	private float m_view_height; //=m_size.y - (next or prev buttons height)
	private final float m_item_spacing; //vertical spacing between 2 items
	
	private List<MenuItem> m_items = new ArrayList<MenuItem>();
	private int m_sel_item=-1;
	
	private int m_first_drawn_item = 0;
	private int m_last_drawn_item = 0;
	
	//page selection
	MenuItemArrow m_left_arrow;
	boolean m_left_arrow_visible = true;
	MenuItemArrow m_right_arrow;
	boolean m_right_arrow_visible = true;
	
	private Texture m_background_texture;
	
	public MenuItemList(Vector position, Vector size, Vector arrow_button_size
			, TextureManager tex_manager) {
		super(position, size);
		
		m_item_spacing = size.y / 50.f;
		
		float button_y_offset = arrow_button_size.y/4.f;
		float button_x_offset = arrow_button_size.x/3.f;
		m_view_height = size.y - arrow_button_size.y 
					- m_item_spacing - button_y_offset;
		
		m_left_arrow = new MenuItemArrow(
				new Vector(position.x+button_x_offset, position.y+button_y_offset)
				, arrow_button_size
				, tex_manager, ArrowType.LEFT);
		m_right_arrow = new MenuItemArrow(
				new Vector(position.x + size.x-arrow_button_size.x-button_x_offset
						, position.y+button_y_offset)
				, arrow_button_size
				, tex_manager, ArrowType.RIGHT);
		
		
		m_background_texture=tex_manager
				.get(R.raw.texture_list_background);
		
		
		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		m_color_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_color_data_white, 4);
		
		handleItemsChanged();
	}
	
	//add/remove/iterate items
	public void addItem(MenuItem item) {
		m_items.add(item);
		handleItemsChanged();
	}
	//insert at position: all items with index>=position will be moved back
	//if position==size(), the item will be added to the end of the list
	public void addItem(MenuItem item, int position) {
		m_items.add(position, item);
		handleItemsChanged();
	}
	public void removeItem(int idx) {
		if(idx == m_sel_item) m_sel_item=-1;
		m_items.remove(idx);
		if(m_first_drawn_item >= m_items.size()) 
			m_first_drawn_item = m_items.size()-1;
		handleItemsChanged();
	}
	public int itemCount() { return m_items.size(); }
	public MenuItem item(int idx) { return m_items.get(idx); }
	
	
	public void nextPage() {
		if(m_last_drawn_item+1 < m_items.size()) 
			m_first_drawn_item = m_last_drawn_item+1;
		handleItemsChanged();
	}
	public void previousPage() {
		float height = m_view_height;
		int i=m_first_drawn_item;
		if(i>=m_items.size()) i=m_items.size()-1;
		while(i>0 && height>=-m_item_spacing) {
			--i;
			height -= m_item_spacing + m_items.get(i).size().y;
		}
		m_first_drawn_item = i+1;
		if(height >= -m_item_spacing) m_first_drawn_item = 0;
		
		handleItemsChanged();
	}
	
	private void handleItemsChanged() {
		//adjust m_last_drawn_item && item positions
		int i=m_first_drawn_item;
		if(i<0) i=0;
		float y_offset = m_position.y + m_size.y;
		float y_offset_min = y_offset - m_view_height;
		while(i<m_items.size() && y_offset >= y_offset_min-m_item_spacing) {
			MenuItem item=m_items.get(i);
			item.pos().x = m_position.x;
			item.pos().y = y_offset - item.size().y;
			y_offset -= item.size().y + m_item_spacing;
			++i;
		}
		m_last_drawn_item = i-2;
		if(y_offset >= y_offset_min-m_item_spacing) 
			m_last_drawn_item = m_items.size()-1;
		
		m_right_arrow_visible = m_last_drawn_item < m_items.size()-1;
		m_left_arrow_visible = m_first_drawn_item > 0;
	}

	public void selectItem(int item_idx) {
		//deselect last
		MenuItem item = getSelectedItem();
		if(item!=null) item.deselect();
		
		m_sel_item = item_idx;
		//select new
		item = getSelectedItem();
		if(item!=null) item.select();
	}
	
	public MenuItem getSelectedItem() {
		if(m_sel_item < 0 || m_sel_item >= m_items.size()) return null;
		return m_items.get(m_sel_item);
	}
	
	
	public void draw(RenderHelper renderer) {	
		
		//background
		renderer.pushModelMat();
		renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
		renderer.modelMatScale(m_size.x, m_size.y, 0.f);
		
		drawTexture(renderer, m_background_texture);
        renderer.popModelMat();
		
		//items
		int last_drawn = m_last_drawn_item;
		if(last_drawn >= m_items.size()) last_drawn = m_items.size()-1;
		int first_drawn = m_first_drawn_item;
		if(first_drawn<0) first_drawn = 0;
		for(int i=first_drawn; i<=last_drawn; ++i) {
			m_items.get(i).draw(renderer);
		}
        
		//arrows
        if(m_left_arrow_visible) m_left_arrow.draw(renderer);
        if(m_right_arrow_visible) m_right_arrow.draw(renderer);
        
	}
	
	
	public void onTouchDown(float x, float y) {
		if(m_left_arrow.isInside(x, y) && m_left_arrow_visible) {
			m_left_arrow.onTouchDown(x, y);
		}
		if(m_right_arrow.isInside(x, y) && m_right_arrow_visible) {
			m_right_arrow.onTouchDown(x, y);
		}
		
		//items
		int last_drawn = m_last_drawn_item;
		if(last_drawn >= m_items.size()) last_drawn = m_items.size()-1;
		int first_drawn = m_first_drawn_item;
		if(first_drawn<0) first_drawn = 0;
		for(int i=first_drawn; i<=last_drawn; ++i) {
			MenuItem item = m_items.get(i);
			if(item.isInside(x, y)) item.onTouchDown(x, y);
		}
	}
	public void onTouchUp(float x, float y) {
		if(m_left_arrow.isInside(x, y) && m_left_arrow_visible) {
			m_left_arrow.onTouchUp(x, y);
			previousPage();
		} else {
			m_left_arrow.deselect();
		}
		if(m_right_arrow.isInside(x, y) && m_right_arrow_visible) {
			m_right_arrow.onTouchUp(x, y);
			nextPage();
		} else {
			m_right_arrow.deselect();
		}
		
		//items
		int last_drawn = m_last_drawn_item;
		if(last_drawn >= m_items.size()) last_drawn = m_items.size()-1;
		int first_drawn = m_first_drawn_item;
		if(first_drawn<0) first_drawn = 0;
		for(int i=first_drawn; i<=last_drawn; ++i) {
			MenuItem item = m_items.get(i);
			if(item.isInside(x, y)) {
				item.onTouchUp(x, y);
				selectItem(i);
			}
		}
		deselectOtherItems();
	}
	
	private void deselectOtherItems() {
		//deselect all items except for the currently selected item
		int last_drawn = m_last_drawn_item;
		if(last_drawn >= m_items.size()) last_drawn = m_items.size()-1;
		int first_drawn = m_first_drawn_item;
		if(first_drawn<0) first_drawn = 0;
		for(int i=first_drawn; i<=last_drawn; ++i) {
			if(i!=m_sel_item) m_items.get(i).deselect();
		}
	}
	
	public void deselect() {
		m_right_arrow.deselect();
		m_left_arrow.deselect();
		deselectOtherItems();
	}
	

}
