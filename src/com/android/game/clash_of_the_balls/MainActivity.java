package com.android.game.clash_of_the_balls;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.app.Activity;
import android.content.Context;

public class MainActivity extends Activity {

    private GLSurfaceView m_gl_view;
    
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

}




class MyGLSurfaceView extends GLSurfaceView {

    private final GameRenderer m_renderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        m_renderer = new GameRenderer();
        setRenderer(m_renderer);

        
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        
        final MotionEvent event = e;

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            	//send event to the renderer
            	queueEvent(new Runnable() {
					@Override
					public void run() {
						m_renderer.handleTouchInput(event);
					}
            	});
        }
        return true;
    }
}
