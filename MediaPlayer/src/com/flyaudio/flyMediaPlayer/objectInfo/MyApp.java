package com.flyaudio.flyMediaPlayer.objectInfo;

import android.R.string;
import android.app.Application;
import android.content.Context;

public class MyApp extends Application {

	private String path;
	private int position;
	/*private String url;
	private boolean flag;*/


	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

}
