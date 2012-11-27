package com.android.game.clash_of_the_balls.game;

import java.lang.ref.WeakReference;
import java.util.Map;

import org.alljoyn.bus.BusException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.GameSettings;
import com.android.game.clash_of_the_balls.game.StaticGameObject.Type;
import com.android.game.clash_of_the_balls.game.event.Event;
import com.android.game.clash_of_the_balls.game.event.EventGameInfo;
import com.android.game.clash_of_the_balls.game.event.EventGameStartNow;
import com.android.game.clash_of_the_balls.network.NetworkServer;
import com.android.game.clash_of_the_balls.network.Networking;

/**
 * GameServer
 * this implements the server of the game: it does the network communication
 * and runs independently in a thread
 * 
 * start this thread when all clients have joined and the start button is pressed
 * network advertisement & listening should be disabled at this point
 * 
 * call initGame before starting the game thread (when all clients are connected)!
 * -> stop the thread between every game & reinit
 *
 */
public class GameServer extends GameBase implements Runnable {
	private static final String TAG_SERVER = "GameServer";
	
	private Looper m_looper=null;
	private NetworkServer m_network_server;
	private Networking m_networking;
	private IncomingHandler m_network_handler;
	
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

	public GameServer(GameSettings s, Networking networking
			, NetworkServer network_server) {
		super(true, s, null);
		m_network_server = network_server;
		m_networking = networking;
	}
	
	
	//called from another thread:
	public void startThread() {
		Thread t=new Thread(this);
		t.start();
	}
	public void stopThread() {
		Looper looper = m_looper;
		if(looper != null) {
			looper.quit();
			try {
				//wait for thread to exit
				looper.getThread().join(800);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public void initGame(GameLevel level) {
		super.initGame(level);
		
		//update networking
		m_network_server.handleReceive();
		
		m_player_count = m_network_server.getConnectedClientCount();
		
		Log.d(TAG_SERVER, "init game: "+m_player_count+" players");
		
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
		
		//TODO: player colors
		
		
		
		for(int i=0; i< m_player_count; ++i) {
			short id = getNextItemId();
			m_network_server.getConnectedClient(i).id = id;
			//create the player (without textures)
			int color = 0xffffffff;
			GamePlayer p = new GamePlayer(this, id, player_pos[indexes[i]], color, null);
			m_game_objects.put(id, p);
		}
	}
	
	
	
	private void handleMessage(Message msg) {
		switch(msg.what) {
		case Networking.HANDLE_RECEIVED_SIGNAL: handleNetworkReceivedSignal();
		break;
		}
	}
	
	private int m_sensor_update_count=0;
	private Vector m_sensor_vector = new Vector();
	
	private void handleNetworkReceivedSignal() {
		Log.v(TAG_SERVER, "Server: received a network signal");
		
		m_network_server.handleReceive();
		
		short id;
		while((id=m_network_server.getSensorUpdate(m_sensor_vector)) != -1) {
			DynamicGameObject obj = getGameObject(id);
			if(obj != null && obj.type == Type.Player) {
				GamePlayer p = (GamePlayer)obj;
				p.acceleration().set(m_sensor_vector);
				++m_sensor_update_count;
			}
		}
		
		if(m_sensor_update_count >= m_player_count) {
			m_sensor_update_count = 0;
			moveGame();
		}
		
	}
	
	private long m_last_time; //for timestepping
	
	private void moveGame() {
		Log.v(TAG_SERVER, "Server: moving the game");
		
		long time = SystemClock.elapsedRealtime(); //or: nanoTime()
		float elapsed_time = (float)(time - m_last_time) / 1000.f;
		m_last_time = time;
		
		//first go back 1/2 RTT & simulate forward?
		
		generate_events = true;
		move(elapsed_time);
		doCollisionHandling();
		applyMove();
		//TODO: check for game end
		
		sendAllEvents();
		generate_events = false;
	}
	
	public void move(float dsec) {
		m_game_field.move(dsec);
		for (Map.Entry<Short, DynamicGameObject> entry : m_game_objects.entrySet()) {
			entry.getValue().move(dsec);
		}
	}
	
	public void applyMove() {
		for (Map.Entry<Short, DynamicGameObject> entry : m_game_objects.entrySet()) {
			entry.getValue().applyMove();
		}
	}
	
	public void run() {
		Looper.prepare();
		m_looper = Looper.myLooper();
		
		m_network_handler = new IncomingHandler(this);
		m_networking.registerEventListener(m_network_handler);
		
		m_network_server.resetSequenceNum();
		
		//first throw away all waiting incoming sensor updates & acks
		while(m_networking.receiveAck()!=null) {}
		while(m_networking.receiveSensorUpdate()!=null) {}
		
		
		/* first send 'game about to start' event with level information */
		addEvent(new EventGameInfo(this, getNextSequenceNum()));
		sendAllEvents();
		
		//wait for start ...
		try {
			Thread.sleep(wait_to_start_game * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//ok let's do it...
		gameStartNow();
		
		m_last_time = SystemClock.elapsedRealtime();
		
		Looper.loop();
		
		m_networking.unregisterEventListener(m_network_handler);
		m_looper = null;
	}
	
	public void gameStartNow() {
		super.gameStartNow();
		addEvent(new EventGameStartNow(getNextSequenceNum()));
		sendAllEvents();
	}
	
	//this also deletes all events from the queue
	private void sendAllEvents() {
		Event e;
		while((e=m_events.poll()) != null) {
			m_network_server.addOutgoingEvent(e);
		}
		try {
			m_network_server.sendEvents();
		} catch (BusException e1) {
			Log.e(TAG_SERVER, "Failed to send events to clients");
			e1.printStackTrace();
		}
	}

	public int getNextSequenceNum() {
		return m_network_server.getSequenceNum();
	}
	
	public String getUniqueNameFromPlayerId(short player_id) {
		return m_network_server.getClientUniqueName(player_id);
	}

}
