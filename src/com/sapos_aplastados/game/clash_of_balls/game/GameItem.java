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

import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import android.util.FloatMath;

import com.sapos_aplastados.game.clash_of_balls.Texture;

/**
 * this represents a game item: when players collide with it
 * it will change a certain property of the player
 *
 */

public class GameItem extends DynamicGameObject {

	public static final float item_effect_duration = 15.f; //[sec]
	
	private static final float item_timeout = 15.f; //item disapears after this timeout

	private float m_scaling; //for drawing, used for dying effect
	private boolean m_is_appearing;
	private float m_time_accumulator = 0.f;
	private boolean m_is_static = false;
	
	public enum ItemType {
		//positive items
		InvisibleToOthers,
		IncreaseMaxSpeed,
		DontFall, //do not fall into holes
		IncreaseMassAndSize,
		IncreaseRestitution, //increase bouncing off walls & between players
		
		//negative items
		DecreaseMassAndSize, //decrease player mass & size
		InvertControls, //invert input controls
		
		//the logic of applying an item is in GamePlayer.applyItem
		//texture is in GameBase.createItem
		
		None
	}
	
    public static ItemType getRandomType() {
        return ItemType.values()[(int) (Math.random() * (ItemType.values().length-1))];
    }
	
	private final ItemType m_item_type;
	public ItemType itemType() { return m_item_type; }
	

	public GameItem(GameBase owner, short id, Vector position
			, Texture texture, ItemType item_type, World world, BodyDef body_def) {
		super(owner, id, Type.Item, texture);
		m_item_type = item_type;
		
		body_def.type = BodyType.STATIC;
		body_def.position.set(position.x, position.y);
		body_def.userData = this;
		m_body = world.createBody(body_def);
		FixtureDef fixture_def = createRectFixtureDef(1.0f, 0.0f, 0.0f, 
				-0.45f, -0.45f, 1.f-0.05f/2.f, 1.f-0.05f/2.f, 0.0f);
		fixture_def.filter.categoryBits = COLLISION_GROUP_NORMAL;
		fixture_def.filter.maskBits = COLLISION_GROUP_NORMAL;
		m_body.createFixture(fixture_def);
		
		m_scaling = 0.01f;
		m_is_appearing = true;
	}
	
	public void setIsStatic(boolean is_static) {
		m_is_static = is_static;
		if(is_static) {
			m_scaling = 1.f;
			m_is_appearing = false;
		}
	}

	public void moveClient(float dsec) {
		super.moveClient(dsec);
		
		if(m_bIs_dying) {
			m_scaling -= 3.f * dsec;
			if(m_scaling < 0.01f) {
				m_bIs_dying = false;
				m_scaling = 0.01f;
			}
		} else if(m_is_appearing) {
			m_scaling += 3.f * dsec;
			if(m_scaling > 1.f) {
				m_is_appearing = false;
				m_scaling = 1.f;
			}
		} else if(!m_is_static) {
			m_time_accumulator += dsec;
			float d = 1.f-FloatMath.sin(FloatMath.cos(m_time_accumulator*4.f)+1.f);
			m_scaling = 1.f + d*d*0.15f;
			if(m_time_accumulator > item_timeout) die();
		}

	}
	
	public void handleImpact(StaticGameObject other, Vector normal) {
		super.handleImpact(other, normal);
		switch(other.type) {
		case Player: die(); //player will apply the item
			break;
		default:
		}
	}

	public void die() {
		if(!m_bIs_dead) {
			m_is_appearing = false;
			m_bIs_dead = true;
			m_bIs_dying = true;
			m_owner.handleObjectDied(this);
		}
	}
	
	protected void doModelTransformation(RenderHelper renderer) {
		//scale & translate
		renderer.pushModelMat();
		renderer.modelMatTranslate(m_body.getPosition().x, m_body.getPosition().y, 0.f);
		renderer.modelMatScale(m_scaling, m_scaling, 0.f);
		renderer.modelMatTranslate(-0.5f, -0.5f, 0.f);
	}
	protected void undoModelTransformation(RenderHelper renderer) {
		renderer.popModelMat();
	}

}
