package info.shangma.voicecontrolrobot.command;

import info.shangma.voicecontrolrobot.R;
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
    
    public CancelCommand(Context context, VoiceActionExecutor executor)
    {
        this.executor = executor;
        this.cancelledPrompt = context.getResources().getString(R.string.device_confirmed);
        this.matcher = new WordMatcher(context.getResources().getStringArray(R.array.device_confirm));
    }
    
    @Override
    public boolean interpret(WordList heard, float [] confidence)
    {
        boolean understood = false;
        if (matcher.isIn(heard.getWords()))
        {
            executor.speak(cancelledPrompt);
            understood = true;
        }
        return understood;
    }
}
