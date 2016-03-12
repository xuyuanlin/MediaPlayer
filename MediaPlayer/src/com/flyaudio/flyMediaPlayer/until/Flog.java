package com.flyaudio.flyMediaPlayer.until;

import android.util.Log;

public class Flog {
	public static String TAG = "flyMediaPlayer";
	static final boolean DEBUG = true;

	public static void d(String msg) {
		if (DEBUG)
			Log.d(TAG, msg);
	}

}
