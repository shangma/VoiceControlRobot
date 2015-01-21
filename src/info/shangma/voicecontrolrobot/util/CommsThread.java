package info.shangma.voicecontrolrobot.util;

import info.shangma.voicecontrolrobot.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.wowwee.robome.RoboMeCommands.RobotCommand;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Shang Ma
 *
 * www.shangma.info
 */

public class CommsThread extends Thread {
	
	private final static String TAG = "CommsThread";
    final BluetoothSocket bluetoothSocket;
    final InputStream inputStream;
    final OutputStream outputStream;
    
	private static boolean robotMovement = false;
	    
    public CommsThread(BluetoothSocket socket) {
        bluetoothSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null; 
        try {
            //---creates the inputstream and outputstream objects
            // for reading and writing through the sockets---
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        	Log.d("CommsThread", e.getLocalizedMessage());
        } 
        inputStream = tmpIn;
        outputStream = tmpOut;
    }
    
    public void run() {
        //---buffer store for the stream---
        byte[] buffer = new byte[1024];
        
        //---bytes returned from read()---
        int bytes;  

        //---keep listening to the InputStream until an 
        // exception occurs---
        while (true) {
            try {
                //---read from the inputStream---
                bytes = inputStream.read(buffer);

                //---convert the entire byte array to string---
                String strReceived = new String(buffer);
                //---extract only the actual string received---
                strReceived = strReceived.substring(
                    0, bytes);
                
                Log.d(TAG, "string received is: " + strReceived);
                
				if (strReceived.equals(CommonUtil.MOVE_COMMAND)) {

					robotMovement = !robotMovement;
					Log.i(TAG, "current move mode is: " + robotMovement);

					if (robotMovement) {
						MainActivity.roboMe
								.sendCommand(RobotCommand.kRobot_HeadTiltAllDown);
					} else {
						MainActivity.roboMe
								.sendCommand(RobotCommand.kRobot_HeadTiltAllUp);
					}
				}
                
            } catch (IOException e) {
            	Log.d("CommsThread", e.getLocalizedMessage());
                break;
            }
        }
    }
        
    //---call this from the main Activity to 
    // send data to the remote device---
    public void write(String str) {    	
    	try {
            outputStream.write(str.getBytes());
        } catch (IOException e) {
        	Log.d("CommsThread", e.getLocalizedMessage());
        }    
    }
 
    //---call this from the main Activity to 
    // shutdown the connection--- 
    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) { 
        	Log.d("CommsThread", e.getLocalizedMessage());
        }
    }
}

