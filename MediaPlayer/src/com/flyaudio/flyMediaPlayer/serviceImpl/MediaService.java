package com.flyaudio.flyMediaPlayer.serviceImpl;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.flyaudio.flyMediaPlayer.R;
import com.flyaudio.flyMediaPlayer.activity.MainActivity;
import com.flyaudio.flyMediaPlayer.activity.PlayerActivity;
import com.flyaudio.flyMediaPlayer.activity.ScanActivity;
import com.flyaudio.flyMediaPlayer.activity.SettingActivity;
import com.flyaudio.flyMediaPlayer.data.DBDao;
import com.flyaudio.flyMediaPlayer.objectInfo.LyricItem;
import com.flyaudio.flyMediaPlayer.objectInfo.MusicInfo;
import com.flyaudio.flyMediaPlayer.objectInfo.MyApp;
import com.flyaudio.flyMediaPlayer.perferences.CoverList;
import com.flyaudio.flyMediaPlayer.perferences.FavoriteList;
import com.flyaudio.flyMediaPlayer.perferences.FolderList;
import com.flyaudio.flyMediaPlayer.perferences.LyricList;
import com.flyaudio.flyMediaPlayer.perferences.MusicList;
import com.flyaudio.flyMediaPlayer.provider.MyAppWidget;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnServiceBinderListener;
import com.flyaudio.flyMediaPlayer.until.AlbumUtil;
import com.flyaudio.flyMediaPlayer.until.Flog;
import com.flyaudio.flyMediaPlayer.until.LyricParser;
import com.flyaudio.flyMediaPlayer.until.ScanUtil;
import com.flyaudio.flyMediaPlayer.view.LyricView;
import com.flyaudio.flyMediaPlayer.view.MyLyricView;

public class MediaService extends Service implements OnBufferingUpdateListener {

	public static final int CONTROL_COMMAND_PLAY = 0;// 控制命令：播放或者暂停
	public static final int CONTROL_COMMAND_PREVIOUS = 1;// 控制命令：上一首
	public static final int CONTROL_COMMAND_NEXT = 2;// 控制命令：下一首
	public static final int CONTROL_COMMAND_MODE = 3;// 控制命令：播放模式切换
	public static final int CONTROL_COMMAND_REWIND = 4;// 控制命令：快退
	public static final int CONTROL_COMMAND_FORWARD = 5;// 控制命令：快进
	public static final int CONTROL_COMMAND_REPLAY = 6;// 控制命令：用于快退、快进后的继续播放

	public static final int ACTIVITY_SCAN = 0x101;// 扫描界面
	public static final int ACTIVITY_MAIN = 0x102;// 主界面
	public static final int ACTIVITY_PLAYER = 0x103;// 播放界面
	public static final int ACTIVITY_SETTING = 0x104;// 设置界面

	public static final String INTENT_LIST_PATH = "musicpath";// 列表
	public static final String INTENT_ACTIVITY = "activity";// 区分来自哪个界面
	public static final String INTENT_LIST_PAGE = "list_page";// 列表页面
	public static final String INTENT_LIST_POSITION = "list_position";// 列表当前项
	public static final String INTENT_FOLDER_POSITION = "folder_position";// 文件夹列表当前项
	public static final String BROADCAST_ACTION_SERVICE = "com.flyaudio.action.service";// 广播标志־

	public static final String PLAY_ACTION = "com.flyaudiomedia.playmusic";
	public static final String NEXT_ACTION = "com.flyaudiomedia.nextone";
	public static final String PREVIOUS_ACTION = "com.flyaudiomedia.previousone";
	public static final String STATE_ACTION = "com.flyaudiomedia.state";
	public static final String MUSICINFO_ACTION = "com.flyaudiomedia.musicInfo";

	private static final int MEDIA_PLAY_ERROR = 0;
	private static final int MEDIA_PLAY_START = 1;
	private static final int MEDIA_PLAY_UPDATE = 2;
	private static final int MEDIA_PLAY_COMPLETE = 3;
	private static final int MEDIA_PLAY_UPDATE_LYRIC = 4;
	private static final int MEDIA_PLAY_REWIND = 5;
	private static final int MEDIA_PLAY_FORWARD = 6;
	private static final int MEDIA_BUTTON_ONE_CLICK = 7;
	private static final int MEDIA_BUTTON_DOUBLE_CLICK = 8;
	private static final int MEDIA_BUTTON_SCAN_MUISCLIST = 9;

	public static final String PREFERENCES_PATH = "musicpath";
	public static final String PREFERENCES_STATE = "state";
	public static final String PREFERENCES_POSITION = "position";

	private final int MODE_NORMAL = 0;// 顺序播放，放到最后一首停止
	private final int MODE_REPEAT_ONE = 1;// 单曲循环
	private final int MODE_REPEAT_ALL = 2;// 全部循环
	private final int MODE_RANDOM = 3;// 随即播放
	private final int UPDATE_LYRIC_TIME = 150;// 歌词更新间隔0.15秒
	private final int UPDATE_UI_TIME = 1000;// UI更新间隔1秒

	private MusicInfo info;// 歌曲的详情
	private List<LyricItem> lyricList;// 歌词列表
	private List<Integer> positionList;// 歌词当前项的集合
	private String mp3Path;// MP3文件路径
	private String lyricPath;// 歌词路径

	private int mode = MODE_NORMAL;// 播放模式(默认顺序播放)
	private int page = MainActivity.SLIDING_MENU_ALL;// 列表页面(默认全部歌曲)
	private int lastPage = 0;// 记住上一次的列表页面
	private int position = 0;// 列表当前项
	private int folderPosition = 0;// 文件夹列表当前项
	private int mp3Current = 0;// 歌曲当前时间
	private int mp3Duration = 0;// 歌曲总时间

	private int buttonClickCounts = 0;

	private boolean hasLyric = false;// 是否有歌词
	private boolean isCommandPrevious = false;// 是否属于上一首操作命令
	private boolean isSuccess = false;

	private MediaPlayer mediaPlayer;
	private MediaBinder mBinder;
	private AlbumUtil albumUtil;
	private LyricView lyricView;
	private LyricView lrcView;
	// private LrcView lyricView1;
	private MyLyricView myLyricView;
	private RemoteViews remoteViews;
	private ServiceHandler mHandler;
	private ServiceReceiver receiver;
	private Notification notification;
	private SharedPreferences preferences;
	private String url;
	private boolean flag;
	private MyApp myApp;

	private boolean running = true;
	private boolean isRunning = false;
	private boolean isPlay = false;

	// private List<LrcContent> mlrcList = new ArrayList<LrcContent>(); //
	// 存放歌词列表对象

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Flog.d("MediaService--onCreate()--start");
		myApp = (MyApp) getApplication();
		mediaPlayer = new MediaPlayer();
		mHandler = new ServiceHandler(this);
		mBinder = new MediaBinder();
		albumUtil = new AlbumUtil();
		lyricList = new ArrayList<LyricItem>();
		positionList = new ArrayList<Integer>();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnBufferingUpdateListener(this);

		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				Flog.d("MediaService---onPrepared()---start");
				mp.start();
				mp3Current = 0;// 重置
				prepared();// 准备播放
				Flog.d("MediaService---onPrepared()---end");
			}
		});
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				Flog.d("MediaService---onCompletion()---start");
				removeAllMsg();// 移除所有消息
				mHandler.sendEmptyMessage(MEDIA_PLAY_COMPLETE);
				Flog.d("MediaService---onCompletion()---end");
			}
		});
		mediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				Flog.d("MediaService---onError()---start");
				removeAllMsg();
				mp.reset();
				page = MainActivity.SLIDING_MENU_ALL;
				position = 0;
				if (mp3Path != null) {
					File file = new File(mp3Path);
					Flog.d("MainActivity-----File-----" + file);
					if (file.exists()) {
						Toast.makeText(getApplicationContext(), "播放出错",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "文件不存在",
								Toast.LENGTH_SHORT).show();
						mHandler.sendEmptyMessage(MEDIA_PLAY_ERROR);
					}
				}

				mp3Path = null;
				Flog.d("MediaService---onError()---end");
				return true;
			}
		});
		mBinder.setOnServiceBinderListener(new OnServiceBinderListener() {

			@Override
			public void seekBarStartTrackingTouch() {
				// TODO Auto-generated method stub
				Flog.d("MediaService---seekBarStartTrackingTouch()---start");
				if (mediaPlayer.isPlaying()) {
					removeUpdateMsg();
				}
				Flog.d("MediaService---seekBarStartTrackingTouch()---end");
			}

			@Override
			public void seekBarStopTrackingTouch(int progress) {
				// TODO Auto-generated method stub
				Flog.d("MediaService---seekBarStopTrackingTouch()---start");
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.seekTo(progress);
					update();
				}
				Flog.d("MediaService---seekBarStopTrackingTouch()---end");
			}

			@Override
			public void LrcStartTrackingTouch() {
				// TODO Auto-generated method stub
				Flog.d("MediaService---LrcStartTrackingTouch()---start");
				if (mediaPlayer.isPlaying()) {
					removeUpdateMsg();
				}
				Flog.d("MediaService---LrcStartTrackingTouch()---end");

			}

			@Override
			public void LrcStopTrackingTouch() {
				// TODO Auto-generated method stub
				Flog.d("MediaService---LrcStopTrackingTouch()---start");
				if (mediaPlayer.isPlaying()) {
					// mediaPlayer.seekTo(updateplayer());
					// update();

					updateplayer();
					update();

				}
				Flog.d("MediaService---LrcStopTrackingTouch()---end");
			}

			@Override
			public void getslidestart() {
				// TODO Auto-generated method stub
				Flog.d("MediaService---getslidestart()---start");
				slidestart();
				Flog.d("MediaService---getslidestart()---end");
			}

			@Override
			public void lrc(LyricView lrcView, LyricView lyricView,
					MyLyricView myLyricView, boolean isKLOK) {
				// TODO Auto-generated method stub
				Flog.d("MediaService---lrc()---start");
				MediaService.this.myLyricView = myLyricView;
				MediaService.this.lyricView = lyricView;
				MediaService.this.lrcView = lrcView;
				// MediaService.this.lyricView1 = lrcView;

				if (MediaService.this.lyricView != null
						&& MediaService.this.lrcView != null) {
					// Flog.d("mBinder.setOnServiceBinderListener---lrc()--1");
					MediaService.this.lyricView.setKLOK(isKLOK);
					MediaService.this.lrcView.setKLOK(isKLOK);
				}
				Flog.d("MediaService---lrc()---end");
			}

			@Override
			public void control(int command) {
				// TODO Auto-generated method stub
				Flog.d("MediaService---control()---start");
				switch (command) {
				case CONTROL_COMMAND_PLAY:// 播放与暂停
					Flog.d("MediaService---control()---CONTROL_COMMAND_PLAY--start");

					if (mediaPlayer.isPlaying()) {
						pause();
					} else {

						if (mp3Path != null) {
							mediaPlayer.start();
							prepared();
						} else {// 无指定情况下播放全部歌曲列表的第一首
							/*
							 * preferences =
							 * getSharedPreferences(MainActivity.PREFERENCES_NAME
							 * , Context.MODE_PRIVATE);
							 * mp3Path=preferences.getString("musicpath", null);
							 * position=preferences.getInt("position", 0);
							 * Flog.d(
							 * "MediaService---control()---CONTROL_COMMAND_PLAY--mp3Path--"
							 * +mp3Path); Flog.d(
							 * "MediaService---control()---CONTROL_COMMAND_PLAY--position--"
							 * +position); if (mp3Path!=null) { play(); }else {
							 * startServiceCommand(); }
							 */
							startServiceCommand();
						}
					}
					Flog.d("MediaService---control()---CONTROL_COMMAND_PLAY--end");
					break;

				case CONTROL_COMMAND_PREVIOUS:// 上一首
					Flog.d("MediaService---control()---CONTROL_COMMAND_PREVIOUS--start");
					previous();
					Flog.d("MediaService---control()---CONTROL_COMMAND_PREVIOUS--end");
					break;

				case CONTROL_COMMAND_NEXT:// 下一首
					Flog.d("MediaService---control()---CONTROL_COMMAND_NEXT--start");
					next();
					Flog.d("MediaService---control()---CONTROL_COMMAND_NEXT--end");
					break;

				case CONTROL_COMMAND_MODE:// 播放模式
					Flog.d("MediaService---control()---CONTROL_COMMAND_MODE--start");
					if (mode < MODE_RANDOM) {
						mode++;
					} else {
						mode = MODE_NORMAL;
					}
					switch (mode) {
					case MODE_NORMAL:
						Flog.d("MediaService---control()---MODE_NORMAL--start");
						Toast.makeText(getApplicationContext(), "顺序播放",
								Toast.LENGTH_SHORT).show();
						Flog.d("MediaService---control()---MODE_NORMAL--end");
						break;

					case MODE_REPEAT_ONE:
						Flog.d("MediaService---control()---MODE_REPEAT_ONE--start");
						Toast.makeText(getApplicationContext(), "单曲循环",
								Toast.LENGTH_SHORT).show();
						Flog.d("MediaService---control()---MODE_REPEAT_ONE--end");
						break;

					case MODE_REPEAT_ALL:
						Flog.d("MediaService---control()---MODE_REPEAT_ALL--start");
						Toast.makeText(getApplicationContext(), "全部循环",
								Toast.LENGTH_SHORT).show();
						Flog.d("MediaService---control()---MODE_REPEAT_ALL--end");
						break;

					case MODE_RANDOM:
						Flog.d("MediaService---control()---MODE_RANDOM--start");
						Toast.makeText(getApplicationContext(), "随机播放",
								Toast.LENGTH_SHORT).show();
						Flog.d("MediaService---control()---MODE_RANDOM--end");
						break;
					}
					mBinder.modeChange(mode);
					Flog.d("MediaService---control()---CONTROL_COMMAND_MODE--end");
					break;

				case CONTROL_COMMAND_REWIND:// 快退
					Flog.d("MediaService---control()---CONTROL_COMMAND_REWIND--start");
					if (mediaPlayer.isPlaying()) {
						removeAllMsg();
						rewind();
					}
					Flog.d("MediaService---control()---CONTROL_COMMAND_REWIND--end");
					break;

				case CONTROL_COMMAND_FORWARD:// 快进
					Flog.d("MediaService---control()---CONTROL_COMMAND_FORWARD--start");
					if (mediaPlayer.isPlaying()) {
						removeAllMsg();
						forward();
					}
					Flog.d("MediaService---control()---CONTROL_COMMAND_FORWARD--end");
					break;
				case CONTROL_COMMAND_REPLAY:// 用于快退、快进后的继续播放
					Flog.d("MediaService---control()---CONTROL_COMMAND_REPLAY--start");
					if (mediaPlayer != null) {
						if (mediaPlayer.isPlaying()) {
							replay();
						}
					}
					Flog.d("MediaService---control()---CONTROL_COMMAND_REPLAY--end");
					break;
				}
			}

		});
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		mode = preferences.getInt(MainActivity.PREFERENCES_MODE, MODE_NORMAL);// 取出上次的播放模式

		notification = new Notification();// 通知栏相关
		notification.icon = R.drawable.ic_launcher;
		notification.flags = Notification.FLAG_NO_CLEAR;
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClass(getApplicationContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		notification.contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, intent, 0);
		remoteViews = new RemoteViews(getPackageName(),
				R.layout.notification_item);
		if (mediaPlayer.isPlaying()) {
			remoteViews.setImageViewResource(R.id.not_play,
					R.drawable.img_button_notification_play_play);
		} else {
			remoteViews.setImageViewResource(R.id.not_play,
					R.drawable.img_button_notification_play_pause);
		}

		receiver = new ServiceReceiver();// 注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
		intentFilter.addAction(BROADCAST_ACTION_SERVICE);
		// intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		intentFilter.addAction("android.intent.action.HEADSET_PLUG");
		// intentFilter.addAction(MyAppWidget.BROADCAST_ACTION_NOT_ABLUM);
		registerReceiver(receiver, intentFilter);
		// appwidget和notification广播
		IntentFilter appIntentFilter = new IntentFilter();
		appIntentFilter.addAction(PLAY_ACTION);
		appIntentFilter.addAction(NEXT_ACTION);
		appIntentFilter.addAction(PREVIOUS_ACTION);
		appIntentFilter.addAction(MUSICINFO_ACTION);
		appIntentFilter.addAction(STATE_ACTION);
		appIntentFilter.addAction(MyAppWidget.BROADCAST_ACTION_NOT_NEXT);
		appIntentFilter.addAction(MyAppWidget.BROADCAST_ACTION_NOT_PLAY);
		appIntentFilter.addAction(MyAppWidget.BROADCAST_ACTION_NOT_PREV);
		appIntentFilter.addAction(MyAppWidget.BROADCAST_ACTION_NOT_STATE);
		appIntentFilter.addAction(MyAppWidget.BROADCAST_ACTION_NOT_EXIT);
		registerReceiver(appWidgetReceiver, appIntentFilter);

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);// 获取电话通讯服务
		telephonyManager.listen(new ServicePhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);// 创建一个监听对象，监听电话状态改变事件

		Flog.d("MediaService--onCreate()--end");
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int percent) {
		// TODO Auto-generated method stub

		Log.d("mediaService" + "onBufferingUpdate", percent + " buffer");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Flog.d("---MediaService------onStartCommand()-------start");

		if (intent != null) {
			boolean isFrist = false;
			String action = intent.getAction();
			preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
					Context.MODE_PRIVATE);
			Flog.d("---MediaService------onStartCommand()-------action--"
					+ action);
			if (action != null) {
				isRunning = preferences.getBoolean(PREFERENCES_STATE, false);
				Flog.d("---MediaService------onStartCommand()-------isRunning--"
						+ isRunning);
				if (isRunning) {
					Flog.d("---MediaService------onStartCommand()-------MusicList.list.size()--"
							+ MusicList.list.size());
					if (MusicList.list.size() == 0) {
						getMusicList();
					}
					isFrist = true;
					isRunning = false;
					preferences.edit().putBoolean(PREFERENCES_STATE, isRunning)
							.commit();
					mp3Path = preferences.getString(PREFERENCES_PATH, null);
					position = preferences.getInt(PREFERENCES_POSITION, 0);
					Flog.d("---MediaService------onStartCommand()-------mp3Path--"
							+ mp3Path);
					Flog.d("---MediaService------onStartCommand()-------position--"
							+ position);

				} else {
					isFrist = false;
				}
				if (action.equals("com.flyaudiomedia.playmusic")) {
					Flog.d("---MediaService------onStartCommand()-----playmusic");
					Flog.d("---MediaService------onStartCommand()-----playmusic--mediaPlayer.isPlaying()---2"
							+ mediaPlayer.isPlaying());
					if (mediaPlayer.isPlaying()) {
						pause();

					} else {
						if (mp3Path != null) {
							if (isFrist) {
								page = MainActivity.SLIDING_MENU_ALL;
								play();
							} else {
								mediaPlayer.start();
								prepared();
							}

						}
					}
				} else if (action.equals("com.flyaudiomedia.nextone")) {
					next();
					Intent playIntent = new Intent("com.flyaudiomedia.play");
					sendBroadcast(playIntent);
				} else if (action.equals("com.flyaudiomedia.previousone")) {
					previous();
					Intent playIntent = new Intent("com.flyaudiomedia.play");
					sendBroadcast(playIntent);
				}
			}
		}

		Bundle bundle = intent.getExtras();
		if (bundle != null && !bundle.isEmpty()) {
			page = bundle.getInt(INTENT_LIST_PAGE, 0);
			position = bundle.getInt(INTENT_LIST_POSITION, 0);
			myApp.setPosition(position);
			Flog.d("MediaService--onStartCommand()---position---" + position);
			folderPosition = bundle.getInt(INTENT_FOLDER_POSITION,
					folderPosition);
			url = bundle.getString("url");
			flag = bundle.getBoolean("flag");
			Flog.d("mediaService---Onstartcommand()---" + url + "---" + "---"
					+ flag);
			play();
		}

		Flog.d("---MediaService------onStartCommand()-------end");
		return super.onStartCommand(intent, flags, startId);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Flog.d("---MediaService------onDestroy()-----start");
		Flog.d("---MediaService------onDestroy()-----mp3Path--" + mp3Path);
		Flog.d("---MediaService------onDestroy()-----position--" + position);
		super.onDestroy();
		isRunning = true;
		if (mediaPlayer != null) {
			stopForeground(true);
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			removeAllMsg();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		unregisterReceiver(receiver);
		unregisterReceiver(appWidgetReceiver);
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		preferences.edit().putInt(MainActivity.PREFERENCES_MODE, mode).commit();// 保存上次的播放模式
		preferences.edit().putString(PREFERENCES_PATH, mp3Path).commit();
		preferences.edit().putInt(PREFERENCES_POSITION, position).commit();
		preferences.edit().putBoolean(PREFERENCES_STATE, isRunning).commit();

		Flog.d("---MediaService------onDestroy()-----end");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Flog.d("---MediaService------onBind()");
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Flog.d("---MediaService------onUnbind()---start");
		myLyricView = null;
		lyricView = null;
		// lyricView1 = null;
		lrcView = null;
		removeAllMsg();// 移除所有消息
		Flog.d("---MediaService------onUnbind()---end");
		return true;// 一定返回true，允许执行onRebind
	}

	@Override
	public void onRebind(Intent intent) {
		Flog.d("MediaService---onRebind()---start");
		// TODO Auto-generated method stub
		super.onRebind(intent);
		Flog.d("MediaService---onRebind()---mediaPlayer.isPlaying()--"
				+ mediaPlayer.isPlaying());
		if (mediaPlayer.isPlaying()) {// 如果正在播放重新绑定服务的时候重新注册
			prepared();// 因为消息已经移除，所有需要重新开启更新操作
		} else {
			if (mp3Path != null) {// 暂停原先播放重新开页面需要恢复原先的状态
				mp3Duration = mediaPlayer.getDuration();
				info.setMp3Duration(mp3Duration);
				Flog.d("MediaService---onRebind()---mp3Duration--"
						+ mp3Duration);
				CoverList.cover = albumUtil.scanAlbumImage(info.getPath());
				mBinder.playStart(info);
				mp3Current = mediaPlayer.getCurrentPosition();
				mBinder.playUpdate(mp3Current);
				mBinder.playPause();
			}
		}

		mBinder.modeChange(mode);
		Flog.d("MediaService---onRebind()---end");
	}

	/**
	 * 播放操作
	 */
	private void play() {
		Flog.d("MediaService--play()--start");
		int size = 0;
		Flog.d("MediaService--play()--page--" + page);
		switch (page) {
		case MainActivity.SLIDING_MENU_ALL:
			size = MusicList.list.size();
			Flog.d("MediaService--play()--size--" + size);

			if (size > 0) {
				myApp.setPosition(position);
				Flog.d("MediaService--play()--position--" + position);
				info = MusicList.list.get(position);

			}
			break;

		case MainActivity.SLIDING_MENU_FAVORITE:
			size = FavoriteList.list.size();

			Flog.d("MediaService--SLIDING_MENU_FAVORITE--size--" + size
					+ "...FavoriteList.list" + FavoriteList.list);
			synchronized (MainActivity.lockObject) {
				if (size > 0 && size > position) {
					info = FavoriteList.list.get(position);
					MainActivity.lockObject.notifyAll();
					myApp.setPosition(position);
				}
			}
			break;

		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			size = FolderList.list.get(folderPosition).getMusicList().size();
			if (size > 0 && folderPosition < size) {
				info = FolderList.list.get(folderPosition).getMusicList()
						.get(position);
			}
			break;
		}
		if (size > 0) {
			mBinder.playStart(info);
			Flog.d("mediaservice---play()---if");
			mp3Path = info.getPath();
			Flog.d("mediaservice---play()---if---" + info.toString());
			Flog.d("mediaservice---play()---if---" + info.getFile());
			// lyricPath=dbDao.queryLrcPath(filename);
			String subLrcPath = mp3Path.substring(mp3Path.lastIndexOf("/") + 1,
					mp3Path.lastIndexOf(".")).replaceAll(" ", "");
			Flog.d("mediaservice---play()---if---LyricList--"
					+ LyricList.map.toString());
			lyricPath = LyricList.map.get(subLrcPath);
			Flog.d("mediaservice---play()---if---subLrcPath--" + subLrcPath);
			Flog.d("mediaservice---play()---if---lyricPath---" + lyricPath);

			if (mp3Path != null) {
				Flog.d("mediaservice---play()---if---1");
				initMedia();
				initLrc();
				Flog.d("mediaservice---play()---if---2");
			}
			lastPage = page;
			if (!isCommandPrevious) {
				Flog.d("mediaservice---play()---if---3");
				positionList.add(position);
				Flog.d("mediaservice---play()---if---4");
			}
			isCommandPrevious = false;

		}
		Flog.d("MediaService--play()---end");
	}

	private void autoPlay() {
		Flog.d("---MediaService------autoPlay()----start");
		if (mode == MODE_NORMAL) {
			if (position != getSize() - 1) {
				next();
			} else {
				mBinder.playPause();
			}
		} else if (mode == MODE_REPEAT_ONE) {
			play();
		} else {
			next();
		}
		Flog.d("---MediaService------autoPlay()----end");
	}

	private void previous() {
		Flog.d("---MediaService------previous()--start");
		int size = getSize();
		if (size > 0) {
			isCommandPrevious = true;
			if (mode == MODE_RANDOM) {
				if (lastPage == page) {
					if (positionList.size() > 1) {
						positionList.remove(positionList.size() - 1);
						position = positionList.get(positionList.size() - 1);

					} else {
						position = (int) (Math.random() * size);

					}
				} else {
					positionList.clear();
					position = (int) (Math.random() * size);
				}
			} else {
				if (position == 0) {
					position = size - 1;
				} else {
					position--;

				}
			}
			myApp.setPosition(position);
			Flog.d("MediaService--previous()--");
			Flog.d("MediaService--previous()--position--" + position);
			startServiceCommand();
			Flog.d("---MediaService------previous()--end");
		}
	}

	private void next() {
		Flog.d("---MediaService------next()--start");
		int size = getSize();
		if (size > 0) {
			if (mode == MODE_RANDOM) {
				position = (int) (Math.random() * size);
			} else {
				if (position == size - 1) {
					position = 0;
				} else {
					position++;
					Flog.d("MediaService--next()--position---" + position);
				}
			}
			myApp.setPosition(position);

			startServiceCommand();
		}
		Flog.d("MediaService--next()--end");
	}

	// 后退
	private void rewind() {
		Flog.d("---MediaService------rewind()-----start");
		int current = mp3Current - 3000;
		mp3Current = current > 0 ? current : 0;
		mBinder.playUpdate(mp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_REWIND, 100);
		Flog.d("MediaService--rewind()--end");
	}

	// 快进
	private void forward() {
		Flog.d("---MediaService------forward()-------start");
		int current = mp3Current + 3000;
		mp3Current = current < mp3Duration ? current : mp3Duration;
		mBinder.playUpdate(mp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_FORWARD, 100);
		Flog.d("MediaService--forward()--end");
	}

	// 重播
	private void replay() {
		Flog.d("---MediaService------replay()-------start");
		if (mHandler.hasMessages(MEDIA_PLAY_REWIND)) {
			mHandler.removeMessages(MEDIA_PLAY_REWIND);
		}
		if (mHandler.hasMessages(MEDIA_PLAY_FORWARD)) {
			mHandler.removeMessages(MEDIA_PLAY_FORWARD);
		}
		mediaPlayer.seekTo(mp3Current);
		mHandler.sendEmptyMessage(MEDIA_PLAY_UPDATE);
		if (lyricView != null && hasLyric && myLyricView != null
				&& lrcView != null) {
			myLyricView.setSentenceEntities(lyricList);
			lyricView.setSentenceEntities(lyricList);
			lrcView.setSentenceEntities(lyricList);
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
					UPDATE_LYRIC_TIME);
		}
		Flog.d("MediaService--replay()--end");
	}

	private int getSize() {
		Flog.d("---MediaService------getSize()--start");
		int size = 0;
		switch (page) {
		case MainActivity.SLIDING_MENU_ALL:
			size = MusicList.list.size();
			Flog.d("MediaService--getSize()--MusicListsize--" + size);
			break;

		case MainActivity.SLIDING_MENU_FAVORITE:
			size = FavoriteList.list.size();
			Flog.d("MediaService--getSize()--FavoriteListsize--" + size);
			break;

		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			size = FolderList.list.get(folderPosition).getMusicList().size();
			Flog.d("MediaService--getSize()--FolderListsize--" + size);
			break;
		}
		Flog.d("---MediaService------getSize()--end");
		return size;
	}

	private void startServiceCommand() {
		Flog.d("MediaService---startServiceCommand()----start");
		Intent intent = new Intent(getApplicationContext(), MediaService.class);
		intent.putExtra(INTENT_LIST_PAGE, page);
		intent.putExtra(INTENT_LIST_POSITION, position);
		// intent.setAction(SHOW_LRC);// ////////////////////////////////
		Flog.d("MediaService---startServiceCommand()--position--" + position);
		startService(intent);
		Flog.d("MediaService---startServiceCommand()----end");

	}

	/**
	 * 初始化媒体播放器
	 */
	private void initMedia() {
		Flog.d("mediaservice---initmedia--start");
		try {

			removeAllMsg();
			if (!isSuccess) {
				mediaPlayer.reset();
				isSuccess = true;
			}
			Flog.d("========>>>>>begin play....");
			Flog.d("========>>>>>setDataSource=========" + mp3Path);
			// Flog.d("========>>>>>setDataSource=========" + apePath);
			if (flag == true) {
				Flog.d("mediaservice---initmedia()---if---true");
				mediaPlayer.setDataSource(url);
			} else {
				Flog.d("mediaservice---initmedia()---else---false");
				mediaPlayer.setDataSource(mp3Path);
			}
			// mediaPlayer.setDataSource(apePath);

			Flog.d("========>>>>>before prepareAsync()");
			if (isSuccess) {
				mediaPlayer.prepareAsync();
				isSuccess = false;
			}

			Flog.d("========>>>>>after prepareAsync()");
			// stopForeground(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Flog.d("mediaservice---initmedia---end");
	}

	private int duration;
	private int index;
	int currentTime;

	public int lrcIndex() {
		Flog.d("---MediaService------lrcIndex()--start");

		if (mediaPlayer.isPlaying()) {
			currentTime = mp3Current;

			duration = mp3Duration;

		}
		if (currentTime < duration) {
			for (int i = 0; i < lyricList.size(); i++) {
				if (i < lyricList.size() - 1) {
					if (currentTime < lyricList.get(i).getTime() && i == 0) {
						index = i;
					}
					if (currentTime > lyricList.get(i).getTime()
							&& currentTime < lyricList.get(i + 1).getTime()) {
						index = i;
					}
				}
				if (i == lyricList.size() - 1
						&& currentTime > lyricList.get(i).getTime()) {
					index = i;
				}
			}
		}
		Flog.d("---MediaService------lrcIndex()----index---" + index);
		Flog.d("---MediaService------lrcIndex()--end");
		return index;

	}

	/**
	 * 初始化歌词
	 */
	private void initLrc() {
		Flog.d("---MediaService------initLrc()---start");
		hasLyric = false;
		if (lyricPath != null) {
			Flog.d("---MediaService------initLrc()----lyric---" + lyricPath);
			try {
				Flog.d("---MediaService------initLrc()-------if");
				LyricParser parser = new LyricParser(lyricPath);
				lyricList = parser.parser();
				// LrcProcess mLrcProcess = new LrcProcess(lyricPath);
				// mLrcProcess.readLRC();
				// mlrcList = mLrcProcess.getLrcList();

				hasLyric = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Flog.d("---MediaService------initLrc()---else");
			if (lyricView != null && myLyricView != null && lrcView != null) {
				lyricView.clear();
				// lyricView1.clear();
				myLyricView.clear();
				lrcView.clear();
			}
		}
		Flog.d("---MediaService------initLrc()---end");
	}

	/**
	 * 准备好开始播放工作
	 */
	private void prepared() {
		Flog.d("---MediaService------prepared()----start");
		mHandler.sendEmptyMessage(MEDIA_PLAY_START);
		if (lyricView != null && myLyricView != null && lrcView != null) {
			Flog.d("---MediaService------prepared()-------1");

			if (hasLyric) {
				Flog.d("---MediaService------prepared()-------2");
				myLyricView.setSentenceEntities(lyricList);
				lyricView.setSentenceEntities(lyricList);
				lrcView.setSentenceEntities(lyricList);
				// lyricView1.setSentenceEntities(lyricList);
				mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
						UPDATE_LYRIC_TIME); // 通知刷新歌词
				running = true;
				Intent clearIntent = new Intent(
						PlayerActivity.BROADCAST_HIDE_LRC);
				sendBroadcast(clearIntent);
			} else {
				Flog.d("---MediaService------prepared()-----is not lrc!!!");
				Intent showIntent = new Intent(
						PlayerActivity.BROADCAST_SHOW_LRC);
				Flog.d("---MediaService------sendBroadcast-----before");
				sendBroadcast(showIntent);
				Flog.d("---MediaService------sendBroadcast-----after");
			}

		}
		Flog.d("---MediaService------prepared()-----end");

	}

	private void getMusicList() {
		DBDao db = new DBDao(getApplicationContext());
		// db.deleteAll();
		ScanUtil scanUtil = new ScanUtil(getApplicationContext());
		db.queryAll(scanUtil.searchAllDirectory());
		db.close();
	}

	/**
	 * 开始播放，获得总时间和AudioSessionId，并启动更新UI任务
	 */
	private void start() {
		Flog.d("MediaService--start()--start");
		Flog.d("MediaService--start()--start--mp3Path--" + mp3Path);
		Flog.d("MediaService--start()--start--position--" + position);
		// preferences=getSharedPreferences(MainActivity.PREFERENCES_NAME,
		// Context.MODE_PRIVATE);
		mp3Duration = mediaPlayer.getDuration();
		Flog.d("MediaService--start()---mp3Duration--" + mp3Duration);
		if (info == null) {
			info = MusicList.list.get(position);
		}
		info.setMp3Duration(mp3Duration);
		Flog.d("MediaService--start()---setAudioSessionId---"
				+ mediaPlayer.getAudioSessionId());
		Flog.d("MediaService--start()---info---" + info.toString());
		info.setAudioSessionId(mediaPlayer.getAudioSessionId());
		if (flag == true) {
			CoverList.cover = null;
		} else {

			CoverList.cover = albumUtil.scanAlbumImage(info.getPath());
		}
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		preferences.edit().putString(PREFERENCES_PATH, mp3Path).commit();
		preferences.edit().putInt(PREFERENCES_POSITION, position).commit();
		mBinder.playStart(info);
		// update();
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);

		// info.setAudioSessionId(getSessionIdFromNative());

		final String artist = info.getArtist();
		final String name = info.getName();
		notification.tickerText = artist + " - " + name;

		Intent playIntent = new Intent(MyAppWidget.BROADCAST_ACTION_NOT_PLAY);
		PendingIntent playPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, playIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.not_play, playPendingIntent);

		Intent prevIntent = new Intent(MyAppWidget.BROADCAST_ACTION_NOT_PREV);
		PendingIntent prevPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, prevIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.not_previous,
				prevPendingIntent);
		Intent nextIntent = new Intent(MyAppWidget.BROADCAST_ACTION_NOT_NEXT);
		PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, nextIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.not_next, nextPendingIntent);
		Intent exitIntent = new Intent(MyAppWidget.BROADCAST_ACTION_NOT_EXIT);
		PendingIntent exitPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, exitIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.not_exit, exitPendingIntent);

		if (CoverList.cover == null) {
			remoteViews.setImageViewResource(R.id.notification_item_album,
					R.drawable.main_img_album);
		} else {
			remoteViews.setImageViewBitmap(R.id.notification_item_album,
					CoverList.cover);
		}

		remoteViews.setTextViewText(R.id.notification_item_name, name);
		remoteViews.setTextViewText(R.id.notification_item_artist, artist);
		remoteViews.setImageViewResource(R.id.not_play,
				R.drawable.img_button_notification_play_pause);

		notification.contentView = remoteViews;
		startForeground(1, notification);// id为0则不显示Notification

		Intent stateIntent = new Intent(STATE_ACTION);
		sendBroadcast(stateIntent);
		Intent appstateIntent = new Intent(MUSICINFO_ACTION);
		sendBroadcast(appstateIntent);
		Flog.d("MediaService--start()--end");
	}

	/**
	 * 更新UI，发现MediaPlayer.getCurrentPosition()的bug很严重，感觉指的不是时间而是帧数，
	 * 而且Handler处理事务要花费时间，虽然间隔1秒的延时时间，但处理完成就不止1秒的时间，
	 * 所以换算后会出现跳秒的情况，机子配置越差的感觉越明显，本想通过自增来实现，但发现误差更大，暂无其他方法了
	 */
	private void update() {
		Flog.d("---MediaService------update()--start");
		// TODO Auto-generated method stub
		mp3Current = mediaPlayer.getCurrentPosition();
		Flog.d("MediaServer--update()--mp3Current--" + mp3Current);
		mBinder.playUpdate(mp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);
		Flog.d("---MediaService------update()--end");

	}

	public void slidestart() {
		Flog.d("MediaService---slidestart()--start");
		myLyricView.showprogress = true;
		running = false;

		Flog.d("MediaService---slidestart()--end");
	}

	public void updateplayer() {
		Flog.d("MediaService---updateplayer()--start");
		myLyricView.showprogress = false;
		// if(Math.abs(lyricView.driftx)<Math.abs(lyricView.drifty)){
		myLyricView.index = myLyricView.index + myLyricView.temp;
		myLyricView.driftx = 0;
		myLyricView.drifty = 0;
		if (myLyricView.repair()) {

			if (index < myLyricView.mSentenceEntities.size()) {
				mediaPlayer.seekTo(myLyricView.mSentenceEntities.get(
						myLyricView.index).getTime());
			}

		} else {
			mediaPlayer.seekTo(0);
		}
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
				UPDATE_LYRIC_TIME); // 通知刷新歌词
		running = true;
		// thread = new Thread(new UIUpdateThread());
		// thread.start();

		Flog.d("MediaService---updateplayer()--end");
	}

	/**
	 * 暂停音乐
	 */
	private void pause() {
		Flog.d("MediaService---pause()---start");
		removeAllMsg();

		mediaPlayer.pause();
		mBinder.playPause();

		remoteViews.setImageViewResource(R.id.not_play,
				R.drawable.img_button_notification_play_play);
		notification.contentView = remoteViews;
		startForeground(1, notification);
		Intent stateIntent = new Intent(STATE_ACTION);
		sendBroadcast(stateIntent);
		Flog.d("MediaService---pause()--end");
	}

	/**
	 * 移除更新UI的消息
	 */
	private void removeUpdateMsg() {
		Flog.d("---MediaService------removeUpdateMsg()--start");
		if (mHandler != null && mHandler.hasMessages(MEDIA_PLAY_UPDATE)) {
			mHandler.removeMessages(MEDIA_PLAY_UPDATE);
		}
		Flog.d("---MediaService------removeUpdateMsg()--end");
	}

	/**
	 * 播放完成
	 */
	private void complete() {
		Flog.d("---MediaService------complete()----start");
		// TODO Auto-generated method stub
		mBinder.playComplete();
		mBinder.playUpdate(mp3Duration);
		autoPlay();
		Flog.d("MediaService---complete()----end");
	}

	/**
	 * 播放出错
	 */
	private void error() {
		Flog.d("---MediaService------error()--start");
		mBinder.playError();
		mBinder.playPause();
		positionList.clear();
		Flog.d("---MediaService------error()--end");
	}

	/**
	 * 刷新歌词
	 */
	private void updateLrcView() {
		Flog.d("---MediaService------updateLrcView()-----start");
		if (lyricList.size() > 0) {
			lyricView.setIndex(getLrcIndex(mediaPlayer.getCurrentPosition(),
					mp3Duration));
			lyricView.invalidate();
			lrcView.setIndex(getLrcIndex(mediaPlayer.getCurrentPosition(),
					mp3Duration));
			lrcView.invalidate();
			myLyricView.updateIndex(mediaPlayer.getCurrentPosition(),
					mp3Duration);
			myLyricView.invalidate();

			if (running) {
				mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
						UPDATE_LYRIC_TIME);
			}

		}
		Flog.d("---MediaService------updateLrcView()-----end");

	}

	/**
	 * 移除更新歌词的消息
	 */
	private void removeUpdateLrcViewMsg() {
		Flog.d("---MediaService------removeUpdateLrcViewMsg()--start");
		if (mHandler != null && mHandler.hasMessages(MEDIA_PLAY_UPDATE_LYRIC)) {
			mHandler.removeMessages(MEDIA_PLAY_UPDATE_LYRIC);
		}
		Flog.d("MediaService---removeUpdateLrcViewMsg()--end");
	}

	/**
	 * 移除所有消息
	 */
	private void removeAllMsg() {
		Flog.d("---MediaService------removeAllMsg()--start");
		removeUpdateMsg();
		removeUpdateLrcViewMsg();
		Flog.d("MediaService---removeAllMsg()--end");
	}

	/**
	 * 耳机线控-处理单击过渡事件
	 */
	private void buttonOneClick() {
		Flog.d("---MediaService------buttonOneClick()--start");
		buttonClickCounts++;
		mHandler.sendEmptyMessageDelayed(MEDIA_BUTTON_DOUBLE_CLICK, 300);
		Flog.d("---MediaService------buttonOneClick()--end");
	}

	/**
	 * 耳机线控-响应单击和双击事件
	 */
	private void buttonDoubleClick() {
		Flog.d("---MediaService------buttonDoubleClick()--start");
		if (buttonClickCounts == 1) {
			mBinder.setControlCommand(CONTROL_COMMAND_PLAY);
		} else if (buttonClickCounts > 1) {
			mBinder.setControlCommand(CONTROL_COMMAND_NEXT);
		}
		buttonClickCounts = 0;
		Flog.d("---MediaService------buttonDoubleClick()--end");
	}

	/**
	 * 歌词同步处理
	 */
	private int[] getLrcIndex(int currentTime, int duration) {
		Flog.d("---MediaService------getLrcIndex()--start");
		int index = 0;
		int size = lyricList.size();
		if (currentTime < duration) {
			for (int i = 0; i < size; i++) {
				if (i < size - 1) {
					if (currentTime < lyricList.get(i).getTime() && i == 0) {
						index = i;
					}
					if (currentTime > lyricList.get(i).getTime()
							&& currentTime < lyricList.get(i + 1).getTime()) {
						index = i;
					}
				}
				if (i == size - 1 && currentTime > lyricList.get(i).getTime()) {
					index = i;
				}
			}
		}
		int temp1 = lyricList.get(index).getTime();
		int temp2 = (index == (size - 1)) ? 0 : lyricList.get(index + 1)
				.getTime() - temp1;
		Flog.d("---MediaService------getLrcIndex()--end");
		return new int[] { index, currentTime, temp1, temp2 };
	}

	// 电话监听
	private class ServicePhoneStateListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			// 如果有电话来的话暂停播放
			Flog.d("MediaService---onCallStateChanged()--start");
			Flog.d("MediaService---onCallStateChanged()--state--" + state);
			Flog.d("MediaService---onCallStateChanged()--isPlay--" + isPlay);
			/*
			 * if (state == TelephonyManager.CALL_STATE_RINGING && mediaPlayer
			 * != null && mediaPlayer.isPlaying()) { pause(); } else if (state
			 * == TelephonyManager.CALL_STATE_IDLE) { // if (mBinder != null) {
			 * // mBinder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
			 * // } if (isPlay) { mediaPlayer.start(); prepared(); }
			 * 
			 * } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
			 * 
			 * if ( mediaPlayer != null && mediaPlayer.isPlaying()) { pause();
			 * isPlay=true; }else { isPlay=false; }
			 * 
			 * }
			 */

			/**
			 * 返回电话状态
			 * 
			 * CALL_STATE_IDLE 无任何状态时 CALL_STATE_OFFHOOK 接起电话时
			 * CALL_STATE_RINGING 电话进来时
			 */

			switch (state) {

			case TelephonyManager.CALL_STATE_RINGING:
				if (mediaPlayer != null && mediaPlayer.isPlaying()) {
					pause();
					isPlay = true;
				} else {
					isPlay = false;
				}

				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (isPlay) {
					mediaPlayer.start();
					prepared();
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if (mediaPlayer != null && mediaPlayer.isPlaying()) {
					pause();
					isPlay = true;
				} else {
					isPlay = false;
				}
				break;
			}
			Flog.d("MediaService---onCallStateChanged()--end");
		}
	}

	private class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Flog.d("---MediaService------onReceive()-----start");
			// TODO Auto-generated method stub
			if (intent != null) {
				Flog.d("---MediaService------onReceive()-----intent--"
						+ intent.getAction());
				// if
				// (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)&&mediaPlayer!=null&&mediaPlayer.isPlaying())
				// {
				// pause();
				// }
				// 设置耳机插拨事件，当拨出耳机时音乐暂停，插入耳机时启动耳机模式
				if (intent.hasExtra("state")) {
					if (intent.getIntExtra("state", 0) == 0) {
						pause();
						// Toast.makeText(context, "headset not connected",
						// Toast.LENGTH_LONG).show();
					} else if (intent.getIntExtra("state", 0) == 1) {
						Toast.makeText(context, "已经启动耳机模式", Toast.LENGTH_LONG)
								.show();
						// mHandler.sendEmptyMessage(MEDIA_BUTTON_ONE_CLICK);
						boolean isActionMediaButton = Intent.ACTION_MEDIA_BUTTON
								.equals(intent.getAction());
						if (isActionMediaButton) {
							KeyEvent event = (KeyEvent) intent
									.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
							if (event == null) {
								return;
							}
							long eventTime = event.getEventTime()
									- event.getDownTime();// 按键按下到松开的时长
							if (eventTime > 1000) {
								mBinder.setControlCommand(CONTROL_COMMAND_PREVIOUS);
								abortBroadcast();
							} else {
								if (event.getAction() == KeyEvent.ACTION_UP) {
									mHandler.sendEmptyMessage(MEDIA_BUTTON_ONE_CLICK);
									abortBroadcast();// 终止广播(不让别的程序收到此广播，免受干扰)
								}

							}
						} else {
							int i = intent.getIntExtra(INTENT_ACTIVITY,
									ACTIVITY_MAIN);
							switch (i) {
							case ACTIVITY_SCAN:
								intent = new Intent(getApplicationContext(),
										ScanActivity.class);
								break;

							case ACTIVITY_MAIN:
								intent = new Intent(getApplicationContext(),
										MainActivity.class);
								Flog.d("---MediaService------onReceive()-------ACTIVITY_MAIN");
								break;

							case ACTIVITY_PLAYER:
								intent = new Intent(getApplicationContext(),
										PlayerActivity.class);
								Flog.d("---MediaService------onReceive()-------ACTIVITY_PLAYER");
								break;

							case ACTIVITY_SETTING:
								intent = new Intent(getApplicationContext(),
										SettingActivity.class);
								break;

							}
						}
					}
				}

			}
			Flog.d("---MediaService------onReceive()-----end");
		}
	}

	/** 桌面小插件和notification */
	protected BroadcastReceiver appWidgetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Flog.d("appWidgetReceiver----onReceive()--start");
			Flog.d("appWidgetReceiver----onReceive()--start---action--"
					+ intent.getAction());
			if (intent.getAction().equals("com.flyaudiomedia.state")) {
				Flog.d("---MediaService------onReceive()---mediaPlayer.isPlaying()---1"
						+ mediaPlayer.isPlaying());
				if (mediaPlayer.isPlaying()) {
					Intent pauseIntent = new Intent("com.flyaudiomedia.play");
					sendBroadcast(pauseIntent);
				} else {
					Intent playIntent = new Intent("com.flyaudiomedia.pause");
					sendBroadcast(playIntent);
				}
			} else if (intent.getAction().equals("com.flyaudiomedia.musicInfo")) {
				Intent musicIntent = new Intent("com.flyaudiomedia.musicinfo");
				if (info != null) {
					musicIntent.putExtra("musicname", info.getName());
					musicIntent.putExtra("musicartist", info.getArtist());
					musicIntent.putExtra("musicpath", info.getPath());
					sendBroadcast(musicIntent);
				}

			}
			if (intent.getAction().equals("com.flyaudio.action.next")) {
				next();
				startForeground(1, notification);

			} else if (intent.getAction()
					.equals("com.flyaudio.action.previous")) {
				previous();
				startForeground(1, notification);

			} else if (intent.getAction().equals("com.flyaudio.action.play")) {
				if (mediaPlayer != null && mediaPlayer.isPlaying()) {

					remoteViews.setImageViewResource(R.id.not_play,
							R.drawable.img_button_notification_play_play);
					notification.contentView = remoteViews;
					startForeground(1, notification);

					// play();
					pause();
				} else {
					remoteViews.setImageViewResource(R.id.not_play,
							R.drawable.img_button_notification_play_pause);
					notification.contentView = remoteViews;
					startForeground(1, notification);
					if (mp3Path != null) {
						mediaPlayer.start();
						prepared();
					}
				}

			} else if (intent.getAction().equals("com.flyaudio.action.exit")) {

				stopForeground(true);
			}

			Flog.d("appWidgetReceiver----onReceive()--end");
		}
	};

	private static class ServiceHandler extends Handler {

		private WeakReference<MediaService> reference;

		public ServiceHandler(MediaService service) {
			// TODO Auto-generated constructor stub
			reference = new WeakReference<MediaService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (reference.get() != null) {
				MediaService theService = reference.get();
				switch (msg.what) {
				case MEDIA_PLAY_START:
					theService.start();
					break;

				case MEDIA_PLAY_UPDATE:
					theService.update();
					break;

				case MEDIA_PLAY_COMPLETE:
					theService.complete();
					break;

				case MEDIA_PLAY_ERROR:
					theService.error();
					break;

				case MEDIA_PLAY_UPDATE_LYRIC:
					theService.updateLrcView();
					break;

				case MEDIA_PLAY_REWIND:
					theService.rewind();
					break;

				case MEDIA_PLAY_FORWARD:
					theService.forward();
					break;

				case MEDIA_BUTTON_ONE_CLICK:
					theService.buttonOneClick();
					break;

				case MEDIA_BUTTON_DOUBLE_CLICK:
					theService.buttonDoubleClick();
					break;
				case MEDIA_BUTTON_SCAN_MUISCLIST:
					theService.getMusicList();
					break;
				}
			}
		}
	}
	// public native int getSessionIdFromNative();

}
