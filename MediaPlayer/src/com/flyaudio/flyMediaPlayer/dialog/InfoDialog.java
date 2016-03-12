package com.flyaudio.flyMediaPlayer.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.flyaudio.flyMediaPlayer.R;
import com.flyaudio.flyMediaPlayer.objectInfo.MusicInfo;


public class InfoDialog extends TVAnimDialog {

	private TextView name;
	private TextView artist;
	private TextView album;
	private TextView genre;
	private TextView time;
	private TextView format;
	private TextView kbps;
	private TextView size;
	private TextView years;
	private TextView hz;
	private TextView path;
	private Button button;

	public InfoDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public InfoDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	protected InfoDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_info);

		name = (TextView) findViewById(R.id.dialog_info_name);
		artist = (TextView) findViewById(R.id.dialog_info_artist);
		album = (TextView) findViewById(R.id.dialog_info_album);
		genre = (TextView) findViewById(R.id.dialog_info_genre);
		time = (TextView) findViewById(R.id.dialog_info_time);
		format = (TextView) findViewById(R.id.dialog_info_format);
		kbps = (TextView) findViewById(R.id.dialog_info_kbps);
		size = (TextView) findViewById(R.id.dialog_info_size);
		years = (TextView) findViewById(R.id.dialog_info_years);
		hz = (TextView) findViewById(R.id.dialog_info_hz);
		path = (TextView) findViewById(R.id.dialog_info_path);
		button = (Button) findViewById(R.id.dialog_info_btn_ok);

		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});

	}

	public void setInfo(MusicInfo info) {
		name.setText("歌曲: " + info.getName());
		artist.setText("歌手: " + info.getArtist());
		album.setText("专辑: " +info.getAlbum());
		genre.setText(info.getGenre());
		time.setText("时长: " + info.getTime());
		format.setText("格式: " +info.getFormat());
		kbps.setText("比特率: " +info.getKbps());
		size.setText("大小: " + info.getSize());
		years.setText("年代: " +info.getYears());
		hz.setText("采样率: " +info.getHz());
		path.setText("路径: " + info.getPath());
		genre.setText("风格: " +info.getGenre());
		
	}

}
