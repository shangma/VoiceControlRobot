package info.shangma.voicecontrolrobot.util;

import info.shangma.audio.util.SoundPoolPlayer;
import info.shangma.voicecontrolrobot.R;
import android.content.Context;
import android.util.Log;

/**
 * @author Shang Ma
 *
 * www.shangma.info
 */

public class SoundPoolPlayerEx extends SoundPoolPlayer{
	
	private static final String TAG = "SoundPoolPlayerEx";

	public SoundPoolPlayerEx(Context pContext) {
		super(pContext);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void playSound() {
		// TODO Auto-generated method stub
		Log.d(TAG, "playing sound");
		this.loadShortResource(R.raw.hello);
		this.playShortResource(R.raw.hello);
	}

}
