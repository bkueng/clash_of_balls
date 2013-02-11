package com.sapos_aplastados.game.clash_of_balls.game;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * this thread gets the sensor data
 * it can be calibrated to use a specific vector as zero vector
 * this specific vector is the average over a certain time period
 *
 */
public class SensorThread implements SensorEventListener {
	HandlerThread m_handler_thread;
	
	private static final float output_scaling = 0.5f;
	
    private Vector3D m_calib_vec; //calibration vector
    private volatile Vector3D m_cur_vec; //is accessed from different threads!
    private Vector3D m_cur_vec_tmp;
    private final Vector3D m_z_axis = new Vector3D(0.f, 0.f, -1.f);
    private volatile boolean m_bIs_calibrating;
    
    Context m_context;
    SensorManager m_sensor_manager;
    
    public SensorThread(Context context) {
    	
    	m_context = context;
    	
    }
    
    public void stopThread() {
    	if(m_sensor_manager!=null) m_sensor_manager.unregisterListener(this);
    	if(m_handler_thread!=null) m_handler_thread.quit();
    }

    public void startThread() {
        m_calib_vec = new Vector3D(0.f, 0.f, -1.f);
        m_cur_vec = new Vector3D();
        m_cur_vec_tmp = new Vector3D();
        
        
        m_handler_thread = new HandlerThread("SensorThread");
        m_handler_thread.start();

        Handler handler = new Handler(m_handler_thread.getLooper());

        m_sensor_manager=(SensorManager)m_context.getSystemService(
    			Context.SENSOR_SERVICE);

        Sensor sensor = m_sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_sensor_manager.registerListener(this, sensor,
                            SensorManager.SENSOR_DELAY_GAME, handler);
        
    }
    
    public void calibrate() {
    	m_calib_vec.set(0.f, 0.f, -0.1f);
    	m_bIs_calibrating = true;
    }
    
    public void stopCalibrate() {
    	if(!m_bIs_calibrating) return;
    	synchronized (this) {
    		m_calib_vec.normalize();
        	m_bIs_calibrating = false;
		}
    	
    }
    public boolean isCalibrating() {
    	return m_bIs_calibrating;
    }
    
    private Vector3D m_tmp_vec_get1=new Vector3D();
    private Vector3D m_tmp_vec_get2=new Vector3D();
    
    public void getCurrentVector(Vector result) {
    	//rotate & project vector to xy plane
    	float angle = m_calib_vec.angle(m_z_axis);
    	Vector3D v_n = m_tmp_vec_get1;
    	v_n.set(m_calib_vec);
    	v_n.cross(m_z_axis); //normal vector
    	
    	Vector3D v = m_tmp_vec_get2;
    	v.set(m_cur_vec);
    	v.rotate(v_n, angle);
    	
    	//rotate because we use portrait format, and another coord system
    	result.set(-v.y * output_scaling, v.x * output_scaling);
    }
    
    
    
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	public void onSensorChanged(SensorEvent event) {
		if(m_bIs_calibrating) {
	    	synchronized (this) {
	    		if(m_bIs_calibrating) {
	    			m_calib_vec.add(-event.values[0], -event.values[1], -event.values[2]);
	    		}
	    	}
		} else {
			m_cur_vec_tmp.set(-event.values[0], -event.values[1], -event.values[2]);
			Vector3D v = m_cur_vec;
			m_cur_vec = m_cur_vec_tmp;
			m_cur_vec_tmp = v;
		}
	}
    
    
}
