package com.flyaudio.flyMediaPlayer.activity;

import java.lang.ref.WeakReference;

import com.flyaudio.flyMediaPlayer.R;
import com.flyaudio.flyMediaPlayer.adapter.ScanAdapter;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaService;
import com.flyaudio.flyMediaPlayer.until.ScanUtil;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.flyaudio.flyMediaPlayer.until.AllListActivity;

public class ScanActivity extends Activity {

	private final String INFO_NORMAL = "扫描歌曲";
	private final String INFO_SCAN = "正在扫描";
	private final String INFO_WAIT = "等待扫描...";
	private final String INFO_FINISH = "扫描完成";

	private boolean scaning = true;// 默认允许扫描
	private boolean canReturn = true;// 默认允许退出

	private ImageButton btnReturn;// 页面结束按钮
	private Button btnScan;// 扫描按钮

	private TextView scanText;// 扫描时的文字更新
	private ListView scanList;// 媒体库目录列表

	private ScanHandler handler;
	private ScanUtil manager;
	private ScanAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		AllListActivity.getInstance().addActivity(this);
		btnReturn = (ImageButton) findViewById(R.id.activity_scan_ib_return);
		btnScan = (Button) findViewById(R.id.activity_scan_btn_scan);
		scanText = (TextView) findViewById(R.id.activity_scan_text);
		scanList = (ListView) findViewById(R.id.activity_scan_lv);
		btnScan.setText(INFO_NORMAL);
		scanText.setText(INFO_WAIT);

		manager = new ScanUtil(getApplicationContext());
		adapter = new ScanAdapter(getApplicationContext(),
				manager.searchAllDirectory());
		scanList.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (canReturn) {// 扫描中暂不可退出
					if (!scaning) {
						sendUpdateBroadcast();// 通知上一个页面更新
					}
					finish();
				}
			}
		});

		// 执行扫描，变为false时再次点击扫描按钮退出扫描
		btnScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (scaning) {
					scaning = false;
					canReturn = false;
					new ScanTask().execute();
					btnScan.setText(INFO_SCAN);
					btnScan.setEnabled(false);
				} else {
					sendUpdateBroadcast();// 通知上一个页面更新
					finish();
				}
			}
		});

		handler = new ScanHandler(this);

		Intent intent = new Intent(MediaService.BROADCAST_ACTION_SERVICE);
		intent.putExtra(MediaService.INTENT_ACTIVITY,
				MediaService.ACTIVITY_SCAN);
		sendBroadcast(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!canReturn) {// 扫描中暂不可退出，此处为!判断
				return true;
			} else {
				if (!scaning) {
					sendUpdateBroadcast();
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 因为Activity的launchMode="SingleTask"时不会执行onActivityResult方法，改为发送广播
	 */
	private void sendUpdateBroadcast() {
		Intent intent = new Intent(MainActivity.BROADCAST_ACTION_SCAN);
		sendBroadcast(intent);
	}

	/**
	 * 文字更新
	 * 
	 * @param text
	 *            内容
	 */

	private void updateText(String text) {
		scanText.setText(text);
	}

	/**
	 * 执行扫描任务的异步任务嵌套类
	 * 
	 * 实现扫描
	 * 
	 */
	private class ScanTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			manager.scanMusicFromSD(adapter.getPath(), handler);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			SharedPreferences preferences = getSharedPreferences(
					MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
			preferences.edit().putBoolean(MainActivity.PREFERENCES_SCAN, true)
					.commit();// 扫描完成！
			btnScan.setText(INFO_FINISH);
			btnScan.setEnabled(true);
			canReturn = true;
		}

	}

	/**
	 * 实时更新UI静态嵌套类
	 * 
	 * 之所以这样写，是因为Handler是个极易内存泄露的对象，我可是吃过亏的，保持软引用可以有效避免
	 * 
	 * 实现更新UI
	 */
	private static class ScanHandler extends Handler {

		private WeakReference<ScanActivity> mReference;

		public ScanHandler(ScanActivity activity) {
			// TODO Auto-generated constructor stub
			mReference = new WeakReference<ScanActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (mReference.get() != null) {
				ScanActivity theActivity = mReference.get();
				theActivity.updateText(msg.obj.toString());
			}
		}

	}

}
