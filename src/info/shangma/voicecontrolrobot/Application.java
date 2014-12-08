package info.shangma.voicecontrolrobot;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import android.util.Log;

public class Application extends android.app.Application {

	public Application() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		Parse.initialize(this, "CZY3cCLhBhVMTU7bpaJssIhUs5KjYXllT3mXkga9",
				"nZpYbqON9DpmtGZi2lNJltPbcmFr815ibfpkJ3yM");

		ParsePush.subscribeInBackground("ActivateRobot", new SaveCallback() {

			@Override
			public void done(ParseException e) {
				// TODO Auto-generated method stub
				if (e == null) {
					Log.d("com.parse.push",
							"successfully subscribed to the broadcast channel.");
				} else {
					Log.e("com.parse.push", "failed to subscribe for push", e);
				}
			}
		});
	}

}
