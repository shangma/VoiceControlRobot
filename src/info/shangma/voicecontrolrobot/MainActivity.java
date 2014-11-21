package info.shangma.voicecontrolrobot;


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
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
	
	private static final String TAG= "Main Activity From VR robot";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//		StrictMode.setThreadPolicy(policy);
	
	}

	
	public void onStartService (View view) {
				
		startDetectionService();
	}
	
	public void onStopService (View view) {
		
		stopDetectionService();
	}
	
	public void onTestService (View view) {
		Log.d(TAG, "test service");		
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
		stopDetectionService();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		startDetectionService();
	}
	
	
}
