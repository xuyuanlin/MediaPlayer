package com.flyaudio.flyMediaPlayer.objectInfo;
//歌词的信息
public class LyricItem {

	private String lyric;//单句歌词
	private int time;//歌词的时间
	private String oneTime;

	public String getLyric() {
		return lyric;
	}

	public void setLyric(String lyric) {
		this.lyric = lyric;
	}
	public String getOnetime() {
		return oneTime;
	}

	public void setOnetime(String oneTime) {
		this.oneTime = oneTime;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
}
