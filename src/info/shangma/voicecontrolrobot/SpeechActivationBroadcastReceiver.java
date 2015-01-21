package info.shangma.voicecontrolrobot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Shang Ma
 *
 * www.shangma.info
 */

public class SpeechActivationBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "SpeechActivationBroadcastReceiver";
	public static final String ACTIVATION_RESULT_BROADCAST_NAME = "info.shangma.voicecontrolrobot.ACTIVATION";
	public static final String ACTIVATION_RESULT_INTENT_KEY = "ACTIVATION_RESULT_INTENT_KEY";


	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		if (intent.getAction().equals(ACTIVATION_RESULT_BROADCAST_NAME))
        {
            if (intent
                    .getBooleanExtra(ACTIVATION_RESULT_INTENT_KEY,
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
