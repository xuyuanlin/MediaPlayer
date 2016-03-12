package com.flyaudio.flyMediaPlayer.provider;

import com.flyaudio.flyMediaPlayer.R;
import com.flyaudio.flyMediaPlayer.activity.LogoActivity;
import com.flyaudio.flyMediaPlayer.activity.MainActivity;
import com.flyaudio.flyMediaPlayer.objectInfo.MyApp;
import com.flyaudio.flyMediaPlayer.perferences.CoverList;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaService;
import com.flyaudio.flyMediaPlayer.until.AlbumUtil;
import com.flyaudio.flyMediaPlayer.until.Flog;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class MyAppWidget extends AppWidgetProvider {
	public static final String BROADCAST_ACTION_NOT_PLAY = "com.flyaudio.action.play";
	public static final String BROADCAST_ACTION_NOT_NEXT = "com.flyaudio.action.next";
	public static final String BROADCAST_ACTION_NOT_PREV = "com.flyaudio.action.previous";
	public static final String BROADCAST_ACTION_NOT_STATE = "com.flyaudio.action.state";
	public static final String BROADCAST_ACTION_NOT_EXIT = "com.flyaudio.action.exit";

	// public static final String BROADCAST_ACTION_NOT_ABLUM =
	// "com.flyaudio.action.ablum";

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Flog.d("MyAppWidget---onDeleted()--start");
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		Flog.d("MyAppWidget---onDisabled()--start");
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		Flog.d("MyAppWidget----onEnabled()--start");
//		myApp=(MyApp)context.getApplicationContext();
		
		super.onEnabled(context);
	
	}
	

	@Override
	public void onReceive(Context context, Intent intent) {
	
		Flog.d("MyAppWidget---onReceive()--start");
		Flog.d("MyAppWidget---onReceive()--start--action--"
				+ intent.getAction());
		AlbumUtil albumUtil = new AlbumUtil();
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.appwidget_media);
		if (intent.getAction().equals("com.flyaudiomedia.pause")) {
			views.setImageViewResource(R.id.appwidget_play,
					R.drawable.appwidget_btn_play_style);

		} else if (intent.getAction().equals("com.flyaudiomedia.play")) {
			views.setImageViewResource(R.id.appwidget_play,
					R.drawable.appwidget_btn_pause_style);

		} else if (intent.getAction().equals("com.flyaudiomedia.musicinfo")) {			
			String musicName = intent.getStringExtra("musicname");
			String musicArtist = intent.getStringExtra("musicartist");
			String musicPath = intent.getStringExtra("musicpath");	
			Flog.d("MyAppWidget---onReceive()--musicName--" + musicName);
			Flog.d("MyAppWidget---onReceive()--musicArtist--" + musicArtist);
			Flog.d("MyAppWidget---onReceive()--musicPath--" + musicPath);
			views.setTextViewText(R.id.appwidget_item_name, musicName);
			views.setTextViewText(R.id.appwidget_item_artist, musicArtist);
			CoverList.cover = albumUtil.scanAlbumImage(musicPath);
			if (CoverList.cover == null) {
				Flog.d("MyAppWidget---onReceive()---CoverList.cover==null");
				views.setImageViewResource(R.id.appwidget_item_album,
						R.drawable.main_img_album);
			} else {
				views.setImageViewBitmap(R.id.appwidget_item_album,
						CoverList.cover);
			}
			intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClass(context, LogoActivity.class);
			intent.putExtra("musicpath", musicPath);		
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			PendingIntent ablumPendingIntent = PendingIntent.getActivity(
					context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			views.setOnClickPendingIntent(R.id.appwidget_item_album,
					ablumPendingIntent);
		}

		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		ComponentName componentName = new ComponentName(context,
				MyAppWidget.class);
		appWidgetManager.updateAppWidget(componentName, views);
		super.onReceive(context, intent);
		Flog.d("MyAppWidget---onReceive()--end");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		
		final ComponentName serviceName = new ComponentName(context,
				MediaService.class);
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.appwidget_media);

		views.setImageViewResource(R.id.appwidget_play,
				R.drawable.player_btn_play_style);
		Intent playIntent = new Intent(MediaService.PLAY_ACTION);
		playIntent.setComponent(serviceName);
		PendingIntent playPending = PendingIntent.getService(context, 0,
				playIntent, 0);
		views.setOnClickPendingIntent(R.id.appwidget_play, playPending);

		Intent prevIntent = new Intent(MediaService.PREVIOUS_ACTION);
		prevIntent.setComponent(serviceName);
		PendingIntent prevPending = PendingIntent.getService(context, 0,
				prevIntent, 0);
		views.setOnClickPendingIntent(R.id.appwidget_previous, prevPending);

		Intent nextIntent = new Intent(MediaService.NEXT_ACTION);
		nextIntent.setComponent(serviceName);
		
		PendingIntent nextPending = PendingIntent.getService(context, 0,
				nextIntent, 0);
		views.setOnClickPendingIntent(R.id.appwidget_next, nextPending);
		Intent musciInfoIntent = new Intent(MediaService.MUSICINFO_ACTION);
		context.sendBroadcast(musciInfoIntent);
		Intent stateIntent = new Intent(MediaService.STATE_ACTION);
		context.sendBroadcast(stateIntent);

		appWidgetManager.updateAppWidget(appWidgetIds, views);
		Flog.d("MyAppWidget---onUpdate()--end");
	}

}
