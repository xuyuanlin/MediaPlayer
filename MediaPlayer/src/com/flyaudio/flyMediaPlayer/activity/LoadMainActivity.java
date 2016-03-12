package com.flyaudio.flyMediaPlayer.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.flyaudio.flyMediaPlayer.objectInfo.LoadMusicInfo;
import com.flyaudio.flyMediaPlayer.until.Flog;
import com.flyaudio.flyMediaPlayer.until.AutoMusicClient;
import com.flyaudio.flyMediaPlayer.R;
import com.flyaudio.flyMediaPlayer.activity.MainActivity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyaudio.flyMediaPlayer.until.AllListActivity;

public class LoadMainActivity extends Activity implements OnItemClickListener {
	private List<LoadMusicInfo> musicInfos = new ArrayList<LoadMusicInfo>();
	private SharedPreferences preferences;
	private int skinId;// 背影ID
	private RelativeLayout skin;
	private ImageButton btnReturn;

	private AutoCompleteTextView autoCompleteTextView;
	private TextView musicName;
	private LoadMusicInfo result;
	private TextView play;
	private TextView load;
	public static final String PREFERENCES_SKIN = "skin";// 存储背影图

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.down_main);
		boolean flag = isNetworkConnected(getApplicationContext());
		if (flag == false) {
			Toast.makeText(getApplicationContext(), "检查网络链接！！！", 3000).show();

		}
		AllListActivity.getInstance().addActivity(this);

		skin = (RelativeLayout) findViewById(R.id.activity_main_skin);

		btnReturn = (ImageButton) findViewById(R.id.activity_scan_ib_return);
		musicName = (TextView) findViewById(R.id.musicName);
		play = (TextView) findViewById(R.id.bofang);
		load = (TextView) findViewById(R.id.load);
		play.setVisibility(4);
		load.setVisibility(4);

		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LoadMainActivity.this.finish();

			}
		});

		autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoComplete_music);
		autoCompleteTextView.setThreshold(2);

		MusicAdapter adapter = new MusicAdapter(this, null);
		autoCompleteTextView.setAdapter(adapter);
		autoCompleteTextView.setOnItemClickListener(this);

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Flog.d("loadmainactivity-----------------------------------------"
				+ position);
		result = musicInfos.get(position);

		autoCompleteTextView.setText(result.getSongname());
		int time = result.getPubtime();
		musicName.setText("歌曲   " + result.getSongname() + "\n" + "\n"
				+ "歌手   " + result.getSingername() + "\n" + "\n" + "专辑   "
				+ result.getAblumname() + "\n" + "\n" + "时间   "
				+ getDateToString(time));

		musicName.clearComposingText();

		play.setVisibility(0);
		load.setVisibility(0);
		play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

			}
		});
		load.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

			}
		});

	}

	public boolean isNetworkConnected(Context context) {

		ConnectivityManager mConnectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}

		return false;
	}

	//
	public static String getDateToString(int time) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		return format.format(new Date(time * 1000L));

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onResume();
		Flog.d("LoadMainActivity--Onstart()");
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		int id = preferences.getInt(MainActivity.PREFERENCES_SKIN,
				R.drawable.skin_bg1);
		Flog.d("LoadMainActivity--Onstart()---1");
		if (skinId != id) {// 判断是否换图
			Flog.d("LoadMainActivity--Onstart()---2");
			skinId = id;
			skin.setBackgroundResource(skinId);
		}
		Flog.d("LoadMainActivity--Onstart()---3");
	}

	public class MusicAdapter extends ArrayAdapter<LoadMusicInfo> implements
			Filterable {
		protected static final String TAG = "CityAdapter";
		private Context context;

		public MusicAdapter(Context context, List<LoadMusicInfo> musicInfo) {
			super(context, R.layout.music_demo, musicInfo);
			// TODO Auto-generated constructor stub
			Flog.d("LOadMainActivity---MusicAdapter");
			this.context = context;
			// this.musicInfos = musicInfo;

		}

		@Override
		public int getCount() {
			Flog.d("LOadMainActivity---MusicAdapter---getcount");

			if (musicInfos != null) {
				Flog.d("LOadMainActivity---MusicAdapter---getcount"
						+ musicInfos.size());
				return musicInfos.size();

			}

			return 0;

		}

		@Override
		public LoadMusicInfo getItem(int position) {
			Flog.d("LOadMainActivity---MusicAdapter---getitem()");

			if (musicInfos != null) {
				return musicInfos.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			Flog.d("LOadMainActivity---MusicAdapter---getItemid()");

			if (musicInfos != null) {
				return musicInfos.get(position).hashCode();

			}
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Flog.d("LOadMainActivity---MusicAdapter---getview()");
			View view = convertView;
			if (view == null) {
				LayoutInflater layoutInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = layoutInflater.inflate(R.layout.music_demo, parent,
						false);

			}
			if (musicInfos != null) {
				TextView tvTextView = (TextView) view
						.findViewById(R.id.musicitem);
				tvTextView.setText(musicInfos.get(position).getSongname());
			}

			return view;

		}

		@Override
		public Filter getFilter() {
			Flog.d("LOadMainActivity---MusicAdapter---getFilter()");

			Filter musicFilter = new Filter() {

				// private List<LoadMusicInfo> musicInfos;

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence contain,
						FilterResults results) {

					Flog.d("LOadMainActivity---MusicAdapter---publicresults()");
					musicInfos = (List<LoadMusicInfo>) results.values;
					notifyDataSetChanged();
					Log.e("cityActivity1", "publishResults");

				}

				@Override
				protected FilterResults performFiltering(CharSequence contait) {
					Flog.d("LOadMainActivity---MusicAdapter---performFiltering()");
					FilterResults results = new FilterResults();
					if (contait == null || contait.length() < 2) {
						return results;

					}
					Log.i("cityActivity2", "FilterResults");
					AutoMusicClient mGet_CityClient = new AutoMusicClient(
							getApplicationContext());
					musicInfos = mGet_CityClient.getMusicList(contait
							.toString());
					Log.e(TAG, "-------getFilter-----" + musicInfos.toString());

					results.values = musicInfos;
					results.count = musicInfos.size();

					return results;

				}
			};
			return musicFilter;
		}

	}

}