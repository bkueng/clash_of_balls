package com.android.game.clash_of_the_balls.game;

import android.opengl.GLES20;

import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.VertexBufferFloat;

/**
 * game object that does not move, but can collide with moving objects
 * 
 * texture is optional
 *
 */
public class StaticGameObject extends GameObject {
	
	public enum Type {
		Background,
		Hole,
		Wall,
		Player,
		Item
	}
	
	public final Type type;
	
	public final short m_id; //object id: this is unique across a game
						   //used by the network to identify a dynamic game object
						   //(it is not used for background objects)
						   //the lowest id is 1
	
	protected float m_rotation_angle=0.f; //in rad, texture rotation
	
	public void setRotation(float angle_rad) {//CCW
		m_rotation_angle = angle_rad;
	}
	
	protected float m_elastic_factor = 1.0f; //for object collisions
	public float elasticFactor() { return m_elastic_factor; }
	
	protected Texture m_texture;
	
	StaticGameObject(final short id, Vector position, Type type
			, Texture texture) {
		super(position);
		this.type = type;
		m_id = id;
		m_texture = texture;
		
	}
	
	public void handleImpact(StaticGameObject other) {
		//do nothing
	}

	public void draw(RenderHelper renderer) {
		
		drawTexture(renderer);
		
		doModelTransformation(renderer);
		
		//position data
		//we assume it's already set to [0,0], [1,1]
		// (VertexBufferFloat.sprite_position_data)
		
        // color
		//we assume it's already set to 0xffffff
		// (VertexBufferFloat.sprite_color_data_white)
		
		renderer.apply();
		
        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);                               
        
        undoModelTransformation(renderer);
	}
	
	protected void drawTexture(RenderHelper renderer) {
		if(m_texture != null) {
			renderer.shaderManager().activateTexture(0);
			m_texture.useTexture(renderer);
		} else {
			renderer.shaderManager().deactivateTexture();
		}
	}
	
	//these can be overridden by subclasses to customize model transformation
	protected void doModelTransformation(RenderHelper renderer) {
		//translate
		renderer.pushModelMat();
		
		if(m_rotation_angle == 0.f) {
			renderer.modelMatTranslate(m_position.x-0.5f, m_position.y-0.5f, 0.f);
		} else {
			renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
			renderer.modelMatRotate(m_rotation_angle*180.f/(float)Math.PI
					, 0.f, 0.f, 1.f);
			renderer.modelMatTranslate(-0.5f, -0.5f, 0.f);
		}
	}
	
	protected void undoModelTransformation(RenderHelper renderer) {
		renderer.popModelMat();
	}
	

	public void move(float dsec) {
		// nothing to do
	}
}
