package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.game.IDrawable;
import com.android.game.clash_of_the_balls.game.RenderHelper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Font2D
 * 2D OpenGL font
 *
 */
public class Font2D implements IDrawable {

	private static final String LOG_TAG = "Font2D";
	
	private Texture m_texture;
	private Typeface m_typeface;
	private String m_string;
	private int m_font_size;
	private int m_alpha;
	private int m_red;
	private int m_green;
	private int m_blue;

	private TextureManager m_texture_manager;
	private VertexBufferFloat m_position_data;
	private VertexBufferFloat m_color_data;
	
	/**
	 * Creates 2D font
	 * 
	 * font_size:	Size of the font
	 * typeface: 	Typeface
	 * 
	 */
	
	public Font2D(TextureManager texture_manager, Typeface typeface, String string, int font_size, int red, int green, int blue, int alpha) {
		
		m_texture_manager = texture_manager;
		m_position_data = new VertexBufferFloat(VertexBufferFloat.sprite_position_data, 3);
		m_color_data = new VertexBufferFloat(VertexBufferFloat.sprite_color_data_white, 4);
		
		doInit(typeface, string, font_size, red, green, blue, alpha);

		m_texture = m_texture_manager.get(createFontBitmap(), VertexBufferFloat.sprite_tex_coords);
		
		Log.d(LOG_TAG, "Font succesfully created");
	}

	private void doInit(Typeface typeface, String string, int font_size, int red, int green, int blue, int alpha) {
		
		m_typeface = typeface;
		m_string = string;
		m_font_size = font_size;
		m_alpha = alpha;
		m_red = red;
		m_green = green;
		m_blue = blue;
		m_texture = null;
	}

	public Bitmap createFontBitmap() {
		
		// Create an empty, mutable bitmap
		Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
		// get a canvas to paint over the bitmap
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0);
		
		// Draw the text
		Paint textPaint = new Paint();
		textPaint.setTypeface(m_typeface);
		textPaint.setTextSize(m_font_size);
		textPaint.setAntiAlias(true);
		textPaint.setARGB(m_alpha, m_red, m_green, m_blue);
		
		// draw the text centered
		canvas.drawText(m_string, 0, 0, textPaint);
		
		Log.d(LOG_TAG, "fontBitmap created...");
		
		return bitmap;
	}

	@Override
	public void draw(RenderHelper renderer) {
		
		renderer.shaderManager().activateTexture(0);
		m_texture.useTexture(renderer);
		
		// position
		int position_handle = renderer.shaderManager().a_Position_handle;
		if(position_handle != -1)
			m_position_data.apply(position_handle);
		
		// color
		int color_handle = renderer.shaderManager().a_Color_handle;
		if(color_handle != -1)
			m_color_data.apply(color_handle);
		
		renderer.apply();
		
        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

		Log.d(LOG_TAG, "draw font...");
	}
}