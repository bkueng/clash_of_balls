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
	private int m_alpha;
	private int m_red;
	private int m_green;
	private int m_blue;

	private String m_string;
	private int m_font_size;
	private TextureManager m_texture_manager;
	
	/**
	 * Creates 2D font
	 * 
	 * font_size:	Size of the font
	 * typeface: 	Typeface
	 * 
	 */
	
	public Font2D(TextureManager texture_manager, Typeface typeface, String string, int font_size, int red, int green, int blue, int alpha) {
		
		m_texture_manager = texture_manager;
		
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

		Log.d(LOG_TAG, "draw font...");
	}
}