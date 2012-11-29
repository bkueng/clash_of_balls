package com.android.game.clash_of_the_balls.network;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.MessageContext;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.SignalEmitter;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.annotation.BusSignal;
import org.alljoyn.bus.annotation.BusSignalHandler;

import com.android.game.clash_of_the_balls.game.Vector;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * thread-safe networking interface to use AllJoyn
 * 
 * incoming messages are queued. get them with the receive* methods.
 * a call to receive* will remove the message from the queue.
 * call registerEventListener to get notifications on receive updates.
 * 
 * call setServerName before calling startAdvertise or joinSession
 * 
 * Note that AllJoyn has a maximum packet size of 2^17 bytes
 * (BusAttachment.ALLJOYN_MAX_PACKET_LEN)
 *
 * for every receive* method there must be someone to call them, because
 * otherwise the queue will fill up and use more and more memory
 * 
 * this is a singleton class
 */
public class Networking {
	private static final String TAG = "Networking";
	
	private Context m_context;
	private boolean m_bInit=false;
	
	private Networking() {}
	private static Networking m_instance;
	
	public static Networking getInstance() {
		if(m_instance==null) m_instance=new Networking();
		return m_instance;
	}
	
	public synchronized void init(Context context) {
		if(m_bInit) return;
		Log.d(TAG, "init Network Object");
		//clear connected clients
		m_connected_clients = new ArrayList<ConnectedClient>();
		m_context = context;
		if(mBus == null)
			mBus = new BusAttachment(context.getPackageName()
					, BusAttachment.RemoteMessage.Receive);
		m_network_service = new NetworkService();
		startBusThread();
		m_background_handler.connect();
		m_bInit=true;
	}
	public void deinit() {
		if(!m_bInit) return;
		Log.d(TAG, "deinit Network Object");
		if(isBusConnected()) {
			m_background_handler.leaveSession();
			m_background_handler.cancelAdvertise();
			m_background_handler.unbindSession();
			m_background_handler.releaseName();
			m_background_handler.disconnect();
		}
		stopBusThread();
		m_bInit=false;
	}
	
	/* public interface */
	
	/* sending messages */
	public void sendSensorUpdate(int ack_seq_num, Vector pos) 
			throws BusException {
		try {
			if (mJoinedToSelf) {
				//this signal is: client --> server. so don't send to clients
				//if (mHostChatInterface != null) 
				//	mHostChatInterface.sensorUpdate(ack_seq_num, pos);
				receivedSensorUpdate(mBus.getUniqueName(), ack_seq_num, pos.x, pos.y); //send to ourself
			} else {
				mChatInterface.sensorUpdate(ack_seq_num, pos.x, pos.y);
			}
		} catch (BusException ex) {
    		alljoynError(Module.USE, AllJoynError.SEND_ERROR, 
    				"Bus exception while sending message: (" + ex + ")");
		}
	}
    public void sendAck(int ack_seq_num) throws BusException {
		try {
			if (mJoinedToSelf) {
				//this signal is: client --> server. so don't send to clients
				//if (mHostChatInterface != null)
				//	mHostChatInterface.ack(ack_seq_num);
				receivedAck(mBus.getUniqueName(), ack_seq_num); //send to ourself
			} else {
				mChatInterface.ack(ack_seq_num);
			}
		} catch (BusException ex) {
    		alljoynError(Module.USE, AllJoynError.SEND_ERROR, 
    				"Bus exception while sending message: (" + ex + ")");
		}
    }
    public void sendGameCommand(byte[] data) throws BusException {
		try {
			if (mJoinedToSelf) {
				//this signal is: server --> clients. so send to all clients
				if (mHostChatInterface != null)
					mHostChatInterface.gameCommand(data);
				receivedGameCommand(mBus.getUniqueName(), data); //send to ourself
			} else {
				mChatInterface.gameCommand(data);
			}
		} catch (BusException ex) {
    		alljoynError(Module.USE, AllJoynError.SEND_ERROR, 
    				"Bus exception while sending message: (" + ex + ")");
		}
    }
	
    /* receiving messages */
    //will return null if there are no messages left
    public NetworkData receiveSensorUpdate() {
    	return m_sensor_updates.poll();
    }
    public NetworkData receiveAck() {
    	return m_acks.poll();
    }
    public NetworkData receiveGameCommand() {
    	return m_game_commands.poll();
    }
    //this will return a server_id
    public String receiveServerFound() {
    	return m_server_found.poll();
    }
    public String receiveServerLost() {
    	return m_server_lost.poll();
    }
    //this is the own unique name
    public String getUniqueName() {
    	return mBus.getUniqueName();
    }
    
    /* joined clients */
    
	public static class ConnectedClient {
		public String unique_id; //same as packet sender
		public String well_known_name; //can be null, until signals are sent to everyone
			//use getNameFromServerId to get the name from this
		
		public short id = -1; //game id: server decides which client gets which id
	}
	private List<ConnectedClient> m_connected_clients;
	
	public synchronized ConnectedClient connectedClient(int idx) {
		if(idx < m_connected_clients.size())
			return m_connected_clients.get(idx);
		return null;
	}
	public synchronized int connectedClientCount() {
		return m_connected_clients.size();
	}
	
    
    //set this before start advertising
    //use -1 for unlimited
    public void setMaxClientCount(int max_count) {
    	m_max_client_count = max_count;
    }
    public int maxClientCount() {
    	return m_max_client_count;
    }
    
    
    private volatile int m_max_client_count = -1;
    
	public synchronized void registerEventListener(Handler h) {
		m_event_listeners.add(h);
	}
	public synchronized void unregisterEventListener(Handler h) {
		for(int i=0; i<m_event_listeners.size(); ++i) {
			if(m_event_listeners.get(i) == h) {
				m_event_listeners.remove(i);
				return;
			}
		}
	}
	//event notification messages -> msg.what
    public static final int HANDLE_RECEIVED_SIGNAL = 0;
    public static final int HANDLE_CLIENT_JOINED = 1;
    public static final int HANDLE_CLIENT_LEFT = 2;
    public static final int HANDLE_SERVER_FOUND = 3;
    public static final int HANDLE_SERVER_LOST = 4;
    public static final int HANDLE_ERROR = 5;
    
    private volatile AllJoynErrorData m_error = null;
    
    public AllJoynErrorData getError() {
    	return m_error;
    }
	
    //Note: server_name must NOT contain '.'
    //it can only consist of: [A-Z][a-z][0-9]_-
    public void setServerName(String server_name) {
    	m_host_server_name = new String(server_name);
    }
    
	/* advertising */
	//call this to let the others discover me
    //first call setServerName
	public void startAdvertise() {
		m_background_handler.requestName();
		m_background_handler.bindSession();
		m_background_handler.advertise();
	}
	public void stopAdvertise() {
		m_background_handler.cancelAdvertise();
		m_background_handler.unbindSession();
		m_background_handler.releaseName();
	}
	
	//listen for other servers
	public void startDiscovery() {
		 m_background_handler.startDiscovery();
	}
	public void stopDiscovery() {
		 m_background_handler.cancelDiscovery();
	}
	
	/* join a network */
	//server_id_to_join is one of the returned receiveServerFound() Strings
    //first call setServerName
	public void joinSession(String server_id_to_join) {
		m_server_id_to_join = new String(server_id_to_join);
		m_background_handler.joinSession();
	}
	public void joinSessionToSelf() {
		//join to ourself
		joinSession(getWellKnownName());
	}
	public void leaveSession() {
		m_background_handler.leaveSession();
	}
	
	public static String getNameFromServerId(String server_id) {
		//server id is: NAME_PREFIX.<guid>.server_name
    	int lastDot = server_id.lastIndexOf('.');
    	if (lastDot < 0) {
    		//this is a format error. we could throw an exception.
    		//but we do our best here to avoid errors
    		return server_id;
    	}
        return server_id.substring(lastDot + 1);
	}
	
	//incoming data
	public static class NetworkData {
		public Object arg1;
		public int ack_num;
		public byte[] data;
		public String sender;
		
	}
	private List<Handler> m_event_listeners = new ArrayList<Handler>();
	private Queue<NetworkData> m_sensor_updates = new ConcurrentLinkedQueue<NetworkData>();
	private Queue<NetworkData> m_acks = new ConcurrentLinkedQueue<NetworkData>();
	private Queue<NetworkData> m_game_commands = new ConcurrentLinkedQueue<NetworkData>();
	
	private Queue<String> m_server_found = new ConcurrentLinkedQueue<String>();
	private Queue<String> m_server_lost = new ConcurrentLinkedQueue<String>();
	
	//what is one of HANDLE_*
	private synchronized void sendEventToListeners(int what) {
		for(Handler h: m_event_listeners) {
			Message msg = h.obtainMessage(what);
			h.sendMessage(msg);
		}
	}
	
	/* AllJoyn stuff */
	
    /**
     * Enumeration of the states of the AllJoyn bus attachment.  This
     * lets us make a note to ourselves regarding where we are in the process
     * of preparing and tearing down the fundamental connection to the AllJoyn
     * bus.
     * 
     * This should really be a more private think, but for the sample we want
     * to show the user the states we are running through.  Because we are
     * really making a data hiding exception, and because we trust ourselves,
     * we don't go to any effort to prevent the UI from changing our state out
     * from under us.
     * 
     * There are separate variables describing the states of the client
     * ("use") and service ("host") pieces.
     */
    public static enum BusAttachmentState {
    	DISCONNECTED,	/** The bus attachment is not connected to the AllJoyn bus */ 
    	CONNECTED,		/** The  bus attachment is connected to the AllJoyn bus */
    	DISCOVERING		/** The bus attachment is discovering remote attachments hosting chat channels */
    }
    
    /**
     * The state of the AllJoyn bus attachment.
     */
    private BusAttachmentState mBusAttachmentState = BusAttachmentState.DISCONNECTED;
    
    private boolean isBusConnected() {
    	return mBusAttachmentState == BusAttachmentState.CONNECTED
    			|| mBusAttachmentState == BusAttachmentState.DISCOVERING;
    }
    public boolean isDiscovering() {
    	return mBusAttachmentState == BusAttachmentState.DISCOVERING;
    }
    
    /**
     * Enumeration of the states of a hosted chat channel.  This lets us make a
     * note to ourselves regarding where we are in the process of preparing
     * and tearing down the AllJoyn pieces responsible for providing the chat
     * service.  In order to be out of the IDLE state, the BusAttachment state
     * must be at least CONNECTED.
     */
    public static enum HostChannelState {
    	IDLE,	        /** There is no hosted chat channel */ 
    	NAMED,		    /** The well-known name for the channel has been successfully acquired */
    	BOUND,			/** A session port has been bound for the channel */
    	ADVERTISED,	    /** The bus attachment has advertised itself as hosting an chat channel */
    	CONNECTED       /** At least one remote device has connected to a session on the channel */
    }
    
    /**
     * The state of the AllJoyn components responsible for hosting an chat channel.
     */
    private HostChannelState mHostChannelState = HostChannelState.IDLE;
    
    /**
     * Enumeration of the states of a hosted chat channel.  This lets us make a
     * note to ourselves regarding where we are in the process of preparing
     * and tearing down the AllJoyn pieces responsible for providing the chat
     * service.  In order to be out of the IDLE state, the BusAttachment state
     * must be at least CONNECTED.
     */
    public static enum UseChannelState {
    	IDLE,	        /** There is no used chat channel */ 
    	JOINED,		    /** The session for the channel has been successfully joined */
    }
    
    /**
     * The state of the AllJoyn components responsible for hosting an chat channel.
     */
    private UseChannelState mUseChannelState = UseChannelState.IDLE;
    
    
    /**
     * This is the AllJoyn background thread handler class.  AllJoyn is a
     * distributed system and must therefore make calls to other devices over
     * networks.  These calls may take arbitrary amounts of time.  The Android
     * application framework is fundamentally single-threaded and so the main
     * Service thread that is executing in our component is the same thread as
     * the ones which appear to be executing the user interface code in the
     * other Activities.  We cannot block this thread while waiting for a
     * network to respond, so we need to run our calls in the context of a
     * background thread.  This is the class that provides that background
     * thread implementation.
     *
     * When we need to do some possibly long-lived task, we just pass a message
     * to an object implementing this class telling it what needs to be done.
     * There are two main parts to this class:  an external API and the actual
     * handler.  In order to make life easier for callers, we provide API
     * methods to deal with the actual message passing, and then when the
     * handler thread is executing the desired method, it calls out to an 
     * implementation in the enclosing class.  For example, in order to perform
     * a connect() operation in the background, the enclosing class calls
     * BackgroundHandler.connect(); and the result is that the enclosing class
     * method doConnect() will be called in the context of the background
     * thread.
     */
    private final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }
        
        /**
         * Exit the background handler thread.  This will be the last message
         * executed by an instance of the handler.
         */
        public void exit() {
            Log.i(TAG, "mBackgroundHandler.exit()");
        	Message msg = m_background_handler.obtainMessage(EXIT);
            m_background_handler.sendMessage(msg);
        }
        
        /**
         * Connect the application to the Alljoyn bus attachment.  We expect
         * this method to be called in the context of the main Service thread.
         * All this method does is to dispatch a corresponding method in the
         * context of the service worker thread.
         */
        public void connect() {
            Log.i(TAG, "mBackgroundHandler.connect()");
        	Message msg = m_background_handler.obtainMessage(CONNECT);
            m_background_handler.sendMessage(msg);
        }
        
        /**
         * Disonnect the application from the Alljoyn bus attachment.  We
         * expect this method to be called in the context of the main Service
         * thread.  All this method does is to dispatch a corresponding method
         * in the context of the service worker thread.
         */
        public void disconnect() {
            Log.i(TAG, "mBackgroundHandler.disconnect()");
        	Message msg = m_background_handler.obtainMessage(DISCONNECT);
            m_background_handler.sendMessage(msg);
        }

        /**
         * Start discovering remote instances of the application.  We expect
         * this method to be called in the context of the main Service thread.
         * All this method does is to dispatch a corresponding method in the
         * context of the service worker thread.
         */
        public void startDiscovery() {
            Log.i(TAG, "mBackgroundHandler.startDiscovery()");
        	Message msg = m_background_handler.obtainMessage(START_DISCOVERY);
        	m_background_handler.sendMessage(msg);
        }
        
        /**
         * Stop discovering remote instances of the application.  We expect
         * this method to be called in the context of the main Service thread.
         * All this method does is to dispatch a corresponding method in the
         * context of the service worker thread.
         */
        public void cancelDiscovery() {
            Log.i(TAG, "mBackgroundHandler.stopDiscovery()");
        	Message msg = m_background_handler.obtainMessage(CANCEL_DISCOVERY);
        	m_background_handler.sendMessage(msg);
        }

        public void requestName() {
            Log.i(TAG, "mBackgroundHandler.requestName()");
        	Message msg = m_background_handler.obtainMessage(REQUEST_NAME);
        	m_background_handler.sendMessage(msg);
        }
        
        public void releaseName() {
            Log.i(TAG, "mBackgroundHandler.releaseName()");
        	Message msg = m_background_handler.obtainMessage(RELEASE_NAME);
        	m_background_handler.sendMessage(msg);
        }
        
        public void bindSession() {
            Log.i(TAG, "mBackgroundHandler.bindSession()");
        	Message msg = m_background_handler.obtainMessage(BIND_SESSION);
        	m_background_handler.sendMessage(msg);
        }
        
        public void unbindSession() {
            Log.i(TAG, "mBackgroundHandler.unbindSession()");
        	Message msg = m_background_handler.obtainMessage(UNBIND_SESSION);
        	m_background_handler.sendMessage(msg);
        }
        
        public void advertise() {
            Log.i(TAG, "mBackgroundHandler.advertise()");
        	Message msg = m_background_handler.obtainMessage(ADVERTISE);
        	m_background_handler.sendMessage(msg);
        }
        
        public void cancelAdvertise() {
            Log.i(TAG, "mBackgroundHandler.cancelAdvertise()");
        	Message msg = m_background_handler.obtainMessage(CANCEL_ADVERTISE);
        	m_background_handler.sendMessage(msg);
        }
        
        public void joinSession() {
            Log.i(TAG, "mBackgroundHandler.joinSession()");
        	Message msg = m_background_handler.obtainMessage(JOIN_SESSION);
        	m_background_handler.sendMessage(msg);
        }
        
        public void leaveSession() {
            Log.i(TAG, "mBackgroundHandler.leaveSession()");
        	Message msg = m_background_handler.obtainMessage(LEAVE_SESSION);
        	m_background_handler.sendMessage(msg);
        }
                 
        /**
         * The message handler for the worker thread that handles background
         * tasks for the AllJoyn bus.
         */
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CONNECT:
	            doConnect();
            	break;
	        case DISCONNECT:
		        doDisconnect();
		    	break;
            case START_DISCOVERY:
	            doStartDiscovery();
            	break;
	        case CANCEL_DISCOVERY:
		        doStopDiscovery();
		    	break;
	        case REQUEST_NAME:
		        doRequestName();
		    	break;
	        case RELEASE_NAME:
		        doReleaseName();
		    	break;		
	        case BIND_SESSION:
		        doBindSession();
		    	break;
	        case UNBIND_SESSION:
		        doUnbindSession();
		        break;
	        case ADVERTISE:
		        doAdvertise();
		    	break;
	        case CANCEL_ADVERTISE:
		        doCancelAdvertise();		        
		    	break;	
	        case JOIN_SESSION:
		        doJoinSession();
		    	break;
	        case LEAVE_SESSION:
		        doLeaveSession();
		        break;
	        case EXIT:
                doExit();
                getLooper().quit();
                break;
		    default:
		    	break;
            }
        }
    }
    
    private static final int EXIT = 1;
    private static final int CONNECT = 2;
    private static final int DISCONNECT = 3;
    private static final int START_DISCOVERY = 4;
    private static final int CANCEL_DISCOVERY = 5;
    private static final int REQUEST_NAME = 6;
    private static final int RELEASE_NAME = 7;
    private static final int BIND_SESSION = 8;
    private static final int UNBIND_SESSION = 9;
    private static final int ADVERTISE = 10;
    private static final int CANCEL_ADVERTISE = 11;
    private static final int JOIN_SESSION = 12;
    private static final int LEAVE_SESSION = 13;
    
    /**
     * The instance of the AllJoyn background thread handler.  It is created
     * when Android decides the Service is needed and is called from the
     * onCreate() method.  When Android decides our Service is no longer 
     * needed, it will call onDestroy(), which spins down the thread.
     */
    private BackgroundHandler m_background_handler = null;
    
    /**
     * Since basically our whole reason for being is to spin up a thread to
     * handle long-lived remote operations, we provide thsi method to do so.
     */
    private void startBusThread() {
    	HandlerThread busThread = new HandlerThread("BackgroundHandler");
        busThread.start();
    	m_background_handler = new BackgroundHandler(busThread.getLooper());
    }
    
    /**
     * When Android decides that our Service is no longer needed, we need to
     * tear down the thread that is servicing our long-lived remote operations.
	 * This method does so. 
     */
    private void stopBusThread() {
        m_background_handler.exit();
        //wait for the background thread to quit
        try {
			m_background_handler.getLooper().getThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * The bus attachment is the object that provides AllJoyn services to Java
     * clients.  Pretty much all communiation with AllJoyn is going to go through
     * this obejct.
     */
    private volatile BusAttachment mBus = null;
    
    /**
     * The well-known name prefix which all bus attachments hosting a channel
     * will use.  The NAME_PREFIX and the channel name are composed to give
     * the well-known name a hosting bus attachment will request and 
     * advertise.
     */
    private static final String NAME_PREFIX = 
    		"com.android.game.clash_of_the_balls.alljoyn";
    private volatile String m_host_server_name; //only server name
    private volatile String m_server_id_to_join; //this is the well-known name
    
    private String getWellKnownName() {
    	return NAME_PREFIX + ".g" + mBus.getGlobalGUIDString() + "."
    			+ m_host_server_name;
    }
    private String getWellKnownNameToJoin() {
    	return m_server_id_to_join;
    }
    
	/**
	 * The well-known session port used as the contact port for the chat service.
	 */
    private static final short CONTACT_PORT = 27;
    
    /**
     * The object path used to identify the service "location" in the bus
     * attachment.
     */
    private static final String OBJECT_PATH = "/gameService";
    
    
    /**
     * The ChatBusListener is a class that listens to the AllJoyn bus for
     * notifications corresponding to the existence of events happening out on
     * the bus.  We provide one implementation of our listener to the bus
     * attachment during the connect(). 
     */
    private class NetworkBusListener extends BusListener {
   		/**
		 * This method is called when AllJoyn discovers a remote attachment
		 * that is hosting an chat channel.  We expect that since we only
		 * do a findAdvertisedName looking for instances of the chat
		 * well-known name prefix we will only find names that we know to
		 * be interesting.  When we find a remote application that is
		 * hosting a channel, we add its channel name it to the list of
		 * available channels selectable by the user.
         *
         * In the class documentation for the BusListener note that it is a
         * requirement for this method to be multithread safe.  This is
         * accomplished by the use of a monitor on the ChatApplication as
         * exemplified by the synchronized attribute of the addFoundChannel
         * method there.
		 */
		public void foundAdvertisedName(String name, short transport, String namePrefix) {
            Log.i(TAG, "mBusListener.foundAdvertisedName(" + name + ")");
            m_server_found.add(name); //name is well known name
            sendEventToListeners(HANDLE_SERVER_FOUND);
		}
		
   		/**
		 * This method is called when AllJoyn decides that a remote bus
		 * attachment that is hosting an chat channel is no longer available.
		 * When we lose a remote application that is hosting a channel, we
		 * remote its name from the list of available channels selectable
		 * by the user.  
         *
         * In the class documentation for the BusListener note that it is a
         * requirement for this method to be multithread safe.  This is
         * accomplished by the use of a monitor on the ChatApplication as
         * exemplified by the synchronized attribute of the removeFoundChannel
         * method there.
		 */
		public void lostAdvertisedName(String name, short transport, String namePrefix) {
            Log.i(TAG, "mBusListener.lostAdvertisedName(" + name + ")");
            m_server_lost.add(name); //name is well known name
            sendEventToListeners(HANDLE_SERVER_LOST);
		}
    }
    
    /**
     * An instance of an AllJoyn bus listener that knows what to do with
     * foundAdvertisedName and lostAdvertisedName notifications.  Although
     * we often use the anonymous class idiom when talking to AllJoyn, the
     * bus listener works slightly differently and it is better to use an
     * explicitly declared class in this case.
     */
    private NetworkBusListener mBusListener = new NetworkBusListener();
    
    /**
     * Implementation of the functionality related to connecting our app
     * to the AllJoyn bus.  We expect that this method will only be called in
     * the context of the AllJoyn bus handler thread; and while we are in the
     * DISCONNECTED state.
     */
    private void doConnect() {
        Log.i(TAG, "doConnect()");
        org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(m_context);
    	assert(mBusAttachmentState == BusAttachmentState.DISCONNECTED);
    	mBus.useOSLogging(true);
    	mBus.setDebugLevel("ALLJOYN_JAVA", 5);
    	mBus.registerBusListener(mBusListener);
    	
        /* 
         * To make a service available to other AllJoyn peers, first
         * register a BusObject with the BusAttachment at a specific
         * object path.  Our service is implemented by the ChatService
         * BusObject found at the "/chatService" object path.
         */
        Status status = mBus.registerBusObject((BusObject)m_network_service, OBJECT_PATH);
        if (Status.OK != status) {
    		alljoynError(Module.HOST, AllJoynError.CONNECT_ERROR, 
    				"Unable to register the bus object: (" + status + ")");
        	return;
        }
    	
    	status = mBus.connect();
    	if (status != Status.OK) {
    		alljoynError(Module.GENERAL, AllJoynError.CONNECT_ERROR, 
    				"Unable to connect to the bus: (" + status + ")");
        	return;
    	}
    	
        status = mBus.registerSignalHandlers(this);
    	if (status != Status.OK) {
    		alljoynError(Module.GENERAL, AllJoynError.CONNECT_ERROR, 
    				"Unable to register signal handlers: (" + status + ")");
        	return;
    	}
        
    	mBusAttachmentState = BusAttachmentState.CONNECTED;
    }  
    
    /**
     * Implementation of the functionality related to disconnecting our app
     * from the AllJoyn bus.  We expect that this method will only be called
     * in the context of the AllJoyn bus handler thread.  We expect that this
     * method will only be called in the context of the AllJoyn bus handler
     * thread; and while we are in the CONNECTED state.
     */
    private boolean doDisconnect() {
        Log.i(TAG, "doDisonnect()");
    	assert(mBusAttachmentState == BusAttachmentState.CONNECTED);
    	mBus.unregisterBusListener(mBusListener);
    	mBus.disconnect();
		mBusAttachmentState = BusAttachmentState.DISCONNECTED;
    	return true;
    }
    
    /**
     * Implementation of the functionality related to discovering remote apps
     * which are hosting chat channels.  We expect that this method will only
     * be called in the context of the AllJoyn bus handler thread; and while
     * we are in the CONNECTED state.  Since this is a core bit of functionalty
     * for the "use" side of the app, we always do this at startup.
     */
    private void doStartDiscovery() {
        Log.i(TAG, "doStartDiscovery()");
    	assert(mBusAttachmentState == BusAttachmentState.CONNECTED);
      	Status status = mBus.findAdvertisedName(NAME_PREFIX);
    	if (status == Status.OK) {
        	mBusAttachmentState = BusAttachmentState.DISCOVERING;
    	} else {
    		alljoynError(Module.USE, AllJoynError.START_DISCOVERY_ERROR,
    			"Unable to start finding advertised names: (" + status + ")");
    	}
    }
    
    /**
     * Implementation of the functionality related to stopping discovery of
     * remote apps which are hosting chat channels.
     */
    private void doStopDiscovery() {
        Log.i(TAG, "doStopDiscovery()");
    	assert(mBusAttachmentState == BusAttachmentState.CONNECTED);
      	mBus.cancelFindAdvertisedName(NAME_PREFIX);
      	mBusAttachmentState = BusAttachmentState.CONNECTED;
    }
       
    /**
     * Implementation of the functionality related to requesting a well-known
     * name from an AllJoyn bus attachment.
     */
    private void doRequestName() {
        Log.i(TAG, "doRequestName()");
    	
        /*
         * In order to request a name, the bus attachment must at least be
         * connected.
         */
        int stateRelation = mBusAttachmentState.compareTo(BusAttachmentState.DISCONNECTED);
    	assert (stateRelation >= 0);
    	
    	/*
    	 * We depend on the user interface and model to work together to not
    	 * get this process started until a valid name is set in the channel name.
    	 */
    	String wellKnownName = getWellKnownName();
    	Log.i(TAG, "Well-known name: "+wellKnownName);
        Status status = mBus.requestName(wellKnownName, BusAttachment.ALLJOYN_REQUESTNAME_FLAG_DO_NOT_QUEUE
        		| BusAttachment.ALLJOYN_NAME_FLAG_ALLOW_REPLACEMENT
        		| BusAttachment.ALLJOYN_REQUESTNAME_FLAG_REPLACE_EXISTING);
        if (status == Status.OK) {
          	mHostChannelState = HostChannelState.NAMED;
        } else {
    		alljoynError(Module.USE, AllJoynError.CONNECT_ERROR, 
    				"Unable to acquire well-known name: (" + status + ")");
        }
    }
    
    /**
     * Implementation of the functionality related to releasing a well-known
     * name from an AllJoyn bus attachment.
     */
    private void doReleaseName() {
        Log.i(TAG, "doReleaseName()");
        
        /*
         * In order to release a name, the bus attachment must at least be
         * connected.
         */
    	assert(mBusAttachmentState == BusAttachmentState.CONNECTED || mBusAttachmentState == BusAttachmentState.DISCOVERING);
    	
    	/*
    	 * We need to progress monotonically down the hosted channel states
    	 * for sanity.
    	 */
    	assert(mHostChannelState == HostChannelState.NAMED);
    	
    	/*
    	 * We depend on the user interface and model to work together to not
    	 * change the name out from under us while we are running.
    	 */
    	String wellKnownName = getWellKnownName();

    	/*
    	 * There's not a lot we can do if the bus attachment refuses to release
    	 * the name.  It is not a fatal error, though, if it doesn't.  This is
    	 * because bus attachments can have multiple names.
    	 */
    	mBus.releaseName(wellKnownName);
    	mHostChannelState = HostChannelState.IDLE;
    }
    
    /**
     * Implementation of the functionality related to binding a session port
     * to an AllJoyn bus attachment.
     */
    private void doBindSession() {
        Log.i(TAG, "doBindSession()");
        
        Mutable.ShortValue contactPort = new Mutable.ShortValue(CONTACT_PORT);
        SessionOpts sessionOpts = new SessionOpts(SessionOpts.TRAFFIC_MESSAGES
        		, true, SessionOpts.PROXIMITY_ANY, SessionOpts.TRANSPORT_ANY);
        
        Status status = mBus.bindSessionPort(contactPort, sessionOpts, new SessionPortListener() {
            /**
             * This method is called when a client tries to join the session
             * we have bound.  It asks us if we want to accept the client into
             * our session.
             *
             * In the class documentation for the SessionPortListener note that
             * it is a requirement for this method to be multithread safe.
             * Since we never access any shared state, this requirement is met.
             */
        	public boolean acceptSessionJoiner(short sessionPort, String joiner, SessionOpts sessionOpts) {
                Log.i(TAG, "SessionPortListener.acceptSessionJoiner(" + sessionPort + ", " + joiner + ", " + sessionOpts.toString() + ")");
        	
                /*
        		 * Accept anyone who can get our contact port correct.
        		 */
        		if (sessionPort == CONTACT_PORT) {
        			
        			
        			int connected_clients = connectedClientCount();
        			
        			if(m_max_client_count==-1 || connected_clients < m_max_client_count) {
        				
        				return true;
        				
        			}
        		}
        		return false;
            }
            
            /**
             * If we return true in acceptSessionJoiner, we admit a new client
             * into our session.  The session does not really exist until a 
             * client joins, at which time the session is created and a session
             * ID is assigned.  This method communicates to us that this event
             * has happened, and provides the new session ID for us to use.
             *
             * In the class documentation for the SessionPortListener note that
             * it is a requirement for this method to be multithread safe.
             * Since we never access any shared state, this requirement is met.
             * 
             * See comments in joinSession for why the hosted chat interface is
             * created here. 
             */
            public void sessionJoined(short sessionPort, int id, String joiner) {
                Log.i(TAG, "SessionPortListener.sessionJoined(" + sessionPort + ", " + id + ", " + joiner + ")");
                mHostSessionId = id;
                SignalEmitter emitter = new SignalEmitter(m_network_service, id
                		, SignalEmitter.GlobalBroadcast.Off);
                mHostChatInterface = emitter.getInterface(AlljoynInterface.class);
            }             
        });
        
        if (status == Status.OK) {
        	mHostChannelState = HostChannelState.BOUND;
        } else {
    		alljoynError(Module.HOST, AllJoynError.CONNECT_ERROR,
    				"Unable to bind session contact port: (" + status + ")");
        	return;
        }
    }
    
    /**
     * Implementation of the functionality related to un-binding a session port
     * from an AllJoyn bus attachment.
     */
    private void doUnbindSession() {
        Log.i(TAG, "doUnbindSession()");
        
        /*
         * There's not a lot we can do if the bus attachment refuses to unbind
         * our port.
         */
     	mBus.unbindSessionPort(CONTACT_PORT);
        mHostChatInterface = null;
     	mHostChannelState = HostChannelState.NAMED;
    }
    
    /**
     * The session identifier of the "host" session that the application
     * provides for remote devices.  Set to -1 if not connected.
     */
    int mHostSessionId = -1;
    
    /**
     * A flag indicating that the application has joined a chat channel that
     * it is hosting.  See the long comment in doJoinSession() for a
     * description of this rather non-intuitively complicated case.
     */
    boolean mJoinedToSelf = false;
    
    /**
     * This is the interface over which the chat messages will be sent in
     * the case where the application is joined to a chat channel hosted
     * by the application.  See the long comment in doJoinSession() for a
     * description of this rather non-intuitively complicated case.
     */
    AlljoynInterface mHostChatInterface = null;
    
    /**
     * Implementation of the functionality related to advertising a service on
     * an AllJoyn bus attachment.
     */
    private void doAdvertise() {
        Log.i(TAG, "doAdvertise()");
        
       	/*
    	 * We depend on the user interface and model to work together to not
    	 * change the name out from under us while we are running.
    	 */
    	String wellKnownName = getWellKnownName();        
        Status status = mBus.advertiseName(wellKnownName, SessionOpts.TRANSPORT_ANY);
        
        if (status == Status.OK) {
        	mHostChannelState = HostChannelState.ADVERTISED;
        } else {
    		alljoynError(Module.HOST, AllJoynError.ADVERTISE_ERROR,
    				"Unable to advertise well-known name: (" + status + ")");
        }
    }
    
    /**
     * Implementation of the functionality related to canceling an advertisement
     * on an AllJoyn bus attachment.
     */
    private void doCancelAdvertise() {
        Log.i(TAG, "doCancelAdvertise()");
        
       	/*
    	 * We depend on the user interface and model to work together to not
    	 * change the name out from under us while we are running.
    	 */
    	String wellKnownName = getWellKnownName();        
        Status status = mBus.cancelAdvertiseName(wellKnownName, SessionOpts.TRANSPORT_ANY);
        
        if (status != Status.OK) {
    		alljoynError(Module.HOST, AllJoynError.ADVERTISE_CANCEL_ERROR,
    				"Unable to cancel advertisement of well-known name: (" + status + ")");
        	return;
        }
        
        /*
         * There's not a lot we can do if the bus attachment refuses to cancel
         * our advertisement, so we don't bother to even get the status.
         */
     	mHostChannelState = HostChannelState.BOUND;
    }
    
    /**
     * Implementation of the functionality related to joining an existing
     * local or remote session.
     */
    private void doJoinSession() {
        Log.i(TAG, "doJoinSession()");
        
        /*
         * There is a relatively non-intuitive behavior of multipoint sessions
         * that one needs to grok in order to understand the code below.  The
         * important thing to uderstand is that there can be only one endpoint
         * for a multipoint session in a particular bus attachment.  This
         * endpoint can be created explicitly by a call to joinSession() or
         * implicitly by a call to bindSessionPort().  An attempt to call
         * joinSession() on a session port we have created with bindSessionPort()
         * will result in an error.
         * 
         * When we call bindSessionPort(), we do an implicit joinSession() and
         * thus signals (which correspond to our chat messages) will begin to
         * flow from the hosted chat channel as soon as we begin to host a
         * corresponding session.
         * 
         * To achieve sane user interface behavior, we need to block those
         * signals from the implicit join done by the bind until our user joins
         * the bound chat channel.  If we do not do this, the chat messages
         * from the chat channel hosted by the application will appear in the
         * chat channel joined by the application.
         *
         * Since the messages flow automatically, we can accomplish this by
         * turning a filter on and off in the chat signal handler.  So if we
         * detect that we are hosting a channel, and we find that we want to
         * join the hosted channel we turn the filter off.
         * 
         * We also need to be able to send chat messages to the hosted channel.
         * This means we need to point the mChatInterface at the session ID of
         * the hosted session.  There is another complexity here since the
         * hosted session doesn't exist until a remote session has joined.
         * This means that we don't have a session ID to use to create a
         * SignalEmitter until a remote device does a joinSession on our
         * hosted session.  This, in turn, means that we have to create the
         * SignalEmitter after we get a sessionJoined() callback in the 
         * SessionPortListener passed into bindSessionPort().  We chose to
         * create the signal emitter for this case in the sessionJoined()
         * callback itself.  Note that this hosted channel signal emitter
         * must be distinct from one constructed for the usual joinSession
         * since a hosted channel may have a remote device do a join at any
         * time, even when we are joined to another session.  If they were
         * not separated, a remote join on the hosted session could redirect
         * messages from the joined session unexpectedly.
         * 
         * So, to summarize, these next few lines handle a relatively complex
         * case.  When we host a chat channel, we do a bindSessionPort which
         * *enables* the creation of a session.  As soon as a remote device
         * joins the hosted chat channel, a session is actually created, and
         * the SessionPortListener sessionJoined() callback is fired.  At that
         * point, we create a separate SignalEmitter using the hosted session's
         * sessionId that we can use to send chat messages to the channel we
         * are hosting.  As soon as the session comes up, we begin receiving
         * chat messages from the session, so we need to filter them until the
         * user joins the hosted chat channel.  In a separate timeline, the
         * user can decide to join the chat channel she is hosting.  She can
         * do so either before or after the corresponding session has been
         * created as a result of a remote device joining the hosted session. 
         * If she joins the hosted channel before the underlying session is
         * created, her chat messages will be discarded.  If she does so after
         * the underlying session is created, there will be a session emitter
         * waiting to use to send chat messages.  In either case, the signal
         * filter will be turned off in order to listen to remote chat
         * messages.
         */
        
       	/*
    	 * We depend on the user interface and model to work together to provide
    	 * a reasonable name.
    	 */
    	final String wellKnownNameToJoin = getWellKnownNameToJoin();
    	
        if (mHostChannelState != HostChannelState.IDLE) {
        	if (wellKnownNameToJoin.equals(getWellKnownName())) {              
             	mUseChannelState = UseChannelState.JOINED;
        		mJoinedToSelf = true;
        		handleClientJoined(getUniqueName(), getWellKnownName());
        		Log.d(TAG, "JoinSession: we are joined to ourself");
                return;
        	}
        }
        
        /*
         * Since we can act as the host of a channel, we know what the other
         * side is expecting to see.
         */
    	short contactPort = CONTACT_PORT;
        SessionOpts sessionOpts = new SessionOpts(SessionOpts.TRAFFIC_MESSAGES
        		, true, SessionOpts.PROXIMITY_ANY, SessionOpts.TRANSPORT_ANY);
        Mutable.IntegerValue sessionId = new Mutable.IntegerValue();
        
        Status status = mBus.joinSession(wellKnownNameToJoin, contactPort, sessionId
        		, sessionOpts, new SessionListener() {
            /**
             * This method is called when the last remote participant in the 
             * chat session leaves for some reason and we no longer have anyone
             * to chat with.
             *
             * In the class documentation for the BusListener note that it is a
             * requirement for this method to be multithread safe.  This is
             * accomplished by the use of a monitor on the ChatApplication as
             * exemplified by the synchronized attribute of the removeFoundChannel
             * method there.
             */
            public void sessionLost(int sessionId) {
                Log.i(TAG, "BusListener.sessionLost(" + sessionId + ")");
        		alljoynError(Module.USE, AllJoynError.JOIN_SESSION_ERROR, 
        				"The session has been lost");
             	mUseChannelState = UseChannelState.IDLE;
            }
            
            public void sessionMemberAdded(int sessionId, String uniqueName) {
            	handleClientJoined(uniqueName, null);
            }
            
            public void sessionMemberRemoved(int sessionId, String uniqueName) {
            	handleClientLeft(uniqueName);
            }
        });
        
        if (status == Status.OK) {
            Log.i(TAG, "doJoinSession(): use sessionId is " + mUseSessionId);
        	mUseSessionId = sessionId.value;
        } else {
    		alljoynError(Module.USE, AllJoynError.JOIN_SESSION_ERROR, 
    				"Unable to join session: (" + status + ")");
        	return;
        }
        
        SignalEmitter emitter = new SignalEmitter(m_network_service, wellKnownNameToJoin, 
        		mUseSessionId, SignalEmitter.GlobalBroadcast.Off);
        mChatInterface = emitter.getInterface(AlljoynInterface.class);
        
        //now that we joined the session we must let the server know which 
        //well-known name we have
        mChatInterface.clientInfoToServer(getWellKnownName());
        
     	mUseChannelState = UseChannelState.JOINED;
    }
    
    /**
     * This is the interface over which the chat messages will be sent.
     */
    AlljoynInterface mChatInterface = null;
    
    /**
     * Implementation of the functionality related to joining an existing
     * remote session.
     */
    private void doLeaveSession() {
        Log.i(TAG, "doLeaveSession()");
        if (mJoinedToSelf == false) {
        	mBus.leaveSession(mUseSessionId);
        } else {
        	handleClientLeft(getUniqueName());
        }
        mUseSessionId = -1;
        mJoinedToSelf = false;
     	mUseChannelState = UseChannelState.IDLE;
    }
    
    private void doExit() {
    	Log.i(TAG, "doExit()");
    	mBus.unregisterBusObject((BusObject)m_network_service);
    	mBus=null;
    }
    
    private synchronized void handleClientJoined(String unique_name
    		, String well_known_name) {
    	if(unique_name != null) {
    		Log.i(TAG, "Client joined: "+unique_name);

    		//check if already added
    		boolean exists=false;
    		for(ConnectedClient client : m_connected_clients) {
    			if(client.unique_id.equals(unique_name)) {
    				exists=true;
    				if(well_known_name != null && client.well_known_name==null)
    					client.well_known_name = well_known_name;
    			}
    		}
    		if(!exists) {
    			ConnectedClient c=new ConnectedClient();
    			c.well_known_name = well_known_name;
    			c.unique_id = unique_name;
    			m_connected_clients.add(c);
    			
    			sendEventToListeners(HANDLE_CLIENT_JOINED);
    		}
    		
    	} else {
    		Log.e(TAG, "handleClientJoined: unique_name is NULL!");
    	}
    }
    private synchronized void handleClientLeft(String unique_name) {
    	if(unique_name != null) {
    		Log.i(TAG, "Client left: "+unique_name);

			for(int i=0; i<m_connected_clients.size(); ++i) {
				if(m_connected_clients.get(i).unique_id.equals(unique_name)) {
					m_connected_clients.remove(i);
				}
			}

    		sendEventToListeners(HANDLE_CLIENT_LEFT);
    	} else {
    		Log.e(TAG, "handleClientLeft: unique_name is NULL!");
    	}
    }
    
    /**
     * The session identifier of the "use" session that the application
     * uses to talk to remote instances.  Set to -1 if not connectecd.
     */
    int mUseSessionId = -1;
    

    /**
     * Our chat messages are going to be Bus Signals multicast out onto an 
     * associated session.  In order to send signals, we need to define an
     * AllJoyn bus object that will allow us to instantiate a signal emmiter.
     */
    class NetworkService implements AlljoynInterface, BusObject {
    	/**                                                                                                                          
         * Intentionally empty implementation of Chat method.  Since this
         * method is only used as a signal emitter, it will never be called
         * directly.
	     */
		@BusSignal
		public void sensorUpdate(int ack_seq_num, double pos_x, double pos_y)
				throws BusException { }
		
		@BusSignal
		public void ack(int ack_seq_num) throws BusException { }
		
		@BusSignal
		public void gameCommand(byte[] data) throws BusException { }

		@BusSignal
		public void clientInfoToServer(String well_known_name) { }

		@BusSignal
		public void clientInfoToClients(String unique_name,
				String well_known_name) { }     
    }

    /**
     * The ChatService is the instance of an AllJoyn interface that is exported
     * on the bus and allows us to send signals implementing messages
     */
    private NetworkService m_network_service;

    /**
     * The signal handler for messages received from the AllJoyn bus.
     * 
     * Since the messages sent on a chat channel will be sent using a bus
     * signal, we need to provide a signal handler to receive those signals.
     * This is it.  Note that the name of the signal handler has the first
     * letter capitalized to conform with the DBus convention for signal 
     * handler names.
     */
    @BusSignalHandler(iface = "com.android.game.clash_of_the_balls.alljoyn", signal = "sensorUpdate")
    public void sensorUpdate(int ack_seq_num, double pos_x, double pos_y) {
    	
        /*
    	 * The only time we allow a signal from the hosted session ID to pass
    	 * through is if we are in mJoinedToSelf state.  If the source of the
    	 * signal is us, we also filter out the signal since we are going to
    	 * locally echo the signal.
     	 */
    	String uniqueName = mBus.getUniqueName();
    	MessageContext ctx = mBus.getMessageContext();
        
         // Always drop our own signals which may be echoed back from the system.
        if (ctx.sender.equals(uniqueName)) {
            Log.i(TAG, "Chat(): dropped our own signal received on session " + ctx.sessionId);
    		return;
    	}
        
        receivedSensorUpdate(ctx.sender, ack_seq_num, pos_x, pos_y);
    }
    private void receivedSensorUpdate(String sender, int ack_seq_num, double pos_x, double pos_y) {
        NetworkData data = new NetworkData();
        data.ack_num = ack_seq_num;
        data.arg1 = new Vector((float)pos_x, (float)pos_y);
        data.sender = sender;
        m_sensor_updates.add(data);
        sendEventToListeners(HANDLE_RECEIVED_SIGNAL);
    }
    @BusSignalHandler(iface = "com.android.game.clash_of_the_balls.alljoyn", signal = "ack")
    public void ack(int ack_seq_num) {
    	
        /*
    	 * The only time we allow a signal from the hosted session ID to pass
    	 * through is if we are in mJoinedToSelf state.  If the source of the
    	 * signal is us, we also filter out the signal since we are going to
    	 * locally echo the signal.
     	 */
    	String uniqueName = mBus.getUniqueName();
    	MessageContext ctx = mBus.getMessageContext();
        
         // Always drop our own signals which may be echoed back from the system.
        if (ctx.sender.equals(uniqueName)) {
            Log.i(TAG, "Chat(): dropped our own signal received on session " + ctx.sessionId);
    		return;
    	}
        
        receivedAck(ctx.sender, ack_seq_num);
    }
    private void receivedAck(String sender, int ack_seq_num) {
        NetworkData data = new NetworkData();
        data.ack_num = ack_seq_num;
        data.sender = sender;
        m_acks.add(data);
        sendEventToListeners(HANDLE_RECEIVED_SIGNAL);
    }
    @BusSignalHandler(iface = "com.android.game.clash_of_the_balls.alljoyn", signal = "gameCommand")
    public void gameCommand(byte[] data) {
    	
        /*
    	 * The only time we allow a signal from the hosted session ID to pass
    	 * through is if we are in mJoinedToSelf state.  If the source of the
    	 * signal is us, we also filter out the signal since we are going to
    	 * locally echo the signal.
     	 */
    	String uniqueName = mBus.getUniqueName();
    	MessageContext ctx = mBus.getMessageContext();
        
         // Always drop our own signals which may be echoed back from the system.
        if (ctx.sender.equals(uniqueName)) {
            Log.i(TAG, "Chat(): dropped our own signal received on session " + ctx.sessionId);
    		return;
    	}

         // Drop signals on the hosted session unless we are joined-to-self.
        if (mJoinedToSelf == false && ctx.sessionId == mHostSessionId) {
            Log.i(TAG, "Chat(): dropped signal received on hosted session " + ctx.sessionId + " when not joined-to-self");
    		return;
    	}
        
        receivedGameCommand(ctx.sender, data);
    }
    private void receivedGameCommand(String sender, byte[] game_data) {
        NetworkData data = new NetworkData();
        data.data = game_data;
        data.sender = sender;
        m_game_commands.add(data);
        sendEventToListeners(HANDLE_RECEIVED_SIGNAL);
    }
    
    @BusSignalHandler(iface = "com.android.game.clash_of_the_balls.alljoyn", signal = "clientInfoToServer")
	public void clientInfoToServer(String well_known_name) {
    	
    	String uniqueName = mBus.getUniqueName();
    	MessageContext ctx = mBus.getMessageContext();
        
         // Always drop our own signals which may be echoed back from the system.
        if (ctx.sender.equals(uniqueName)) {
            Log.i(TAG, "Chat(): dropped our own signal received on session " + ctx.sessionId);
    		return;
    	}
        
        Log.i(TAG, "got client info: unique name="+ctx.sender
        		+", well-known name="+well_known_name);
        
        handleClientJoined(ctx.sender, well_known_name);
        // send update to all clients
        synchronized (this) {
        	if (mHostChatInterface != null) {
        		for(int i=0; i<m_connected_clients.size(); ++i) {
        			ConnectedClient c = m_connected_clients.get(i);
        			if(c.unique_id!=null && c.well_known_name!=null)
        				mHostChatInterface.clientInfoToClients(c.unique_id, c.well_known_name);
        		}
        	}
		}
    }

    @BusSignalHandler(iface = "com.android.game.clash_of_the_balls.alljoyn", signal = "clientInfoToClients")
	public void clientInfoToClients(String unique_name,
			String well_known_name) {
    	
    	String uniqueName = mBus.getUniqueName();
    	MessageContext ctx = mBus.getMessageContext();
        
         // Always drop our own signals which may be echoed back from the system.
        if (ctx.sender.equals(uniqueName)) {
            Log.i(TAG, "Chat(): dropped our own signal received on session " + ctx.sessionId);
    		return;
    	}
         // Drop signals on the hosted session unless we are joined-to-self.
        if (mJoinedToSelf == false && ctx.sessionId == mHostSessionId) {
            Log.i(TAG, "Chat(): dropped signal received on hosted session " + ctx.sessionId + " when not joined-to-self");
    		return;
    	}
        
        Log.i(TAG, "got info from server: unique name="+unique_name
        		+", well-known name="+well_known_name);
        
        handleClientJoined(unique_name, well_known_name);
    }   
    
    
    /* error handling */
	/**
	 * Enumeration of the high-level moudules in the system.  There is one
	 * value per module.
	 */
	public static enum Module {
		NONE,
		GENERAL,
		USE,
		HOST
	}
	public static enum AllJoynError {
		CONNECT_ERROR,
		START_DISCOVERY_ERROR,
		ADVERTISE_ERROR,
		ADVERTISE_CANCEL_ERROR,
		JOIN_SESSION_ERROR,
		SEND_ERROR,
		BUS_EXCEPTION
	}
	public static class AllJoynErrorData {
		public Module module;
		public AllJoynError error;
		public String error_string;
	}
	
	private void alljoynError(Module m, AllJoynError e, String s) {
		Log.e("AllJoyn", s);
		AllJoynErrorData error = new AllJoynErrorData();
		error.module = m;
		error.error = e;
		error.error_string = s;
		m_error = error;
		
		sendEventToListeners(HANDLE_ERROR);
	}
    
    /*
     * Load the native alljoyn_java library.  The actual AllJoyn code is
     * written in C++ and the alljoyn_java library provides the language
     * bindings from Java to C++ and vice versa.
     */
    static {
        Log.i(TAG, "System.loadLibrary(\"alljoyn_java\")");
        System.loadLibrary("alljoyn_java");
    }
    
    
}
