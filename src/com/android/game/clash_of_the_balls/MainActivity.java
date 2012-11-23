package com.android.game.clash_of_the_balls;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class MainActivity extends Activity {
	
	private static final String LOG_TAG="MainActivity";

    private MyGLSurfaceView m_gl_view;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        m_gl_view = new MyGLSurfaceView(this);
        setContentView(m_gl_view);
        
        //start the network service
        Intent intent = new Intent(this, NetworkService.class);
        if(startService(intent)==null)
        	Log.e(LOG_TAG, "Failed to start NetworkService");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        m_gl_view.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        m_gl_view.onResume();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	stopService(new Intent(this, NetworkService.class));
    	m_gl_view.onDestroy();
    }

}




class MyGLSurfaceView extends GLSurfaceView {

    private final GameRenderer m_renderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        m_renderer = new GameRenderer(this.getContext());
        setRenderer(m_renderer);

        
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
    
    public void onDestroy() {
    	queueEvent(new Runnable() {
			public void run() {
				m_renderer.onDestroy();
			}
    	});
    }

    @Override
    public boolean onTouchEvent(final MotionEvent e) {
        
    	final float x = e.getX(), y=e.getY();
    	final int event = e.getAction();
    	
        switch (event) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            	//send event to the renderer
            	queueEvent(new Runnable() {
					public void run() {
						m_renderer.handleTouchInput(x, y, event);
					}
            	});
        }
        return true;
    }
}
