package com.android.game.clash_of_the_balls.game;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.FontNumbers;
import com.android.game.clash_of_the_balls.ShaderManager.ShaderType;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.GameItem.ItemType;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo.PlayerInfo;

/**
 * this represents a player of the game. ie a ball 
 *
 */
public class GamePlayer extends DynamicGameObject {
	
	private float m_max_speed = 5.f;

	private float m_color[]=new float[4]; //RGBA
	
	private float m_scaling=1.f; //for drawing, used for dying effect
	private float m_scaling_speed;
	
	
	public float m_radius;
	public float m_radius_dest; //m_radius should change to this value
	private static final int SMALL_RADIUS = 0;
	private static final int NORMAL_RADIUS = 1;
	private static final int LARGE_RADIUS = 2;
	private static final float m_normal_radius = 0.5f;
	private static final float m_radiuses[] = new float[] {
		m_normal_radius / 2.f, m_normal_radius, m_normal_radius*1.3f 
		};
	private FixtureDef m_radius_fixtures[]=new FixtureDef[m_radiuses.length];
	private Fixture m_cur_fixture;
	
	private static final float normal_restitution = 1.0f;
	private static final float high_restitution = 2.0f;
	
	//item
	private float m_item_timeout = 0.f;
	public ItemType m_item_type = ItemType.None;
	private GameItem m_overlay_item = null;
	public static final float overlay_item_height = 0.09f;
	private FontNumbers m_overlay_font_numbers;
	
	public float[] color() { return m_color; }
	
	private Vector m_acceleration = new Vector();
	private float m_sensor_scaling = 5.f; //influences the acceleration
	public Vector acceleration() { return m_acceleration; }

	public void applySensorVector(Vector v) {
		if(!m_bIs_dead) {
			m_acceleration.set(v);
			if(m_item_type == ItemType.InvertControls)
				m_acceleration.mul(-1.f);
			m_acceleration.mul(m_sensor_scaling);
		}
	}
	
	protected Texture m_overlay_texture;
	protected Texture m_glow_texture;
	private static final float min_glow_scaling = 0.8f;
	private static final float max_glow_scaling = 1.5f;
	private float m_glow_scaling=min_glow_scaling;
	private float m_texture_scaling = 1.f;

	public GamePlayer(GameBase owner, short id, Vector position
			, int color, Texture texture, Texture texture_overlay
			, Texture texture_glow
			, FontNumbers overlay_font_numbers, World world, BodyDef body_def) {
		super(owner, id, Type.Player, texture);
		m_overlay_texture = texture_overlay;
		m_glow_texture = texture_glow;
		RenderHelper.initColorArray(color, m_color);
		m_overlay_font_numbers = overlay_font_numbers;
		initPlayerBody(world, position, body_def);
	}
	
	public GamePlayer(PlayerInfo info, GameBase owner, Texture texture_base
			, Texture texture_overlay, Texture texture_glow
			, FontNumbers overlay_font_numbers, World world
			, BodyDef body_def) {
		super(owner, info.id, Type.Player, texture_base);
		m_overlay_texture = texture_overlay;
		m_glow_texture = texture_glow;
		RenderHelper.initColorArray(info.color, m_color);
		m_overlay_font_numbers = overlay_font_numbers;
		initPlayerBody(world, new Vector(info.pos_x, info.pos_y), body_def);
	}
	
	private void initPlayerBody(World world, Vector position, BodyDef body_def) {
		body_def.type = BodyType.DYNAMIC;
		body_def.position.set(position.x, position.y);
		body_def.fixedRotation = true;
		body_def.linearDamping = 2.0f; //speed friction
		body_def.angle = 0.f;
		body_def.userData = this;
		m_body = world.createBody(body_def);
		
		for(int i=0; i<m_radiuses.length; ++i) {
			m_radius_fixtures[i] = createCircleFixtureDef(1.0f, 0.0f, normal_restitution, 
					0.f, 0.f, m_radiuses[i]);
			m_radius_fixtures[i].filter.categoryBits = COLLISION_GROUP_NORMAL;
			m_radius_fixtures[i].filter.maskBits = COLLISION_GROUP_NORMAL;
		}
		
		//apply normal radius
		m_cur_fixture = m_body.createFixture(m_radius_fixtures[NORMAL_RADIUS]);
		m_radius = m_radius_dest = m_radiuses[NORMAL_RADIUS];
		
		//hole collisions: use a point fixture (really small circle)
		FixtureDef fixture_def = createCircleFixtureDef(1.0f, 0.0f, 0.0f, 
				0.f, 0.f, 0.01f);
		fixture_def.filter.categoryBits = COLLISION_GROUP_NORMAL;
		fixture_def.filter.maskBits = COLLISION_GROUP_HOLE;
		m_body.createFixture(fixture_def);
	}
	
	private Vector m_tmp_speed = new Vector();
	private Vec2 m_tmp_vec = new Vec2();
	
	public void move(float dsec) {
		super.move(dsec);
		
		
		//update speed
		m_tmp_speed.set(m_body.getLinearVelocity().x, m_body.getLinearVelocity().y);
		
		m_tmp_speed.x += dsec * m_acceleration.x;
		m_tmp_speed.y += dsec * m_acceleration.y;
		
		float speed = m_tmp_speed.length();
		if(speed > m_max_speed) m_tmp_speed.mul(m_max_speed / speed);
		
		m_tmp_vec.set(m_tmp_speed.x, m_tmp_speed.y);
		m_body.setLinearVelocity(m_tmp_vec);
		
		//current item
		if(m_item_type != ItemType.None) {
			if(m_item_timeout - dsec <= 0.f) {
				disableItem();
			} else {
				m_item_timeout-=dsec;
			}
		}
		//move item if needed ...
		if(m_item_type == ItemType.DontFall) {
			if(m_glow_scaling < max_glow_scaling) {
				m_glow_scaling += dsec * 0.5f;
				if(m_glow_scaling > max_glow_scaling)
					m_glow_scaling = max_glow_scaling;
			}
		} else {
			if(m_glow_scaling > min_glow_scaling) {
				m_glow_scaling -= dsec * 0.5f;
				if(m_glow_scaling < min_glow_scaling) 
					m_glow_scaling = min_glow_scaling;
			}
		}
	}
	
	
	public void moveClient(float dsec) {
		super.moveClient(dsec);
		
		if(m_bIs_dying) {
			m_scaling -= (m_scaling_speed + 1.5f*dsec) * dsec;
			m_scaling_speed += dsec * 2.f*1.5f;
			if(m_scaling < 0.01f) {
				m_bIs_dying = false;
				m_scaling = 0.01f;
			}
		} else {
			//is radius changing?
			if(m_radius != m_radius_dest) {
				if(m_radius < m_radius_dest) {
					m_radius += dsec * 0.5f;
					if(m_radius > m_radius_dest) m_radius = m_radius_dest;
				} else {
					m_radius -= dsec * 0.5f;
					if(m_radius < m_radius_dest) m_radius = m_radius_dest;
				}
			}
		}
		
	}
	
	public void handleImpact(StaticGameObject other, Vector normal) {
		super.handleImpact(other, normal);
		switch(other.type) {
		case Hole:
			if(m_owner.is_server && m_item_type != ItemType.DontFall) {
				die();
				m_body.setLinearDamping(0.5f);
				m_tmp_vec.set(normal.x, normal.y);
				m_body.setLinearVelocity(m_tmp_vec);
			}
			break;
		case Item: applyItem((GameItem) other);
			break;
		default:
		}
	}
	
	public void applyItem(GameItem item) {
		float new_duration = GameItem.item_effect_duration;
		//if we already have this item, we accumulate the time
		if(item.itemType() == m_item_type) new_duration += m_item_timeout;
		if(new_duration > GameItem.item_effect_duration*5.f)
			new_duration = GameItem.item_effect_duration*5.f;
		
		//we only allow one item at a time
		disableItem();
		
		m_item_type = item.itemType();
		switch(item.itemType()) {
		case IncreaseMaxSpeed: 
			m_max_speed *= 2.f;
			m_body.setLinearDamping(m_body.getLinearDamping() / 2.f);
			break;
		case InvertControls:
			break;
		case InvisibleToOthers:
			break;
		case IncreaseMassAndSize:
			setRadius(LARGE_RADIUS);
			break;
		case IncreaseRestitution:
			setRestitution(high_restitution);
			break;
		case DecreaseMassAndSize:
			setRadius(SMALL_RADIUS);
			break;
		case DontFall:
			break;
		}
		m_item_timeout = new_duration;
		Vector position=new Vector(0.f, 0.f);
		m_overlay_item = m_owner.createItem((short)-1, item.itemType(), position);
		m_overlay_item.setIsStatic(true);
	}
	
	private void disableItem() {
		if(m_item_type != ItemType.None) {
			switch(m_item_type) {
			case IncreaseMaxSpeed: 
				m_max_speed /= 2.f;
				m_body.setLinearDamping(m_body.getLinearDamping() * 2.f);
				break;
			case InvertControls:
				break;
			case InvisibleToOthers:
				break;
			case IncreaseMassAndSize:
				setRadius(NORMAL_RADIUS);
				break;
			case IncreaseRestitution:
				setRestitution(normal_restitution);
				break;
			case DecreaseMassAndSize:
				setRadius(NORMAL_RADIUS);
				break;
			case DontFall:
				break;
			}
			
			m_item_timeout = 0.f;
			m_item_type = ItemType.None;
			m_overlay_item=null;
		}
		
	}
	
	private void setRestitution(float restitution) {
		m_cur_fixture.setRestitution(restitution);
	}
	
	private void setRadius(int which_radius) {
		m_radius_dest = m_radiuses[which_radius];
		final float restitution = m_cur_fixture.getRestitution();
		m_body.destroyFixture(m_cur_fixture);
		if(!m_bIs_dead) {
			m_cur_fixture = m_body.createFixture(m_radius_fixtures[which_radius]);
			m_cur_fixture.setRestitution(restitution);
		}
	}
	
	
	private boolean isInvisible() {
		return m_item_type == ItemType.InvisibleToOthers
				&& m_owner.ownPlayer()!=this;
	}
	
	
	public void die() {
		if(!m_bIs_dead) {
			disableItem();
			m_acceleration.set(0.f, 0.f);
			m_scaling = 1.f;
			m_scaling_speed = 0.f;
			m_bIs_dead = true;
			m_bIs_dying = true;
			m_owner.handleObjectDied(this);
		}
	}
	
	//draw game overlay information
	//this is called using screen coordinates
	public void drawOverlay(RenderHelper renderer) {
		if(m_overlay_item!=null) {
			final float offset = renderer.screenHeight()*0.02f;
			final float item_size = renderer.screenHeight()*overlay_item_height;
			
			renderer.pushModelMat();
			renderer.modelMatTranslate(offset+item_size*0.5f, offset+item_size*0.5f, 0.f);
			renderer.modelMatScale(item_size, item_size, 0.f);
			m_overlay_item.draw(renderer);
			renderer.popModelMat();
			
			if(m_overlay_font_numbers!=null) {
				m_overlay_font_numbers.draw(renderer, Math.max((int)m_item_timeout + 1, 0)
						, offset*2.f + item_size, offset, item_size);
			}
		}
	}
	
	public void draw(RenderHelper renderer) {
		if(!isReallyDead() && !isInvisible()) {
			
			if(m_glow_texture!=null && (m_item_type == ItemType.DontFall 
					|| m_glow_scaling > min_glow_scaling)) {
				
				m_texture_scaling = m_glow_scaling;
				doModelTransformation(renderer);
				
				renderer.shaderManager().useShader(ShaderType.TypeWarp);
				Game.applyDefaultPosAndColor(renderer);
				
				renderer.shaderManager().activateTexture(0);
				m_glow_texture.useTexture(renderer);
				
				renderer.apply();
				
		        // Draw
		        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
				
				undoModelTransformation(renderer);
				renderer.shaderManager().useShader(ShaderType.TypeDefault);
				Game.applyDefaultPosAndColor(renderer);
				m_texture_scaling = 1.f;
			}
			
			doModelTransformation(renderer);
			
			//colored texture: m_texture
			
			if(m_texture != null) {
				renderer.shaderManager().activateTexture(0);
				m_texture.useTexture(renderer);
				
		        // color
				int color_handle = renderer.shaderManager().u_Color_handle;
				if(color_handle != -1)
					GLES20.glUniform4fv(color_handle, 1, m_color, 0);
				
				renderer.apply();
				
		        // Draw
		        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);     
		        
			}
			
	        
			//overlay texture
			
			if(m_overlay_texture != null) {
				Game.applyDefaultPosAndColor(renderer);
				
				renderer.shaderManager().activateTexture(0);
				m_overlay_texture.useTexture(renderer);
				
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
		renderer.modelMatTranslate(m_body.getPosition().x, m_body.getPosition().y, 0.f);
		renderer.modelMatScale(m_texture_scaling * m_scaling*m_radius*2.f
				, m_texture_scaling * m_scaling*m_radius*2.f, 0.f);
		renderer.modelMatTranslate(-0.5f, -0.5f, 0.f);
	}
	protected void undoModelTransformation(RenderHelper renderer) {
		renderer.popModelMat();
	}

}
