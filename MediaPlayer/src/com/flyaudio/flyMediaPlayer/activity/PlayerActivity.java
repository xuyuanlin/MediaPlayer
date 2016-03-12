package com.flyaudio.flyMediaPlayer.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.flyaudio.flyMediaPlayer.R;
import com.flyaudio.flyMediaPlayer.dialog.AboutDialog;
import com.flyaudio.flyMediaPlayer.dialog.InfoDialog;
import com.flyaudio.flyMediaPlayer.objectInfo.MusicInfo;
import com.flyaudio.flyMediaPlayer.objectInfo.MyApp;
import com.flyaudio.flyMediaPlayer.perferences.CoverList;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaService;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnModeChangeListener;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnPlayCompleteListener;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnPlayErrorListener;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnPlayPauseListener;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnPlayStartListener;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder.OnPlayingListener;
import com.flyaudio.flyMediaPlayer.until.Flog;
import com.flyaudio.flyMediaPlayer.until.FormatUtil;
import com.flyaudio.flyMediaPlayer.until.LyricGesture;
import com.flyaudio.flyMediaPlayer.view.LyricView;
import com.flyaudio.flyMediaPlayer.view.MyLyricView;
import com.flyaudio.flyMediaPlayer.view.PushView;
import com.flyaudio.flyMediaPlayer.view.VisualizerView;
import com.flyaudio.flyMediaPlayer.until.AllListActivity;

public class PlayerActivity extends Activity implements OnClickListener,
		OnLongClickListener, OnTouchListener, OnSeekBarChangeListener {

	private final String TIME_NORMAL = "00:00";
	private final int[] modeImage = { R.drawable.player_btn_mode_normal_style,
			R.drawable.player_btn_mode_repeat_one_style,
			R.drawable.player_btn_mode_repeat_all_style,
			R.drawable.player_btn_mode_random_style };
	Button seekbarFinish;
	SeekBar seekBarA,seekBarR,seekBarG,seekBarB;
	private int skinId;// 背影ID
	private int colorId;// 歌词的高亮
	private int musicPosition;// 当前播放歌曲索引

	private boolean isFavorite = false;// 是否最爱
	private boolean isPortraitActivity = true;// 是否竖屏
	private boolean isFirstTransition3dAnimation = true;// 3D

	private ImageButton btnMode;// 播放模式
	private ImageButton btnReturn;// 返回
	private ImageButton btnPrevious;// 上一首
	private ImageButton btnPlay;// 播放
	private ImageButton btnNext;// 下一首
	private ImageButton btnFavorite;// 最爱
	private ImageButton menuButton;// 菜单
	private ImageButton menuAbout;// 菜单--关于
	private ImageButton menuInfo;// 菜单--详情
	private ImageButton menuSetting;// 菜单--设置
	private ImageButton menuExit;// 菜单--退出

	private RelativeLayout skin;// 背影图
	private RelativeLayout menu;// 菜单

	private TextView currentTime;// 当前时间
	private TextView totalTime;// 总时间
	private SeekBar seekBar;// 进度条
	private SeekBar seekVolumeBar;// 音量进度条
	private PushView mp3Name;
	private PushView mp3Info;
	private PushView mp3Artist;
	private ImageView mp3Cover;
	private ImageView mp3Favorite;
	private LyricView lyricView;
	private LyricView lrcView;
	private MyLyricView myLyricView;// 所有歌词的呈现页面
	private PopupWindow popupVolume;
	private VisualizerView visualizer;// 均衡器视图
	// private MyVisualizerView mVisualizerView;

	private Intent playIntent;
	private MediaBinder binder;
	private MusicInfo musicInfo;
	private AudioManager audioManager;
	private SharedPreferences preferences;
	private ServiceConnection serviceConnection;
	// private TextView text;
//	private ImageButton volume;
	private TextView lrcText;
	private boolean isAllLrc = false;
	// public static LrcView lyricView1;// 所有歌词的呈现页面
	public static final String BROADCAST_SHOW_LRC = "com.flyaudio.SHOW_LRC";
	public static final String BROADCAST_HIDE_LRC = "com.flyaudio.HIDE_LRC";
	private MyApp myApp;
	/*
	 * private Equalizer equalizer; // 均衡器 private BassBoost bassBoost;// 重低音控制器
	 */
	

	private float ldriftx;
	private float ldrifty;
	private PlayerReceiver mReceiver;
	private int type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		myApp = (MyApp) getApplication();
		AllListActivity.getInstance().addActivity(this);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		type = 0;
		init();
		mReceiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_SHOW_LRC);
		filter.addAction(BROADCAST_HIDE_LRC);
		registerReceiver(mReceiver, filter);

		Intent intent = new Intent(MediaService.BROADCAST_ACTION_SERVICE);
		intent.putExtra(MediaService.INTENT_ACTIVITY,
				MediaService.ACTIVITY_PLAYER);
		sendBroadcast(intent);

	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Flog.d("PlayerActivity--Onstart()");
		super.onStart();
		int id1 = preferences.getInt(MainActivity.PREFERENCES_SKIN,
				R.drawable.skin_bg1);
		int id2 = preferences.getInt(MainActivity.PREFERENCES_LYRIC,
				Color.argb(250, 251, 248, 29));
		if (skinId != id1) {// 判断是否更换背景图
			skinId = id1;
			if (isPortraitActivity) {
				skin.setBackgroundResource(skinId);
			}
		}
		if (colorId != id2) {// 判断是否更改歌词亮度
			colorId = id2;
			lyricView.setLyricHighlightColor(colorId);
			// lyricView1.setLyricHighlightColor(colorId);
			lrcView.setLyricHighlightColor(colorId);
			myLyricView.setLyricHighlightColor(colorId);
			Flog.d("PlayerActivity--Onstart()---end");
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		if (serviceConnection != null) {
			unbindService(serviceConnection);// 一定要在finish之前解除绑定
			serviceConnection = null;
		}
		if (isPortraitActivity) {
			// mvisualizer.release();
			// mediaPlayer=null;
			// mVisualizerView.releaseVisualizerFx();
			visualizer.releaseVisualizerFx();// 暂停更新音乐可视化界面动画
			Flog.d("---PlayerActivity---visualizer.releaseVisualizerFx()");
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		if (serviceConnection != null) {
			unbindService(serviceConnection);
			serviceConnection = null;
		}

		if (popupVolume.isShowing()) {
			popupVolume.dismiss();// 切换前消失音量框
		}

		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			isPortraitActivity = true;
			initPortraitActivity();// 重新初始化竖屏界面
		} else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			isPortraitActivity = false;
			if (visualizer != null) {
				visualizer.releaseVisualizerFx();// 释放音乐可视化界面
				// mVisualizerView.releaseVisualizerFx();
				Flog.d("---PlayerActivity---visualizer.releaseVisualizerFx()");
			}

		}
	}

	private void init() {
		Flog.d("PlayerActivity--init()--start");
		musicPosition = getIntent().getIntExtra(
				MainActivity.BROADCAST_INTENT_POSITION, 0);// ***************************************************************************************
		Flog.d("PlayerActivity--init()--musicPosition-----" + musicPosition);

		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		skinId = preferences.getInt(MainActivity.PREFERENCES_SKIN,
				R.drawable.skin_bg1);
		colorId = preferences.getInt(MainActivity.PREFERENCES_LYRIC,
				Color.argb(250, 251, 248, 29));
		playIntent = new Intent(getApplicationContext(), MediaService.class);

//		Configuration config = getResources().getConfiguration();
		// if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {\

		isPortraitActivity = true;

		initPortraitActivity();

		// } else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE)
		// {
		// isPortraitActivity = false;
		// initLandscapeActivity();
		// }
		initPopupVolume();
		
		Flog.d("PlayerActivity--init()--end");
	}

	// 竖频初始化控件及相应的监听
	private void initPortraitActivity() {
		Flog.d("PlayerActivity--initPortraitActivity()--start");
		setContentView(R.layout.activity_player);

		skin = (RelativeLayout) findViewById(R.id.activity_player_skin);
//		volume = (ImageButton) findViewById(R.id.activity_player_ib_volume);
		btnReturn = (ImageButton) findViewById(R.id.activity_player_ib_return);
		btnMode = (ImageButton) findViewById(R.id.activity_player_ib_mode);
		btnPrevious = (ImageButton) findViewById(R.id.activity_player_ib_previous);
		btnPlay = (ImageButton) findViewById(R.id.activity_player_ib_play);
		btnNext = (ImageButton) findViewById(R.id.activity_player_ib_next);
		btnFavorite = (ImageButton) findViewById(R.id.activity_player_ib_favorite);
		seekBar = (SeekBar) findViewById(R.id.activity_player_seek);
		currentTime = (TextView) findViewById(R.id.activity_player_tv_time_current);
		totalTime = (TextView) findViewById(R.id.activity_player_tv_time_total);
		mp3Name = (PushView) findViewById(R.id.activity_player_tv_name);
		mp3Info = (PushView) findViewById(R.id.activity_player_tv_info);
		mp3Artist = (PushView) findViewById(R.id.activity_player_tv_artist);
		mp3Cover = (ImageView) findViewById(R.id.activity_player_cover);
		mp3Favorite = (ImageView) findViewById(R.id.activity_player_iv_favorite);
		lrcView = (LyricView) findViewById(R.id.activity_player_lyric);
		lyricView = (LyricView) findViewById(R.id.activity_player_lyricview);
		myLyricView = (MyLyricView) findViewById(R.id.activity_player_lview);
		visualizer = (VisualizerView) findViewById(R.id.activity_player_visualizer);
		lrcText = (TextView) findViewById(R.id.lrctext);
		currentTime.setText(TIME_NORMAL);
		totalTime.setText(TIME_NORMAL);


		lrcText.setOnClickListener(this);
		visualizer.setOnClickListener(this);
		btnReturn.setOnClickListener(this);
		btnMode.setOnClickListener(this);
		btnPrevious.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		btnFavorite.setOnClickListener(this);
		mp3Artist.setOnClickListener(this);
		btnPrevious.setOnLongClickListener(this);
		btnNext.setOnLongClickListener(this);
		btnPrevious.setOnTouchListener(this);
		btnNext.setOnTouchListener(this);
		seekBar.setOnSeekBarChangeListener(this);

		btnMode.setImageResource(modeImage[preferences.getInt(
				MainActivity.PREFERENCES_MODE, 0)]);
		skin.setBackgroundResource(skinId);
		lyricView.setLyricHighlightColor(colorId);
		lrcView.setLyricHighlightColor(colorId);
		myLyricView.setLyricHighlightColor(colorId);

		serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				binder = null;
			}
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				Flog.d("PlayerActivity--------onServiceConnected---");
				binder = (MediaBinder) service;

				if (binder != null) {
					myLyricView.setOnTouchListener(new LyricGesture(
							PlayerActivity.this, binder, myLyricView));
					Flog.d("PlayerActivity----onServiceConnected----binder---"
							+ (binder == null));
					binder.setOnPlayStartListener(new OnPlayStartListener() {

						@Override
						public void onStart(MusicInfo info) {
							Flog.d("PlayerActivity--onServiceConnected--onStart()--start");
							// TODO Auto-generated method stub

							mp3Name.setText(info.getName());
							currentTime.setText(TIME_NORMAL);
							totalTime.setText(info.getTime());
							seekBar.setMax(info.getMp3Duration());
							
							Flog.d("PlayerActivity----onServiceConnected--info.getAudioSessionId()--"
									+ info.getAudioSessionId());
							try {
								visualizer.setupVisualizerFx(0);

							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}
							//音乐列表的listview

							ArrayList<String> list = new ArrayList<String>();
							list.add(info.getFormat());
							list.add("大小: " + info.getSize());
							list.add(info.getGenre());
							list.add(info.getAlbum());
							list.add(info.getYears());
							list.add(info.getChannels());
							list.add(info.getKbps());
							list.add(info.getHz());
							mp3Info.setTextList(list);
							mp3Artist.setText(info.getArtist());
							isFirstTransition3dAnimation = true;
							if (CoverList.cover == null) {
								startTransition3dAnimation(BitmapFactory
										.decodeResource(getResources(),
												R.drawable.player_cover_default));
							} else {
								startTransition3dAnimation(CoverList.cover);
							}
							btnPlay.setImageResource(R.drawable.player_btn_pause_style);
							isFavorite = info.isFavorite();
							Flog.d("PlayerActivity----onServiceConnected()---isFavorite---"
									+ isFavorite);
							btnFavorite
									.setImageResource(isFavorite ? R.drawable.player_btn_favorite_star_style
											: R.drawable.player_btn_favorite_nostar_style);
							musicInfo = info;
							Flog.d("PlayerActivity----onServiceConnected()---onStart---end");

						}
					});
					binder.setOnPlayingListener(new OnPlayingListener() {

						@Override
						public void onPlay(int currentPosition) {// //定位到了----------------------------currentPosition
							// TODO Auto-generated method stub

							Flog.d("PlayerActivity--onServiceConnected--onPlay()");
							seekBar.setProgress(currentPosition);
							currentTime.setText(FormatUtil
									.formatTime(currentPosition));

						}
					});
					binder.setOnPlayPauseListener(new OnPlayPauseListener() {

						@Override
						public void onPause() {
							// TODO Auto-generated method stub
							Flog.d("PlayerActivity--onServiceConnected--onPause()");

							btnPlay.setImageResource(R.drawable.player_btn_play_style);
						}
					});
					binder.setOnPlayCompletionListener(new OnPlayCompleteListener() {

						@Override
						public void onPlayComplete() {
							// TODO Auto-generated method stub

						}
					});
					binder.setOnPlayErrorListener(new OnPlayErrorListener() {

						@Override
						public void onPlayError() {
							// TODO Auto-generated method stub

						}
					});
					binder.setOnModeChangeListener(new OnModeChangeListener() {

						@Override
						public void onModeChange(int mode) {
							// TODO Auto-generated method stub
							btnMode.setImageResource(modeImage[mode]);
						}
					});
					binder.setLyricView(lrcView,lyricView, myLyricView, true);// 设置歌词视图，是卡拉OK模式S
				}
			}
		};
		bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);

		initPathMenu();
		Flog.d("PlayerActivity--initPortraitActivity()--end");
	}

	private void setRGB(){	
		final Dialog selectDialog = new Dialog(this, 0);
		selectDialog.setCancelable(true);
		selectDialog.setTitle(getResources().getString(R.string.xml_set_color));
		Window window = selectDialog.getWindow();
		window.setGravity(Gravity.BOTTOM);
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.3f;
		window.setAttributes(lp);
		LayoutInflater mInflater = LayoutInflater.from(this);
		LinearLayout layout = (LinearLayout) mInflater.inflate(
				R.layout.dialog_layout, null);
		selectDialog.setContentView(layout);
		 seekbarFinish = (Button) selectDialog
				.findViewById(R.id.seekbarFinish);
		seekBarA = (SeekBar) layout.findViewById(R.id.seekBarA);
		seekBarR = (SeekBar) layout.findViewById(R.id.seekBarR);
		seekBarG = (SeekBar) layout.findViewById(R.id.seekBarG);
		seekBarB = (SeekBar) layout.findViewById(R.id.seekBarB);
		seekBarA.setMax(255);
		seekBarR.setMax(255);
		seekBarG.setMax(255);
		seekBarB.setMax(255);
		seekBarA.setOnSeekBarChangeListener(this);
		seekBarR.setOnSeekBarChangeListener(this);
		seekBarG.setOnSeekBarChangeListener(this);
		seekBarB.setOnSeekBarChangeListener(this);
		seekbarFinish.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectDialog.dismiss();// 隐藏对话框
			}
		});
		seekBarA.setProgress(VisualizerView.a);
		seekBarR.setProgress(VisualizerView.r);
		seekBarG.setProgress(VisualizerView.g);
		seekBarB.setProgress(VisualizerView.b);
		selectDialog.show();// 显示对话框
	}
	
	
	private void initPathMenu() {
		Flog.d("PlayerActivity--initPathMenu()--start");
		menu = (RelativeLayout) findViewById(R.id.activity_player_menu);
		menuButton = (ImageButton) findViewById(R.id.activity_player_ib_menu);
		menuAbout = (ImageButton) findViewById(R.id.activity_player_ib_menu_about);
		menuInfo = (ImageButton) findViewById(R.id.activity_player_ib_menu_info);
		menuSetting = (ImageButton) findViewById(R.id.activity_player_ib_menu_setting);
		menuExit = (ImageButton) findViewById(R.id.activity_player_ib_menu_exit);

		menuButton.setOnClickListener(this);
		menuAbout.setOnClickListener(this);
		menuInfo.setOnClickListener(this);
		menuSetting.setOnClickListener(this);
		menuExit.setOnClickListener(this);
		menu.setOnTouchListener(this);
		Flog.d("PlayerActivity--initPathMenu()--end");
	}

	private void initPopupVolume() {
		Flog.d("PlayerActivity--initPopupVolume()--start");
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		View view = LayoutInflater.from(this).inflate(R.layout.popup_volume,
				null);// 引入窗口配置文件
		popupVolume = new PopupWindow(view, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, false);
		seekVolumeBar = (SeekBar) view.findViewById(R.id.pupup_volume_seek);
		seekVolumeBar.setMax(audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		seekVolumeBar.setOnSeekBarChangeListener(this);
		popupVolume.setBackgroundDrawable(new BitmapDrawable());
		popupVolume.setOutsideTouchable(true);
		Flog.d("PlayerActivity--initPopupVolume()--end");
	}

	public boolean updatelab(float dx, float dy, boolean toggle) {
		if (toggle) {
			ldriftx = dx + ldriftx;
			ldrifty = dy + ldrifty;
		} else {
			if (Math.abs(ldriftx) < Math.abs(ldrifty)) {
				return true;
			}
			ldriftx = 0;
			ldrifty = 0;
		}
		return false;
	}

	public void updateprogress(float dx, float dy) {
		myLyricView.driftx = dx + myLyricView.driftx;
		myLyricView.drifty = dy + myLyricView.drifty;
		myLyricView.invalidate();// 更新视图
	}


	private void showAboutDialog() {
		AboutDialog aboutDialog = new AboutDialog(this);
		aboutDialog.show();
	}

	private void showInfoDialog() {
		if (musicInfo != null) {
			InfoDialog infoDialog = new InfoDialog(this);
			infoDialog.show();
			infoDialog.setInfo(musicInfo);
		} else {
			Toast.makeText(getApplicationContext(), "无歌曲信息", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void setMenuEnabled(boolean enabled) {
		Flog.d("PlayerActivity--setMenuEnabled()--start");
		menuButton.setEnabled(enabled);
		menuAbout.setEnabled(enabled);
		menuInfo.setEnabled(enabled);
		menuSetting.setEnabled(enabled);
		menuExit.setEnabled(enabled);
		Flog.d("PlayerActivity--setMenuEnabled()--end");
	}

	@Override
	public void onClick(View v) {
		Flog.d("PlayActivity--onClick()");
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.activity_player_visualizer:
			Flog.d("PlayerActivity---------activity_player_visualizer");
//			
			Intent affectIntent = new Intent(this, AffectActivity.class);
			affectIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicInfo.getAudioSessionId());
			startActivityForResult(affectIntent, 13);

			break;
		/*
		 * case R.id.myvisualizerView:// 频谱分析的切换 type++;
		 * Flog.d("PlayActivity--onTouch()----myvisualizerView---type---" +
		 * type); if (type > 2) { type = 0; } switch (type) { case 0:
		 * mVisualizerView.clearRenderers(); addBarGraphRenderers(); break; case
		 * 1: mVisualizerView.clearRenderers(); // addCircleBarRenderer();
		 * addCircleRenderer(); break; case 2: mVisualizerView.clearRenderers();
		 * // addCircleRenderer(); addCircleBarRenderer(); break;
		 * 
		 * case 3: mVisualizerView.clearRenderers(); addLineRenderer(); break;
		 * 
		 * }
		 * 
		 * break;
		 */

		case R.id.activity_player_ib_return:// 返回
			finish();
			break;

		case R.id.activity_player_ib_mode:// 模式
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_MODE);
			}
			break;

		case R.id.activity_player_ib_previous:// 向前
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
			}
			break;

		case R.id.activity_player_ib_play:// 播放
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
			}
			break;

		case R.id.activity_player_ib_next:// 向后
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
			}
			break;

		case R.id.activity_player_ib_favorite:// 我的最爱
			if (seekBar.getMax() > 0) { // 此判断表示播放过歌曲
				musicPosition = myApp.getPosition();
				Flog.d("--PlayerActivity--activity_player_ib_favorite--musicPosition--"
						+ musicPosition);

				Intent intent = new Intent(
						MainActivity.BROADCAST_ACTION_FAVORITE);

				intent.putExtra(MainActivity.BROADCAST_INTENT_POSITION,
						musicPosition);
				sendBroadcast(intent);

				if (isFavorite) {
					btnFavorite
							.setImageResource(R.drawable.player_btn_favorite_nostar_style);
					isFavorite = false;
				} else {
					btnFavorite
							.setImageResource(R.drawable.player_btn_favorite_star_style);
					startFavoriteImageAnimation();
					isFavorite = true;
				}
				Flog.d("--PlayerActivity--isFavorite----" + isFavorite);
				// DBDao db=new DBDao(PlayerActivity.this);
				//
				// db.update(musicInfo.getName(), isFavorite);// 更新数据库

			}
			break;
		// /////////////////////////////////////////////////////////////////////////
		case R.id.activity_player_tv_artist:
			seekVolumeBar.setProgress(audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC));
			popupVolume.showAsDropDown(mp3Name);
			break;
		case R.id.activity_player_ib_menu:// 关闭菜单
			startMenuRotateAnimationOut(R.id.activity_player_ib_menu);
			break;

		case R.id.activity_player_ib_menu_about: // 弹出关于本软件
			menuAbout.startAnimation(startAmplifyIconAnimation());
			menuInfo.startAnimation(startShrinkIconAnimation());
			menuSetting.startAnimation(startShrinkIconAnimation());
			menuExit.startAnimation(startShrinkIconAnimation());
			startMenuRotateAnimationOut(R.id.activity_player_ib_menu_about);
			break;

		case R.id.activity_player_ib_menu_info:// 弹出歌曲详情
			menuInfo.startAnimation(startAmplifyIconAnimation());
			menuAbout.startAnimation(startShrinkIconAnimation());
			menuSetting.startAnimation(startShrinkIconAnimation());
			menuExit.startAnimation(startShrinkIconAnimation());
			startMenuRotateAnimationOut(R.id.activity_player_ib_menu_info);
			break;

		case R.id.activity_player_ib_menu_setting:// 设置
			menuSetting.startAnimation(startAmplifyIconAnimation());
			menuAbout.startAnimation(startShrinkIconAnimation());
			menuInfo.startAnimation(startShrinkIconAnimation());
			menuExit.startAnimation(startShrinkIconAnimation());
			startMenuRotateAnimationOut(R.id.activity_player_ib_menu_setting);
			break;

		case R.id.activity_player_ib_menu_exit:// 退出
			menuExit.startAnimation(startAmplifyIconAnimation());
			menuAbout.startAnimation(startShrinkIconAnimation());
			menuInfo.startAnimation(startShrinkIconAnimation());
			menuSetting.startAnimation(startShrinkIconAnimation());
			startMenuRotateAnimationOut(R.id.activity_player_ib_menu_exit);
			break;

		case R.id.activity_player_landscape_ib_previous:
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
			}
			break;

		case R.id.activity_player_landscape_ib_play:
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
			}
			break;

		case R.id.activity_player_landscape_ib_next:
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
			}
			break;

		case R.id.activity_player_landscape_cover:
			seekVolumeBar.setProgress(audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC));
			popupVolume.showAsDropDown(mp3Name);
			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.activity_player_ib_previous:// 竖屏模式快退
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REWIND);
			}
			break;

		case R.id.activity_player_ib_next:// 竖屏模式快进
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_FORWARD);
			}
			break;

		case R.id.activity_player_landscape_ib_previous:// 横屏模式快退
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_FORWARD);
			}
			break;

		case R.id.activity_player_landscape_ib_next:// 横屏模式快进
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
		switch (v.getId()) {
		case R.id.activity_player_menu:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (menuButton.isShown()) {
					if (menuButton.isEnabled()) {
						startMenuRotateAnimationOut(R.id.activity_player_ib_menu);
					}
					return true;// 类似触摸Dialog以外的区域让其消失
				} else if (mp3Favorite.isShown()) {
					return true;// 动画执行屏蔽所有事件
				}
			}
			break;

		case R.id.activity_player_ib_previous:// 竖屏模式松手播放
			if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
			}
			break;

		case R.id.activity_player_ib_next:// 竖屏模式松手播放
			if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
			}
			break;

		case R.id.activity_player_landscape_ib_previous:// 横屏模式松手播放
			if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
			}
			break;

		case R.id.activity_player_landscape_ib_next:// 横屏模式松手播放
			if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
			}
			break;

		}
		return false;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (serviceConnection != null) {
			unbindService(serviceConnection);
			serviceConnection = null;
		}
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
		case R.id.activity_player_seek:
			if (fromUser && seekBar.getMax() > 0) {
				currentTime.setText(FormatUtil.formatTime(progress));
			}
			break;

		case R.id.pupup_volume_seek:
			if (fromUser) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						progress, 0);
			}
			break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		if (seekBar.getId() == R.id.activity_player_seek) {
			if (binder != null) {
				binder.seekBarStartTrackingTouch();
			}
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		if (seekBar.getId() == R.id.activity_player_seek) {
			if (binder != null) {
				binder.seekBarStopTrackingTouch(seekBar.getProgress());
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (isPortraitActivity) {
			if (keyCode == KeyEvent.KEYCODE_MENU) { // 跟随音量键调节
				if (popupVolume.isShowing()) {
					popupVolume.dismiss();
				}
				if (menuButton.isShown()) {
					if (menuButton.isEnabled()) {
						startMenuRotateAnimationOut(R.id.activity_player_ib_menu);
					}
				} else {
					startMenuRotateAnimationIn();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (menuButton.isShown()) {
					if (menuButton.isEnabled()) {
						startMenuRotateAnimationOut(R.id.activity_player_ib_menu);
					}
					return true;
				}
			}
		}
		if (popupVolume.isShowing()) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				seekVolumeBar.setProgress(seekVolumeBar.getProgress() + 1);
			} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				seekVolumeBar.setProgress(seekVolumeBar.getProgress() - 1);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void startMenuRotateAnimationIn() {
		AnimationSet animationSet = new AnimationSet(true);
		RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 360.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		Animation alphaAnimation = new AlphaAnimation(0.2f, 1.0f);

		animationSet.addAnimation(rotateAnimation);
		animationSet.addAnimation(alphaAnimation);
		animationSet.setDuration(500);
		animationSet.setFillAfter(true);
		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				setMenuEnabled(true);
			}
		});

		menuButton.setVisibility(View.VISIBLE);
		menuButton.startAnimation(animationSet);
		startIconTranslateAnimationIn();
	}

	/**
	 * 菜单关闭动画
	 * 
	 * @param id
	 *            ButtonId
	 */
	private void startMenuRotateAnimationOut(final int id) {
		setMenuEnabled(false);
		AnimationSet animationSet = new AnimationSet(true);
		RotateAnimation rotateAnimation = new RotateAnimation(0.0f, -360.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

		animationSet.addAnimation(rotateAnimation);
		animationSet.addAnimation(alphaAnimation);
		animationSet.setDuration(500);
		animationSet.setFillAfter(true);

		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				menuButton.setVisibility(View.GONE);
				menuButton.setClickable(false);
				menuButton.setFocusable(false);
				switch (id) {
				case R.id.activity_player_ib_menu_about:// 显示关于
					showAboutDialog();
					break;

				case R.id.activity_player_ib_menu_info:// 显示歌曲详情
					showInfoDialog();
					break;

				case R.id.activity_player_ib_menu_setting:// 跳转设置界面
					Intent intent = new Intent(getApplicationContext(),
							SettingActivity.class);
					startActivity(intent);
					break;

				case R.id.activity_player_ib_menu_exit:
					sendBroadcast(new Intent(MainActivity.BROADCAST_ACTION_EXIT));
					finish();
					break;
				}
			}
		});
		menuButton.startAnimation(animationSet);
		if (id == R.id.activity_player_ib_menu) {
			startIconTranslateAnimationOut();
		}
	}

	/**
	 * 图标的动画(入动画)
	 */
	private void startIconTranslateAnimationIn() {
		final int w = menuButton.getWidth() / 2;
		final int h = menuButton.getHeight() / 2;
		for (int i = 1; i < 5; i++) {// 从1开始目的是过滤掉menuButton
			ImageButton imageButton = (ImageButton) menu.getChildAt(i);
			imageButton.setVisibility(View.VISIBLE);
			MarginLayoutParams params = (MarginLayoutParams) imageButton
					.getLayoutParams();

			AnimationSet animationset = new AnimationSet(false);
			RotateAnimation rotateAnimation = new RotateAnimation(0.0f,
					-360.0f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			rotateAnimation.setInterpolator(new LinearInterpolator());
			Animation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
			TranslateAnimation translateAnimation = null;
			switch (i) {
			case 1:
				translateAnimation = new TranslateAnimation(params.rightMargin
						+ w, 0.0f, 0.0f, 0.0f);
				break;

			case 2:
				translateAnimation = new TranslateAnimation(0.0f, 0.0f,
						params.bottomMargin + h, 0.0f);
				break;

			case 3:
				translateAnimation = new TranslateAnimation(-params.leftMargin
						- w, 0.0f, 0.0f, 0.0f);
				break;

			case 4:
				translateAnimation = new TranslateAnimation(0.0f, 0.0f,
						-params.topMargin - h, 0.0f);
				break;
			}
			translateAnimation.setInterpolator(new OvershootInterpolator(2F));// 弹出再回来的动画的效果

			animationset.addAnimation(rotateAnimation);// 旋转动画必须比位移动画先执行
			animationset.addAnimation(alphaAnimation);
			animationset.addAnimation(translateAnimation);// 自己改改顺序就知道为什么
			animationset.setDuration(500);
			animationset.setFillAfter(true);

			imageButton.startAnimation(animationset);
		}
	}

	/**
	 * 图标的动画(出动画)
	 */
	private void startIconTranslateAnimationOut() {
		final int w = menuButton.getWidth() / 2;
		final int h = menuButton.getHeight() / 2;
		for (int i = 1; i < 5; i++) {
			final ImageButton imageButton = (ImageButton) menu.getChildAt(i);
			MarginLayoutParams params = (MarginLayoutParams) imageButton
					.getLayoutParams();

			AnimationSet animationset = new AnimationSet(false);
			RotateAnimation rotateAnimation = new RotateAnimation(360.0f, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			rotateAnimation.setInterpolator(new LinearInterpolator());
			Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
			TranslateAnimation translateAnimation = null;

			switch (i) {
			case 1:
				translateAnimation = new TranslateAnimation(0.0f,
						params.rightMargin + w, 0.0f, 0.0f);
				break;

			case 2:
				translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
						params.bottomMargin + h);
				break;

			case 3:
				translateAnimation = new TranslateAnimation(0f,
						-params.leftMargin - w, 0.0f, 0.0f);
				break;

			case 4:
				translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
						-params.topMargin - h);
				break;
			}

			animationset.addAnimation(rotateAnimation);
			animationset.addAnimation(alphaAnimation);
			animationset.addAnimation(translateAnimation);
			animationset.setDuration(500);
			animationset.setFillAfter(true);

			animationset.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					imageButton.setVisibility(View.GONE);
				}
			});
			imageButton.startAnimation(animationset);
		}
	}

	/**
	 * icon缩小消失的动画
	 */
	private Animation startShrinkIconAnimation() {
		ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
				0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setDuration(300);
		scaleAnimation.setFillAfter(true);
		return scaleAnimation;
	}

	/**
	 * icon放大渐变消失的动画
	 */
	private Animation startAmplifyIconAnimation() {
		AnimationSet animationset = new AnimationSet(true);

		ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 4.0f, 1.0f,
				4.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

		animationset.addAnimation(scaleAnimation);
		animationset.addAnimation(alphaAnimation);
		animationset.setDuration(300);
		animationset.setFillAfter(true);

		return animationset;
	}

	/**
	 * 我的最爱图片动画
	 */
	private void startFavoriteImageAnimation() {
		AnimationSet animationset = new AnimationSet(false);

		ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f,
				1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setInterpolator(new OvershootInterpolator(5F));
		scaleAnimation.setDuration(700);
		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		alphaAnimation.setDuration(500);
		alphaAnimation.setStartOffset(700);

		animationset.addAnimation(scaleAnimation);
		animationset.addAnimation(alphaAnimation);
		animationset.setFillAfter(true);

		animationset.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mp3Favorite.setVisibility(View.GONE);
			}
		});
		mp3Favorite.setVisibility(View.VISIBLE);
		mp3Favorite.startAnimation(animationset);
	}

	/**
	 * 专辑封面翻转动画
	 * 
	 * @param bitmap
	 *            专辑封面图
	 */
	private void startTransition3dAnimation(final Bitmap bitmap) {
		final int w = mp3Cover.getWidth() / 2;
		final int h = mp3Cover.getHeight() / 2;
		final MarginLayoutParams params = (MarginLayoutParams) mp3Cover
				.getLayoutParams();

		final Rotate3dAnimation rotation1 = new Rotate3dAnimation(0.0f, 90.0f,
				params.leftMargin + w, params.topMargin + h, 300.0f, true);
		rotation1.setDuration(500);
		rotation1.setFillAfter(true);
		rotation1.setInterpolator(new AccelerateInterpolator());

		final Rotate3dAnimation rotation2 = new Rotate3dAnimation(270.0f,
				360.0f, params.leftMargin + w, params.topMargin + h, 300.0f,
				false);
		rotation2.setDuration(500);
		rotation2.setFillAfter(true);
		rotation2.setInterpolator(new AccelerateInterpolator());

		rotation1.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				if (isFirstTransition3dAnimation) {
					isFirstTransition3dAnimation = false;
					mp3Cover.setImageBitmap(bitmap);
					mp3Cover.startAnimation(rotation2);
				}
			}
		});
		mp3Cover.startAnimation(rotation1);
	}

	/**
	 * 移植于ApiDemos里的的Rotate3dAnimation
	 * 
	 * 这里有个bug，180度翻转图片会反过来，所以只能折中的旋转一半又反转回来，效果就差了，交给各位去完善了
	 */
	private class Rotate3dAnimation extends Animation {
		private final float mFromDegrees;
		private final float mToDegrees;
		private final float mCenterX;
		private final float mCenterY;
		private final float mDepthZ;
		private final boolean mReverse;
		private Camera mCamera;

		/**
		 * Creates a new 3D rotation on the Y axis. The rotation is defined by
		 * its start angle and its end angle. Both angles are in degrees. The
		 * rotation is performed around a center point on the 2D space, definied
		 * by a pair of X and Y coordinates, called centerX and centerY. When
		 * the animation starts, a translation on the Z axis (depth) is
		 * performed. The length of the translation can be specified, as well as
		 * whether the translation should be reversed in time.
		 * 
		 * @param fromDegrees
		 *            the start angle of the 3D rotation
		 * @param toDegrees
		 *            the end angle of the 3D rotation
		 * @param centerX
		 *            the X center of the 3D rotation
		 * @param centerY
		 *            the Y center of the 3D rotation
		 * @param reverse
		 *            true if the translation should be reversed, false
		 *            otherwise
		 */
		public Rotate3dAnimation(float fromDegrees, float toDegrees,
				float centerX, float centerY, float depthZ, boolean reverse) {
			mFromDegrees = fromDegrees;
			mToDegrees = toDegrees;
			mCenterX = centerX;
			mCenterY = centerY;
			mDepthZ = depthZ;
			mReverse = reverse;
		}

		@Override
		public void initialize(int width, int height, int parentWidth,
				int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			mCamera = new Camera();
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			final float fromDegrees = mFromDegrees;
			float degrees = fromDegrees
					+ ((mToDegrees - fromDegrees) * interpolatedTime);

			final float centerX = mCenterX;
			final float centerY = mCenterY;
			final Camera camera = mCamera;

			final Matrix matrix = t.getMatrix();

			camera.save();
			if (mReverse) {
				camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
			} else {
				camera.translate(0.0f, 0.0f, mDepthZ
						* (1.0f - interpolatedTime));
			}
			camera.rotateY(degrees);
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);
		}
	}

	/*
	 * private void addBarGraphRenderers() {
	 * Flog.d("PlayerActivity---addBarGraphRenderers()"); Paint paint = new
	 * Paint(); paint.setStrokeWidth(18f); paint.setAntiAlias(true); //
	 * paint.setColor(Color.argb(200, 56, 138, 252));
	 * paint.setColor(Color.argb(255, 255, 255, 255)); // paint.setXfermode(new
	 * PorterDuffXfermode(Mode.SRC_OVER)); // paint.setAlpha(50);
	 * BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(5, paint,
	 * false); mVisualizerView.addRenderer(barGraphRendererBottom);
	 * 
	 * Paint paint2 = new Paint(); paint2.setStrokeWidth(12f);
	 * paint2.setAntiAlias(true); paint2.setColor(Color.argb(200, 181, 111,
	 * 233)); BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4,
	 * paint2, true); mVisualizerView.addRenderer(barGraphRendererTop);
	 * 
	 * }
	 * 
	 * private void addCircleBarRenderer() {
	 * Flog.d("PlayerActivity---addCircleBarRenderer()"); Paint paint = new
	 * Paint(); paint.setStrokeWidth(8f); paint.setAntiAlias(true);
	 * paint.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));
	 * paint.setColor(Color.argb(255, 222, 92, 143)); CircleBarRenderer
	 * circleBarRenderer = new CircleBarRenderer(paint, 32, true);
	 * mVisualizerView.addRenderer(circleBarRenderer); }
	 * 
	 * private void addCircleRenderer() {
	 * Flog.d("PlayerActivity---addCircleRenderer()"); Paint paint = new
	 * Paint(); paint.setStrokeWidth(3f); paint.setAntiAlias(true);
	 * paint.setColor(Color.argb(255, 222, 92, 143)); CircleRenderer
	 * circleRenderer = new CircleRenderer(paint, true);
	 * mVisualizerView.addRenderer(circleRenderer); }
	 * 
	 * private void addLineRenderer() {
	 * Flog.d("PlayerActivity---addLineRenderer()"); Paint linePaint = new
	 * Paint(); linePaint.setStrokeWidth(1f); linePaint.setAntiAlias(true);
	 * linePaint.setColor(Color.argb(88, 0, 128, 255));
	 * 
	 * Paint lineFlashPaint = new Paint(); lineFlashPaint.setStrokeWidth(5f);
	 * lineFlashPaint.setAntiAlias(true);
	 * lineFlashPaint.setColor(Color.argb(188, 255, 255, 255)); LineRenderer
	 * lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
	 * mVisualizerView.addRenderer(lineRenderer); }
	 */

	// public void barPressed(View view)
	// {
	// addBarGraphRenderers();
	// }
	//
	// public void circlePressed(View view)
	// {
	// addCircleRenderer();
	// }
	//
	// public void circleBarPressed(View view)
	// {
	// addCircleBarRenderer();
	// }
	//
	// public void linePressed(View view)
	// {
	// addLineRenderer();
	// }
	//
	// public void clearPressed(View view)
	// {
	// mVisualizerView.clearRenderers();
	// }

	private class PlayerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Flog.d("PlayerReceiver---onReceive()--start");
			Flog.d("PlayerReceiver---onReceive()--action--"
					+ intent.getAction());
			if (intent != null) {
				String action = intent.getAction();
				if (action.equals("com.flyaudio.SHOW_LRC")) {
					lrcText.setVisibility(TextView.VISIBLE);
					lrcText.setText("此歌曲没有本地歌词，请从网上下载...");
					
				}
				else if (action.equals("com.flyaudio.HIDE_LRC")) {
					lrcText.setText("");
				}
			}
			Flog.d("PlayerReceiver---onReceive()--end");

		}
	}

}
