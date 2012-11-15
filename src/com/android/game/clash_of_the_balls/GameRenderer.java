package com.android.game.clash_of_the_balls;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.android.game.clash_of_the_balls.game.MatrixStack;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;


public class GameRenderer implements GLSurfaceView.Renderer {

    private static final String LOG_TAG = "GameRenderer";
    
    private int m_width;
    private int m_height;
    
    private UIHandler m_ui_handler;
    private MatrixStack m_matrix_stack;
    
    private long m_last_time=0;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        //init GL
        
        //GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        
    }
    
    private void init() throws Exception {
    	if(m_width == 0 || m_height == 0) 
    		throw new Exception("width or height is 0");
    	
    	m_last_time = SystemClock.elapsedRealtime(); //or: nanoTime()
    	
    	m_ui_handler = new UIHandler(m_width, m_height);
    	m_matrix_stack = new MatrixStack();
    	//TODO: set projection matrix
    	
    }
    

    @Override
    public void onDrawFrame(GL10 unused) {

    	try {
    		if(m_ui_handler==null) init();

    		/* Move the game or menu */
    		long time = SystemClock.elapsedRealtime(); //or: nanoTime()
    		float elapsed_time = (float)(time - m_last_time) / 1000.f; 
    		m_ui_handler.move(elapsed_time);
    		m_last_time = time;


    		/* Render the scene */
    		Log.v(LOG_TAG, "rendering frame");

    		// Draw background color
    		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

    		m_ui_handler.draw(m_matrix_stack);

    	} catch(Exception e) {
    		Log.e(LOG_TAG, "Exception in renderer");
    		e.printStackTrace();
    		System.exit(-1);
    	}
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
    	// but we disable screen rotation thus it happens only on startup
        GLES20.glViewport(0, 0, width, height);
        m_width=width;
        m_height=height;

        Log.d(LOG_TAG, "onSurfaceChanged: w="+width+", h="+height);
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(LOG_TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}

