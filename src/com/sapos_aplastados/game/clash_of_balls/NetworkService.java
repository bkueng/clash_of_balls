/*
 * Copyright (C) 2012-2013 Hans Hardmeier <hanshardmeier@gmail.com>
 * Copyright (C) 2012-2013 Andrin Jenal
 * Copyright (C) 2012-2013 Beat Küng <beat-kueng@gmx.net>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
package com.sapos_aplastados.game.clash_of_balls;

import com.sapos_aplastados.game.clash_of_balls.network.Networking;

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
