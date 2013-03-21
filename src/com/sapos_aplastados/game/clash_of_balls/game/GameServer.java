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

package com.sapos_aplastados.game.clash_of_balls.game;

import java.lang.ref.WeakReference;

import org.alljoyn.bus.BusException;
import org.jbox2d.common.Vec2;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.sapos_aplastados.game.clash_of_balls.GameLevel;
import com.sapos_aplastados.game.clash_of_balls.GameSettings;
import com.sapos_aplastados.game.clash_of_balls.game.GameItem.ItemType;
import com.sapos_aplastados.game.clash_of_balls.game.GameStatistics.Statistic;
import com.sapos_aplastados.game.clash_of_balls.game.StaticGameObject.Type;
import com.sapos_aplastados.game.clash_of_balls.game.event.Event;
import com.sapos_aplastados.game.clash_of_balls.game.event.EventGameEnd;
import com.sapos_aplastados.game.clash_of_balls.game.event.EventGameInfo;
import com.sapos_aplastados.game.clash_of_balls.game.event.EventGameStartNow;
import com.sapos_aplastados.game.clash_of_balls.game.event.EventImpact;
import com.sapos_aplastados.game.clash_of_balls.game.event.EventItemAdded;
import com.sapos_aplastados.game.clash_of_balls.game.event.EventItemUpdate;
import com.sapos_aplastados.game.clash_of_balls.game.event.EventPool;
import com.sapos_aplastados.game.clash_of_balls.network.NetworkServer;
import com.sapos_aplastados.game.clash_of_balls.network.Networking;
import com.sapos_aplastados.game.clash_of_balls.network.Networking.ConnectedClient;

/**
 * GameServer
 * this implements the server of the game: it does the network communication
 * and runs independently in a thread
 * 
 * start this thread when all clients have joined and the start button is pressed
 * network advertisement & listening should be disabled at this point
 * 
 * call initGame before starting the game thread (when all clients are connected)!
 * call startGame to start the game
 * -> after game end the game can be reinit & restarted 
 * 		(thread does not need to be destroyed)
 *
 */
public class GameServer extends GameBase implements Runnable {
	private static final String TAG_SERVER = "GameServer";
	
	private Looper m_looper=null;
	private NetworkServer m_network_server;
	private Networking m_networking;
	private volatile IncomingHandler m_msg_handler = null;
	private Thread m_thread = null;
	
	public static final int HANDLE_GAME_START = 1000;
	
	static class IncomingHandler extends Handler {
		private final WeakReference<GameServer> m_service; 

		IncomingHandler(GameServer service) {
			m_service = new WeakReference<GameServer>(service);
		}
		@Override
		public void handleMessage(Message msg) {
			GameServer service = m_service.get();
			if (service != null) service.handleMessage(msg);
		}
	}
	
	private final Runnable m_timeout_check;
	
	public GameServer(GameSettings s, Networking networking
			, NetworkServer network_server) {
		super(true, s, null);
		m_network_server = network_server;
		m_networking = networking;
		
		m_timeout_check = new Runnable() {
				public void run() {
					handleTimeout();
				}
			}; 
		m_event_pool = new EventPool(20);
	}
	
	
	//called from another thread:
	public void startThread() {
		if(m_thread != null) return; //thread is already running
		
		Log.d(TAG_SERVER, "Server: starting the thread");
		
		m_thread=new Thread(this);
		m_thread.start();
		//wait until started
		while(m_msg_handler == null) {
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) { }
		}
	}
	public void stopThread() {
		if(m_thread == null) return;
		
		Log.d(TAG_SERVER, "Server: stopping the thread");
		
		Looper looper = m_looper;
		if(looper != null) {
			if(m_msg_handler!=null)
				m_networking.unregisterEventListener(m_msg_handler);
			m_looper = null;
			looper.quit();
			try {
				//wait for thread to exit
				looper.getThread().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		m_thread = null;
	}
	public void initGame(GameLevel level) {
		super.initGame(level);
		
		//update networking
		m_network_server.handleReceive();
		
		m_initial_player_count = m_network_server.getConnectedClientCount();
		m_current_player_count = m_initial_player_count;
		
		Log.d(TAG_SERVER, "init game: "+m_initial_player_count+" players");
		
		//get the player positions 
		Vector player_pos[] = new Vector[m_level.player_count];
		int k=0;
		for(int x = 0; x < m_level.width; ++x) {
			for(int y = 0; y < m_level.height; ++y) {
				if(m_level.foreground(x, y) == GameLevel.TYPE_PLAYER) {
					player_pos[k] = new Vector((float)x+0.5f, (float)y+0.5f);
					++k;
				}
			}
		}
		//randomize positions
		int indexes[] = new int[m_level.player_count];
		for(int i=0; i<indexes.length; ++i) indexes[i] = i;
		for(int i=0; i<indexes.length+10; ++i) {
			int k1 = (int)(Math.random() * (indexes.length));
			int k2 = (int)(Math.random() * (indexes.length));
			int tmp = indexes[k1];
			indexes[k1] = indexes[k2];
			indexes[k2] = tmp;
		}
		
		int colors[] = getDiffColors(m_initial_player_count);
		
		for(int i=0; i< m_initial_player_count; ++i) {
			short id = getNextItemId();
			ConnectedClient client = m_network_server.getConnectedClient(i);
			if(client!=null) client.id = id;
			//create the player (without textures)
			GamePlayer p = new GamePlayer(this, id, player_pos[indexes[i]]
					, colors[i], null, null, m_world, m_body_def);
			m_game_objects.put(id, p);
		}
	}
	
	//get n distinct colors in ARGB format (A=0xff)
	static int[] getDiffColors(int n) {
		int ret[]=new int[n];
		float hsv[] = new float[3];
		for(int k=0; k<n; ++k) {
			int i = (360/n) * k;
		    float h = (float)i;
		    float s = (float) (0.9f + Math.random() * 0.1f);
		    float l = (float) (0.5f + Math.random() * 0.1f);
		    
		    //convert to hsv
		    hsv[0] = h;
		    l *= 2;
		    s *= (l <= 1.f) ? l : 2.f - l;
		    hsv[2] = (l + s) / 2.f;
		    hsv[1] = (2.f * s) / (l + s);
		    
		    ret[k] = Color.HSVToColor(hsv);
		}
		return ret;
	}
	
	//this can be called from another thread to start a game
	//call this after the game is initialized
	//it sends game start commands to the connected clients
	public void startGame() {
		Message msg = m_msg_handler.obtainMessage(HANDLE_GAME_START);
		m_msg_handler.sendMessage(msg);
	}
	
	
	
	/* here start the thread internal methods */
	
	private void handleMessage(Message msg) {
		switch(msg.what) {
		case Networking.HANDLE_RECEIVED_SIGNAL: handleNetworkReceivedSignal(false);
		break;
		case HANDLE_GAME_START: handleGameStart();
		break;
		}
	}
	
	private static final int network_receive_timeout = 100; //[ms]
								//if we did not receive any network updates within
								//this timeout, we force a game move update
	
	private void handleTimeout() {
		if(!m_had_network_packets) {
			Log.w(TAG_SERVER, "Server: did not receive any client updates in "+
					network_receive_timeout+" ms. forcing update now");
			handleNetworkReceivedSignal(true);
		}
		m_had_network_packets = false;
		if(m_bIs_game_running) {
			m_msg_handler.postDelayed(m_timeout_check, network_receive_timeout);
		}
	}
	
	private int m_sensor_update_count=0;
	private Vector m_sensor_vector = new Vector();
	private boolean m_had_network_packets=false;
	
	private void handleNetworkReceivedSignal(boolean force_move) {
		
		m_had_network_packets = true;
		
		m_network_server.handleReceive();
		
		if(m_bIs_game_running) {
		
			short id;
			while((id=m_network_server.getSensorUpdate(m_sensor_vector)) != -1) {
				DynamicGameObject obj = getMoveableGameObject(id);
				if(obj != null && obj.type == Type.Player) {
					if(!obj.isDead()) {
						GamePlayer p = (GamePlayer)obj;
						p.applySensorVector(m_sensor_vector);
					}
					++m_sensor_update_count;
				}
			}

			if(m_sensor_update_count >= initialPlayerCount() || force_move) {
				m_sensor_update_count = 0;
				moveGame();
			}
		}
		
	}
	
	private void handleGameStart() {
		Log.d(TAG_SERVER, "Server: starting the game");
		
		m_next_item_time = 0.f;
		
		//first throw away all waiting incoming sensor updates & acks
		while(m_networking.receiveSensorUpdate()!=null) {}
		
		
		/* first send 'game about to start' event with level information */
		addEvent(m_event_pool.getEventGameInfo(this));
		sendAllEvents();
		
		//wait for start: wait_to_start_game seconds
		for(int i=0; i< 2*wait_to_start_game && m_looper!=null; ++i) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//ok let's do it...
		gameStartNow();
		
		m_last_time = SystemClock.elapsedRealtime();
		
	}
	
	
	private long m_last_time; //for timestepping
	
	private void moveGame() {
		
		long time = SystemClock.elapsedRealtime(); //or: nanoTime()
		float elapsed_time = (float)(time - m_last_time) / 1000.f;
		m_last_time = time;
		
		generate_events = true;
		moveClient(elapsed_time);
		move(elapsed_time);
		handleGenerateItems(elapsed_time);
		removeDeadObjects();
		checkGameEnd(elapsed_time);
		
		sendAllEvents();
		generate_events = false;
	}
	
	private float m_next_item_time=0.f; //[sec] when to generate next item
	
	private void handleGenerateItems(float dsec) {
		if(!GameSettings.place_items) return;
		
		if(m_next_item_time <= 0.f) {
			m_next_item_time = 3.f + (float)Math.random()*5.f; //3-8 sec
		}
		
		if((m_next_item_time-=dsec) <= 0.f && m_items_count < max_items_count) {
			Log.d(TAG_SERVER, "trying to put an Item on the field");
			
			ItemType type = GameItem.getRandomType();
			Vector position = new Vector();
			if(getFreeRandomField(position, 1.1f)) {
				Log.d(TAG_SERVER, "adding Item at x="+position.x+", y="+position.y);

				GameItem item = addItem(getNextItemId(), type, position);
				if(generate_events) {
					addEvent(m_event_pool.getEventItemAdded(this, item));
				}
			}
		}
	}
	
	
	private boolean m_is_game_ending = false;
	private float m_game_ending_timeout;
	
	private void checkGameEnd(float elapsed_time) {
		//in debug mode we allow a single player -> don't end the game
		if(GameSettings.debug && initialPlayerCount()==1
				&& currentPlayerCount()!=0) return;
		
		//game ends if there is only 1 player left (or 0)
		//a small timeout is used after only 1 or 0 player is left
		if(m_is_game_ending) {
			if((m_game_ending_timeout -= elapsed_time) < 0.f) {
				gameEnd();
			}
		} else if(currentPlayerCount() <= 1) {
			m_game_ending_timeout = 1.f; //wait for 1 sec until game end
			m_is_game_ending = true;
		}
	}
	
	public void run() {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Looper.prepare();
		m_looper = Looper.myLooper();
		
		m_msg_handler = new IncomingHandler(this);
		m_networking.registerEventListener(m_msg_handler);
		
		Looper.loop();
		
		m_networking.unregisterEventListener(m_msg_handler);
		
		m_msg_handler = null;
		m_looper = null;
		m_bIs_game_running = false;
	}
	
	public void gameStartNow() {
		super.gameStartNow();
		addEvent(m_event_pool.getEventGameStartNow());
		sendAllEvents();
		m_is_game_ending=false;
		IncomingHandler h = m_msg_handler;
		if(h!=null)
			h.postDelayed(m_timeout_check, network_receive_timeout);
	}
	
	public void gameEnd() {
		super.gameEnd();
		//update statistics: points of the still living player(s)
		Statistic stat = m_settings.game_statistics.currentRoundStatistics();
		for(DynamicGameObject item : m_game_objects.values()) {
			if(item.type == Type.Player && !item.isDead()) {
				stat.setPlayerPoints(item.m_id, initialPlayerCount() - currentPlayerCount());
			}
		}
		m_settings.game_statistics.applyCurrentRoundStatistics();
		
		addEvent(m_event_pool.getEventGameEnd(m_settings.game_statistics));
		//after here the game stopped & this thread is simply waiting 
		//for next game initialization & game start (called from UIHandler)
	}
	
	protected void handleObjectDied(DynamicGameObject obj) {
		super.handleObjectDied(obj);
		//statistics
		if(obj.type == Type.Player) {
			Statistic stat = m_settings.game_statistics.currentRoundStatistics();
			stat.setPlayerPoints(obj.m_id, initialPlayerCount() - currentPlayerCount()-1);
		}
	}
	
	protected void handleImpact(StaticGameObject obja,
			StaticGameObject objb, Vector impact_point, Vector normal) {
		
		if (generate_events) {
			//the client will call handleImpact when he receives this event
			addEvent(m_event_pool.getEventImpact(obja.m_id, objb.m_id, normal));
		}
		obja.handleImpact(objb, normal);
		normal.mul(-1.f);
		objb.handleImpact(obja, normal);
	}
	
	//this also deletes all events from the queue
	private void sendAllEvents() {
		Event e;
		while((e=m_events.poll()) != null) {
			m_network_server.addOutgoingEvent(e);
			m_event_pool.recycle(e);
		}
		try {
			m_network_server.sendEvents();
		} catch (BusException e1) {
			Log.e(TAG_SERVER, "Failed to send events to clients");
			e1.printStackTrace();
		}
	}

	public String getUniqueNameFromPlayerId(short player_id) {
		return m_network_server.getClientUniqueName(player_id);
	}

}
