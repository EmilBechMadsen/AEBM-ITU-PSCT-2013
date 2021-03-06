package dk.itu.psct.activityrecorder;

import java.security.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {
	SensorManager mSensorManager;
	Sensor mSensor;
	private boolean mInitialized;
	private float mLastX, mLastY, mLastZ;
	private final float NOISE = (float) 2.0;
	private LinkedList<Recording> recordingQueue;
	private LinkedList<Recording> transferQueue;
	private String recordingName = "";
	Runnable transmitter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    	mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	transferQueue = new LinkedList<Recording>();
    	recordingQueue = new LinkedList<Recording>();
    	
    	transmitter = new Runnable() {
			Boolean transferInitiated = false; 
			@Override
			public void run() {
				String previouslySent = "";
				int i = 0;
				while (true)
				{
					if (transferQueue.size() > 0)
					{
						transferInitiated = true;
						final Recording rec = transferQueue.poll();
						if (rec.getName() == previouslySent)
						{
							i++;
						} else {
							i = 1;
							previouslySent = rec.getName();
						}
						final int msgCount = i;
						runOnUiThread(new Runnable() {
							@Override
							public void run()
							{
								TextView message = (TextView) findViewById(R.id.errorMessage);
								message.setText("Transferring: " + rec.getName() + " - #" + msgCount);
							}
						});
						processRecording(rec);
					} else if (transferQueue.size() == 0 && transferInitiated)	// transfer is done.
					{
						runOnUiThread(new Runnable() {
							@Override
							public void run()
							{
								TextView message = (TextView) findViewById(R.id.errorMessage);
								message.setText("Transfer completed.");
							}
						});
						transferInitiated = false;
					}
				}
				
			}
		};
		new Thread(transmitter).start();
    	
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void sendClick(View view)
    {
		TextView message = (TextView) findViewById(R.id.errorMessage);
    	if (!recording && recordingQueue.size() > 0)
    	{
    		transferQueue.addAll(recordingQueue);
    		message.setText("Transfer initiated.");
    		recordingQueue.clear();
    	} else if (recording) 
    	{
    		message.setText("Still recording..");
    	} else if (recordingQueue.size() == 0)
    	{
    		message.setText("No recordings saved.");
    	}
    }
    
    public void deleteClick(View view)
    {
    	TextView message = (TextView) findViewById(R.id.errorMessage);
    	if (recordingQueue.size() > 0)
    	{
    		recordingQueue.clear();
    		message.setText("Recording(s) deleted.");
    	} else {
    		message.setText("No recordings saved.");
    	}
    }
    
    boolean recording;
    public void startRecording(View view) throws InterruptedException
    {
    	
    	TextView errorMessage = (TextView) findViewById(R.id.errorMessage);
    	Button btn = (Button) findViewById(R.id.button1);
    	if (!recording)
    	{
    		EditText nameLabel = (EditText) findViewById(R.id.nameField);
    		if (nameLabel.getText().length() == 0)
    		{
    			errorMessage.setText("Please specify a name");
    			return;
    		} else {
    			errorMessage.setText("Get ready to start on beep!");
    			recordingName = getCurrentTimeAsString() + " - " + nameLabel.getText().toString();
    			
    	    	Runnable r = new Runnable() {
    				
    				@Override
    				public void run() {
    					startRecording();
    				}
    			};
    			new Thread(r).start();
    		}

    	} else {
    		mSensorManager.unregisterListener(this);
    		recording = false;
    		errorMessage.setText("");
    		recordingName = "";
			btn.setText("Start recording");
    	}
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
	}
    
    @Override
	protected void onPause() {
    	super.onPause();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// nevermind in this demo
	}
	
	public void startRecording()
	{

		// Play a sound to indicate start of capture.
		try {
			Thread.sleep(5000);
	        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
	        r.play();
		} catch (Exception e1){
			e1.printStackTrace();
		}
		
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    	recording = true;
    	
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
		    	TextView errorMessage = (TextView) findViewById(R.id.errorMessage);
		    	Button btn = (Button) findViewById(R.id.button1);
		    	btn.setText("Stop recording");
				errorMessage.setText("Recording.. Name: "  + recordingName);	
			}
		});
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		TextView tvX= (TextView)findViewById(R.id.x_axis);

		TextView tvY= (TextView)findViewById(R.id.y_axis);

		TextView tvZ= (TextView)findViewById(R.id.z_axis);
		
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
		
			mInitialized = true;
		} else {
			float deltaX = Math.abs(mLastX - x);
			float deltaY = Math.abs(mLastY - y);
			float deltaZ = Math.abs(mLastZ - z);
			if (deltaX < NOISE) deltaX = (float)0.0;
			if (deltaY < NOISE) deltaY = (float)0.0;
			if (deltaZ < NOISE) deltaZ = (float)0.0;
			mLastX = x;
			mLastY = y;
			mLastZ = z;
		}
		
		addRecording(x, y, z);
		
		tvX.setText(mLastX + "");
		tvY.setText(mLastY + "");
		tvZ.setText(mLastZ + "");
	}
	
	void addRecording(float x, float y, float z)
	{
		recordingQueue.addLast(new Recording(recordingName, getCurrentTimeAsString(), x,y,z));
	}
	
	public String getCurrentTimeAsString()
	{
		Calendar c = Calendar.getInstance();
		int milisecond = c.get(Calendar.MILLISECOND);
		int second = c.get(Calendar.SECOND);
		int minute = c.get(Calendar.MINUTE);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		return hour+":"+minute+":"+second+"."+milisecond;
	}
	
	public void processRecording(Recording rec)
	{
		rec.sendRecording();
	}
    
}
