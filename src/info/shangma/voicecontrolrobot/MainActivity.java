package info.shangma.voicecontrolrobot;


import info.shangma.utils.string.Inflector;
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

public class MainActivity extends Activity implements OnInitListener {
	
	private static final String TAG= "Main Activity From VR robot";
	private TextToSpeech mTTS;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		startDetectionService();
	}
	
	
	public void onStartService (View view) {
				
		startDetectionService();
	}
	
	public void onStopService (View view) {
		
		stopDetectionService();
	}
	
	public void onTestService (View view) {
		Log.d(TAG, "test service");
		Inflector mInflector = Inflector.getInstance();
		Log.d(TAG, mInflector.singularize(new String("tax")));
		
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
	
	
	
	
}
