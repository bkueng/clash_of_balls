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

import com.sapos_aplastados.game.clash_of_balls.Texture;

/**
 * this class is used for objects that do not interact with the world (box2d)
 * eg. background textures
 *
 */

public class StaticGameObjectNoInteraction extends StaticGameObject {
	
	private Vector m_position;
	protected float m_rotation_angle=0.f; //in rad, texture rotation

	public void setRotation(float angle_rad) {//CCW
		m_rotation_angle = angle_rad;
	}
	
	StaticGameObjectNoInteraction(final short id, Vector position, Type type
			, Texture texture) {
		super(id, type, texture);
		m_position = new Vector(position);
		//m_shape is not used here
	}
	
	
	protected void doModelTransformation(RenderHelper renderer) {
		//translate
		renderer.pushModelMat();
		
		if(m_rotation_angle == 0.f) {
			renderer.modelMatTranslate(m_position.x-0.5f
					, m_position.y-0.5f, 0.f);
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
}
