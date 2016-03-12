package com.flyaudio.flyMediaPlayer.until;

import com.flyaudio.flyMediaPlayer.activity.PlayerActivity;
import com.flyaudio.flyMediaPlayer.serviceImpl.MediaBinder;
import com.flyaudio.flyMediaPlayer.view.MyLyricView;

import android.app.Service;
import android.content.ServiceConnection;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;

public class LyricGesture implements OnTouchListener,OnGestureListener{
	private PlayerActivity context;
	private GestureDetector mGestureDetector;
	private MediaBinder mBinder;
	private int way;
	private boolean starttoggle;
	private boolean updatetoggle;
	private MyLyricView lView1;
	public LyricGesture(PlayerActivity context,MediaBinder mediaBinder,MyLyricView lView1){
		this.context = context;
		this.mBinder=mediaBinder;
		this.lView1=lView1;
		mGestureDetector = new GestureDetector(context, this);
	}
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		Flog.d("LyricGesture---onTouch()");
		Flog.d("LyricGesture---onTouch()---mBinder---"+mBinder);
		if(event.getAction() ==1)
		{
			way = 0;
			Flog.d("LyricGesture---onTouch()---updatetoggle--"+updatetoggle);
			if(updatetoggle){
				if (mBinder != null) {
					Flog.d("LyricGesture---onTouch()---mBinder");
					mBinder.LrcStopTrackingTouch();
				}
			updatetoggle = false;
			}
		}
		return mGestureDetector.onTouchEvent(event);
	}
	@Override
	public boolean onDown(MotionEvent arg0) {
//		starttoggle = true;
		return true;
	}
	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		
					
		
		return false;
	}
	@Override
	public void onLongPress(MotionEvent arg0) {
	}
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		Flog.d("onScroll()");
		Flog.d("onScroll()----way---"+way);
		if(way<3){
			context.updatelab(-arg2, -arg3, true);
			way++;
		}else{
		starttoggle = context.updatelab(-arg2, -arg3, false);
		Flog.d("onScroll()----starttoggle---"+starttoggle);
		if(starttoggle){
		updatetoggle = true;
//		lView1.showprogress = true;
		if (mBinder != null) {
			Flog.d("LyricGesture---onScroll()---mBinder");
			mBinder.getslidestart();
		}
		starttoggle = false;
		}
		Flog.d("onScroll()----updatetoggle---"+updatetoggle);
		if(updatetoggle){
		context.updateprogress(-arg2,-arg3);
		}
		}
		return false;
	}
	@Override
	public void onShowPress(MotionEvent arg0) {
	}
	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}
/*	public int updateplayer(){
		lView1.showprogress = false;
//		if(Math.abs(lyricView.driftx)<Math.abs(lyricView.drifty)){
		lView1.index=lView1.index+lView1.temp;
		lView1.driftx = 0;
		lView1.drifty = 0;
		if(lView1.repair()){
//		 return Integer.parseInt(lView1.time[lView1.index-1]);
		 return lView1.mSentenceEntities.get(lView1.index-1).getTime();
		}

		return 0;
	}*/
	

}
