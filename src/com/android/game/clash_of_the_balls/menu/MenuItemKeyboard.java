package com.android.game.clash_of_the_balls.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.v4.content.Loader.ForceLoadContentObserver;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.R;
import com.android.game.clash_of_the_balls.Texture;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.VertexBufferFloat;
import com.android.game.clash_of_the_balls.game.RenderHelper;
import com.android.game.clash_of_the_balls.game.Vector;

/**
 * Menu item with keyboard input
 *
 */
public class MenuItemKeyboard extends MenuItem {

	
	private String LOG_TAG = "debug";
	
	private Context m_activity_context;
	private Texture m_texture;
	private TextureManager m_tex_manager;
	private String m_dialog_text;
	private String m_text_input;

	
	public MenuItemKeyboard(Vector position, Vector size, Font2D font, 
			TextureManager tex_manager
			, Context activity_context,String text) {
		super(position, size, font);
		m_dialog_text = text;
		m_tex_manager=tex_manager;
		m_activity_context = activity_context;
		m_texture=m_tex_manager
				.get(R.raw.texture_grey_unpressed_button);
		m_position_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_position_data, 3);
		m_color_data = new VertexBufferFloat
				(VertexBufferFloat.sprite_color_data_white, 4);
	}
	
	public String getString(){
		return m_text_input;
	}
	
	public void onTouchDown(float x, float y) {
		// do nothing
	}
	public void onTouchUp(float x, float y) {
		
		
		((Activity) m_activity_context).runOnUiThread(new Runnable()
        {
            public void run()
            { 
            	AlertDialog.Builder alert = new AlertDialog.Builder(m_activity_context);

            	alert.setTitle("Clash of Balls");
            	alert.setMessage(m_dialog_text);

            	// Set an EditText view to get user input 
            	final EditText input = new EditText(m_activity_context);
            	alert.setView(input);

            	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {
            	  m_text_input = input.getText().toString();
            	  Log.d(LOG_TAG,"Text input: "+m_text_input);
            	  }
            	});

            	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            	  public void onClick(DialogInterface dialog, int whichButton) {
            	    // Canceled.
            	  }
            	});

            	alert.show();
            }
         }
  );

	}

	public void draw(RenderHelper renderer) {			
		renderer.shaderManager().activateTexture(0);
		m_texture.useTexture(renderer);
		int model_mat_pos = renderer.pushModelMat();
		float model_mat[] = renderer.modelMat();
		Matrix.setIdentityM(model_mat, model_mat_pos);
		Matrix.translateM(model_mat, model_mat_pos, m_position.x, m_position.y, 0.f);
		Matrix.scaleM(model_mat, model_mat_pos, this.size().x, this.size().y, 0.f);
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
