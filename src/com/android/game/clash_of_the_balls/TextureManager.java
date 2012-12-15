package com.android.game.clash_of_the_balls;

import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * manages all textures: new textures are all generated here
 * same textures are only loaded once
 *
 * if a texture is loaded using mipmapping, the following loadings of the same
 * textures will always use mipmapping, no matter what the argument is
 * the same holds for the other case
 */
public class TextureManager {
	
	public Context m_activity_context;
	
	private Map<Integer, TextureBase> m_textures; //key is raw_res_id
	
	public TextureManager(Context activity_context) {
		m_activity_context = activity_context;
		m_textures = new TreeMap<Integer, TextureBase>();		
	}
	
	//reload all textures into memory: do this when context is lost
	public void reloadAllTextures() {
		for (Map.Entry<Integer, TextureBase> entry : m_textures.entrySet()) {
			entry.getValue().reloadTexture();
		}
		// reload all fonts as well
		Font2D.reloadFonts();
	}
	
	// this will return a texture with default tex coords (for a sprite)
	public Texture get(int raw_res_id) {
		return get(raw_res_id, null, true);
	}
	public Texture get(int raw_res_id, boolean use_mipmapping) {
		return get(raw_res_id, null, use_mipmapping);
	}
	
	public Texture get(int raw_res_id, float[] tex_coords, boolean use_mipmapping) {
		TextureBase texture=m_textures.get(raw_res_id);
		if(texture == null) {
			TextureBase tex = new TextureBase(m_activity_context, raw_res_id
					, use_mipmapping);
			Texture ret = new Texture(tex, tex_coords);
			m_textures.put(raw_res_id, tex);
			return ret;
		}
		return new Texture(texture, tex_coords);
	}
	
	public Texture get(Bitmap bitmap, float[] tex_coords, boolean use_mipmapping) {
		TextureBase tex = new TextureBase(bitmap, use_mipmapping);
		Texture ret = new Texture(tex, tex_coords);
		return ret;
	}
	
}
