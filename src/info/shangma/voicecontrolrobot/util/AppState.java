package info.shangma.voicecontrolrobot.util;

public class AppState {
	
	public static final int Uninitialized = -1;
	public static final int Initialized = 0;
	public static final int FoundRequiredProduct = 1;
	public static final int ProcessNextQuery =2;

	
	public static final int UnFoundRequiredProduct = 3;
	public static final int RetryLastQuery = 4;
	
	public static final int EndOfQuery = 8;

	
	private static AppState instance;
	private int currentState = Uninitialized;
	

	
	private AppState() {
		currentState = Initialized;
	}
	
	public static AppState getAppStateInstance() {
		
		if (instance == null) {
			instance = new AppState();
		}
		
		return instance;
	}
	
	
	public int getCurrentState() {
		return currentState;
	}
	
	public void setCurrentState(int state) {
		currentState = state;
	}
	
	

}
