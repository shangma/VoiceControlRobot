package info.shangma.voicecontrolrobot.command;

import info.shangma.voicecontrolrobot.R;
import android.content.Context;
import root.gast.speech.text.WordList;
import root.gast.speech.text.match.WordMatcher;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;

public class ConfirmCommand implements VoiceActionCommand {

	private VoiceActionExecutor executor;
	private String confirmPrompt;
	private WordMatcher matcher;

	public ConfirmCommand(Context context, VoiceActionExecutor executor) {
		this.executor = executor;
		this.confirmPrompt = context.getResources().getString(
				R.string.food_cancelled);
		this.matcher = new WordMatcher(context.getResources().getStringArray(
				R.array.food_cancel));
	}

	@Override
	public boolean interpret(WordList heard, float[] confidenceScores) {
		// TODO Auto-generated method stub
		return false;
	}

}
