package com.android.game.clash_of_the_balls;

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
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);

        
        new LoadViewTask().execute();    
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

    
    private class LoadViewTask extends AsyncTask<Void, Integer, Void>
    {
    	
    	private int m_time = 2000;
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
		
		//The code to be executed in a background thread.
		@Override
		protected Void doInBackground(Void... params) 
		{
			/* This is just a code that delays the thread execution 4 times, 
			 * during 850 milliseconds and updates the current progress. This 
			 * is where the code that is going to be executed on a background
			 * thread must be placed. 
			 */
			try 
			{
				//Get the current thread's token
				synchronized (this) 
				{
					//Initialize an integer (that will act as a counter) to zero
					int counter = 0;
					int step = 13;
					//While the counter is smaller than four
					while(counter <= m_time)
					{
						this.wait(step);
						//Increment the counter 
						counter+=step;
						//Set the current progress. 
						//This value is going to be passed to the onProgressUpdate() method.
						publishProgress((int)((float)counter/(float)m_time*100));
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
			progressDialog.setProgress(values[0]);
		}

		//after executing the code in the thread
		@Override
		protected void onPostExecute(Void result) 
		{
			//close the progress dialog
			progressDialog.dismiss();
			//initialize the View
			//setContentView(R.layout.main);
		} 	
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


