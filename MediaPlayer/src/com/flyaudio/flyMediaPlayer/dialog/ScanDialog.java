package com.flyaudio.flyMediaPlayer.dialog;

import com.flyaudio.flyMediaPlayer.R;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class ScanDialog extends TVAnimDialog {

	private Button button;

	public ScanDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ScanDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	protected ScanDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_scan);

		button = (Button) findViewById(R.id.dialog_scan_btn_ok);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});

	}

}
