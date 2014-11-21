package info.shangma.voicecontrolrobot.comm;

import info.shangma.voicecontrolrobot.SpeechActivationBroadcastReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
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

import root.gast.speech.text.WordList;

import com.alchemyapi.api.AlchemyAPI;

import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class KeywordQuery {
	
	private static KeywordQuery queryInstance;
	private String mQuery;
	
	private final static String TAG = "SimpleHttpGetTask";
	private String url = "http://128.195.204.85/robot/response.jsp?query="; 
	
	private AlchemyAPI alchemyObj;
	
	private String currKeyword;	
	private Context mContext;
	
	public static KeywordQuery getQueryInstance(Context context, String heard) {
		if (queryInstance == null) {
			queryInstance = new KeywordQuery(context, heard);
		}
		
		return queryInstance;
	}
	
	private KeywordQuery(Context context, String heard) 
	{
		mContext = context;
		mQuery = heard;
		alchemyObj = AlchemyAPI.GetInstanceFromString("f54e554a09119e3cb6e5c8485118b1a31736e996");
		currKeyword = null;
	}
	
	public void executeQuery() {
		new SimpleHttpGetTask().execute(mQuery);
	}
	
	// this is OVERSIMPLE (for the example), in the real world, if you have big
	// responses, use the stream
	private static String convertStreamToString(InputStream is) {
		try {
			return new java.util.Scanner(is, "UTF-8").useDelimiter("\\A")
					.next();
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
	}
	
	
	private class SimpleHttpGetTask extends AsyncTask<String, Void, String> {
		
		private final static String TAG = "SimpleHttpGetTask";
		private String url = "http://128.195.204.85/robot/response.jsp?query="; 
		@Override
		protected String doInBackground(String... passed) {
			// TODO Auto-generated method stub
			
			String heardSentence = passed[0];
			
				
			try {
				Document doc = alchemyObj.TextGetRankedKeywords(heardSentence);
				doc.getDocumentElement().normalize();
				NodeList nodeList = doc.getElementsByTagName("results");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element firstElement = (Element) node;
						NodeList firstList = firstElement
								.getElementsByTagName("keyword");
						Node node2 = firstList.item(0);
						if (node2.getNodeType() == Node.ELEMENT_NODE) {
							Element secondElement = (Element) node;
							NodeList keyList = secondElement
									.getElementsByTagName("text");
							System.out
									.println(keyList.item(0).getTextContent());
							currKeyword = keyList.item(0).getTextContent();

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


			
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android-MyUserAgent");
			HttpRequestBase request = null;
			HttpResponse response = null;
			String result = null;

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

				Log.d("TAG", "http GET completed, code:" + code + " message:"
						+ message);

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = null;
					try {
						instream = entity.getContent();

						// convert stream if gzip header present in response
						Header contentEncoding = response
								.getFirstHeader("Content-Encoding");
						if (contentEncoding != null
								&& contentEncoding.getValue().equalsIgnoreCase(
										"gzip")) {
							instream = new GZIPInputStream(instream);
						}

						// returning the string here, but if you want return
						// the
						// code, whatever
						result = convertStreamToString(instream);

						// int mResult = Integer.parseInt(result);
						// if (mResult != -1) {
						// client.close();
						// break;
						// }

//						if (!result.equals("-1")) {
//							client.close();
//						}
						// queryResultList.add(result);

					} finally {
						if (instream != null) {
							instream.close();
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				Log.e(TAG, "Error with HTTP GET", e);
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Intent intent = new Intent(SpeechActivationBroadcastReceiver.ACTIVATION_RESULT_BROADCAST_NAME);
			intent.putExtra(SpeechActivationBroadcastReceiver.ACTIVATION_RESULT_INTENT_KEY, true);
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mContext.sendBroadcast(intent);
		}
		
	}

}
