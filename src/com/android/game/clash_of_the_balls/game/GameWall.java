package com.android.game.clash_of_the_balls.game;

import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import com.android.game.clash_of_the_balls.Texture;

public class GameWall extends StaticGameObject {
	
	public enum WallType {
		Wall_horizontal,
		Wall_vertical,
		Wall_cross,
		Wall_Corner_up_right,
		Wall_Corner_up_left,
		Wall_Corner_down_right,
		Wall_Corner_down_left
	}
	private final WallType m_wall_type;

	public GameWall(GameBase owner, short id, Vector pos, WallType wall_type
			, Texture texture, World world, BodyDef body_def) {
		
		super(id, Type.Wall, texture);		
		m_wall_type = wall_type;
		
		final float restitution = 1.0f;
		float angle = 0.f;
		
		switch(m_wall_type) {
		case Wall_Corner_down_left: 
			angle = 2.f*(float) Math.PI / 2;
			break;
		case Wall_Corner_down_right: 
			angle = 3.f*(float) Math.PI / 2;
			break;
		case Wall_Corner_up_left:
		case Wall_horizontal: 
			angle = (float) Math.PI / 2;
			break;
		case Wall_Corner_up_right:
		case Wall_vertical:
		case Wall_cross:
			break;
		}
	
		body_def.type = BodyType.STATIC;
		body_def.position.set(pos.x, pos.y);
		body_def.angle = angle;
		body_def.userData = this;
		m_body = world.createBody(body_def);
		
		switch(m_wall_type) {
		case Wall_Corner_down_left:
		case Wall_Corner_down_right:
		case Wall_Corner_up_left:
		case Wall_Corner_up_right:
			addWallRectFixture(-0.11f,0.23f,0.22f,0.22f, restitution);
			addWallRectFixture(-0.11f,-0.11f,0.22f,0.22f, restitution);
			addWallRectFixture(0.22f,-0.11f,0.22f,0.22f, restitution);
			break;
		case Wall_horizontal:
		case Wall_vertical:
			addWallRectFixture(-0.11f, -0.5f, 0.22f, 1.0f, restitution);
			break;
		case Wall_cross:
			addWallRectFixture(-0.11f, -0.5f, 0.22f, 1.0f, restitution);
			addWallRectFixture(-0.5f, -0.11f, 1.0f, 0.22f, restitution);
			break;
		default:
			break;
		
		}
		
	}
	
	private void addWallRectFixture(float x, float y, float w, float h, float restitution) {
		FixtureDef fixture_def = createRectFixtureDef(0.0f, 0.0f, restitution, 
				x, y, w, h, 0.0f);
		fixture_def.filter.categoryBits = COLLISION_GROUP_NORMAL;
		fixture_def.filter.maskBits = COLLISION_GROUP_NORMAL;
		m_body.createFixture(fixture_def);
	}
}
