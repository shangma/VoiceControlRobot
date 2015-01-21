package info.shangma.voicecontrolrobot.command;

import java.util.Timer;
import java.util.TimerTask;

import info.shangma.speech.text.WordList;
import info.shangma.speech.text.match.WordMatcher;
import info.shangma.speech.voiceaction.VoiceActionCommand;
import info.shangma.speech.voiceaction.VoiceActionExecutor;
import info.shangma.voicecontrolrobot.AcknowledgementPresentActivity;
import info.shangma.voicecontrolrobot.R;
import info.shangma.voicecontrolrobot.SpeechRecognitionLauncher;

import android.content.Context;
import android.content.Intent;

/**
 * @author Shang Ma
 *
 * www.shangma.info
 */

public class CancelCommand implements VoiceActionCommand
{
    private VoiceActionExecutor executor;
    private String cancelledPrompt;
    private WordMatcher matcher;
    private Context mContext;
    
    public CancelCommand(Context context, VoiceActionExecutor executor)
    {
    	this.mContext = context;
        this.executor = executor;
        this.cancelledPrompt = context.getResources().getString(R.string.device_confirmed);
        this.matcher = new WordMatcher(context.getResources().getStringArray(R.array.lookup_cancel));
    }
    
    @Override
    public boolean interpret(WordList heard, float [] confidence)
    {
        boolean understood = false;
        if (matcher.isIn(heard.getWords()))
        {
            executor.speak(cancelledPrompt);
            understood = true;
            
            
            Timer timer_1 = new Timer();			
			timer_1.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					CancelCommand.this.mContext.startActivity(new Intent(CancelCommand.this.mContext, AcknowledgementPresentActivity.class));
					((SpeechRecognitionLauncher)CancelCommand.this.mContext).finish();
				}
			}, 2000);
        }
        return understood;
    }
}
