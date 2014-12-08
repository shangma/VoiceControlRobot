package info.shangma.voicecontrolrobot.util;

import info.shangma.voicecontrolrobot.MainActivity;
import info.shangma.voicecontrolrobot.SpeechRecognitionLauncher;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

public class PushReceiver extends ParsePushBroadcastReceiver{
	
	private static final String TAG = "Push Receiver";

	@Override
	protected void onPushOpen(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		super.onPushOpen(arg0, arg1);
	}

	@Override
	protected void onPushReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onPushReceive(context, intent);
		Intent i = new Intent(context, SpeechRecognitionLauncher.class);
//        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        
        
	}

}
