package com.flyaudio.flyMediaPlayer.activity;


import com.flyaudio.flyMediaPlayer.R;
import com.flyaudio.flyMediaPlayer.dialog.AboutDialog;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaService;
import com.flyaudio.flyMediaPlayer.until.AllListActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class SettingActivity extends Activity {

	private int skinId;
	private int colorId;
	private int[] skins = { R.drawable.skin_bg1, R.drawable.skin_bg2,
			R.drawable.skin_bg3 }, // 背影图
			colors = { Color.argb(250, 251, 248, 29),// 默认
					Color.argb(250, 255, 0, 0),// 红色
					Color.argb(250, 0, 255, 0),// 绿色
					Color.argb(250, 8, 255, 245),// 蓝色
					Color.argb(250, 185, 10, 245) };// 紫色

	private LinearLayout viewSkin;// 背影图
	private LinearLayout viewLyric;// 歌词

	private ImageButton btnReturn,mEqualizer;

	private RelativeLayout mViewAbout,mViewIdea;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		AllListActivity.getInstance().addActivity(this);
		viewSkin = (LinearLayout) findViewById(R.id.activity_setting_view_skin);
		viewLyric = (LinearLayout) findViewById(R.id.activity_setting_view_lyric);
		btnReturn = (ImageButton) findViewById(R.id.activity_setting_ib_return);
		mEqualizer=(ImageButton)findViewById(R.id.equlaizer_btn);
		mViewAbout=(RelativeLayout)findViewById(R.id.setting_about);
		mViewIdea=(RelativeLayout)findViewById(R.id.setting_idea);
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		skinId = preferences.getInt(MainActivity.PREFERENCES_SKIN,
				R.drawable.skin_bg1);
		colorId = preferences.getInt(MainActivity.PREFERENCES_LYRIC, colors[0]);

		final int size1 = viewSkin.getChildCount();
		for (int i = 0; i < size1; i++) {
			final ImageView imageView = (ImageView) viewSkin.getChildAt(i);
			final int resources = skins[i];
			if (resources == skinId) {
				imageView.setEnabled(false);
			}
			imageView.setId(i);
			imageView.setImageResource(resources);
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					v.setEnabled(false);
					preferences.edit()
							.putInt(MainActivity.PREFERENCES_SKIN, resources)
							.commit();
					for (int j = 0; j < size1; j++) {
						if (v.getId() != j) {
							viewSkin.getChildAt(j).setEnabled(true);
						}
					}
				}
			});
		}

		final int size2 = viewLyric.getChildCount();
		for (int i = 0; i < size2; i++) {
			final LinearLayout layout = (LinearLayout) viewLyric.getChildAt(i);
			final ImageView imageView = (ImageView) layout.getChildAt(0);
			final int color = colors[i];
			if (color == colorId) {
				layout.setEnabled(false);
			}
			imageView.setImageDrawable(new ColorDrawable(color));
			layout.setId(i);
			layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					v.setEnabled(false);
					preferences.edit()
							.putInt(MainActivity.PREFERENCES_LYRIC, color)
							.commit();
					for (int j = 0; j < size2; j++) {
						if (v.getId() != j) {
							viewLyric.getChildAt(j).setEnabled(true);
						}
					}
				}
			});
		}
		
		mViewIdea.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction()==MotionEvent.ACTION_DOWN) {
					mViewAbout.setBackgroundColor(Color.argb(250, 8, 255, 245));
				}
				if (event.getAction()==MotionEvent.ACTION_UP) {
					mViewAbout.setBackgroundColor(Color.WHITE);
				}
				return true;
			}
		});
		mViewAbout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction()==MotionEvent.ACTION_DOWN) {
					mViewAbout.setBackgroundColor(Color.argb(250, 8, 255, 245));
					showAboutDialog();
				}
				if (event.getAction()==MotionEvent.ACTION_UP) {
					mViewAbout.setBackgroundColor(Color.WHITE);
				}
				
				return true;
			}
		});
		mEqualizer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent affectIntent = new Intent(SettingActivity.this, AffectActivity.class);
				affectIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0);
				startActivityForResult(affectIntent, 13);
			}
		});
		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		Intent intent = new Intent(MediaService.BROADCAST_ACTION_SERVICE);
		intent.putExtra(MediaService.INTENT_ACTIVITY,
				MediaService.ACTIVITY_SETTING);
		sendBroadcast(intent);
	}
	private void showAboutDialog() {
		AboutDialog aboutDialog = new AboutDialog(SettingActivity.this);
		aboutDialog.show();
	}

}
