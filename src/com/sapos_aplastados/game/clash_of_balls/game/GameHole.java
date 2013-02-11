package com.sapos_aplastados.game.clash_of_balls.game;

import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import com.sapos_aplastados.game.clash_of_balls.Texture;

public class GameHole extends StaticGameObject {

	public enum HoleType {
		Boarder_left,
		Boarder_up,
		Boarder_right,
		Boarder_down,
		
		Corner_up_right,
		Corner_up_left,
		Corner_down_right,
		Corner_down_left,
		
		Hole_single,
		Hole_Full, //Completely empty
		Hole_Corner_up_right,
		Hole_Corner_up_left,
		Hole_Corner_down_right,
		Hole_Corner_down_left,
		Hole_Head_Down,
		Hole_Head_Up,
		Hole_Head_Left,
		Hole_Head_Right,
	}
	private final HoleType m_hole_type;
	
	public HoleType holeType() { return m_hole_type; }
	
	
	GameHole(GameBase owner, short id, Vector position, HoleType hole_type
			, Texture texture, World world, BodyDef body_def) {
		
		super(id, Type.Hole, texture);
		float angle = 0.f;
		
		m_hole_type = hole_type;

		switch(hole_type) {
		case Hole_Corner_up_right:
		case Hole_Head_Up:
		case Hole_single:
		case Hole_Full:
		case Boarder_left:
		case Corner_up_right:
			break;
		case Hole_Corner_down_right:
		case Hole_Head_Right:
		case Boarder_up:
		case Corner_up_left:
			angle = 3.f * (float) Math.PI / 2;
			break;
		case Hole_Corner_down_left:
		case Hole_Head_Down:
		case Boarder_right:
		case Corner_down_left:
			angle = 2.f * (float) Math.PI / 2;
			break;
		case Hole_Corner_up_left:
		case Hole_Head_Left:
		case Boarder_down:
		case Corner_down_right:
			angle = (float) Math.PI / 2;
			break;
		}
		

		body_def.type = BodyType.STATIC;
		body_def.position.set(position.x, position.y);
		body_def.angle = angle;
		body_def.userData = this;
		m_body = world.createBody(body_def);

		switch(hole_type) {
		case Boarder_down:
		case Boarder_left:
		case Boarder_right:
		case Boarder_up:
			addHoleRectFixture(-0.5f, -0.5f, 0.21f, 1.0f);
			break;
		case Corner_down_left:
		case Corner_down_right:
		case Corner_up_left:
		case Corner_up_right:
			addHoleRectFixture(-0.5f, -0.5f, 0.21f, 1.0f);
			addHoleRectFixture(-0.5f, -0.5f, 1.0f, 0.21f);
			break;
		case Hole_Corner_down_left:
		case Hole_Corner_down_right:
		case Hole_Corner_up_left:
		case Hole_Corner_up_right:
			addHoleCircleFixture(0.f, 0.f, 0.5f);
			addHoleRectFixture(-0.5f, -0.5f, 0.5f, 1.0f);
			addHoleRectFixture(-0.5f, -0.5f, 1.0f, 0.5f);
			break;
		case Hole_Full:
			addHoleRectFixture(-0.5f, -0.5f, 1.0f, 1.0f);
			break;
		case Hole_Head_Down:
		case Hole_Head_Left:
		case Hole_Head_Right:
		case Hole_Head_Up:
			addHoleCircleFixture(0.f, 0.f, 0.5f);
			addHoleRectFixture(-0.5f, -0.5f, 1.0f, 0.5f);
			break;
		case Hole_single:
			addHoleCircleFixture(0.f, 0.f, 0.5f);
			break;
		default:
			break;

		}
	}
	
	private void addHoleRectFixture(float x, float y, float w, float h) {
		FixtureDef fixture_def = createRectFixtureDef(0.0f, 0.0f, 0.0f, 
				x, y, w, h, 0.0f);
		fixture_def.filter.categoryBits = COLLISION_GROUP_HOLE;
		fixture_def.filter.maskBits = COLLISION_GROUP_NORMAL;
		m_body.createFixture(fixture_def);
	}
	private void addHoleCircleFixture(float x, float y, float radius) {
		FixtureDef fixture_def = createCircleFixtureDef(0.0f, 0.0f, 0.0f, 
				x, y, radius);
		fixture_def.filter.categoryBits = COLLISION_GROUP_HOLE;
		fixture_def.filter.maskBits = COLLISION_GROUP_NORMAL;
		m_body.createFixture(fixture_def);
	}
}
