package info.shangma.voicecontrolrobot.util;

import java.util.Random;

/**
 * @author Shang Ma
 *
 * www.shangma.info
 */

public class CommonUtil {
	
	public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	

	public static final int ISFORROBOT = 1;
	public static final int ISFORCLIENT = 0;
	public static int ISCLIENT = ISFORCLIENT; // 0 for client, and 1 for robot
	
	
	public final static String MOVE_COMMAND = "move";
}

