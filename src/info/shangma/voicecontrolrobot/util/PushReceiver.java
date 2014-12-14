package info.shangma.voicecontrolrobot.util;

import info.shangma.voicecontrolrobot.MainActivity;
import info.shangma.voicecontrolrobot.SpeechRecognitionLauncher;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;
import com.wowwee.robome.RoboMeCommands.RobotCommand;

public class PushReceiver extends ParsePushBroadcastReceiver{
	
	private static final String TAG = "Push Receiver";
	private static boolean robotMovement = false;
	
	@Override
	protected void onPushOpen(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		super.onPushOpen(arg0, arg1);
	}

	@Override
	protected void onPushReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onPushReceive(context, intent);
		
		if (CommonUtil.ISCLIENT == 1) {
			Log.d(TAG, "Got Notified!");
			Log.d(TAG, "Version " + MainActivity.roboMe.getLibVersion());
			
			robotMovement = !robotMovement;
			
			Log.i(TAG, "current move mode is: " + robotMovement);
			
			if (robotMovement) {
				MainActivity.roboMe.sendCommand(RobotCommand.kRobot_HeadTiltAllDown);
			} else {
				MainActivity.roboMe.sendCommand(RobotCommand.kRobot_HeadTiltAllUp);
			}
						
		} else if (CommonUtil.ISCLIENT == 0) {
			Intent i = new Intent(context, SpeechRecognitionLauncher.class);
	        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        context.startActivity(i);
		}       
        
	}

}
