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
	
	public static class ConnectedClient {
		public String unique_id; //network id
		public short id = -1; //game id: server decides which client gets which id
	}
	
	private List<ConnectedClient> m_connected_clients=new ArrayList<ConnectedClient>();
	
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
		{ return m_connected_clients.get(idx); }
	public int getConnectedClientCount() { return m_connected_clients.size(); }
	
	
	public int getSequenceNum() {
		return m_next_sequence_num++;
	}
	public void resetSequenceNum() {
		m_next_sequence_num = 0;
	}
	
	//open a game
	public void startAdvertise(String server_name) {
		m_networking.startAdvertise(server_name);
	}
	public void stopAdvertise() {
		m_networking.stopAdvertise();
	}
	
	//send events to clients
	void addOutgoingEvent(Event e) {
		//TODO: add important events to queue to be acked
		
		try {
			e.write(m_outgoing_stream);
		} catch (IOException e1) {
			Log.e(TAG, "Failed to write output stream");
		}
	}
	
	void sendEvents() throws BusException {
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
	void handleReceive() {
		//connected clients
		String client_unique_id;
		while((client_unique_id=m_networking.receiveClientJoined()) != null) {
			//check if already added
			boolean exists=false;
			for(ConnectedClient client : m_connected_clients) {
				if(client.unique_id.equals(client_unique_id)) exists=true;
			}
			if(!exists) {
				ConnectedClient c=new ConnectedClient();
				c.unique_id = client_unique_id;
				m_connected_clients.add(c);
				Log.i(TAG, "New Client connected: "+client_unique_id);
			}
		}
		//lost clients
		while((client_unique_id=m_networking.receiveClientLeft()) != null) {
			//find where
			for(int i=0; i<m_connected_clients.size(); ++i) {
				if(m_connected_clients.get(i).unique_id.equals(client_unique_id)) {
					m_connected_clients.remove(i);
					Log.i(TAG, "Client left: "+client_unique_id);
				}
			}
		}
		
		//acks 
		NetworkData d;
		while((d=m_networking.receiveAck()) != null) {
			short client_id = getClientId(d.sender);
			handleAckReceived(d.ack_num, client_id);
		}
		
	}
	
	//receive sensor updates
	//will return player id or -1 if no update
	public int getSensorUpdate(Vector pos_out) {
		NetworkData d=m_networking.receiveSensorUpdate();
		if(d!=null) {
			pos_out.set((Vector)d.arg1);
			short player_id = getClientId(d.sender);
			if(player_id!=-1) handleAckReceived(d.ack_num, player_id);
			return player_id;
		}
		return -1;
	}
	
	private short getClientId(String unique_id) {
		short ret=-1;
		if(unique_id != null) {
			for(ConnectedClient client : m_connected_clients) {
				if(client.unique_id.equals(unique_id))
					return client.id;
			}
			Log.w(TAG, "Received data but cannot find associated client id!");
		} else {
			Log.w(TAG, "Received data: sender String is NULL!");
		}
		return ret;
	}
	
	private void handleAckReceived(int ack_seq_num, int player_id) {
		//TODO
		
	}
	
}
