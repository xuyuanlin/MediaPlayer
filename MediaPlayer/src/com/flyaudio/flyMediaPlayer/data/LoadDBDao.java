package com.flyaudio.flyMediaPlayer.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.flyaudio.flyMediaPlayer.data.LoadDBOpenHelper;
import com.flyaudio.flyMediaPlayer.objectInfo.AppInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LoadDBDao {
	private LoadDBOpenHelper openHelper;

	public LoadDBDao(Context context) {
		openHelper = new LoadDBOpenHelper(context);
	}

	/**
	 * 获取每条线程已经下载的文件长度
	 * 
	 * @param path
	 * @return
	 */
	public Map<Integer, Integer> getData(String path) {
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = db
				.rawQuery(
						"select threadid, downlength from filedownlog where downpath=?",
						new String[] { path });
		Map<Integer, Integer> data = new HashMap<Integer, Integer>();
		while (cursor.moveToNext()) {
			data.put(cursor.getInt(0), cursor.getInt(1));
		}
		cursor.close();
		db.close();
		return data;
	}

	/**
	 * 保存每条线程已经下载的文件长度
	 * 
	 * @param path
	 * @param map
	 */
	public void save(String path, Map<Integer, Integer> map) {// int threadid,
																// int position
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
				db.execSQL(
						"insert into filedownlog(downpath, threadid, downlength) values(?,?,?)",
						new Object[] { path, entry.getKey(), entry.getValue() });
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		db.close();
	}

	/**
	 * 实时更新每条线程已经下载的文件长度
	 * 
	 * @param path
	 * @param map
	 */
	public void update(String path, int threadId, int pos) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL(
				"update filedownlog set downlength=? where downpath=? and threadid=?",
				new Object[] { pos, path, threadId });
		db.close();
	}

	/**
	 * 当文件下载完成后，删除对应的下载记录
	 * 
	 * @param path
	 */

	/**
	 * 保存下载的具体信息
	 * 
	 * @param infos
	 */
	public void addInfos(String path, int appLength, String musicName) {
		SQLiteDatabase database = openHelper.getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put("path", path);
		cv.put("length", 0);
		cv.put("appLength", appLength);
		cv.put("downMusicName", musicName);

		database.insert("app_info", null, cv);
		database.close();

	}

	public List<AppInfo> getList() {
		SQLiteDatabase database = openHelper.getReadableDatabase();
		List<AppInfo> list = new ArrayList<AppInfo>();
		String sql = "select * from app_info";
		Cursor cursor = database.rawQuery(sql, null);

		// cursor.moveToFirst();
		while (cursor.moveToNext()) {
			AppInfo info = new AppInfo(cursor.getString(1), cursor.getInt(2),
					cursor.getString(4));// //////////////
			list.add(info);
		}
		cursor.close();
		database.close();
		return list;
	}

	/**
	 * 查看数据库中是否有数据
	 * 
	 * @param urlstr
	 * @return
	 */
	public boolean isHasAppInfors(String path) {
		SQLiteDatabase database = openHelper.getReadableDatabase();
		String sql = "select count(*)  from app_info where path=?";
		Cursor cursor = database.rawQuery(sql, new String[] { path });
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		database.close();
		return count == 0;
	}

	/**
	 * 关闭数据库
	 */
	public void closeDb() {
		openHelper.close();
	}

	/**
	 * 保存下载的具体信息
	 * 
	 * @param infos
	 */
	public void addShowInfos(String path, String musicName) {
		SQLiteDatabase database = openHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("path", path);
		cv.put("musicName", musicName);
		database.insert("show_info", null, cv);
		database.close();
		// database.execSQL(sql, bindArgs);
	}

	/**
	 * 查看下载的具体信息
	 * 
	 * @param infos
	 */
	public List<AppInfo> getShowList() {
		SQLiteDatabase database = openHelper.getReadableDatabase();
		List<AppInfo> list = new ArrayList<AppInfo>();
		String sql = "select * from show_info";
		Cursor cursor = database.rawQuery(sql, null);
		// cursor.moveToFirst();
		while (cursor.moveToNext()) {
			AppInfo info = new AppInfo(cursor.getString(1), cursor.getInt(2),
					cursor.getString(4));
			list.add(info);
		}
		cursor.close();
		database.close();
		return list;
	}

	/**
	 * 下载完成后删除数据库中的数据
	 * 
	 * @param url
	 */
	public void deleteApp(String path) {
		SQLiteDatabase database = openHelper.getReadableDatabase();
		System.out.println("appinfo-----------" + path);
		database.delete("app_info", "path=?", new String[] { path });
		database.close();
		System.out
				.println("delete app_info-----------------------------------------");
	}

	public void deleteInstall(String path) {
		SQLiteDatabase database = openHelper.getReadableDatabase();
		database.delete("show_info", "path=?", new String[] { path });
		database.close();
		System.out
				.println("delete show_info-----------------------------------------");
	}

	public void delete(String path) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("delete from filedownlog where downpath=?",
				new Object[] { path });

		db.close();
		System.out
				.println("delete filedownlog---------------------------------------------");
	}

}
