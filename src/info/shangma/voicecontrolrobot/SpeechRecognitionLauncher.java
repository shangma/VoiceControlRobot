/*
 * Copyright 2012 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.shangma.voicecontrolrobot;

import info.shangma.voicecontrolrobot.comm.KeywordQuery;
import info.shangma.voicecontrolrobot.command.CancelCommand;
import info.shangma.voicecontrolrobot.command.DeviceLookup;
import info.shangma.voicecontrolrobot.data.FtsIndexedFoodDatabase;

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
import root.gast.speech.voiceaction.MultiCommandVoiceAction;
import root.gast.speech.voiceaction.VoiceAction;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import root.gast.speech.voiceaction.WhyNotUnderstoodListener;
import android.R.integer;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
	private FtsIndexedFoodDatabase foodDb;

	private TextView log;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fooddialogmulti);
 
		log = (TextView) findViewById(R.id.tv_resultlog);
		
		
//		initDbs();
		initDialog();
		Log.i(TAG, "finish initialization");
	}

	private void initDbs() {
		foodDb = FtsIndexedFoodDatabase.getInstance(this);

		if (foodDb.isEmpty()) {
			Log.d(TAG, "loading foods");
			InputStream stream = getResources().openRawResource(R.raw.foods);
			try {
				foodDb.loadFrom(stream);
			} catch (IOException io) {
				Log.d(TAG, "failed to load db");
			}
		}
	}

	private void initDialog() {
		if (executor == null) {
			executor = new VoiceActionExecutor(this);
		}
		lookupVoiceAction = makeLookup();

	}

	private void clearLog() {
		log.setText("");
	}

	private void appendToLog(String appendThis) {
		String currentLog = log.getText().toString();
		currentLog = currentLog + "\n" + appendThis;
		log.setText(currentLog);
	}

	@Override
	public void onSuccessfulInit(TextToSpeech tts) {
		super.onSuccessfulInit(tts);
//		prompt();
		
		executor.setTts(getTts());
		System.out.println("1. has prompt or not: " + lookupVoiceAction.hasSpokenPrompt());
		executor.execute(lookupVoiceAction);
	}

	private VoiceAction makeLookup() {
		
		final String LOOKUP_PROMPT = getResources().getString(
				R.string.speech_launcher_prompt);

		FtsIndexedFoodDatabase foodDb = FtsIndexedFoodDatabase
				.getInstance(SpeechRecognitionLauncher.this);

		// match it with two levels of strictness
		boolean relaxed = false;

		VoiceActionCommand cancelCommand = new CancelCommand(this, executor);
//		VoiceActionCommand removeCommand = new RemoveFood(this, executor,
//				foodDb, relaxed);
//		VoiceActionCommand addCommand = new AddFood(this, executor, foodDb, relaxed);
		VoiceActionCommand lookupCommand = new DeviceLookup(this, executor, foodDb);

		relaxed = true;
//		VoiceActionCommand removeCommandRelaxed = new RemoveFood(this,
//				executor, foodDb, relaxed);
//		VoiceActionCommand addCommandRelaxed = new AddFood(this, executor,
//				foodDb, relaxed);

		VoiceAction voiceAction = new MultiCommandVoiceAction(Arrays.asList(
				cancelCommand, lookupCommand));
		// don't retry
		voiceAction.setNotUnderstood(new WhyNotUnderstoodListener(this,
				executor, false));
		voiceAction.setPrompt(LOOKUP_PROMPT);

		voiceAction.setSpokenPrompt(LOOKUP_PROMPT);

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
	public void onDone(String utteranceId) {
		if (utteranceId.equals(ON_DONE_PROMPT_TTS_PARAM)) {
			executor.setTts(getTts());
			executor.execute(lookupVoiceAction);
		}
	}


	@Override
	protected void receiveWhatWasHeard(List<String> heard,
			float[] confidenceScores) {
		// satisfy abstract class, this class handles the results directly
		// instead of using this method

		Log.d(TAG, "I just received " + heard.size());

		clearLog();
		for (int i = 0; i < heard.size(); i++) {
			appendToLog(heard.get(i) + " " + confidenceScores[i]);
		}

		executor.handleReceiveWhatWasHeard(heard, confidenceScores);
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		executor.execute(lookupVoiceAction);

	}
	
}
