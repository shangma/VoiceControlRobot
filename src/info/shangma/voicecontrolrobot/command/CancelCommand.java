package info.shangma.voicecontrolrobot.command;

import java.util.Timer;
import java.util.TimerTask;

import info.shangma.voicecontrolrobot.R;
import info.shangma.voicecontrolrobot.SpeechRecognitionLauncher;
import root.gast.speech.text.WordList;
import root.gast.speech.text.match.WordMatcher;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import android.content.Context;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
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
					((SpeechRecognitionLauncher)CancelCommand.this.mContext).finish();
				}
			}, 2000);
        }
        return understood;
    }
}
