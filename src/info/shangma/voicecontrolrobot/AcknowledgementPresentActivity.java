package info.shangma.voicecontrolrobot;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;

public class AcknowledgementPresentActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_acknowledgement);
		
		Timer timer = new Timer();
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				AcknowledgementPresentActivity.this.finish();
			}
		}, 2000);
	}
	
	

}
