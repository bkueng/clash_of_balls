package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.game.IDrawable;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
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
	private Vector m_position;
	private Vector m_text_field_size;
	
	private Typeface m_typeface;
	private String m_string;
	private int m_font_size;
	private TextAlign m_align;
	private float m_font_width;
	private float m_font_height;
	private float m_x_offset;
	private float m_y_offset;
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
	public enum TextAlign {
		LEFT, RIGHT, CENTER
	}
	
	public Font2D(TextureManager texture_manager, Typeface typeface, String string,
			int font_size, TextAlign align, Vector position, Vector text_field_size, int red, int green, int blue, int alpha) {
		
		m_texture_manager = texture_manager;
		m_position_data = new VertexBufferFloat(VertexBufferFloat.sprite_position_data, 3);
		m_color_data = new VertexBufferFloat(VertexBufferFloat.sprite_color_data_white, 4);
		
		doInit(typeface, string, font_size, align, position, text_field_size, red, green, blue, alpha);

		m_texture = m_texture_manager.get(createFontBitmap(), VertexBufferFloat.sprite_tex_coords);
		
		Log.d(LOG_TAG, "Font succesfully created");
	}

	private void doInit(Typeface typeface, String string, int font_size,
			TextAlign align, Vector position, Vector text_field_size, int red, int green, int blue, int alpha) {
		
		// Set members
		m_typeface = typeface;
		m_string = string;
		m_font_size = font_size;
		m_position = position;
		m_text_field_size = text_field_size;
		m_font_width = 0;
		m_font_height = 0;
		m_x_offset = 0;
		m_y_offset = 0;
		m_alpha = alpha;
		m_red = red;
		m_green = green;
		m_blue = blue;
		m_texture = null;
		
		/*
		 * LEFT, CENTER, RIGHT
		 */
		m_align = align;
	}
	
	private Paint.Align getAlignment(TextAlign align) {
		switch(align) {
			case LEFT: 
				return Paint.Align.LEFT;
			
			case CENTER: 
				m_x_offset = m_text_field_size.x / 2;
				return Paint.Align.CENTER;
			
			case RIGHT:
				m_x_offset = m_text_field_size.x;
				return Paint.Align.RIGHT;
				
			default:
				return Paint.Align.LEFT;
		}
	}

	public void setString(String string) {
		doInit(m_typeface, string, m_font_size, TextAlign.LEFT, m_position, m_text_field_size, m_red, m_green, m_blue, m_alpha);
		
		m_texture = m_texture_manager.get(createFontBitmap(), VertexBufferFloat.sprite_tex_coords);
	}
	
	public void reloadTexture() {
		m_texture = m_texture_manager.get(createFontBitmap(), VertexBufferFloat.sprite_tex_coords);
	}
	
	private Bitmap createFontBitmap() {

		// Create text paint to customize font
		Paint textPaint = new Paint();

		textPaint.setTypeface(m_typeface);
		textPaint.setTextSize(m_font_size);
		textPaint.setAntiAlias(true);
		textPaint.setARGB(m_alpha, m_red, m_green, m_blue);
		
	    // Get font width
	    m_font_width = textPaint.measureText(m_string);
		// Alignment
		textPaint.setTextAlign(getAlignment(m_align));
	    
		// Get font metrics
	    Paint.FontMetrics fm = textPaint.getFontMetrics();
	    m_font_height = Math.round( Math.abs(fm.bottom) + Math.abs(fm.top));
		m_y_offset = (m_text_field_size.y - m_font_height) / 2 + Math.abs(fm.top);

		
		// Create an empty, mutable bitmap
		Bitmap bitmap = Bitmap.createBitmap((int)Math.round(m_text_field_size.x), (int)Math.abs(m_text_field_size.y), Bitmap.Config.ARGB_8888);
		// Get a canvas to paint over the bitmap
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0);
		
		// Draw the text centered
		canvas.drawRGB(255, 0, 127); // TODO: Delete when finished
		canvas.drawText(m_string, m_x_offset, m_y_offset, textPaint);
				
		return bitmap;
	}

	@Override
	public void draw(RenderHelper renderer) {
		
		renderer.shaderManager().activateTexture(0);
		m_texture.useTexture(renderer);
		
		int model_mat_pos = renderer.pushModelMat();
		float model_mat[] = renderer.modelMat();
		Matrix.setIdentityM(model_mat, model_mat_pos);
		Matrix.translateM(model_mat, model_mat_pos, m_position.x, m_position.y, 0.f);
		Matrix.scaleM(model_mat, model_mat_pos, m_text_field_size.x, m_text_field_size.y, 0.f);
		
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

		renderer.popModelMat();
	}
}