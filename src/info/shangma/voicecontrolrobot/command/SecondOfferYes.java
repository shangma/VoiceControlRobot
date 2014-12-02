package info.shangma.voicecontrolrobot.command;

import java.util.Arrays;

import info.shangma.voicecontrolrobot.R;
import info.shangma.voicecontrolrobot.util.AppState;
import android.content.Context;
import root.gast.speech.text.WordList;
import root.gast.speech.text.match.WordMatcher;
import root.gast.speech.voiceaction.MultiCommandVoiceAction;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import root.gast.speech.voiceaction.WhyNotUnderstoodListener;

public class SecondOfferYes implements VoiceActionCommand{
	
	private static final String TAG = "Second Offer Yes";
	
	private WordMatcher matchYes;
	private VoiceActionExecutor executor;
	
	private Context context;
	
    public SecondOfferYes(Context context, VoiceActionExecutor executor) {
    	
    	String[] commandYes = context.getResources().getStringArray(R.array.second_offer_yes);
    	matchYes = new WordMatcher(commandYes);
    	
    	this.context = context;
    	this.executor = executor;
    }

	@Override
	public boolean interpret(WordList heard, float[] confidenceScores) {
		// TODO Auto-generated method stub
		boolean understood = false;
		
		// match yes
		if (matchYes.isIn(heard.getWords())) {
			
			if (AppState.getAppStateInstance().getCurrentState() == AppState.FoundRequiredProduct) {
				
				VoiceActionCommand lookupCommand = new ProductLookup(this.context, this.executor);
				VoiceActionCommand cancelCommand = new CancelCommand(this.context, this.executor);

				MultiCommandVoiceAction responseAction = new MultiCommandVoiceAction(Arrays.asList(lookupCommand, cancelCommand));
		        responseAction.setNotUnderstood(new WhyNotUnderstoodListener(context, executor, true));
		        
		        
		        String promptString = this.context.getResources().getString(R.string.second_offer_accepted);
		        responseAction.setPrompt(promptString);
		        responseAction.setSpokenPrompt(promptString);
		        
		        AppState.getAppStateInstance().setCurrentState(AppState.Initialized);
		        executor.execute(responseAction);
			}
			
			if (AppState.getAppStateInstance().getCurrentState() == AppState.UnFoundRequiredProduct) {
				
				VoiceActionCommand lookupCommand = new ProductLookup(this.context, this.executor);
				VoiceActionCommand cancelCommand = new CancelCommand(this.context, this.executor);

				MultiCommandVoiceAction responseAction = new MultiCommandVoiceAction(Arrays.asList(lookupCommand, cancelCommand));
		        responseAction.setNotUnderstood(new WhyNotUnderstoodListener(context, executor, true));
		        
		        
		        String promptString = this.context.getResources().getString(R.string.second_try_accepted);
		        responseAction.setPrompt(promptString);
		        responseAction.setSpokenPrompt(promptString);
		        
		        AppState.getAppStateInstance().setCurrentState(AppState.Initialized);
		        executor.execute(responseAction);
			}
			
			understood = true;
		}
		
		return understood;
	}

}
