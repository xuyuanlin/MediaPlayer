package com.flyaudio.flyMediaPlayer.view;

import java.util.ArrayList;
import java.util.List;

import com.flyaudio.flyMediaPlayer.objectInfo.LyricItem;
import com.flyaudio.flyMediaPlayer.until.Flog;
import com.flyaudio.flyMediaPlayer.until.TimeParseTool;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class MyLyricView extends TextView {
	private Paint mPaint;
	private float mX;
	private Paint mPathPaint;
	public int index = 0;
	public float mTouchHistoryY;
	private int mY;
	private int currentDuringTime;
	private float middleY;
	private final int DY = 60;
	public float driftx;
	public float drifty;
	private float drift_r;
	public boolean showprogress;
	public int temp = 0;
	private int lyricSize;
	public List<LyricItem> mSentenceEntities = new ArrayList<LyricItem>();

	// public String[] time =
	// {"1490","14740","20940","27140","33100","40880","46790","53830","59680","66410","69760","73000","79610","82970","86260","93340","96320","100340","106380","114580","119970","123060","126380","133320","140680","174980","180190","187590","192830","199590","203160","206520","213210","216630","219930","226040","230090","233340","239900","248300","253060","256620","259970","266730","274640","281540","99999999"};
	public MyLyricView(Context context) {
		super(context);
		init();
	}

	public MyLyricView(Context context, AttributeSet attr) {
		super(context, attr);
		init();
	}

	public MyLyricView(Context context, AttributeSet attr, int i) {
		super(context, attr, i);
		init();
	}

	private void init() {
		setFocusable(true);
		// 非高亮
		mPaint = new Paint();
		mPaint.setAntiAlias(true);	
		mPaint.setTextSize(30);
		mPaint.setColor(Color.argb(250, 255, 255, 255));
		mPaint.setTypeface(Typeface.SERIF);

		// 高亮
		mPathPaint = new Paint();
		mPathPaint.setAntiAlias(true);
		mPathPaint.setTextSize(36);
		mPathPaint.setColor(Color.argb(250, 251, 248, 29));
		mPathPaint.setTypeface(Typeface.SERIF);
	}

	protected void onDraw(Canvas canvas) {

		Flog.d("MyLyricView--onDraw()----start");
		super.onDraw(canvas);
//		if (drifty > -40.0)
//			drifty = (float) (drifty - 40.0 / (currentDuringTime / 100));

		int j = (int) (-drifty / 60);
		if (temp < j) {
			temp++;
		} else if (temp > j) {
			temp--;
		}
		if (index + temp >= 0 && index + temp < lyricSize - 1)
			drift_r = drifty;

		// canvas.drawColor(0xEFeffff);
		Paint p = mPaint;
		Paint p2 = mPathPaint;
		p.setTextAlign(Paint.Align.CENTER);

		if (index == -1)
			return;
		p2.setTextAlign(Paint.Align.CENTER);
		Flog.d("MyLyricView--onDraw()----" + mSentenceEntities.size());
		Flog.d("MyLyricView--onDraw()-index---" + index);
		if (index < lyricSize && index > 0) {		
		    canvas.drawText(mSentenceEntities.get(index).getLyric(), mX,
							middleY + drift_r, p2);
		}

		if (showprogress && index + temp < lyricSize) {
			Flog.d("---MyLyricView--onDraw()----2");
			p2.setTextAlign(Paint.Align.LEFT);
			if (index + temp >= 0) {
				canvas.drawText(
						TimeParseTool.MsecParseTime(mSentenceEntities.get(
								index + temp).getTime()), 0, middleY, p2);
			} else {
				canvas.drawText("00:00", 0, middleY, p2);
			}
			canvas.drawLine(0, middleY + 1, mX * 2, middleY + 1, p2);
		}
		float tempY = middleY + drift_r;

		for (int i = index - 1; i >= 0; i--) {
			Flog.d("---MyLyricView--onDraw()----3");

			tempY = tempY - DY;
			if (tempY < 0) {
				break;
			}
			if (i < lyricSize) {
				Flog.d("MyLyricView--onDraw()-i--1---" + i);
				canvas.drawText(mSentenceEntities.get(i).getLyric(), mX,
						tempY, p);
			}

		}
		tempY = middleY + drift_r;

		for (int i = index+1; i < lyricSize; i++) {
			tempY = tempY + DY;
			if (tempY > mY) {
				break;
			}
			Flog.d("MyLyricView--onDraw()-i--2---" + i);
			canvas.drawText(mSentenceEntities.get(i).getLyric(), mX, tempY, p);
		}

	}

	protected void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);
		mX = w * 0.5f;
		mY = h;
		middleY = h * 0.5f;
	}

	public void setLyricHighlightColor(int color) {
		Flog.d("LyrcView1--setLyricHighLightColor()");
		mPathPaint.setColor(color);
	}

	public void clear() {
		this.mSentenceEntities.clear();
		this.index = 0;
		this.lyricSize = 0;
		this.invalidate();
	}

	public void setSentenceEntities(List<LyricItem> mSentenceEntities) {
		Flog.d("LyrcView1--setSentenceEntities()");
		this.mSentenceEntities = mSentenceEntities;
		Flog.d("LyrcView1--setSentenceEntities()--" + mSentenceEntities.size());
		this.lyricSize = mSentenceEntities.size();
	}

	public float updateIndex(int mp3Current, int mp3Duration) {
		Flog.d("---MyLyricView------updateIndex()--start");

//		Flog.d("---MyLyricView------updateIndex()--mp3Current--" + mp3Current);
//		Flog.d("---MyLyricView------updateIndex()--mp3Duration--" + mp3Duration);
//		Flog.d("---MyLyricView------updateIndex()--lyricSize--" + lyricSize);
		if (mp3Current < mp3Duration) {
			for (int i = 0; i < lyricSize; i++) {
				if (i < lyricSize - 1) {
//					Flog.d("---MyLyricView------updateIndex()--mSentenceEntities.get(i).getLrcTime()--"
//							+ mSentenceEntities.get(i).getTime());
//					Flog.d("---MyLyricView------updateIndex()--mSentenceEntities.get(i+1).getLrcTime()--"
//							+ mSentenceEntities.get(i + 1).getTime());
					if (mp3Current < mSentenceEntities.get(i).getTime()
							&& i == 0) {

						index = i;
						currentDuringTime = mSentenceEntities.get(i)
								.getTime();
						Flog.d("---MyLyricView------updateIndex()--index--0--"
								+ index);
					}
					if (mp3Current > mSentenceEntities.get(i).getTime()
							&& mp3Current < mSentenceEntities.get(i + 1)
									.getTime()
							)//防止后面歌词出现乱位比如：后面两句如果是显示歌词来子那里的就会报错
					{

						index = i;
						currentDuringTime = mSentenceEntities.get(index + 1)
								.getTime()
								- mSentenceEntities.get(index).getTime();
//						index++;
						drifty = 0;
						driftx = 0;
						Flog.d("---MyLyricView------updateIndex()--index--1--"
								+ index);
					}
				}
				if (i == lyricSize - 1
						&& mp3Current > mSentenceEntities.get(i).getTime()) {
					index = i;
					currentDuringTime = mp3Current
							- mSentenceEntities.get(i).getTime();
					Flog.d("---MyLyricView------updateIndex()--index--2--"
							+ index);
				}
			}
		}
		if (drifty > -60.0)
			drifty = (float) (drifty - 60.0 / (currentDuringTime / 100));
		if (index == -1)
			return -1;
		Flog.d("---MyLyricView------updateIndex()--drifty--" + drifty);
		Flog.d("---MyLyricView------updateIndex()--index--" + index);
		Flog.d("---MyLyricView------updateIndex()--end");
		return drifty;
	}

	public void setIndex(int index) {
		this.index = index;

	}

	public boolean repair() {
		if (index <= 0) {
			index = 0;
			return false;
		}
		if (index > lyricSize){
			index = lyricSize-1;
		}
			
		return true;
	}

}
