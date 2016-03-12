package com.flyaudio.flyMediaPlayer.activity;

import java.io.File;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.flyaudio.flyMediaPlayer.R;
import com.flyaudio.flyMediaPlayer.adapter.MusicAdapter;
import com.flyaudio.flyMediaPlayer.adapter.SlidingAdapter;
import com.flyaudio.flyMediaPlayer.data.DBDao;
import com.flyaudio.flyMediaPlayer.dialog.DeleteDialog;
import com.flyaudio.flyMediaPlayer.dialog.InfoDialog;
import com.flyaudio.flyMediaPlayer.dialog.MenuDialog;
import com.flyaudio.flyMediaPlayer.dialog.ScanDialog;
import com.flyaudio.flyMediaPlayer.dialog.TVAnimDialog.OnTVAnimDialogDismissListener;
import com.flyaudio.flyMediaPlayer.objectInfo.MusicInfo;
import com.flyaudio.flyMediaPlayer.objectInfo.MyApp;
import com.flyaudio.flyMediaPlayer.perferences.CoverList;
import com.flyaudio.flyMediaPlayer.perferences.FavoriteList;
import com.flyaudio.flyMediaPlayer.perferences.FolderList;
import com.flyaudio.flyMediaPlayer.perferences.MusicList;
import com.flyaudio.flyMediaPlayer.provider.MyAppWidget;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaService;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnPlayCompleteListener;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnPlayErrorListener;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnPlayPauseListener;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnPlayStartListener;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnPlayingListener;
import com.flyaudio.flyMediaPlayer.slidingMenu.SlidingListActivity;
import com.flyaudio.flyMediaPlayer.slidingMenu.SlidingMenu;
import com.flyaudio.flyMediaPlayer.until.Flog;
import com.flyaudio.flyMediaPlayer.until.FormatUtil;
import com.flyaudio.flyMediaPlayer.until.ScanUtil;
import com.flyaudio.flyMediaPlayer.until.AllListActivity;

public class MainActivity extends SlidingListActivity implements
		OnClickListener, OnLongClickListener, OnTouchListener,
		OnTVAnimDialogDismissListener {

	public static final int SLIDING_MENU_SCAN = 0;// 侧边-扫描歌曲
	public static final int SLIDING_MENU_INTERENTSCAN = 1;// 侧边-扫描歌曲
	public static final int SLIDING_MENU_ALL = 2;// 侧边-全部歌曲
	public static final int SLIDING_MENU_FAVORITE = 3;// 侧边-最爱
	public static final int SLIDING_MENU_FOLDER = 4;// 侧边-文件夹
	public static final int SLIDING_MENU_MANAGER = 5;
	public static final int SLIDING_MENU_EXIT = 6;// 侧边--退出
	public static final int SLIDING_MENU_FOLDER_LIST = 7;// 侧边-文件夹--文件夹列表
	public static final int DIALOG_DISMISS = 0;// 对话框消失
	public static final int DIALOG_SCAN = 1;
	public static final int DIALOG_MENU_REMOVE = 2;
	public static final int DIALOG_MENU_DELETE = 3;
	public static final int DIALOG_MENU_INFO = 4;
	public static final int DIALOG_DELETE = 5;
	public static final int DIALOG_MENU_RINGTONE = 6;
	public static final int DIALOG_MENU_SHARE = 7;
	public static final String PREFERENCES_NAME = "settings";
	public static final String PREFERENCES_MODE = "mode";// 存储播放模式
	public static final String PREFERENCES_SCAN = "scan";// 存储是否扫描过
	public static final String PREFERENCES_SKIN = "skin";// 存储背影图
	public static final String PREFERENCES_LYRIC = "lyric";// 存储歌词的高亮颜色
	
	
	public static final String BROADCAST_ACTION_SCAN = "com.flyaudio.action.scan";// 扫描广播־
	public static final String BROADCAST_ACTION_MENU = "com.flyaudio.action.menu";// 弹出菜单标志
	public static final String BROADCAST_ACTION_FAVORITE = "com.flyaudio.action.favorite";// 最爱广播
	public static final String BROADCAST_ACTION_EXIT = "com.flyaudio.action.exit";// 退出广播־
	public static final String BROADCAST_INTENT_PAGE = "com.flyaudio.intent.page";// 页面状态״̬
	public static final String BROADCAST_INTENT_POSITION = "com.flyaudio.intent.position";// 歌曲的索引
	public static final String SHOW_LRC = "com.flyaudio.SHOW_LRC";
	private final String TITLE_ALL = "播放列表";
	private final String TITLE_FAVORITE = "我的最爱";
	private final String TITLE_FOLDER = "文件夹";
	private final String TITLE_NORMAL = "无音乐播放";
	private final String TIME_NORMAL = "00:00";
	private int skinId;// 背影ID
	private int slidingPage = SLIDING_MENU_ALL;// 页面状态
	private int playerPage;// 发送给PlayerActivity的页面状态
	private int musicPosition;// 当前播放歌曲索引
	private int folderPosition;// 文件夹列表索引
	private int favoritePosition;// 最爱列表的索引
	private int dialogMenuPosition;// 记住弹出歌曲列表菜单的歌曲索引
	private boolean canSkip = true;// 防止用户频繁点击造成多次解除服务
	private boolean bindState = false;// 绑定状态״̬
	private String mp3Current;// 歌曲当前时长
	private String mp3Duration;// 歌曲总时长
	private String dialogMenuPath;// 记住弹出歌曲列表菜单的歌曲列表
	private TextView mainTitle;// 列表标题
	private TextView mainSize;// 歌曲数量
	private TextView mainArtist;// 艺术家
	private TextView mainName;// 歌曲名称
	private TextView mainTime;// 歌曲时间
	private ImageView mainAlbum;// 专辑图片
	private ImageButton btnMenu;// 侧滑菜单按钮
	private ImageButton btnPrevious;
	private ImageButton btnPlay;
	private ImageButton btnNext;
	private LinearLayout skin;// 背景图
	private LinearLayout viewBack;// 返回上一级
	private LinearLayout viewControl;// 底部播放控制视图
	private Intent playIntent;
	private MediaBinder binder;
	private MainReceiver receiver;
	private SlidingMenu slidingMenu;
	private MusicAdapter musicAdapter;
	private SharedPreferences preferences;
	private ServiceConnection serviceConnection;
	private DBDao dbDao;
	private int poistion;
	private MusicInfo musicInfo;
	private MyApp myApp;
	private String path;

	public static final Object lockObject = new Object();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Flog.d("MainActivity--onCreate()");
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AllListActivity.getInstance().addActivity(this);
		myApp = (MyApp) getApplication();
		
		path = myApp.getPath();	
		Flog.d("MainActivity--onCreate()--path---" + path);
		if (path==null) {
			Intent intent=getIntent();
			path=intent.getStringExtra("musicpath");
					
		}
		
		init();
		
	
		
		// execute();

	}

	public void execute(String path) {

		if (path != null) {
			Flog.d("-----MainActivity----path---" + path);
			dbDao = new DBDao(getApplicationContext());
			Flog.d("-----MainActivity-------isquery---" + dbDao.isQuery(path));
			if (dbDao.isQuery(path)) {
				for (int i = 0; i < MusicList.list.size(); i++) {
					if (MusicList.list.get(i).getPath().equals(path)) {
						poistion = i;
					}
				}

			} else {
				String fileName = path.substring(path.lastIndexOf("/") + 1,
						path.lastIndexOf("."));
				String musicName = path.substring(path.lastIndexOf("/") + 1);
				Flog.d("-----MainActivity----fileName-----" + fileName);
				String folder = path.substring(0, path.lastIndexOf("/"));
				ScanUtil scanUtil = new ScanUtil(getApplicationContext());
				musicInfo = scanUtil.scanMusicTag(fileName, path);

				dbDao.add(fileName, musicName, path, folder, false,
						musicInfo.getTime(), musicInfo.getSize(),
						musicInfo.getArtist(), musicInfo.getFormat(),
						musicInfo.getAlbum(), musicInfo.getYears(),
						musicInfo.getChannels(), musicInfo.getGenre(),
						musicInfo.getKbps(), musicInfo.getHz());

				MusicList.list.add(musicInfo);
				poistion = MusicList.list.indexOf(musicInfo);
				
			}

			musicPosition=poistion;
			slidingPage = musicAdapter.getPage();
			playIntent.putExtra(MediaService.INTENT_LIST_PAGE, slidingPage);
			Flog.d("-----MainActivity----MusicInfo-----  " + poistion);
			playIntent.putExtra(MediaService.INTENT_LIST_POSITION, poistion);
//			playIntent.putExtra(MediaService.INTENT_LIST_PATH, path);
			startService(playIntent);

		}
		Flog.d("MainActivity---execut()---end");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Flog.d("Mainactivity---onResume()");
		
		super.onResume();
		execute(path);
		
		

		int id = preferences.getInt(MainActivity.PREFERENCES_SKIN,
				R.drawable.skin_bg1);
		if (skinId != id) {// 判断是否换图
			skinId = id;
			skin.setBackgroundResource(skinId);
		}
		Intent intent2 = new Intent(MediaService.BROADCAST_ACTION_SERVICE);
		intent2.putExtra(MediaService.INTENT_ACTIVITY,
				MediaService.ACTIVITY_MAIN);
		sendBroadcast(intent2);

		bindState = bindService(playIntent, serviceConnection,
				Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (serviceConnection != null) {
			if (bindState) {
				unbindService(serviceConnection);
			}
			serviceConnection = null;
		}
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

	private void init() {
		Flog.d("MainActivity--init()");
		initSlidingMenu();
		initActivity();
		initServiceConnection();
	}

	/*
	 * 初始化侧滑相关
	 * 
	 * <---设置SlidingMenu的几种手势模式--->
	 * 
	 * TOUCHMODE_FULLSCREEN：全屏模式，在content页面中，滑动，可以打开SlidingMenu
	 * 
	 * TOUCHMODE_MARGIN：边缘模式，在content页面中，如果想打开SlidingMenu，
	 * 你需要在屏幕边缘滑动才可以打开SlidingMenu
	 * 
	 * TOUCHMODE_NONE：自然是不能通过手势打开啦
	 */
	private void initSlidingMenu() {
		Flog.d("MainActivity--initSlidingMenu()");
		setBehindContentView(R.layout.activity_main_sliding);

		slidingMenu = getSlidingMenu();
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setShadowWidth(20);

		ListView listView = (ListView) slidingMenu.getMenu().findViewById(
				R.id.activity_main_sliding_list);
		listView.setAdapter(new SlidingAdapter(getApplicationContext()));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (viewBack.getVisibility() != View.GONE) {
					viewBack.setVisibility(View.GONE);
				}
				switch (position) {
				case SLIDING_MENU_SCAN:// 扫描歌曲
					intentScanActivity();
					break;
				case SLIDING_MENU_INTERENTSCAN:// 扫描歌曲
					Intent intent = new Intent(getApplicationContext(),
							LoadMainActivity.class);
					startActivity(intent);
					break;

				case SLIDING_MENU_ALL:// 全部歌曲
					if (musicAdapter.getPage() != SLIDING_MENU_ALL) {
						mainTitle.setText(TITLE_ALL);
						musicAdapter.update(SLIDING_MENU_ALL);
						mainSize.setText(musicAdapter.getCount() + "首歌曲");
					}
					break;

				case SLIDING_MENU_FAVORITE:// 我的最爱
					if (musicAdapter.getPage() != SLIDING_MENU_FAVORITE) {
						mainTitle.setText(TITLE_FAVORITE);
						musicAdapter.update(SLIDING_MENU_FAVORITE);
						mainSize.setText(musicAdapter.getCount() + "首歌曲");
					}
					break;

				case SLIDING_MENU_FOLDER:
					if (musicAdapter.getPage() != SLIDING_MENU_FOLDER) {
						mainTitle.setText(TITLE_FOLDER);
						musicAdapter.update(SLIDING_MENU_FOLDER);
						mainSize.setText(musicAdapter.getCount() + "个文件夹");
					}
					break;

				case SLIDING_MENU_MANAGER:
					Flog.d("Mainactivity---slidemenu---managerLoad-----------------------------------------------------------------------------onclick");
					Intent intent2 = new Intent(getApplicationContext(),
							DownloadActivity.class);
					startActivity(intent2);
					break;

				case SLIDING_MENU_EXIT:
					exitProgram();
					break;
				}
				toggle();// 关闭侧边栏
			}
		});
	}

	private void initActivity() {
		skin = (LinearLayout) findViewById(R.id.activity_main_skin);
		btnMenu = (ImageButton) findViewById(R.id.activity_main_ib_menu);
		mainTitle = (TextView) findViewById(R.id.activity_main_tv_title);
		mainSize = (TextView) findViewById(R.id.activity_main_tv_count);
		mainArtist = (TextView) findViewById(R.id.activity_main_tv_artist);
		mainName = (TextView) findViewById(R.id.activity_main_tv_name);
		mainTime = (TextView) findViewById(R.id.activity_main_tv_time);
		mainAlbum = (ImageView) findViewById(R.id.activity_main_iv_album);
		viewBack = (LinearLayout) findViewById(R.id.activity_main_view_back);
		viewControl = (LinearLayout) findViewById(R.id.activity_main_view_bottom);
		btnPrevious = (ImageButton) findViewById(R.id.activity_main_ib_previous);
		btnPlay = (ImageButton) findViewById(R.id.activity_main_ib_play);
		btnNext = (ImageButton) findViewById(R.id.activity_main_ib_next);

		mainTitle.setText(TITLE_ALL);
		mainName.setText(TITLE_NORMAL);
		mainTime.setText(TIME_NORMAL + " - " + TIME_NORMAL);
		viewBack.setOnClickListener(this);
		btnMenu.setOnClickListener(this);
		viewControl.setOnClickListener(this);
		btnPrevious.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		btnPrevious.setOnLongClickListener(this);
		btnNext.setOnLongClickListener(this);
		btnPrevious.setOnTouchListener(this);
		btnNext.setOnTouchListener(this);

		// 获得音乐列表
		musicAdapter = new MusicAdapter(getApplicationContext(),
				SLIDING_MENU_ALL);
		// musicAdapter.notifyDataSetChanged();
		setListAdapter(musicAdapter);
		Flog.d("MainActivity----setListAdapter");
		mainSize.setText(musicAdapter.getCount() + "首");

		playIntent = new Intent(getApplicationContext(), MediaService.class);

		receiver = new MainReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_ACTION_SCAN);
		filter.addAction(BROADCAST_ACTION_MENU);
		filter.addAction(BROADCAST_ACTION_FAVORITE);
		filter.addAction(BROADCAST_ACTION_EXIT);
		registerReceiver(receiver, filter);

		preferences = getSharedPreferences(PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		if (!preferences.getBoolean(PREFERENCES_SCAN, false)) {
			ScanDialog scanDialog = new ScanDialog(this);
			scanDialog.setDialogId(DIALOG_SCAN);
			scanDialog.setOnTVAnimDialogDismissListener(this);
			scanDialog.show();
		}
	}

	private void initServiceConnection() {
		Flog.d("----MainActivity-----initServiceConnection()---");
		serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				binder = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				Flog.d("----MainActivity-----onServiceConnected()");									
				binder = (MediaBinder) service;
				Flog.d("----MainActivity-----onServiceConnected()---1");
				if (binder != null) {
					Flog.d("----MainActivity-----onServiceConnected()---2----"+binder.toString());
					canSkip = true;// 重置
					binder.setOnPlayStartListener(new OnPlayStartListener() {
						

						@Override
						public void onStart(MusicInfo info) {

							// TODO Auto-generated method stub

							Flog.d("----MainActivity-----onServiceConnected()---onStart()--");
							playerPage = musicAdapter.getPage();
							mainArtist.setText(info.getArtist());
							mainName.setText(info.getName());
							mp3Duration = info.getTime();
							Flog.d("----MainActivity-----onServiceConnected()---mp3Duration--"+mp3Duration);
							Flog.d("----MainActivity-----onServiceConnected()---mp3Current--"+mp3Current);
							if (mp3Current == null) {
								mainTime.setText(TIME_NORMAL + " - "
										+ mp3Duration);
							} else {
								mainTime.setText(mp3Current + " - "
										+ mp3Duration);
							}
							if (CoverList.cover == null) {
								mainAlbum
										.setImageResource(R.drawable.main_img_album);
							} else {
								mainAlbum.setImageBitmap(CoverList.cover);
							}
							btnPlay.setImageResource(R.drawable.main_btn_pause);
						}
					});
					binder.setOnPlayingListener(new OnPlayingListener() {

						@Override
						public void onPlay(int currentPosition) {
							// TODO Auto-generated method stub
							Flog.d("----MainActivity-----onServiceConnected()---onPlay()--");
							mp3Current = FormatUtil.formatTime(currentPosition);
							mainTime.setText(mp3Current + " - " + mp3Duration);
							
						}
					});
					binder.setOnPlayPauseListener(new OnPlayPauseListener() {

						@Override
						public void onPause() {
							// TODO Auto-generated method stub
							Flog.d("----MainActivity-----onServiceConnected()---onPause()--");
							btnPlay.setImageResource(R.drawable.main_btn_play);
						}
					});
					binder.setOnPlayCompletionListener(new OnPlayCompleteListener() {

						@Override
						public void onPlayComplete() {
							// TODO Auto-generated method stub
							Flog.d("----MainActivity-----onServiceConnected()---onPlayComplete()--");
							mp3Current = null;
						}
					});
					binder.setOnPlayErrorListener(new OnPlayErrorListener() {

						@Override
						public void onPlayError() {
							// TODO Auto-generated method stub
							Flog.d("----MainActivity-----onServiceConnected()---onPlayError()--");
							dialogMenuPosition = musicPosition;
							removeList();
						}
					});
					binder.setLyricView(null,null,null,true);
					Flog.d("----MainActivity-----onServiceConnected()---end");
				}
			}
		};
		
	}

	/**
	 * 带返回值跳转至扫描页面
	 */
	private void intentScanActivity() {
		Flog.d("MainActivity--intiScanActivity()");
		Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
		startActivity(intent);
	}

	/**
	 * 从当前歌曲列表中移除
	 */
	private void removeList() {
		MusicInfo info = null;
		int size = 0;
		switch (slidingPage) {
		case MainActivity.SLIDING_MENU_ALL:
			size = MusicList.list.size();
			info = MusicList.list.get(dialogMenuPosition);
			break;

		case MainActivity.SLIDING_MENU_FAVORITE:
			size = FavoriteList.list.size();
			info = FavoriteList.list.get(dialogMenuPosition);
			break;

		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			size = FolderList.list.get(folderPosition).getMusicList().size();
			info = FolderList.list.get(folderPosition).getMusicList()
					.get(dialogMenuPosition);
			break;
		}
		if (dialogMenuPath == null) {
			dialogMenuPath = info.getPath();
		}
		MusicList.list.remove(info);
		FavoriteList.list.remove(info);
		for (int i = 0; i < FolderList.list.size(); i++) {
			FolderList.list.get(i).getMusicList().remove(info);
		}
		musicAdapter.update(slidingPage);
		mainSize.setText(musicAdapter.getCount() + "首歌曲");

		DBDao db = new DBDao(getApplicationContext());
		db.delete(dialogMenuPath);
		// db.updateMusic(info.getId());
		db.close();
		if (binder != null && musicPosition == dialogMenuPosition) {
			if (musicPosition == (size - 1)) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
			} else {
				playIntent.putExtra(MediaService.INTENT_LIST_PAGE, slidingPage);
				Flog.d("MainActivity--reMoveList()--" + musicPosition);
				playIntent.putExtra(MediaService.INTENT_LIST_POSITION,
						musicPosition);// *****************************************************************************S
				startService(playIntent);
			}
		}
	}

	private void deleteFile() {
		Flog.d("MainActivity--deleFile()");
		File file = new File(dialogMenuPath);
		if (file.delete()) {
			Toast.makeText(getApplicationContext(), "文件以被删除！",
					Toast.LENGTH_LONG).show();
			removeList();
		}
	}

	private void exitProgram() {
		Flog.d("MainActivity--exitOrogram()--start");
		stopService(playIntent);
		
		Intent playstateIntent = new Intent("com.flyaudiomedia.pause");
		sendBroadcast(playstateIntent);
		AllListActivity.getInstance().exit();
		Flog.d("MainActivity--exitOrogram()--end");
		// finish();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.activity_main_view_back:// 返回上一级监听
			viewBack.setVisibility(View.GONE);
			mainTitle.setText(TITLE_FOLDER);
			musicAdapter.update(SLIDING_MENU_FOLDER);
			mainSize.setText(musicAdapter.getCount() + "个文件夹");
			break;

		case R.id.activity_main_ib_menu:// 侧滑按钮监听
			showMenu();
			break;

		case R.id.activity_main_view_bottom:// 底部播放控制视图监听
			if (serviceConnection != null && canSkip) {
				canSkip = false;
				unbindService(serviceConnection);
				bindState = false;
			}
			Intent intent = new Intent(getApplicationContext(),
					PlayerActivity.class);
			intent.putExtra(BROADCAST_INTENT_POSITION, musicPosition);

			Flog.d("MainActivity--musicPosition--" + musicPosition);
			startActivity(intent);
			break;

		case R.id.activity_main_ib_previous:
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
			}
			break;

		case R.id.activity_main_ib_play:
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
			}
			break;

		case R.id.activity_main_ib_next:
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
			}
			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.activity_main_ib_previous:
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REWIND);
			}
			break;

		case R.id.activity_main_ib_next:
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_FORWARD);
			}
			break;
		}
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
			binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
		}
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		slidingPage = musicAdapter.getPage();
		playIntent.putExtra(MediaService.INTENT_LIST_PAGE, slidingPage);
		musicPosition = position;
		switch (slidingPage) {
		case SLIDING_MENU_FOLDER:// 文件夹
			folderPosition = position;
			viewBack.setVisibility(View.VISIBLE);
			mainTitle.setText(FolderList.list.get(folderPosition)
					.getMusicFolder());
			musicAdapter.setFolderPosition(folderPosition);
			musicAdapter.update(SLIDING_MENU_FOLDER_LIST);
			mainSize.setText(musicAdapter.getCount() + "首歌曲");
			return;

		case SLIDING_MENU_FOLDER_LIST:// 文件夹歌曲列表
			playIntent.putExtra(MediaService.INTENT_FOLDER_POSITION,
					folderPosition);
			break;
		}
		playIntent.putExtra(MediaService.INTENT_LIST_POSITION, musicPosition);

		Flog.d("MainActivity--musicPosition--" + musicPosition);

		startService(playIntent);

	}

	@Override
	public void onDismiss(int dialogId) {
		// TODO Auto-generated method stub
		switch (dialogId) {
		case DIALOG_SCAN:// 跳转至扫描页面
			intentScanActivity();
			break;

		case DIALOG_MENU_REMOVE:// 执行移除
			removeList();
			break;
		case DIALOG_MENU_RINGTONE:// 显示铃声
		      setRing();
			break;
		case DIALOG_MENU_SHARE:// 显示分享
		      getShare();
			break;

		case DIALOG_MENU_DELETE:// 显示删除对话框
			DeleteDialog deleteDialog = new DeleteDialog(this);
			deleteDialog.setOnTVAnimDialogDismissListener(this);
			deleteDialog.show();
			break;

		case DIALOG_MENU_INFO:// 显示歌曲详情
			Flog.d("----------DIALOG_MENU_INFO--------------");
			InfoDialog infoDialog = new InfoDialog(this);
			infoDialog.setOnTVAnimDialogDismissListener(this);
			infoDialog.show();
			switch (slidingPage) {// 必须在show后执行
			case MainActivity.SLIDING_MENU_ALL:
				Flog.d("MainActivity----MusicList---dialogMenuPosition---"
						+ dialogMenuPosition);
				Flog.d("MainActivity----MusicList---dialogMenuPosition---"
						+ MusicList.list.get(dialogMenuPosition).toString());
				String p = MusicList.list.get(dialogMenuPosition).getPath();
				Flog.d("mainactiviy--infodialog---" + p);
				infoDialog.setInfo(MusicList.list.get(dialogMenuPosition));
				break;

			case MainActivity.SLIDING_MENU_FAVORITE:
				synchronized (lockObject) {
					infoDialog.setInfo(FavoriteList.list
							.get(dialogMenuPosition));
					lockObject.notifyAll();
				}

				Flog.d("MainActivity----FavoriteList--dialogMenuPosition---"
						+ dialogMenuPosition);
				break;

			case MainActivity.SLIDING_MENU_FOLDER_LIST:
				infoDialog.setInfo(FolderList.list.get(folderPosition)
						.getMusicList().get(dialogMenuPosition));
				break;
			}
			break;

		case DIALOG_DELETE:// 执行删除
			deleteFile();
			break;
		}
	}
	
	/**
	 * 设置铃声
	 */
	protected void setRing() {
		Flog.d("MainActivity----setRing()");
		Flog.d("MainActivity----setRing()---musicPosition---"+musicPosition);
		Flog.d("MainActivity----setRing()---dialogMenuPosition---"+dialogMenuPosition);
		RadioGroup rg_ring = new RadioGroup(MainActivity.this);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		rg_ring.setLayoutParams(params);
		// 第一个单选按钮，来电铃声
		final RadioButton rbtn_ringtones = new RadioButton(MainActivity.this);
		rbtn_ringtones.setText("来电铃声");
		rg_ring.addView(rbtn_ringtones, params);
		// 第二个单选按钮，闹铃铃声
		final RadioButton rbtn_alarms = new RadioButton(MainActivity.this);
		rbtn_alarms.setText("闹铃铃声");
		rg_ring.addView(rbtn_alarms, params);
		// 第三个单选按钮，通知铃声
		final RadioButton rbtn_notifications = new RadioButton(
				MainActivity.this);
		rbtn_notifications.setText("通知铃声");
		rg_ring.addView(rbtn_notifications, params);
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(
				MainActivity.this);
		mBuilder.setTitle("设置铃声").setView(rg_ring)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						dialog.dismiss();
						if (rbtn_ringtones.isChecked()) {
							try {
								// 设置来电铃声
								setRingtone(dialogMenuPosition);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (rbtn_alarms.isChecked()) {
							try {
								// 设置闹铃
								setAlarm(dialogMenuPosition);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (rbtn_notifications.isChecked()) {
							try {
								// 设置通知铃声
								setNotifaction(dialogMenuPosition);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 设置提示音
	 * 
	 * @param position
	 */
	protected void setNotifaction(int position) {
		musicInfo = MusicList.list.get(position);
		Flog.d("MainActivity----setNotifaction()---musicInfo---"+musicInfo.toString());
		Flog.d("MainActivity----setNotifaction()---musicInfo.getPath()---"+musicInfo.getPath());
		File sdfile = new File(musicInfo.getPath());
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
		values.put(MediaStore.Audio.Media.IS_ALARM, false);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);

		Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
				.getAbsolutePath());
		Uri newUri = this.getContentResolver().insert(uri, values);
		RingtoneManager.setActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_NOTIFICATION, newUri);
		Toast.makeText(getApplicationContext(), "设置通知铃声成功！", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 设置闹铃
	 * 
	 * @param position
	 */
	protected void setAlarm(int position) {
		musicInfo = MusicList.list.get(position);
		File sdfile = new File(musicInfo.getPath());
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, true);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);

		Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
				.getAbsolutePath());
		Uri newUri = this.getContentResolver().insert(uri, values);
		RingtoneManager.setActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_ALARM, newUri);
		Toast.makeText(getApplicationContext(), "设置闹钟铃声成功！", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 设置来电铃声
	 * 
	 * @param position
	 */
	protected void setRingtone(int position) {
		Flog.d("MainActivity---setRingtone()--start");
		Flog.d("MainActivity---setRingtone()--position--"+position);
		 int music_id=0;
		musicInfo = MusicList.list.get(position);
		Flog.d("MainActivity---setRingtone()--musicInfo--"+musicInfo.toString());
		File sdfile = new File(musicInfo.getPath());
		Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
				.getAbsolutePath());
		Flog.d("MainActivity---setRingtone()----uri--"+uri);
		Flog.d("MainActivity---setRingtone()--MediaStore.MediaColumns.DATA--"+sdfile.getAbsolutePath());
		Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.MediaColumns.DATA, new String[]{sdfile.getAbsolutePath()},null);
		if (cursor==null) {
			Flog.d("MainActivity---setRingtone()--Cursor==null--");
		}
		
		if (cursor!=null) {
			Flog.d("MainActivity---setRingtone()--------");
			music_id=cursor.getInt(0);
			Flog.d("MainActivity---setRingtone()--music_id--"+music_id);
			while (cursor.moveToNext()) {
				String dataString=cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
				Flog.d("MainActivity---setRingtone()--dataString--"+dataString);
			}
			
		}
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns._ID, music_id);
		values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
//		values.put(MediaStore.Audio.Media.DATA, sdfile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, false);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);

		
		
		Uri newUri = getApplicationContext().getContentResolver().insert(uri, values);
		Flog.d("MainActivity---setRingtone()----newUri--"+newUri);
		RingtoneManager.setActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_RINGTONE, newUri);
		Toast.makeText(getApplicationContext(), "设置来电铃声成功！", Toast.LENGTH_SHORT)
				.show();
		Flog.d("MainActivity---setRingtone()--end");
	}
	
	private void getShare(){
		Intent intent=new Intent(Intent.ACTION_SEND);    
        intent.setType("image/*");    
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share");    
        intent.putExtra(Intent.EXTRA_TEXT, "I have successfully share my message through my app !!!");            
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    
        startActivity(Intent.createChooser(intent, getTitle()));    
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			toggle();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private class MainReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent != null) {
				final String action = intent.getAction();

				if (action.equals(BROADCAST_ACTION_EXIT)) {
					exitProgram();
					return;
				} else if (action.equals(BROADCAST_ACTION_SCAN)) {
					if (musicAdapter != null) {
						// 从扫描页面返回的更新全部歌曲列表数据
						musicAdapter.update(SLIDING_MENU_ALL);
						mainSize.setText(musicAdapter.getCount() + "首歌曲");
					}
					return;
				}

				// 没有传值的就是通过播放界面标记我的最爱的，所以默认赋值上次点击播放的页面，为0则默认为全部歌曲
				slidingPage = intent.getIntExtra(BROADCAST_INTENT_PAGE,
						playerPage == 0 ? SLIDING_MENU_ALL : playerPage);

				Flog.d("slidingPage------" + slidingPage);
				Flog.d("playerPage------" + playerPage);
				dialogMenuPosition = intent.getIntExtra(
						BROADCAST_INTENT_POSITION, 0);
				MusicInfo info = null;
				switch (slidingPage) {
				case MainActivity.SLIDING_MENU_ALL:
					info = MusicList.list.get(dialogMenuPosition);
					break;

				case MainActivity.SLIDING_MENU_FAVORITE:

					Flog.d("MainActivity.SLIDING_MENU_FAVORITE-----");
					int size = FavoriteList.list.size();
					

					synchronized (lockObject) {
						if (FavoriteList.list.isEmpty()) {
							return;
						}else {
							if (size<=dialogMenuPosition) {
								dialogMenuPosition=0;
							}

							info = FavoriteList.list.get(dialogMenuPosition);
							lockObject.notifyAll();
						}
						
					}

					Flog.d("MainActivity.SLIDING_MENU_FAVORITE---info--" + info);
					Flog.d("MainActivity.SLIDING_MENU_FAVORITE---dialogMenuPosition--"
							+ dialogMenuPosition);
					break;

				case MainActivity.SLIDING_MENU_FOLDER_LIST:
					info = FolderList.list.get(folderPosition).getMusicList()
							.get(dialogMenuPosition);
					break;
				}

				if (info != null) {
					if (action.equals(BROADCAST_ACTION_MENU)) {
						MenuDialog menuDialog = new MenuDialog(
								MainActivity.this);
						menuDialog
								.setOnTVAnimDialogDismissListener(MainActivity.this);
						menuDialog.show();
						menuDialog.setDialogTitle(info.getName());// 必须在show后执行
					} else if (action.equals(BROADCAST_ACTION_FAVORITE)) {
						// 因为源数据是静态的，所以赋值给info也指向了静态数据的那块内存，直接改info的数据就行
						// 不知我的理解对否。而且这算不算内存泄露？？？
						Flog.d("MainActivity--isFavorite--" + info.isFavorite());
						if (info.isFavorite()) {
							info.setFavorite(false);// 删除标记
							synchronized (lockObject) {
								FavoriteList.list.remove(info);// 移除
								lockObject.notifyAll();
							}

							Flog.d("MainActivity--isFavorite--remove--"
									+ FavoriteList.list.size());
						} else {
							info.setFavorite(true);// 标记为喜爱
							synchronized (lockObject) {
								FavoriteList.list.add(info);// 新增
								lockObject.notifyAll();
							}

							Flog.d("MainActivity--isFavorite--add--"
									+ FavoriteList.list.size());
							FavoriteList.sort();// 重新排序
						}
						DBDao db = new DBDao(getApplicationContext());
						db.update(info.getName(), info.isFavorite());// 更新数据库
						db.close();// 必须关闭
						musicAdapter.update(musicAdapter.getPage());
						mainSize.setText(musicAdapter.getCount() + "首歌曲");
					}
					dialogMenuPath = info.getPath();
				}
			}
		}
	}

}
