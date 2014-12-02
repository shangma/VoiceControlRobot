/*
 * Copyright 2011 Greg Milette and Adam Stroud
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
package info.shangma.voicecontrolrobot.command;

import info.shangma.utils.string.Inflector;
import info.shangma.voicecontrolrobot.util.AppState;
import info.shangma.voicecontrolrobot.util.CommonUtil;
import info.shangma.voicecontrolrobot.R;
import info.shangma.voicecontrolrobot.SpeechActivationBroadcastReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alchemyapi.api.AlchemyAPI;

import root.gast.speech.text.WordList;
import root.gast.speech.voiceaction.MultiCommandVoiceAction;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import root.gast.speech.voiceaction.WhyNotUnderstoodListener;
import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class ProductLookup implements VoiceActionCommand
{
    private static final String TAG = "Product Lookup";

    private VoiceActionExecutor executor;
    private Context context;
    
	private String url = "http://128.195.204.85/robot/response.jsp?query="; 
	private AlchemyAPI alchemyObj;

	private boolean lookupResult;
	private String resultAisleNumber;
	
	private String currentProduct;
	
	public String[] catalog = {"none",
								"seasonal", "outdoor and barbecue", "extension", "light", "vacuum",
								"kitchen", "houseware", "houseware", "storage", "cleaning",
								"bird and garden", "penst control", "connector", "drain and plumbing", "faucet",
								"silicon and paint", "painting", "miscellaneous", "tools", "safety",
								"electrical", "auto", "hanger and lock", "miscellaneous", "nut and screw"};
	

	
	AndroidHttpClient client;
	
	private AsyncTask<String, Void, Void> mTask;
	
	Inflector mInflector;

    public ProductLookup(Context context, VoiceActionExecutor executor)
    {
        this.context = context;
        this.executor = executor;
		
        alchemyObj = AlchemyAPI.GetInstanceFromString("f54e554a09119e3cb6e5c8485118b1a31736e996");
    	lookupResult = false;
    	resultAisleNumber = null;
    	mInflector = Inflector.getInstance();
    }
    
    
    public boolean interpret(WordList heard, float[] confidence)
    {
    	
    	mTask = new SimpleHttpGetTask();
    	try {
			mTask.execute(heard.getSource());
			mTask.get();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	Log.i(TAG, "ready to return");
    	return lookupResult ;
    }
    
	private static String convertStreamToString(InputStream is) {
		try {
			return new java.util.Scanner(is, "UTF-8").useDelimiter("\\A")
					.next();
		} catch (java.util.NoSuchElementException e) {
			return "";
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
	
	private String getStringRevised(String beforeRevised)
	{
		String afterRevised = mInflector.singularize(beforeRevised).replace(' ', '&');
			
		return afterRevised;
	}

    private class SimpleHttpGetTask extends AsyncTask<String, Void, Void> {
		

		@Override
		protected Void doInBackground(String... passed) {
			// TODO Auto-generated method stub
			
			String heardSentence = passed[0];
			String currKeyword = null;
			try {
				Log.d(TAG, "Send out for keyword: " + heardSentence);
				Document doc = alchemyObj.TextGetRankedKeywords(heardSentence);
				doc.getDocumentElement().normalize();
				NodeList nodeList = doc.getElementsByTagName("results");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element firstElement = (Element) node;
						NodeList firstList = firstElement
								.getElementsByTagName("keyword");
						if (firstList.getLength() > 0) {
							Node node2 = firstList.item(0);
							if (node2.getNodeType() == Node.ELEMENT_NODE) {
								Element secondElement = (Element) node;
								NodeList keyList = secondElement
										.getElementsByTagName("text");
								currKeyword = keyList.item(0).getTextContent();
								Log.d(TAG, "keyword found: " + currKeyword);

							}
						} else {
							Log.d(TAG, "No keyword found");
							Log.d(TAG, getStringFromDocument(doc));
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


			HttpRequestBase request = null;
			HttpResponse response = null;
			
			if (client == null) {
				client = AndroidHttpClient.newInstance("Android-MyUserAgent");
			}

			if (currKeyword != null) {
				currKeyword = getStringRevised(currKeyword);
				currentProduct = currKeyword;
				String queryUrl = url + currKeyword;
				Log.d("TAG", "hitting URL:" + queryUrl);

				request = new HttpGet(queryUrl);

				try {

					response = client.execute(request);

					// Log.d(TAG, response.toString());

					int code = response.getStatusLine().getStatusCode();
					String message = response.getStatusLine().getReasonPhrase();

					// use the response here
					// if the code is 200 thru 399 it worked or was redirected
					// if the code is >= 400 there was a problem

					Log.d("TAG", "http GET completed, code:" + code
							+ " message:" + message);

					HttpEntity entity = response.getEntity();
					if (entity != null) {
						InputStream instream = null;
						try {
							instream = entity.getContent();

							// convert stream if gzip header present in response
							Header contentEncoding = response
									.getFirstHeader("Content-Encoding");
							if (contentEncoding != null
									&& contentEncoding.getValue()
											.equalsIgnoreCase("gzip")) {
								instream = new GZIPInputStream(instream);
							}

							resultAisleNumber = convertStreamToString(instream);

							Log.d(TAG, "i found aisle number is: " + resultAisleNumber);
							if (!resultAisleNumber.equals("-1")) {
								lookupResult = true;
							}

						} finally {
							if (instream != null) {
								instream.close();
							}
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					Log.e(TAG, "Error with HTTP GET", e);
				} finally {
//					client.close();
				}
			}
			else {
				Log.d(TAG, "Keyword is null. No GET");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			VoiceActionCommand secondOfferYes = new SecondOfferYes(context, executor);
			VoiceActionCommand secondOfferNo = new SecondOfferNo(context, executor);
			String secondOfferPrompt;
			String toSay;
			
			if (lookupResult) {
				if (AppState.getAppStateInstance().getCurrentState() == AppState.Initialized) {
					AppState.getAppStateInstance().setCurrentState(AppState.FoundRequiredProduct);
				}
				secondOfferPrompt= context.getString(R.string.second_offer_for_help);
				String[] aisleNumbers = resultAisleNumber.split(",");
				Log.d(TAG, "the number of result is: " + aisleNumbers.length);
				String aisleReport = new String();
				for (int i = 0; i < aisleNumbers.length; i++) {
					aisleReport += "aisle " + aisleNumbers[i];
					int currentIndex = Integer.valueOf(aisleNumbers[i]);
					aisleReport += " the " + catalog[currentIndex] + " category ";
				}
				toSay = String.format(secondOfferPrompt, currentProduct, aisleReport);
				Log.d(TAG, "to say is: " +  toSay);
				
			}
			else {
				if (AppState.getAppStateInstance().getCurrentState() == AppState.Initialized) {
					AppState.getAppStateInstance().setCurrentState(AppState.UnFoundRequiredProduct);
				}
				secondOfferPrompt = context.getString(R.string.second_offer_for_retry);
				toSay = secondOfferPrompt;
			}
			
			String simplePromptForLook = "Yes or No?";
			MultiCommandVoiceAction responseAction = new MultiCommandVoiceAction(Arrays.asList(secondOfferYes, secondOfferNo));
			responseAction.setPrompt(simplePromptForLook);
			responseAction.setSpokenPrompt(toSay);
			
            responseAction.setNotUnderstood(new WhyNotUnderstoodListener(
                    context, executor, true));
            
            executor.execute(responseAction);
		}
		
		
	}
    
}
