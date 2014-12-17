package info.shangma.voicecontrolrobot;

import java.util.Timer;
import java.util.TimerTask;

import root.gast.speech.tts.TextToSpeechUtils;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

public class AcknowledgementPresentActivity extends Activity implements OnInitListener {
	private final static String TAG = "AcknowledgementPresentActivity";
	
	private TextToSpeech mTTS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_acknowledgement);
		
		mTTS = new TextToSpeech(this, this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(TAG, "onPause");
		
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
			mTTS = null;
		}
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		
		prompt("Thank you! Good Day!");
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				AcknowledgementPresentActivity.this.finish();
			}
		}, 2000);
	}
	
	private void prompt(String speech) {
		mTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
	}

}
