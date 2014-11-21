package info.shangma.voicecontrolrobot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SpeechActivationBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "SpeechActivationBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		if (intent.getAction().equals(
				DetectionService.ACTIVATION_RESULT_BROADCAST_NAME))
        {
            if (intent
                    .getBooleanExtra(
                    		DetectionService.ACTIVATION_RESULT_INTENT_KEY,
                            false))
            {
                Log.d(TAG,
                        "SpeechActivationBroadcastReceiver taking action");
                // launch something that prompts the user...
                Intent i = new Intent(context, SpeechRecognitionLauncher.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
	}

}