package com.flyaudio.flyMediaPlayer.until;

import java.util.LinkedList;
import java.util.List;
import com.flyaudio.flyMediaPlayer.until.Flog;
import android.app.Activity;
import android.app.Application;

public class AllListActivity extends Application {
	private List<Activity> activityList = new LinkedList();
	private static AllListActivity instance;

	private AllListActivity() {
	}

	// 单例模式中获取唯一的Mapplication实例
	public static AllListActivity getInstance() {
		if (null == instance) {
			instance = new AllListActivity();
		}
		return instance;

	}

	// 添加Activity到容器中
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	// 遍历所有Activity并finish

	public void exit() {
		Flog.d("alllistactivity---exit()-------------------------exit");

		for (Activity activity : activityList) {
			if (activity != null) {
				Flog.d("alllistactivity---exit()---" + activity);
				activity.finish();
			}
		}

//		System.exit(0); // 关闭JVM

	}
}