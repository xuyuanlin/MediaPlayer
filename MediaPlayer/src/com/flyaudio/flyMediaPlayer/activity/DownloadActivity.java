package com.flyaudio.flyMediaPlayer.activity;

import java.io.File;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import com.flyaudio.flyMediaPlayer.R;
import com.flyaudio.flyMediaPlayer.activity.MainActivity;
import com.flyaudio.flyMediaPlayer.serviceImpl.DownloadProgressListener;
import com.flyaudio.flyMediaPlayer.until.AllListActivity;
import com.flyaudio.flyMediaPlayer.until.DBUtil;
import com.flyaudio.flyMediaPlayer.until.FileDownloader;
import com.flyaudio.flyMediaPlayer.until.Flog;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadActivity extends Activity {
	// private Button testDo ;
	private ListView progressList;
	private List<HashMap<String, Object>> list;
	private MyAdapter myAdapet;

	private MyHandler myHandler;
	public int fileSize;
	private String music;// 传音乐的名称
	private UpdateRunnable ur;
	private int max;
	public static final String PREFERENCES_SKIN = "skin";// 存储背影图
	private String path;
	private File saveDir;
	private boolean flag;
	private LinearLayout skin;
	private SharedPreferences preferences;
	private int skinId;// 背影ID
	private DBUtil mu;
	private ImageButton btnReturn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		com.flyaudio.flyMediaPlayer.until.Flog
				.d("DownloadActivity---oncreate()");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.show);
		myHandler = new MyHandler();
		mu = new DBUtil(DownloadActivity.this);
		AllListActivity.getInstance().addActivity(this);
		getstartIntent();
		list = mu.getAppList();
		Flog.d("DownloadActivity---oncreate()-----list" + list.size());
		checkSdcard();
		findView();
		initList();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onResume();
		Flog.d("DownloadActivity---onstart()");
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		int id = preferences.getInt(MainActivity.PREFERENCES_SKIN,
				R.drawable.skin_bg1);
		if (skinId != id) {// 判断是否换图
			skinId = id;
			skin.setBackgroundResource(skinId);
		}
		Flog.d("DownloadActivity---onstart()---end");
	}

	public void getstartIntent() {// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		Flog.d("DownloadActivity---getstartIntent()");
		Intent intent = getIntent();
		Flog.d("DownloadActivity---getstartIntent()---" + intent.toString());
		if (intent.getStringExtra("path") != null) {
			Flog.d("DownloadActivity---getstartIntent()---if");
			String path = intent.getStringExtra("path");
			music = intent.getStringExtra("music");
			Flog.d("DownloadActivity---getstartIntent()---" + path);
			mu.addInfo(path, max, music);
			flag = true;
		}
		Flog.d("DownloadActivity---getstartIntent()---end");

	}

	private void findView() {
		Flog.d("DownloadActivity---findView()");

		progressList = (ListView) findViewById(R.id.list);
		skin = (LinearLayout) findViewById(R.id.activity_main_skin);
		btnReturn = (ImageButton) findViewById(R.id.activity_scan_ib_return);
		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				DownloadActivity.this.finish();
			}
		});
		Flog.d("DownloadActivity---findView()---end");

	}

	public void checkSdcard() {
		Flog.d("DownloadActivity---checkSdcard()");
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			addListerner();

		} else {
			Toast.makeText(getApplicationContext(), "install sdcard", 1000)
					.show();

		}
		Flog.d("DownloadActivity---checkSdcard()---end");
	}

	private void initList() {
		Flog.d("DownloadActivity---initList()");
		Flog.d("DownloadActivity---initList()----1111111111111111111111111111111111111");
		Flog.d("DownloadActivity---initList()---" + list);
		Flog.d("DownloadActivity---initList()---" + list.toString());
		myAdapet = new MyAdapter(DownloadActivity.this, list, flag);
		Flog.d("DownloadActivity---initList()---22222222222222222222222222222222222");
		progressList.setAdapter(myAdapet);
		Flog.d("DownloadActivity---initList()---3333333333333333333333333333333");

		progressList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View v,
					final int position, long arg3) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(
						DownloadActivity.this);

				builder.setTitle("选择操作：");
				builder.setItems(R.array.load_action,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								switch (which) {
								case 0:
									Flog.d("downloadactivity---onitemlongonclick()---alertdialog---" + 1);
									exit();

									break;

								case 1:
									Flog.d("downloadactivity---onitemlongonclick()---alertdialog---" + 2);
									ur = new UpdateRunnable(position,
											myHandler, path, music, saveDir);// /////////////////////
																				// 第一个的参数传值必须是线程的ID
									new Thread(ur).start();
									break;

								default:
									break;
								}

							}
						});

				builder.show();

				return false;
			}
		});

	}

	private void addListerner() {
		Flog.d("DownloadActivity---addListener()");

		String adscard = "/mnt/sdcard/flyaudio";
		File file = new File(adscard);
		saveDir = file;
		if (list != null) {
			Flog.d("DownloadActivity---addListener()---if");
			for (int i = 0; i < list.size(); i++) {
				path = list.get(i).get("title").toString();
				path = path + ".html";
				// System.out.println("downloadactivity-----" + path);
				ur = new UpdateRunnable(i, myHandler, path, music, saveDir);// /////////////////////
				new Thread(ur).start();
			}
		}

	}

	private void exit() {
		Flog.d("DownLoadActivity---exit()");
		if (ur != null) {
			ur.exit();

		}

	}

	// 模拟处理线程
	private final class UpdateRunnable implements Runnable {
		int id;
		String path;
		File saveDir;
		MyHandler handler;
		FileDownloader downLoader;
		private String muisc;

		public UpdateRunnable(int id, MyHandler handler, String path,
				String music, File saveDir) {

			this.id = id;
			this.handler = handler;
			this.path = path;
			this.saveDir = saveDir;
			this.muisc = music;
		}

		public void exit() {
			if (downLoader != null)
				downLoader.exit();
		}

		@Override
		public void run() {
			Flog.d("DownloadActivity---UpdateRunnable---run()");
			try {
				downLoader = new FileDownloader(getApplicationContext(), path,
						music, saveDir, 1);
				Flog.d("DownloadActivity---UpdateRunnable---run()---download");
				downLoader.download(new DownloadProgressListener() {
					public void onDownloadSize(int size) {
						Message msg = handler.obtainMessage();
						// Message msg = new Message();
						fileSize = downLoader.getFileSize();
						msg.what = 1;
						msg.arg1 = id;
						msg.getData().putInt("size", size);
						msg.obj = path;
						msg.sendToTarget();
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				handler.sendMessage(handler.obtainMessage(-1));
			}

		}
	}

	// 消息Handler用来更新UI
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Flog.d("DownloadActivity---myHandler");
			switch (msg.what) {
			case 1:
				int id = msg.arg1;
				int current = msg.getData().getInt("size");
				updateProgress(id, current, fileSize);
				if (current >= fileSize) {
					Toast.makeText(DownloadActivity.this, music + "下载完成！！！",
							4000).show();

					String ms = msg.obj.toString();

					String path = ms.substring(0, ms.lastIndexOf("."));

					mu.deleteAppInfo(path);// app_info
					mu.deleteApp(msg.obj.toString());// fileDownLog

				}

				break;

			}
			super.handleMessage(msg);
		}

		private void updateProgress(int id, int currentPos, int sizeMax) {
			Flog.d("DownloadActivity---myHandler---updateProgress()");
			HashMap<String, Object> dataTemp = list.get(id);
			dataTemp.put("current", currentPos);
			dataTemp.put("max", sizeMax);
			myAdapet.chargeProgress(id, dataTemp);
		}
	}

	// List的显示
	class MyAdapter extends BaseAdapter {

		List<HashMap<String, Object>> list;
		LayoutInflater infl = null;
		ListView listView = null;
		private Integer progress;
		private LayoutInflater listContainer;
		private boolean flags;

		public final class ListView {
			public ProgressBar progressBar;
			public TextView musicName;
			public TextView total;

			public TextView percent;

		}

		public MyAdapter(Context context, List<HashMap<String, Object>> list,
				boolean flag) {
			Flog.d("DownloadActivity---myadapter");
			this.list = list;
			listContainer = LayoutInflater.from(context);
			this.flags = flag;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub

			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub

			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			Flog.d("DownloadActivity---MyAdapter---getView()");
			// TODO Auto-generated method stub

			if (convertView == null) {
				listView = new ListView();
				convertView = listContainer.inflate(R.layout.list, null);
				listView.total = (TextView) convertView
						.findViewById(R.id.total);

				listView.musicName = (TextView) convertView
						.findViewById(R.id.music);

				listView.percent = (TextView) convertView
						.findViewById(R.id.percent);
				listView.progressBar = (ProgressBar) convertView
						.findViewById(R.id.progressBar1);
				convertView.setTag(listView);
			} else {
				listView = (ListView) convertView.getTag();
			}
			HashMap<String, Object> detail = list.get(position);
			String t = (String) detail.get("title");
			Integer a = (Integer) detail.get("max");

			progress = (Integer) detail.get("current");
			listView.musicName.setText((CharSequence) list.get(position).get(
					"musicName"));

			listView.progressBar.setMax(a);
			listView.progressBar.setProgress(progress);
			if (a != 0) {
				listView.total.setText(BtoM(a));
				Flog.d("DownloadActivity---MyAdapter---percent---" + progress);
				listView.percent.setText((progress * 100 / a) + "%");
				Flog.d("DownloadActivity---MyAdapter---percent---%---"
						+ progress / a);

			}

			return convertView;
		}

		public String BtoM(Integer i) {
			Flog.d("DownloadActivity---myadapter---BtoM()");
			float f;
			String a;
			float f1;
			if (i / 1024 > 1024) {
				f = i / (1024 * 1024);

				BigDecimal b = new BigDecimal(f);
				f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
				a = String.valueOf(f1) + " m";

			} else {
				f = i / 1024;

				BigDecimal b = new BigDecimal(f);
				f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
				a = String.valueOf(f1) + " kb";
			}

			return a;
		}

		// 改变进度，postion就是要改的那个进度
		public void chargeProgress(int postion, HashMap<String, Object> detail) {
			Flog.d("DownloadActivity---myadapter---chargeProgress()");
			this.list.set(postion, detail);
			notifyDataSetChanged();
		}

	}

}