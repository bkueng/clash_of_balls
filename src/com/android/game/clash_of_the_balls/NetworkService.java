package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.network.Networking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NetworkService extends Service {
	private static final String LOG_TAG = "NetworkService";

	public IBinder onBind(Intent intent) {
        return null;
	}
	
	public void onCreate() {
		Log.i(LOG_TAG, "NetworkServer: onCreate()");
        Networking.getInstance().init(this);
 	}
	
	public void onDestroy() {
		Log.i(LOG_TAG, "NetworkServer: onDestroy()");
		Networking.getInstance().deinit();
 	}
    
	public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
	}
}
