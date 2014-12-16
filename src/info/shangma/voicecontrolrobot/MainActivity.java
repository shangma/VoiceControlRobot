package info.shangma.voicecontrolrobot;


import info.shangma.utils.string.Inflector;
import info.shangma.voicecontrolrobot.util.CommonUtil;
import info.shangma.voicecontrolrobot.util.ConnectToServerThread;
import info.shangma.voicecontrolrobot.util.ServerThread;
import info.shangma.voicecontrolrobot.util.SimpleHttpGetTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.parse.ParsePush;
import com.wowwee.robome.RoboMe;
import com.wowwee.robome.RoboMe.RoboMeListener;
import com.wowwee.robome.RoboMeCommands.IncomingRobotCommand;
import com.wowwee.robome.SensorStatus;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener, RoboMeListener {
	
	private static final String TAG= "Main Activity";
	private TextToSpeech mTTS;

	public static RoboMe roboMe;
	
	private static final String TABLET_BT_ADDRESS = "10:30:47:DB:3B:04";
	private static final String ONE_X_BT_ADDRESS = "A0:F4:50:6E:B5:63";
	private static final String NEXUS_FIVE = "50:55:27:60:5F:02";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate got called for MainActivity");

		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		if (CommonUtil.ISCLIENT == CommonUtil.ISFORROBOT) {

			setContentView(R.layout.robotface);
			roboMe = new RoboMe(this, this);
			
			makeDiscoverable();

		} else if (CommonUtil.ISCLIENT == CommonUtil.ISFORCLIENT) {
			setContentView(R.layout.activity_main);
		}

		
//		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//		StrictMode.setThreadPolicy(policy);
	
		mTTS = new TextToSpeech(this, this);
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
			
	        //---start the socket server---
			((Application) this.getApplicationContext()).serverThread = new ServerThread(((Application) this.getApplicationContext()).bluetoothAdapter);
			((Application) this.getApplicationContext()).serverThread.start();

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

	public void onClickStart(View view) {
		
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
					
					Toast.makeText(this, "Require connection", Toast.LENGTH_LONG);
					Log.d(TAG, "Connected to: " + device.getName());
				}
			}
		}
        
		Intent i = new Intent(this, SpeechRecognitionLauncher.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(i);
	}
	public void onStartService (View view) {
				
		startDetectionService();
	}
	
	public void onStopService (View view) {
		
		stopDetectionService();
	}
	
	public void onTestService (View view) {

		// test bluetooth
		// it is working
		((Application)this.getApplicationContext()).SendMessage(CommonUtil.MOVE_COMMAND);
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
    
}
