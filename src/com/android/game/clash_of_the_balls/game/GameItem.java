package com.android.game.clash_of_the_balls.game;

import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import com.android.game.clash_of_the_balls.Texture;

/**
 * this represents a game item: when players collide with it
 * it will change a certain property of the player
 *
 */

public class GameItem extends DynamicGameObject {

	public static final float item_effect_duration = 20.f; //[sec]

	private float m_scaling=1.f; //for drawing, used for dying effect
	
	public enum ItemType {
		//positive items
		InvisibleToOthers,
		IncreaseMaxSpeed,
		
		//negative items
		MassAndSize, //decrease player mass & size
		InvertControls, //invert input controls
		
		//the logic of applying an item is in GamePlayer.applyItem
		
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
	}

	public void move(float dsec) {
		super.move(dsec);

		if(m_bIs_dying) {
			m_scaling -= 3.f * dsec;
			if(m_scaling < 0.01f) {
				m_bIs_dying = false;
				m_scaling = 0.01f;
			}
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
			m_scaling = 1.f;
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
