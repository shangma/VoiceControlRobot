package info.shangma.voicecontrolrobot;


import info.shangma.voicecontrolrobot.util.CommonUtil;
import info.shangma.voicecontrolrobot.util.ConnectToServerThread;
import info.shangma.voicecontrolrobot.util.ServerThread;

import java.io.IOException;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.wowwee.robome.RoboMe;
import com.wowwee.robome.RoboMe.RoboMeListener;
import com.wowwee.robome.RoboMeCommands.IncomingRobotCommand;
import com.wowwee.robome.SensorStatus;

public class MainActivity extends Activity implements OnInitListener, RoboMeListener {
	
	private static final String TAG= "Main Activity";
	private TextToSpeech mTTS;

	public static RoboMe roboMe;
	
	private static final String TABLET_BT_ADDRESS = "10:30:47:DB:3B:04";
	private static final String ONE_X_BT_ADDRESS = "A0:F4:50:6E:B5:63";
	private static final String NEXUS_FIVE = "50:55:27:60:5F:02";
	
	// navigation drawer
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate got called for MainActivity");

		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		//
		if (CommonUtil.ISCLIENT == CommonUtil.ISFORROBOT) {

			setContentView(R.layout.robotface);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			roboMe = new RoboMe(this, this);
			
			makeDiscoverable();

		} else if (CommonUtil.ISCLIENT == CommonUtil.ISFORCLIENT) {
			setContentView(R.layout.activity_main);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			
			//Navigation Drawer
			
	        mPlanetTitles = getResources().getStringArray(R.array.planets_array);
	        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	        mDrawerList = (ListView) findViewById(R.id.left_drawer);

	        // set a custom shadow that overlays the main content when the drawer opens
	        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
	        // set up the drawer's list view with items and click listener
	        mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, mPlanetTitles));
	        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
			
		}


        

		
//		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//		StrictMode.setThreadPolicy(policy);
	
		// tts for this one only, aka for "Hello"
		mTTS = new TextToSpeech(this, this);
		
		// bluetooth communication
		
		if (CommonUtil.ISCLIENT == CommonUtil.ISFORCLIENT) {
			
	        if (!enableBluetoothComm()) {
				speakWords("Bluetooth is not available.");
			}
	        
		} else if (CommonUtil.ISCLIENT == CommonUtil.ISFORROBOT) {
			
	        //---start the socket server---
			((Application) this.getApplicationContext()).serverThread = new ServerThread(((Application) this.getApplicationContext()).bluetoothAdapter);
			((Application) this.getApplicationContext()).serverThread.start();
		}

	}
	
	public boolean enableBluetoothComm() {
		
		//---if you are already talking to someone...---
        if (((Application)this.getApplicationContext()).connectToServerThread!=null) {
            try {
                //---close the connection first---
            	((Application)this.getApplicationContext()).connectToServerThread.bluetoothSocket.close();
            } catch (IOException e) {
            	Log.d("MainActivity", e.getLocalizedMessage());            	
            }
        }
        
        //---connect to the selected Bluetooth device---
		Set<BluetoothDevice> pairedDevices = ((Application)this.getApplicationContext()).bluetoothAdapter.getBondedDevices();
        Log.d(TAG, "the size of paired: " + pairedDevices.size());
        if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				Log.d(TAG, "name: " + device.getName() + " | Address: " + device.getAddress()); 
				if (device.getAddress().equals(NEXUS_FIVE)) {
					
					((Application)this.getApplicationContext()).connectToServerThread = new 
				            ConnectToServerThread(device, ((Application)this.getApplicationContext()).bluetoothAdapter);
					((Application)this.getApplicationContext()).connectToServerThread.start();
					
					Log.d(TAG, "Connected to: " + device.getName());
					return true;
				}
			}
		}
        return false;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(TAG, "onPause got called for MainActivity");
		
		stopDetectionService();
		
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
		
		if (CommonUtil.ISCLIENT == CommonUtil.ISFORROBOT) {
			roboMe.stopListening();
			
			((Application)this.getApplicationContext()).bluetoothAdapter.cancelDiscovery();
		}
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "onResume got called for MainActivity");

		if (CommonUtil.ISCLIENT == CommonUtil.ISFORROBOT) {
			roboMe.setVolume(12);
			roboMe.startListening();

		} else if (CommonUtil.ISCLIENT == CommonUtil.ISFORCLIENT) {
			// startDetectionService();
//			((Application) this.getApplicationContext()).DiscoverDevices();
		}
	}
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "onDestroy got called for MainActivity");

		if (CommonUtil.ISCLIENT == CommonUtil.ISFORCLIENT) {
			
			if (((Application)this.getApplicationContext()).discoverDevicesReceiver != null) {
	        	try {
	            	unregisterReceiver(((Application)this.getApplicationContext()).discoverDevicesReceiver);        		
	        	} catch(Exception e) {
	        		
	        	}
	        }
	                
	        //---if you are currently connected to someone...---
	        if (((Application)this.getApplicationContext()).connectToServerThread!=null) {            
	            try {
	                //---close the connection---
	            	((Application)this.getApplicationContext()).connectToServerThread.bluetoothSocket.close();
	            } catch (IOException e) {
	            	Log.d("MainActivity", e.getLocalizedMessage());
	            }
	        }  
	        
		} else if (CommonUtil.ISCLIENT == CommonUtil.ISFORROBOT) {
			
			//---stop the thread running---
	        if (((Application)this.getApplicationContext()).serverThread!=null) ((Application)this.getApplicationContext()).serverThread.cancel();
		}
	}

	public void makeDiscoverable() {
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300); 
        startActivity(i);
	}
	
	private void startDetectionService() {
		Intent intent = DetectionService.makeStartServiceIntent(this);
		startService(intent);
	}
	
	private void stopDetectionService() {
		Intent intent = DetectionService.makeStopServiceIntent(this);
		startService(intent);
	}
	
	private boolean isNetworkConnectionAvailable() {  
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo info = cm.getActiveNetworkInfo();     
	    if (info == null) return false;
	    State network = info.getState();
	    return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
	}
	
	private void speakWords(String speech) {
		mTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
	}

	@Override
	public void onInit(int arg0) {
		// TODO Auto-generated method stub
		
		// check network availability
		if (!isNetworkConnectionAvailable()) {
			speakWords("Network is not available! Please check it");
			stopDetectionService();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MainActivity.this.finish();
		}
	}
	
	// robot callback

	@Override
	public void commandReceived(IncomingRobotCommand command) {
		// TODO Auto-generated method stub
		Log.d("RoboMe","Received event " + command);
		
		if(command.isSensorStatus()){
			SensorStatus status = command.readSensorStatus();
			Log.d("RoboMe",String.format("Edge: %b Chest 20cm: %b 50cm: %b 100cm: %b", status.edge, status.chest_20cm, status.chest_50cm, status.chest_100cm)); 
		}
	}

	@Override
	public void headsetPluggedIn() {
		// TODO Auto-generated method stub
		Log.d("RoboMe", "Headset plugged in");
	}

	@Override
	public void headsetUnplugged() {
		// TODO Auto-generated method stub
		Log.d("RoboMe", "Headset unplugged");
	}

	@Override
	public void roboMeConnected() {
		// TODO Auto-generated method stub
		Log.d("RoboMe", "RoboMe Connected");
	}

	@Override
	public void roboMeDisconnected() {
		// TODO Auto-generated method stub
		Log.d("RoboMe", "RoboMe Disconnected");
	}

	@Override
	public void volumeChanged(float volume) {
		// TODO Auto-generated method stub
		Log.d("RoboMe", "Volume changed to " + volume + "%");
	} 
	
    
    static Handler UIupdater = new Handler() {
        @Override
        public void handleMessage(Message msg) {              
            int numOfBytesReceived = msg.arg1;
            byte[] buffer = (byte[]) msg.obj;
            //---convert the entire byte array to string---
            String strReceived = new String(buffer);
            //---extract only the actual string received---
            strReceived = strReceived.substring(
                0, numOfBytesReceived);
            //---display the text received on the TextView---  
        }
    };
    
    // Navigation Drawer
    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
            selectItem(position);
		}
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
    	Log.d(TAG, "Item being clicked: " + position);
    	switch (position) {
		case 0:
			if (!isNetworkConnectionAvailable()) {
				speakWords("Network is not available.");
			} else {
				speakWords("Network is working.");
			}
			break;
		case 1:
			if (!enableBluetoothComm()) {
				speakWords("Bluetooth is not available.");
			} else {
				speakWords("Bluetooth is working.");
			}
			break;
		case 2:
			testService();
			break;
		default:
			break;
		}
    }
    
	public void onClickStart(View view) {        
		Intent i = new Intent(this, SpeechRecognitionLauncher.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(i);
	}
	private void forceStartService() {
				
		startDetectionService();
	}
	
	private void forceStopService() {
		
		stopDetectionService();
	}
	
	private void testService() {

		// test bluetooth
		((Application)this.getApplicationContext()).SendMessage(CommonUtil.MOVE_COMMAND);
	}
}
