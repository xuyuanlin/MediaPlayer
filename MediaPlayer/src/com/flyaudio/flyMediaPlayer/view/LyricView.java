package com.flyaudio.flyMediaPlayer.view;

import java.util.ArrayList;
import java.util.List;
import com.flyaudio.flyMediaPlayer.objectInfo.LyricItem;
import com.flyaudio.flyMediaPlayer.until.Flog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class LyricView extends TextView {

	private int index = 0;// 索引，第几句
	private int lyricSize = 0;// 歌词的总句数

	private int currentTime = 0;// 当前歌曲的播放位置
	private int dunringTime = 0;// 当前歌曲的持续时间
	private int startTime = 0;// 当前歌词的开始时间

	private float width = 0;// 获取画布的宽
	private float height = 0;// ----高
	private float tempW = 0;// 计算画布的中间位置（宽）
	private float tempH = 0;// ----------------（高）
	private float tempYHigh = 0;// 计算OK的第一句的Y轴位置
	private float tempYLow = 0;// ----二

	private float textHeight = 35;// 单行字的高度
	private float textSize = 33;// 字体的大小

	private Paint currentPaint = null;// 当前句画笔
	private Paint defaultPaint = null;// 非当前句画笔
	private List<LyricItem> mSentenceEntities = new ArrayList<LyricItem>();
	private int[] paintColorsCurrent = { Color.argb(250, 251, 248, 29),
			Color.argb(250, 255, 255, 255) };
	private int[] paintColorsDefault = { Color.argb(250, 255, 255, 255),
			Color.argb(250, 255, 255, 255) };

	private boolean isKLOK = false;

	public LyricView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public LyricView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	public LyricView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		setFocusable(true);

		// 高亮部分
		currentPaint = new Paint();
		currentPaint.setAntiAlias(true);
		currentPaint.setTextAlign(Paint.Align.CENTER);
		currentPaint.setColor(Color.argb(250, 251, 248, 29));
		currentPaint.setTextSize(textSize);
		currentPaint.setTypeface(Typeface.SERIF);

		// 非高亮部分
		defaultPaint = new Paint();
		defaultPaint.setAntiAlias(true);
		defaultPaint.setTextAlign(Paint.Align.CENTER);
		defaultPaint.setColor(Color.argb(250, 255, 255, 255));
		defaultPaint.setTextSize(textSize);
		defaultPaint.setTypeface(isKLOK ? Typeface.SERIF : Typeface.DEFAULT);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		if (canvas == null || lyricSize <= 0
				|| index >= mSentenceEntities.size()) {
			return;
		}

		if (isKLOK) {
			int nextIndex = index + 1;// 下一句
			String text = mSentenceEntities.get(index).getLyric();// 获得歌词
			float len = this.getTextWidth(currentPaint, text);// 该句歌词的精确长度
			float position = dunringTime == 0 ? 0
					: ((float) currentTime - (float) startTime)
							/ (float) dunringTime;// 计算当前位置

			if (index % 2 == 0) {
				float start1 = len / 2;// 第一句的起点位置
				LinearGradient gradient = new LinearGradient(0, 0, len, 0,
						paintColorsCurrent, new float[] { position, position },
						TileMode.CLAMP);// 重绘jian bian
				currentPaint.setShader(gradient);
				canvas.drawText(text, start1, tempYHigh, currentPaint);

				if (nextIndex < lyricSize) {
					text = mSentenceEntities.get(nextIndex).getLyric();
					len = this.getTextWidth(currentPaint, text);
					float start2 = width - len / 2;
					gradient = new LinearGradient(start2, 0, width, 0,
							paintColorsDefault, null, TileMode.CLAMP);
					defaultPaint.setShader(gradient);
					canvas.drawText(text, start2, tempYLow, defaultPaint);
				}
			} else {
				float start2 = width - len / 2;
				float w = width > len ? width - len : 0;
				LinearGradient gradient = new LinearGradient(w, 0, width, 0,
						paintColorsCurrent, new float[] { position, position },
						TileMode.CLAMP);
				defaultPaint.setShader(gradient);
				canvas.drawText(text, start2, tempYLow, defaultPaint);

				if (nextIndex < lyricSize) {
					text = mSentenceEntities.get(nextIndex).getLyric();
					len = this.getTextWidth(currentPaint, text);
					float start1 = len / 2;
					gradient = new LinearGradient(0, 0, len, 0,
							paintColorsDefault, null, TileMode.CLAMP);
					currentPaint.setShader(gradient);
					canvas.drawText(text, start1, tempYHigh, currentPaint);
				}
			}
		} else {
			float plus = dunringTime == 0 ? 0
					: (((float) currentTime - (float) startTime) / (float) dunringTime)
							* (float) 30;
			
			canvas.translate(0, -plus);

			try {
				canvas.drawText(mSentenceEntities.get(index).getLyric(), tempW,
						tempH, currentPaint);

				float tempY = tempH;
			
				for (int i = index - 1; i >= 0; i--) {
					
					tempY = tempY - textHeight;

					canvas.drawText(mSentenceEntities.get(i).getLyric(), tempW,
							tempY, defaultPaint);
				}
				tempY = tempH;
				
				for (int i = index + 1; i < lyricSize; i++) {
					
					tempY = tempY + textHeight;
					if (tempY > height) {
						break;
					}
					canvas.drawText(mSentenceEntities.get(i).getLyric(), tempW,
							tempY, defaultPaint);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				// setText("没有歌词文件......");
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);

		this.width = w;
		this.height = h;
		this.tempW = w / 2;
		this.tempH = h / 2;
		this.tempYHigh = tempH;
		this.tempYLow = tempH + textHeight;
	}

	private int getTextWidth(Paint paint, String str) {
		int iRet = 0;
		if (str != null && str.length() > 0) {
			int len = str.length();
			float[] widths = new float[len];
			paint.getTextWidths(str, widths);
			for (int j = 0; j < len; j++) {
				iRet += (int) Math.ceil(widths[j]);
			}
		}
		return iRet;
	}

	public void setKLOK(boolean isKLOK) {
		this.isKLOK = isKLOK;
	}

	public void setLyricHighlightColor(int color) {
		Flog.d("LyricviewView--setLyricHighLightColor()");
		paintColorsCurrent = new int[] { color, Color.argb(250, 255, 255, 255) };
		currentPaint.setColor(color);
	}

	public void setSentenceEntities(List<LyricItem> mSentenceEntities) {
		Flog.d("Lyricview--setSentenceEntities()");
		this.mSentenceEntities = mSentenceEntities;
		this.lyricSize = mSentenceEntities.size();
	}

	public void setIndex(int[] indexInfo) {
		this.index = indexInfo[0];
		this.currentTime = indexInfo[1];
		this.startTime = indexInfo[2];
		this.dunringTime = indexInfo[3];
	}

	public void clear() {
		this.mSentenceEntities.clear();
		this.index = 0;
		this.lyricSize = 0;
		this.invalidate();
	}
}
