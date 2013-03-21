/*
 * Copyright (C) 2012-2013 Hans Hardmeier <hanshardmeier@gmail.com>
 * Copyright (C) 2012-2013 Andrin Jenal
 * Copyright (C) 2012-2013 Beat Küng <beat-kueng@gmx.net>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

package com.sapos_aplastados.game.clash_of_balls.game;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.FixtureDef;

import android.opengl.GLES20;

import com.sapos_aplastados.game.clash_of_balls.Texture;
import com.sapos_aplastados.game.clash_of_balls.VertexBufferFloat;

/**
 * game object that does not move, but can collide with moving objects
 * 
 * texture is optional
 *
 */
public class StaticGameObject {
	
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
	
	protected Texture m_texture;
	
	protected Body m_body=null;
	
	public Vec2 pos() { return m_body.getPosition(); }
	public Vec2 speed() { return m_body.getLinearVelocity(); }
	
	
	public boolean hasMoved() {
		return m_body.isAwake() && type==Type.Player; //currently only players can move
	}
	
	
	//collision categories for box2d
	public static final int COLLISION_GROUP_NORMAL = 1<<0;
	public static final int COLLISION_GROUP_HOLE = 1<<1;
	
	
	StaticGameObject(final short id, Type type, Texture texture) {
		this.type = type;
		m_id = id;
		m_texture = texture;
		
	}
	
	//normal points from this to other
	public void handleImpact(StaticGameObject other, Vector normal) {
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
		
		if(m_body.getAngle() == 0.f) {
			renderer.modelMatTranslate(m_body.getPosition().x-0.5f
					, m_body.getPosition().y-0.5f, 0.f);
		} else {
			renderer.modelMatTranslate(m_body.getPosition().x, m_body.getPosition().y, 0.f);
			renderer.modelMatRotate(m_body.getAngle()*180.f/(float)Math.PI
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
	
	
	protected static FixtureDef createFixtureDef(float density, float friction
			, float restitution) {
		FixtureDef def = new FixtureDef();
		def.density = density;
		def.friction = friction;
		def.restitution = restitution;
		return def;
	}
	
	protected static FixtureDef createCircleFixtureDef(float density, float friction
			, float restitution, float x, float y, float radius) {
		FixtureDef def = createFixtureDef(density, friction, restitution);
		CircleShape shape = new CircleShape();
		shape.m_radius = radius;
		shape.m_p.set(x, y);
		def.shape = shape;
		return def;
	}
	protected static FixtureDef createRectFixtureDef(
			float density, float friction, float restitution, float x, float y
			, float w, float h, float angle) {
		FixtureDef def = createFixtureDef(density, friction, restitution);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(w / 2.0f, h / 2.0f, new Vec2(x+w/2.f, y+h/2.f), angle);
		def.shape = shape;
		return def;
	}
}
