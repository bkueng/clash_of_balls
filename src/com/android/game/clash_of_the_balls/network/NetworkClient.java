package com.android.game.clash_of_the_balls.network;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.alljoyn.bus.BusException;

import android.util.Log;

import com.android.game.clash_of_the_balls.game.event.Event;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.network.Networking.AllJoynError;
import com.android.game.clash_of_the_balls.network.Networking.AllJoynErrorData;
import com.android.game.clash_of_the_balls.network.Networking.NetworkData;

/**
 * this class handles all client-side networking stuff
 * - list of available servers
 * - start/stop discovery of servers
 * - connect to a server (join game)
 * - receive events from server
 * - send sensor updates
 * - networking errors
 *
 */
public class NetworkClient {
	private static final String TAG = "NetworkClient";
	
	private final Networking m_networking;
	private boolean m_bHas_sensor_update=false;
	private Vector m_sensor_update;
	
	private List<String> m_available_servers=new ArrayList<String>();
	
	private Queue<Event> m_available_events = new LinkedList<Event>();
	
	
	public NetworkClient(Networking networking) {
		m_networking = networking;
	}
	
	public String getOwnUniqueName() {
		return m_networking.getUniqueName();
	}
	
	//this will return the server_id
	//to get the (displayable) name, call: Networking.getNameFromServerId()
	public String serverId(int idx) {
		return m_available_servers.get(idx);
	}
	public int serverIdCount() { return m_available_servers.size(); }
	
	//look for open games
	public void startDiscovery() {
		m_networking.startDiscovery();
	}
	public void stopDiscovery() {
		m_networking.stopDiscovery();
	}
	
	public void setOwnName(String name) {
		m_networking.setServerName(name);
	}
	//join a game
	public void connectToServer(String server_id) {
		m_networking.joinSession(server_id);
	}
	
	
	public boolean hasEvents() { return !m_available_events.isEmpty(); }
	public Event getNextEvent() { return m_available_events.poll(); }
		//does not remove the element from the queue
	public Event peekNextEvent() { return m_available_events.peek(); }
	
	
	//call this every frame, or in a regular time period
	public void handleReceive() {
		try {
			//available servers
			String server_id;
			//joined servers
			while((server_id=m_networking.receiveServerFound()) != null) {
				//check if already added
				boolean exists=false;
				for(String server : m_available_servers) {
					if(server.equals(server_id)) exists=true;
				}
				if(!exists) {
					m_available_servers.add(server_id);
					Log.i(TAG, "New Server found: "+server_id);
				}
			}
			//lost servers
			while((server_id=m_networking.receiveServerLost()) != null) {
				//find where
				for(int i=0; i<m_available_servers.size(); ++i) {
					if(m_available_servers.get(i).equals(server_id)) {
						m_available_servers.remove(i);
						Log.i(TAG, "Server lost: "+server_id);
					}
				}
			}
			
			//server updates
			NetworkData d;
			while((d=m_networking.receiveGameCommand()) != null) {
				Log.v(TAG, "Client: Received game command");
				
				ByteArrayInputStream bais = new ByteArrayInputStream(d.data);
				DataInputStream di = new DataInputStream(bais);
				Event e;
				while((e=Event.read(di)) != null) {
					m_available_events.add(e);
					
					//TODO: timestamp handling
					
				}
			}
			
			if(m_bHas_sensor_update) {
				m_bHas_sensor_update=false;
				m_networking.sendSensorUpdate(-1, m_sensor_update);
			}
		} catch(BusException e) {
			Log.e(TAG, "BusException");
			e.printStackTrace();
			m_network_error = new AllJoynErrorData();
			m_network_error.error_string = "";
			m_network_error.error = AllJoynError.BUS_EXCEPTION;
		}
	}
	
	private AllJoynErrorData m_network_error=null;
	
	//will return null if no error
	public AllJoynErrorData getNetworkError() {
		if(m_network_error != null) {
			AllJoynErrorData d = m_network_error;
			m_network_error = null;
			return d;
		}
		return m_networking.getError();
	}
	
	public void sensorUpdate(Vector new_data) {
		m_sensor_update = new_data;
		m_bHas_sensor_update=true;
	}
	
}
