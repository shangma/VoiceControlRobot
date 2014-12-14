package info.shangma.voicecontrolrobot;

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

import root.gast.speech.SpeechRecognizingAndSpeakingActivity;
import root.gast.speech.tts.TextToSpeechUtils;
import root.gast.speech.voiceaction.AbstractVoiceAction;
import root.gast.speech.voiceaction.MultiCommandVoiceAction;
import root.gast.speech.voiceaction.VoiceAction;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import root.gast.speech.voiceaction.WhyNotUnderstoodListener;
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
import android.widget.TextView;

import com.alchemyapi.api.AlchemyAPI;


/**
 * Starts a speech recognition dialog and then sends the results to
 * {@link SpeechRecognitionResultsActivity}
 * 
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SpeechRecognitionLauncher extends
		SpeechRecognizingAndSpeakingActivity {
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
		
		final String LOOKUP_PROMPT = getResources().getString(
				R.string.speech_launcher_prompt);

		// match it with two levels of strictness
		boolean relaxed = false;

		VoiceActionCommand cancelCommand = new CancelCommand(this, executor);
		VoiceActionCommand lookupCommand = new ProductLookup(this, executor);

		VoiceAction voiceAction = new MultiCommandVoiceAction(Arrays.asList(
				cancelCommand, lookupCommand));
		// don't retry
		voiceAction.setNotUnderstood(new WhyNotUnderstoodListener(this,
				executor, false));
		voiceAction.setPrompt(LOOKUP_PROMPT);

		voiceAction.setSpokenPrompt(LOOKUP_PROMPT);
		voiceAction.setActionType(AbstractVoiceAction.FirstVoiceActionOutofTwo);

		return voiceAction;
	}

	private void prompt() {
		Log.d(TAG, "Speak prompt");
		getTts().speak(getString(R.string.speech_launcher_prompt),
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

	/*
	@Override
	protected void recognitionFailure(int errorCode) {
		// TODO Auto-generated method stub
		super.recognitionFailure(errorCode);
		
		//handle unclear recognition
		currentFailureTimeStamp = SystemClock.currentThreadTimeMillis();
		if (prevFailureTimeStamp == -1) {
			prevFailureTimeStamp = currentFailureTimeStamp;
		}

		long timeDifference = currentFailureTimeStamp - prevFailureTimeStamp;

		if ((timeDifference < TIME_INTERVAL_REPEATABLE) && (failureRetry > 0)) {

			// repeat current recognition
			repeatCurrentRecognition();
		} else if ((timeDifference <= TIME_INTERVAL_REPEATABLE)
				&& (failureRetry <= 0)) {
			// run out of retry, go back to the beginning

			failureRetry = 3;
			AppState.getAppStateInstance().setCurrentState(AppState.EndOfQuery);
			this.finish();
		} else {
			// repeat current recognition
			failureRetry = 3;
			prevFailureTimeStamp = currentFailureTimeStamp;
			repeatCurrentRecognition();
		}

	}
	*/
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
