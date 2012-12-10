package com.android.game.clash_of_the_balls.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alljoyn.bus.BusException;

import android.util.Log;

import com.android.game.clash_of_the_balls.game.event.Event;
import com.android.game.clash_of_the_balls.game.Vector;
import com.android.game.clash_of_the_balls.network.Networking.ConnectedClient;
import com.android.game.clash_of_the_balls.network.Networking.NetworkData;

/**
 * this class handles all server-side networking stuff
 * - list of connected clients
 * - start/stop advertise (open a new game)
 * - send events to clients
 * - receive sensor updates
 *
 */
public class NetworkServer {
	private static final String TAG = "NetworkServer";
	
	private int m_next_sequence_num;
	
	private final ByteArrayOutputStream m_outgoing_byte_stream
			= new ByteArrayOutputStream();
	private final DataOutputStream m_outgoing_stream
			= new DataOutputStream(m_outgoing_byte_stream);

	private final Networking m_networking;
	
	public NetworkServer(Networking networking) {
		m_networking = networking;
		resetSequenceNum();
	}
	
	public ConnectedClient getConnectedClient(int idx) 
		{ return m_networking.connectedClient(idx); }
	public int getConnectedClientCount() { return m_networking.connectedClientCount(); }
	
	
	public int getSequenceNum() {
		return m_next_sequence_num++;
	}
	public void resetSequenceNum() {
		m_next_sequence_num = 0;
	}
	
	public void setOwnName(String name) {
		m_networking.setServerName(name);
	}
	public void setMaxClientCount(int max_count) {
		m_networking.setMaxClientCount(max_count);
	}
	public void joinSessionToSelf() {
		m_networking.joinSessionToSelf();
	}
	//open a game
	public void startAdvertise() {
		m_networking.startAdvertise();
	}
	public void stopAdvertise() {
		m_networking.stopAdvertise();
	}
	
	//send events to clients
	public void addOutgoingEvent(Event e) {
		try {
			e.write(m_outgoing_stream);
		} catch (IOException e1) {
			Log.e(TAG, "Failed to write output stream");
		}
	}
	
	public void sendEvents() throws BusException {
		try {
			m_outgoing_stream.flush();
			if(m_outgoing_byte_stream.size() > 0) {
				m_networking.sendGameCommand(m_outgoing_byte_stream.toByteArray());
				m_outgoing_byte_stream.reset();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	//call this regularly to handle incoming network data
	public void handleReceive() {
		//nothing to do
	}
	
	//receive sensor updates
	//will return player id or -1 if no update
	public short getSensorUpdate(Vector pos_out) {
		NetworkData d=m_networking.receiveSensorUpdate();
		if(d!=null) {
			pos_out.set((Vector)d.arg1);
			return getClientId(d.sender);
		}
		return (short)-1;
	}
	
	public short getClientId(String unique_id) {
		short ret=-1;
		if(unique_id != null) {
			for(int i=0; i<m_networking.connectedClientCount(); ++i) {
				ConnectedClient client = m_networking.connectedClient(i);
				if(client!=null && client.unique_id.equals(unique_id))
					return client.id;
			}
			Log.w(TAG, "Received data but cannot find associated client id (unique_id = "
					+unique_id+", client_count="+m_networking.connectedClientCount()+")!");
		} else {
			Log.w(TAG, "Received data: sender String is NULL!");
		}
		return ret;
	}
	
	public String getClientUniqueName(short client_id) {
		for(int i=0; i<m_networking.connectedClientCount(); ++i) {
			ConnectedClient client = m_networking.connectedClient(i);
			if(client!=null && client.id == client_id)
				return client.unique_id;
		}
		return "";
	}
	
}
