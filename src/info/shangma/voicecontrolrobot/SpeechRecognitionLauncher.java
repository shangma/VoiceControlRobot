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
	
	private List<String> keywordList;
	private float[] confidence;
	
	private String conversation;
	
	private AlchemyAPI alchemyObj;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fooddialogmulti);
 
		log = (TextView) findViewById(R.id.tv_resultlog);
		keywordList = new ArrayList<String>();
		
//		alchemyObj = AlchemyAPI.GetInstanceFromString("f54e554a09119e3cb6e5c8485118b1a31736e996");

		
		initDbs();
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
			System.out.println("1. has prompt or not: " + lookupVoiceAction.hasSpokenPrompt());
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
		confidence = new float[confidenceScores.length];
		for (int i = 0; i < heard.size(); i++) {
			appendToLog(heard.get(i) + " " + confidenceScores[i]);
			confidence[i] = confidenceScores[i];
		}
		
		Log.d(TAG, "the orignal size of keyword list: " + keywordList.size());

		extractKeyword(heard);		
	}
	
	private void extractKeyword(List<String> heard)
	{
		if (heard.size() > 0) {
//			new KeywordFetchTask().execute(heard);
		}
	}
	
	private static String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);

			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	private class KeywordFetchTask extends AsyncTask<List<String>, Void, List<String>> {

		@Override
		protected List<String> doInBackground(List<String>... passed) {
			// TODO Auto-generated method stub
			
			List<String> heard = passed[0];
			String heardSentence;
			
			for (int j = 0; j < heard.size(); j++) {
				heardSentence = heard.get(j);
				
				try {
					Document doc = alchemyObj.TextGetRankedKeywords(heardSentence);
					
//					System.out.println("--------------------");
//					Log.d(TAG, getStringFromDocument(doc));
//					System.out.println("--------------------");

					doc.getDocumentElement().normalize();
					
//					Log.d(TAG, doc.getDocumentElement().getNodeName());
//					Log.d(TAG, Short.toString(doc.getDocumentElement().getNodeType()));
					NodeList nodeList = doc.getElementsByTagName("results");
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node node = nodeList.item(i);
//						Log.d(TAG, node.getNodeName());
//						Log.d(TAG, Short.toString(node.getNodeType()));

						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element firstElement = (Element) node;
							NodeList firstList = firstElement.getElementsByTagName("keyword");
							Node node2 = firstList.item(0);
							if (node2.getNodeType() == Node.ELEMENT_NODE) {
								Element secondElement = (Element) node;
								NodeList keyList = secondElement.getElementsByTagName("text");
								System.out.println(keyList.item(0).getTextContent());
								keywordList.add(keyList.item(0).getTextContent());
							}
						}			
						
					}
					
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
						
			return null;
		}

		@Override
		protected void onPostExecute(List<String> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			executor.handleReceiveWhatWasHeard(keywordList, confidence);

			Log.d(TAG, "the size of keyword list: " + keywordList.size());
			
			for (int i = 0; i < keywordList.size(); i++) {
				Log.d(TAG, "the keyword at " + i + " is " + keywordList.get(i));
			}
			
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			executor.execute(lookupVoiceAction);
		}
		
	}
	
}
