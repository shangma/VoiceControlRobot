package info.shangma.voicecontrolrobot.command;

import java.util.Arrays;

import info.shangma.speech.text.WordList;
import info.shangma.speech.text.match.WordMatcher;
import info.shangma.speech.voiceaction.VoiceActionCommand;
import info.shangma.speech.voiceaction.VoiceActionExecutor;
import info.shangma.voicecontrolrobot.R;
import info.shangma.voicecontrolrobot.util.AppState;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * @author Shang Ma
 *
 * www.shangma.info
 */

public class SecondOfferNo implements VoiceActionCommand{
	
	private static final String TAG = "Second Offer No";
	
	private WordMatcher matchNo;
	private VoiceActionExecutor executor;
	
	private Context context;
	
    public SecondOfferNo(Context context, VoiceActionExecutor executor) {
    	
    	String[] commandNo = context.getResources().getStringArray(R.array.second_offer_no);
    	matchNo = new WordMatcher(commandNo);
    	
    	this.context = context;
    	this.executor = executor;
    }

	@Override
	public boolean interpret(WordList heard, float[] confidenceScores) {
		// TODO Auto-generated method stub
		boolean understood = false;
		
		// match yes
		if (matchNo.isIn(heard.getWords())) {
			
			if (AppState.getAppStateInstance().getCurrentState() == AppState.FoundRequiredProduct) {
				
				executor.speak(this.context.getResources().getString(R.string.second_offer_denied), VoiceActionExecutor.END_OF_QUERY_SPEAK);
			}
			
			if (AppState.getAppStateInstance().getCurrentState() == AppState.UnFoundRequiredProduct) {
				executor.speak(this.context.getResources().getString(R.string.second_try_denied), VoiceActionExecutor.END_OF_QUERY_SPEAK);
			}
			
			AppState.getAppStateInstance().setCurrentState(AppState.EndOfQuery);
						
			understood = true;
		}
		
		return understood;
	}

}
