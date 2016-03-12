package com.flyaudio.flyMediaPlayer.until;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flyaudio.flyMediaPlayer.data.LoadDBDao;
import com.flyaudio.flyMediaPlayer.objectInfo.AppInfo;
import com.flyaudio.flyMediaPlayer.until.Flog;
public class DBUtil {
	private LoadDBDao dao;

	public DBUtil(Context context) {
		dao = new LoadDBDao(context);
	}

	/**
	 * 获取下载的数据
	 * 
	 * @return
	 */
	public List<HashMap<String, Object>> getAppList() {
		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		List<AppInfo> list = dao.getList();
		Flog.d("mainUtil---getApplist()---" + list.size());
		if (list.size() != 0) {
			for (int i = 0; i < list.size(); i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				String path = list.get(i).getPath().toString();
				String musicName = list.get(i).getMusicName().toString();
				int max = list.get(i).getMax();
				map.put("title", path);
				map.put("current", 0);
				map.put("max", max);
				map.put("musicName", musicName);
				data.add(map);

			}
		} else {
			return data;
		}
		return data;
	}

	public void deleteApp(String path) {
		dao.delete(path);
	}

	public void addInfo(String path, int length, String musicName) {
		dao.addInfos(path, length, musicName);
	}

	public boolean isHasAppInfor(String path) {
		return dao.isHasAppInfors(path);
	}

	public void deleteAppInfo(String path) {
		dao.deleteApp(path);
	}

	public void close() {
		dao.closeDb();
	}

	/*
	 * public List<Map<String, String>> getShowList() { List<Map<String,
	 * String>> data = new ArrayList<Map<String, String>>(); List<AppInfo> list
	 * = dao.getShowList(); for (int i = 0; i < list.size(); i++) { Map<String,
	 * String> map = new HashMap<String, String>(); String path =
	 * list.get(i).getPath().toString(); map.put("path", path); data.add(map); }
	 * return data; }
	 */

	// public DownloadInfo showInfos(String urlstr){
	// return dao.showInfos(urlstr);
	// }

	public void deleteInstall(String path) {
		dao.deleteInstall(path);
	}
}
