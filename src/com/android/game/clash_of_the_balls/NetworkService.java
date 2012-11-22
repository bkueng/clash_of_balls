package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.network.Networking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NetworkService extends Service {

	public IBinder onBind(Intent intent) {
        return null;
	}
	
	public void onCreate() {
        Networking.getInstance().init(this);
 	}
	
	public void onDestroy() {
		Networking.getInstance().deinit();
 	}
    
	public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
	}
}
