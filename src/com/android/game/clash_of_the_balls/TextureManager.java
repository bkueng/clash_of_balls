package com.android.game.clash_of_the_balls;

import java.util.Map;
import java.util.TreeMap;

import android.content.Context;

/**
 * manages all textures: new textures are all generated here
 * same textures are only loaded once
 *
 */
public class TextureManager {
	
	private Context m_activity_context;
	private Map<Integer, TextureBase> m_textures; //key is raw_res_id
	
	public TextureManager(Context activity_context) {
		m_activity_context = activity_context;
		m_textures = new TreeMap<Integer, TextureBase>();
	}
	
	// this will return a texture with default tex coords (for a sprite)
	public Texture get(int raw_res_id) {
		return get(raw_res_id, null);
	}
	
	public Texture get(int raw_res_id, float[] tex_coords) {
		TextureBase texture=m_textures.get(raw_res_id);
		if(texture == null) {
			Texture ret = new Texture(m_activity_context, raw_res_id, tex_coords);
			m_textures.put(raw_res_id, ret);
			return ret;
		}
		return new Texture(texture, tex_coords);
	}
	
}
