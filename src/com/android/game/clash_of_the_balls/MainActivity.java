package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.MainActivity.LoadViewTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
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

	private ProgressDialog progressDialog;
    private MyGLSurfaceView m_gl_view;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Font2D.resetFonts();
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);

        
        LoadViewTask progress_view = new LoadViewTask();
        progress_view.execute();
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        m_gl_view = new MyGLSurfaceView(this, progress_view);
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
    	dismissDialog();
    }
    
    @Override
    public void onBackPressed() {
    	if(m_gl_view != null && !m_gl_view.onBackPressed())
    		super.onBackPressed();
    }
    
	public void dismissDialog() {
		//close the progress dialog
    	ProgressDialog dialog = progressDialog;
    	if(dialog != null) dialog.dismiss();
    	progressDialog = null;
	}

    
    public class LoadViewTask extends AsyncTask<Void, Integer, Void>
    {
    	
    	private volatile int m_progress = 0; //progress: [0,100]
    	
    	//Before running code in the separate thread
		@Override
		protected void onPreExecute() 
		{
			//Create a new progress dialog
			progressDialog = new ProgressDialog(MainActivity.this);
			//Set the progress dialog to display a horizontal progress bar 
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			//Set the dialog title to 'Loading...'
			progressDialog.setTitle("Loading Game...");
			//Set the dialog message to 'Loading application View, please wait...'
			progressDialog.setMessage("Please wait...");
			//This dialog can't be canceled by pressing the back key
			progressDialog.setCancelable(false);
			//This dialog isn't indeterminate
			progressDialog.setIndeterminate(false);
			//The maximum number of items is 100
			progressDialog.setMax(100);
			//Set the current progress to zero
			progressDialog.setProgress(0);
			//Display the progress dialog
			progressDialog.show();
		}
		
		//if progress is 100, progress bar will be hidden
		public void setProgress(int progress) {
			synchronized (this) {
				m_progress = progress;
				this.notify();
			}
		}
		
		//The code to be executed in a background thread.
		@Override
		protected Void doInBackground(Void... params) 
		{
			try 
			{
				while(m_progress < 100) {
					//Get the current thread's token
					synchronized (this) 
					{
						this.wait();
						//Set the current progress. 
						//This value is going to be passed to the onProgressUpdate() method.
						publishProgress(m_progress);
					}
				}
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			return null;
		}

		//Update the progress
		@Override
		protected void onProgressUpdate(Integer... values) 
		{
			//set the current progress of the progress dialog
			if(progressDialog!=null) progressDialog.setProgress(values[0]);
		}

		//after executing the code in the thread
		@Override
		protected void onPostExecute(Void result) 
		{
			dismissDialog();
		} 	
		
    }

}




class MyGLSurfaceView extends GLSurfaceView {

    private final GameRenderer m_renderer;

    public MyGLSurfaceView(Context context, LoadViewTask progress_view) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        m_renderer = new GameRenderer(this.getContext(), progress_view);
        setRenderer(m_renderer);

        
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
    
    //returns true if back button is handled, false if system should handle it
    public boolean onBackPressed() {
    	return m_renderer.onBackPressed();
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


