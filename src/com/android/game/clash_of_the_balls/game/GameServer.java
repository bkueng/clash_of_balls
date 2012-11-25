package com.android.game.clash_of_the_balls.game;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.android.game.clash_of_the_balls.GameLevel;
import com.android.game.clash_of_the_balls.GameSettings;
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
 * call initGame before starting the game thread!
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
		
		//TODO: init players -> set m_player_count
		//	-> use & set m_next_object_id
		//  -> also set NetworkServer player ids
		
	}
	
	
	
	private void handleMessage(Message msg) {
		switch(msg.what) {
		case Networking.HANDLE_RECEIVED_SIGNAL: handleNetworkReceivedSignal();
		break;
		}
	}
	
	private void handleNetworkReceivedSignal() {
		Log.v(TAG_SERVER, "received a network signal");
		//acks
		
		//sensor updates -> apply to players
		
	}
	
	private void moveGame() {
		//time
		
		//first go back 1/2 RTT & simulate forward?
		
		generate_events = true;
		//move game
		//object collision detection
		//apply move's
		//send events to client's
		//delete all events
		generate_events = false;
	}
	
	public void run() {
		Looper.prepare();
		m_looper = Looper.myLooper();
		
		m_network_handler = new IncomingHandler(this);
		m_networking.registerEventListener(m_network_handler);
		
		//first throw away all waiting incoming sensor updates & acks
		while(m_networking.receiveAck()!=null) {}
		while(m_networking.receiveSensorUpdate()!=null) {}
		
		
		/* first send 'game about to start' event with level information */
		
		Looper.loop();
		
		m_networking.unregisterEventListener(m_network_handler);
		m_looper = null;
	}

	public int getNextSequenceNum() {
		return m_network_server.getSequenceNum();
	}
	
	public String getUniqueNameFromPlayerId(short player_id) {
		//TODO -> use NetworkServer
		
		return "";
	}

}
