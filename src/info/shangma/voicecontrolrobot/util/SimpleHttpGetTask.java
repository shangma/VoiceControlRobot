package info.shangma.voicecontrolrobot.util;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class SimpleHttpGetTask extends AsyncTask<String, Void, String> {
	
	private final static String TAG = "SimpleHttpGetTask";
	private String url = "http://128.195.204.85/robot/response.jsp?query="; 
	@Override
	protected String doInBackground(String... args) {
		// TODO Auto-generated method stub
		
		if (args == null || args.length != 1) {
			Log.w("TAG", "args size must be 1: URL to invoke");
			return null;
		}
		
		String queryUrl = url + args[0];
		Log.d("TAG", "hitting URL:" + queryUrl);
		
		AndroidHttpClient client = AndroidHttpClient.newInstance("Android-MyUserAgent");
		HttpRequestBase request = new HttpGet(queryUrl);
		HttpResponse response = null;
		
		try {
			response = client.execute(request);
			
//			Log.d(TAG, response.toString());
			
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

					// returning the string here, but if you want return the
					// code, whatever
					String result = convertStreamToString(instream);
					Log.d("TAG", "http GET result (as String):" + result);
					return result;

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
			client.close();
		}
		
		return null;
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

}
