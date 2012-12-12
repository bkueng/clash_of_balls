package com.android.game.clash_of_the_balls.game;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo.PlayerInfo;

/**
 * this represents a player of the game. ie a ball 
 *
 */
public class GamePlayer extends DynamicGameObject {
	
	public final float m_radius = 0.5f;
	public final float m_mass = 10;
	
	private float m_max_speed = 5.f;
	private float m_friction = 3.0f;
	private Vector m_fric_dir = new Vector();

	private int m_color; //ARGB
	private VertexBufferFloat m_color_data_colored;
	
	private float m_scaling=1.f; //for drawing, used for dying effect
	private float m_scaling_speed;
	
	public int color() { return m_color; }
	
	private Vector m_acceleration = new Vector();
	private float m_sensor_scaling = 5.f; //influences the acceleration
	public Vector acceleration() { return m_acceleration; }

	public void applySensorVector(Vector v) {
		if(!m_bIs_dead) {
			m_acceleration.set(v);
			m_acceleration.mul(m_sensor_scaling);
		}
	}
	
	protected Texture m_overlay_texture;

	public GamePlayer(GameBase owner, short id, Vector position
			, int color, Texture texture, Texture texture_overlay) {
		super(owner, id, position, Type.Player, texture);
		m_overlay_texture = texture_overlay;
		m_color = color;
		initColorData(m_color);
	}
	
	public GamePlayer(PlayerInfo info, GameBase owner, Texture texture_base
			, Texture texture_overlay) {
		super(owner, info.id, new Vector(info.pos_x, info.pos_y), Type.Player
				, texture_base);
		m_overlay_texture = texture_overlay;
		m_color = info.color;
		initColorData(m_color);
	}
	
	private void initColorData(int color) {
		float color_data[] = new float[4*4];
		for(int i=0; i<4; ++i) {
			color_data[i*4 + 0] = (float)Color.red(color) / 255.f;
			color_data[i*4 + 1] = (float)Color.green(color) / 255.f;
			color_data[i*4 + 2] = (float)Color.blue(color) / 255.f;
			color_data[i*4 + 3] = (float)Color.alpha(color) / 255.f;
		}
		m_color_data_colored = new VertexBufferFloat(color_data, 4);
	}
	
	public void move(float dsec) {
		super.move(dsec);
		
		//update position
		m_new_pos.x += m_speed.x * dsec;
		m_new_pos.y += m_speed.y * dsec;
		//update speed
		if(m_owner.generate_events) {
			m_speed.x += dsec * m_acceleration.x;
			m_speed.y += dsec * m_acceleration.y;
			
			if (m_speed.length() > dsec * m_friction) {
				m_fric_dir.set(m_speed);
				m_fric_dir.mul(-1/m_speed.length());
				m_fric_dir.mul(dsec);
				m_speed.add(m_fric_dir);
			} else {
				m_speed.set(0,0);
			}
			
			float speed = m_speed.length();
			if(speed > m_max_speed) m_speed.mul(m_max_speed / speed);
		}
		
		m_has_moved = true;
		
		if(m_bIs_dying) {
			m_scaling -= (m_scaling_speed + 1.5f*dsec) * dsec;
			m_scaling_speed += dsec * 2.f*1.5f;
			if(m_scaling < 0.01f) {
				m_bIs_dying = false;
				m_scaling = 0.01f;
			}
		}
	}
	
	public void handleImpact(StaticGameObject other) {
		super.handleImpact(other);
		switch(other.type) {
		case Hole: die();
			break;
		default:
		}
	}
	public void die() {
		if(!m_bIs_dead) {
			m_acceleration.set(0.f, 0.f);
			m_scaling = 1.f;
			m_scaling_speed = 0.f;
			m_bIs_dead = true;
			m_bIs_dying = true;
			m_owner.handleObjectDied(this);
		}
	}
	
	public void draw(RenderHelper renderer) {
		if(!isReallyDead()) {
			
			doModelTransformation(renderer);
			
			//colored texture: m_texture
			
			if(m_texture != null) {
				renderer.shaderManager().activateTexture(0);
				m_texture.useTexture(renderer);
				
				//position data
				int position_handle = renderer.shaderManager().a_Position_handle;
				if(position_handle != -1)
					m_position_data.apply(position_handle);
				
		        // color
				int color_handle = renderer.shaderManager().a_Color_handle;
				if(color_handle != -1)
					m_color_data_colored.apply(color_handle);
				
				renderer.apply();
				
		        // Draw
		        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);     
		        
			}
			
	        
			//overlay texture
			
			if(m_overlay_texture != null) {
				renderer.shaderManager().activateTexture(0);
				m_overlay_texture.useTexture(renderer);
				
				//position data
				int position_handle = renderer.shaderManager().a_Position_handle;
				if(position_handle != -1)
					m_position_data.apply(position_handle);
				
		        // color
				int color_handle = renderer.shaderManager().a_Color_handle;
				if(color_handle != -1)
					m_color_data.apply(color_handle);
				
				renderer.apply();
				
		        // Draw
		        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
			}
	        
	        undoModelTransformation(renderer);
			
		}
	}
	
	protected void doModelTransformation(RenderHelper renderer) {
		//scale & translate
		renderer.pushModelMat();
		renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
		renderer.modelMatScale(m_scaling*m_radius*2.f
				, m_scaling*m_radius*2.f, 0.f);
		renderer.modelMatTranslate(-0.5f, -0.5f, 0.f);
	}
	protected void undoModelTransformation(RenderHelper renderer) {
		renderer.popModelMat();
	}

}
