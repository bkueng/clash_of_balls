package com.android.game.clash_of_the_balls.game;

import android.opengl.Matrix;


/**
 * GameView
 * this is used by the Game to determine the rectangle of the current view
 * if the game level is big, the view is moved around depending on the users
 * position. it manipulates the model view matrix
 *
 */
public class GameView extends GameObject {
	
	private static final float boundary_size = 1.f/4.f; //in screen size, 
					//if tracked object crosses bounaries, the view is moved
	
	private Vector m_output_size; //in screen coordinates
	
	private float m_level_width;
	private float m_level_height;
	
	private float m_scaling; //game_pos * m_scaling = output pos
	// m_position is in output coordinates
	
	private GameObject m_object_to_track;
	
	public GameView(float output_width, float output_height
			, GameObject object_to_track, float level_width, float level_height) {
		m_object_to_track = object_to_track;
		m_output_size = new Vector(output_width, output_height);
		m_level_width = level_width;
		m_level_height = level_height;
		fitLevelToOutputView();
	}
	
	public void setObjectToTrack(GameObject object_to_track) {
		m_object_to_track = object_to_track;
		checkObjectWithinBoundary();
		checkLevelWithinOutputView();
	}
	
	//amount: 1 = no change
	//       >1 = zoom in
	//       <1 = zoom out
	public void zoom(float amount) {
		if(amount <= 0.f) return;
		
		float new_scaling = m_scaling / amount;
		if(new_scaling > 2.f * m_output_size.x) { //restrict max zoom level 
			new_scaling = 2.f * m_output_size.x;
		}
		m_position.x -= (new_scaling-m_scaling)/2.f;
		m_position.y -= (new_scaling-m_scaling)/2.f;
		m_scaling = new_scaling;

		handleViewSizeChanged();
	}
	
	//v: in screen coordinates
	public void moveView(Vector v) {
		m_position.add(v);
		handleViewSizeChanged();
	}
	
	//set zoom such that one tile has the output width & height output_tile_size
	public void setZoomToTileSize(float output_tile_size) {
		m_scaling = output_tile_size;
		handleViewSizeChanged();
	}
	public float getZoomToTileSize() {
		return m_scaling; //game tile size = 1 -> output tile size = scaling * 1
	}

	public void draw(RenderHelper renderer) {
		// nothing to do
	}

	public void move(float dsec) {
		checkObjectWithinBoundary();
		checkLevelWithinOutputView();
	}
	
	//call this before rendering
	public void applyView(RenderHelper renderer) {
		//translate & scale
		int model_mat_pos = renderer.pushModelMat();
		float model_mat[] = renderer.modelMat();
		Matrix.setIdentityM(model_mat, model_mat_pos);
		Matrix.translateM(model_mat, model_mat_pos, m_position.x, m_position.y, 0.f);
		Matrix.scaleM(model_mat, model_mat_pos, m_scaling, m_scaling, 0.f);
	}
	//call this after rendering
	public void resetView(RenderHelper renderer) {
		renderer.popModelMat();
	}
	
	private void handleViewSizeChanged() {
		//if level is smaller then output view, fit level into output view
		boolean smaller_x=m_scaling*m_level_width < m_output_size.x;
		boolean smaller_y=m_scaling*m_level_height < m_output_size.y;
		if(smaller_x) {
			if(smaller_y) fitLevelToOutputView();
			else
				m_position.x = (m_output_size.x-m_level_width*m_scaling) / 2.f;
		} else if(smaller_y) {
			m_position.y = (m_output_size.y-m_level_height*m_scaling) / 2.f;
		}
		checkObjectWithinBoundary();
		checkLevelWithinOutputView();
	}
	
	//moves the view if the object is out of boundary
	private void checkObjectWithinBoundary() {
		
		if(m_object_to_track != null) {
			Vector pos = m_object_to_track.pos();
			float scale_x = m_scaling;
			if(pos.x*scale_x < m_position.x + boundary_size*m_output_size.x) {
				m_position.x = pos.x*scale_x - boundary_size*m_output_size.x;
			} else if(pos.x*scale_x > m_position.x + m_scaling*m_level_width
					- boundary_size*m_output_size.x) {
				m_position.x = pos.x*scale_x - 
					(m_position.x + m_scaling*m_level_width
					- boundary_size*m_output_size.x);
			}
			float scale_y = m_scaling;
			if(pos.y*scale_y < m_position.y + boundary_size*m_output_size.y) {
				m_position.y = pos.y*scale_y - boundary_size*m_output_size.y;
			} else if(pos.y*scale_y > m_position.y + m_scaling*m_level_height 
					- boundary_size*m_output_size.y) {
				m_position.y = pos.y*scale_y - 
					(m_position.y + m_scaling*m_level_height
					- boundary_size*m_output_size.y);
			}
		}
	}
	
	private void checkLevelWithinOutputView() {
		if(m_scaling*m_level_width > m_output_size.x) { //does not fit into screen
			if(m_position.x > 0.f) m_position.x=0.f;
			else if(m_position.x + m_scaling*m_level_width < m_output_size.x) 
				m_position.x = m_output_size.x - m_scaling*m_level_width;
		} else {
			m_position.x = (m_output_size.x-m_level_width*m_scaling) / 2.f;
		}
		if(m_scaling*m_level_height > m_output_size.y) {
			if(m_position.y > 0.f) m_position.y=0.f;
			else if(m_position.y + m_scaling*m_level_height < m_output_size.y) 
				m_position.y = m_output_size.y - m_scaling*m_level_height;
		} else {
			m_position.y = (m_output_size.y-m_level_height*m_scaling) / 2.f;
		}
	}
	
	public void fitLevelToOutputView() {
		//make the level touch the output from inside (like the menu)
		float scaling_x = m_output_size.x / m_level_width;
		float scaling_y = m_output_size.y / m_level_height;
		m_scaling = Math.min(scaling_x, scaling_y);
		m_position.x = (m_output_size.x-m_level_width*m_scaling) / 2.f;
		m_position.y = (m_output_size.y-m_level_height*m_scaling) / 2.f;
	}
}
