package info.shangma.voicecontrolrobot;

import info.shangma.speech.SpeechRecognizingAndSpeakingActivity;
import info.shangma.speech.tts.TextToSpeechUtils;
import info.shangma.speech.voiceaction.AbstractVoiceAction;
import info.shangma.speech.voiceaction.MultiCommandVoiceAction;
import info.shangma.speech.voiceaction.VoiceAction;
import info.shangma.speech.voiceaction.VoiceActionCommand;
import info.shangma.speech.voiceaction.VoiceActionExecutor;
import info.shangma.speech.voiceaction.WhyNotUnderstoodListener;
import info.shangma.voicecontrolrobot.command.CancelCommand;
import info.shangma.voicecontrolrobot.command.ProductLookup;
import info.shangma.voicecontrolrobot.command.SecondOfferNo;
import info.shangma.voicecontrolrobot.command.SecondOfferYes;
import info.shangma.voicecontrolrobot.util.AppState;
import info.shangma.voicecontrolrobot.util.SoundPoolPlayerEx;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.alchemyapi.api.AlchemyAPI;

/**
 * @author Shang Ma
 *
 * www.shangma.info
 */

public class SpeechRecognitionLauncher extends SpeechRecognizingAndSpeakingActivity {
	private static final String TAG = "SpeechRecognitionLauncher";

	private static final String ON_DONE_PROMPT_TTS_PARAM = "ON_DONE_PROMPT";

	private VoiceActionExecutor executor;

	private VoiceAction lookupVoiceAction;
//	private TextView log;

	private long prevFailureTimeStamp = -1L;
	private long currentFailureTimeStamp = -1L;
	private int failureRetry;
	private int TIME_INTERVAL_REPEATABLE = 8; // ms
	
	private SoundPoolPlayerEx mSoundPlayer; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher_voice_recognition);
 
//		log = (TextView) findViewById(R.id.tv_resultlog);
		mSoundPlayer = new SoundPoolPlayerEx(this);
		
		initDialog();		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		Log.i(TAG, "finish initialization");
	}

	private void initDialog() {
		if (executor == null) {
			executor = new VoiceActionExecutor(this);
		}
		lookupVoiceAction = makeLookup();
		executor.setSoundPlayer(mSoundPlayer);
	}

//	public void clearLog() {
//		log.setText("");
//	}
//
//	public void setLog(String theLog) {
//		log.setText(theLog);
//	}
//	
//	private void appendToLog(String appendThis) {
//		String currentLog = log.getText().toString();
//		currentLog = currentLog + "\n" + appendThis;
//		log.setText(currentLog);
//	}

	@Override
	public void onSuccessfulInit(TextToSpeech tts) {
		super.onSuccessfulInit(tts);
		
		executor.setTts(getTts());
		if (AppState.getAppStateInstance().getCurrentState() != AppState.Initialized) {
			AppState.getAppStateInstance().setCurrentState(AppState.Initialized);
		}
		Log.d(TAG, "Ready for the first query");
		executor.execute(lookupVoiceAction);
		failureRetry = 3;
	}

	private VoiceAction makeLookup() {
		
		// match it with two levels of strictness
		boolean relaxed = false;

		VoiceActionCommand cancelCommand = new CancelCommand(this, executor);
		VoiceActionCommand lookupCommand = new ProductLookup(this, executor);

		VoiceAction voiceAction = new MultiCommandVoiceAction(Arrays.asList(
				cancelCommand, lookupCommand));
		// don't retry
		voiceAction.setNotUnderstood(new WhyNotUnderstoodListener(this,
				executor, false));
		
		String LOOKUP_PROMPT = getResources().getString(R.string.speech_launcher_prompt);
		voiceAction.setPrompt(LOOKUP_PROMPT);
		voiceAction.setSpokenPrompt(LOOKUP_PROMPT);
		
		voiceAction.setActionType(AbstractVoiceAction.FirstVoiceActionOutofTwo);

		return voiceAction;
	}

	private void prompt(String promptText) {
		Log.d(TAG, promptText);
		getTts().speak(promptText,
				TextToSpeech.QUEUE_FLUSH,
				TextToSpeechUtils.makeParamsWith(ON_DONE_PROMPT_TTS_PARAM));
	}

	/**
	 * super class handles registering the UtteranceProgressListener and calling
	 * this
	 */

	@Override
	protected void receiveWhatWasHeard(List<String> heard,
			float[] confidenceScores) {
		// satisfy abstract class, this class handles the results directly
		// instead of using this method

		Log.d(TAG, "I just received " + heard.size());

//		clearLog();
//		for (int i = 0; i < heard.size(); i++) {
//			appendToLog(heard.get(i) + " " + confidenceScores[i]);
//		}

		executor.handleReceiveWhatWasHeard(heard, confidenceScores);

	}

	
	@Override
	protected void recognitionFailure(int errorCode) {
		// TODO Auto-generated method stub
		super.recognitionFailure(errorCode);
		
		switch (errorCode) {
		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
			startActivity(new Intent(this, AcknowledgementPresentActivity.class));
			this.finish();
			break;
		
		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
		case SpeechRecognizer.ERROR_NETWORK:
			prompt("Network is not right.");
			
			Timer timer_1 = new Timer();			
			timer_1.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					SpeechRecognitionLauncher.this.finish();
				}
			}, 4000);
			break;
		case SpeechRecognizer.ERROR_NO_MATCH:
			prompt("Sorry. I do not understand what you said. Would you like to try again?");
			
			Timer timer_2 = new Timer();			
			timer_2.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					SpeechRecognitionLauncher.this.finish();
				}
			}, 4000);
			break;
			
		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
		case SpeechRecognizer.ERROR_SERVER:
			prompt("Sorry! Could you try this service later?");
			
			Timer timer_3 = new Timer();
			timer_3.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					SpeechRecognitionLauncher.this.finish();
				}
			}, 4000);
			break;
		default:
			break;
		}
	}
	
	private void repeatCurrentRecognition() {
		
		failureRetry--;

		
		switch (AppState.getAppStateInstance().getCurrentState()) {
		case AppState.Initialized:
		case AppState.FoundRequiredProduct:
		case AppState.UnFoundRequiredProduct:
					
			String toSay = this.getString(R.string.unclear_recognition_prompt);
			executor.speak(toSay, VoiceActionExecutor.EXECUTE_AFTER_SPEAK);

			break;

		default:
			break;
		}
	}
}
