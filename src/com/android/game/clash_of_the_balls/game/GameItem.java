package com.android.game.clash_of_the_balls.game;

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
	
	public final Rectangle border; //for object intersection
	
    public static ItemType getRandomType() {
        return ItemType.values()[(int) (Math.random() * (ItemType.values().length-1))];
    }
	
	private final ItemType m_item_type;
	public ItemType itemType() { return m_item_type; }
	

	public GameItem(GameBase owner, short id, Vector position
			, Texture texture, ItemType item_type) {
		super(owner, id, position, Type.Item, texture);
		m_item_type = item_type;
		
		border = new Rectangle(-0.45f, -0.45f, 1.f-0.05f/2.f, 1.f-0.05f/2.f);
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
	
	public void handleImpact(StaticGameObject other) {
		super.handleImpact(other);
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
		renderer.modelMatTranslate(m_position.x, m_position.y, 0.f);
		renderer.modelMatScale(m_scaling, m_scaling, 0.f);
		renderer.modelMatTranslate(-0.5f, -0.5f, 0.f);
	}
	protected void undoModelTransformation(RenderHelper renderer) {
		renderer.popModelMat();
	}

}
