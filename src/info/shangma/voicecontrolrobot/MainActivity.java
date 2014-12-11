package info.shangma.voicecontrolrobot;


import info.shangma.utils.string.Inflector;
import info.shangma.voicecontrolrobot.util.CommonUtil;
import info.shangma.voicecontrolrobot.util.SimpleHttpGetTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity implements OnInitListener, RoboMeListener {
	
	private static final String TAG= "Main Activity From VR robot";
	private TextToSpeech mTTS;

	public static RoboMe roboMe;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (CommonUtil.ISCLIENT == 1) {

			setContentView(R.layout.robotface);
			roboMe = new RoboMe(this, this);

		} else if (CommonUtil.ISCLIENT == 0) {
			setContentView(R.layout.activity_main);

		}

		
//		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//		StrictMode.setThreadPolicy(policy);
	
		mTTS = new TextToSpeech(this, this);
	}

	private void startDetectionService() {
		Intent intent = new Intent(MainActivity.this, DetectionService.class);
		startService(intent);
	}
	
	private void stopDetectionService() {
		Intent intent = new Intent(MainActivity.this, DetectionService.class);
		intent.putExtra(DetectionService.ACTIVATION_STOP_INTENT_KEY, true);
		startService(intent);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		stopDetectionService();
		
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
		
		if (CommonUtil.ISCLIENT == 1) {
			roboMe.stopListening();
		}
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "onResume got called for MainActivity");
		
		if (CommonUtil.ISCLIENT == 1) {
			roboMe.setVolume(12);
			roboMe.startListening();
		} else if (CommonUtil.ISCLIENT == 0) {
			startDetectionService();
		}

	}
	
	
	public void onStartService (View view) {
				
		startDetectionService();
	}
	
	public void onStopService (View view) {
		
		stopDetectionService();
	}
	
	public void onTestService (View view) {
//		ParsePush push = new ParsePush();
//		push.setChannel("ActivateRobot");
//		push.setMessage("Got the Notfication.");
//		push.sendInBackground();;
		
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
	
	
	
	
}
