package com.flyaudio.flyMediaPlayer.data;

import java.util.ArrayList;
import java.util.List;
import com.flyaudio.flyMediaPlayer.objectInfo.FolderInfo;
import com.flyaudio.flyMediaPlayer.objectInfo.MusicInfo;
import com.flyaudio.flyMediaPlayer.objectInfo.ScanInfo;
import com.flyaudio.flyMediaPlayer.perferences.FavoriteList;
import com.flyaudio.flyMediaPlayer.perferences.FolderList;
import com.flyaudio.flyMediaPlayer.perferences.LyricList;
import com.flyaudio.flyMediaPlayer.perferences.MusicList;
import com.flyaudio.flyMediaPlayer.until.Flog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * By CWD 2013 Open Source Project
 * 
 * <br>
 * <b>对数据库的增删查改等</b></br>
 * 
 * @version 2013.05.19 v1.0 实现增、查、改、是否存在<br>
 *          2013.06.16 v1.1 实现新增单条音乐更为详情的记录，更新查询方法<br>
 *          2013.06.23 v1.2 实现新增单条音乐歌词信息的记录，加入歌词查询方法<br>
 *          2013.08.04 v1.3 实现单条音乐的数据库删除<br>
 *          2013.08.05 v1.4 新增单条音乐的专辑信息，前面竟然漏掉了这么重要的数据...<br>
 *          2013.08.05 v1.5 修正扫描文件夹歌曲列表的一个错误<br>
 *          2013.08.29 v1.6 修正真机上扫描报错的问题，原因是文件名中含有'，SQL语句查询出错</br>
 */
public class DBDao {

	private DBHelper helper;
	private SQLiteDatabase db;

	/**
	 * 创建和初始化数据库，使用完记得调用close方法关闭数据库
	 * 
	 * @param context
	 */
	public DBDao(Context context) {
		// TODO Auto-generated constructor stub
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();
	}

	/**
	 * 新增单条音乐数据信息
	 * 
	 * @param fileName
	 *            文件名(目的是用来作为唯一名称用以判断是否存在)
	 * @param musicName
	 *            音乐名称
	 * @param musicPath
	 *            音乐路径
	 * @param musicFolder
	 *            音乐隶属文件夹
	 * @param isFavorite
	 *            是否为最喜爱音乐
	 * @param musicTime
	 *            音乐时长
	 * @param musicSize
	 *            音乐文件大小
	 * @param musicArtist
	 *            音乐艺术家
	 * @param musicFormat
	 *            音乐格式(编码类型)
	 * @param musicAlbum
	 *            音乐专辑
	 * @param musicYears
	 *            音乐年代
	 * @param musicChannels
	 *            音乐声道
	 * @param musicGenre
	 *            音乐风格
	 * @param musicKbps
	 *            音乐比特率
	 * @param musicHz
	 *            音乐采样率
	 * @return 新增成功的条数，失败返回-1
	 */
	public long add(String fileName, String musicName, String musicPath,
			String musicFolder, boolean isFavorite, String musicTime,
			String musicSize, String musicArtist, String musicFormat,
			String musicAlbum, String musicYears, String musicChannels,
			String musicGenre, String musicKbps, String musicHz) {
		ContentValues values = new ContentValues();
		// values.put(DBData.MUSIC_ID, id);
		values.put(DBData.MUSIC_FILE, fileName);
		values.put(DBData.MUSIC_NAME, musicName);
		values.put(DBData.MUSIC_PATH, musicPath);
		values.put(DBData.MUSIC_FOLDER, musicFolder);
		values.put(DBData.MUSIC_FAVORITE, isFavorite ? 1 : 0);// 数据库定义字段数据为整型
		values.put(DBData.MUSIC_TIME, musicTime);
		values.put(DBData.MUSIC_SIZE, musicSize);
		values.put(DBData.MUSIC_ARTIST, musicArtist);
		values.put(DBData.MUSIC_FORMAT, musicFormat);
		values.put(DBData.MUSIC_ALBUM, musicAlbum);
		values.put(DBData.MUSIC_YEARS, musicYears);
		values.put(DBData.MUSIC_CHANNELS, musicChannels);
		values.put(DBData.MUSIC_GENRE, musicGenre);
		values.put(DBData.MUSIC_KBPS, musicKbps);
		values.put(DBData.MUSIC_HZ, musicHz);
		long result = db.insert(DBData.MUSIC_TABLENAME, DBData.MUSIC_FILE,
				values);
		return result;
	}

	/**
	 * 新增单条音乐歌词信息
	 * 
	 * @param fileName
	 *            文件名(目的是用来作为唯一名称用以判断是否存在)
	 * @param lrcPath
	 *            歌词路径
	 * @return 新增成功的条数，失败返回-1
	 */
	public long addLyric(String fileName, String lrcPath) {

		Flog.d("DBDao---addlryic()");
		ContentValues values = new ContentValues();
		values.put(DBData.LYRIC_FILE, fileName);
		values.put(DBData.LYRIC_PATH, lrcPath);
		long result = db.insert(DBData.LYRIC_TABLENAME, DBData.LYRIC_FILE,
				values);
		return result;
	}
	public String queryLrcPath(String musicFolder){
		String lrcPath=null;
		Cursor cursor = db.query(DBData.LYRIC_TABLENAME, null,
				DBData.LYRIC_FILE + "=?  " ,
				new String[] { musicFolder }, null, null, null);
		if(cursor!=null){
			if(cursor.getCount()==0){
				lrcPath=null;
			}
			while (cursor.moveToNext()) {
				lrcPath=cursor.getString(2);
			}
		}
		return lrcPath;
	}

	/**
	 * 更新音乐相关记录，只更新用户是否标记为最喜爱音乐
	 * 
	 * @param musicName
	 *            音乐名称
	 * @param isFavorite
	 *            是否为最喜爱音乐(true:1 else false:0)
	 * @return 影响的行数
	 */
	public int update(String musicName, boolean isFavorite) {
		ContentValues values = new ContentValues();
		values.put(DBData.MUSIC_FAVORITE, isFavorite ? 1 : 0);// 数据库定义字段数据为整型
		int result = db.update(DBData.MUSIC_TABLENAME, values,
				DBData.MUSIC_NAME + "=?", new String[] { musicName });

		return result;
	}

	// public int updateMusic(int id) {
	// ContentValues values = new ContentValues();
	// values.put(DBData.MUSIC_ID,id );// 数据库定义字段数据为整型
	// int result = db.update(DBData.MUSIC_TABLENAME, values,
	// null, null);
	//
	// return result;
	// }

	/**
	 * 查询对应条件的数据库信息是否存在
	 * 
	 * 建议此处不要写SQL语句，即rawQuery查询。因为某些文件名中就带有'，所以肯定报错！
	 * 
	 * @param musicName
	 *            音乐名称
	 * @param musicFolder
	 *            音乐隶属文件夹
	 * @return 是否存在
	 */
	public boolean queryExist(String fileName, String musicFolder) {
		boolean isExist = false;

		Cursor cursor = db.query(DBData.MUSIC_TABLENAME, null,
				DBData.MUSIC_FILE + "=? AND " + DBData.MUSIC_FOLDER + "=?",
				new String[] { fileName, musicFolder }, null, null, null);
		if (cursor.getCount() > 0) {
			isExist = true;
		}
		return isExist;
	}

	/**
	 * 查询数据库保存的各媒体库目录下所有音乐信息和歌词
	 * 
	 * @param scanList
	 *            音乐媒体库所有目录
	 */
	public void queryAll(List<ScanInfo> scanList) {
		Flog.d("DBDao-----queryAll()");
		MusicList.list.clear();
		FolderList.list.clear();
		FavoriteList.list.clear();
		LyricList.map.clear();

		final int listSize = scanList.size();
		Cursor cursor = null;
		// 查询各媒体库目录下所有音乐信息
		for (int i = 0; i < listSize; i++) {
			final String folder = scanList.get(i).getFolderPath();
			cursor = db.rawQuery("SELECT * FROM " + DBData.MUSIC_TABLENAME
					+ " WHERE " + DBData.MUSIC_FOLDER + "='" + folder + "'",
					null);
			List<MusicInfo> listInfo = new ArrayList<MusicInfo>();
			if (cursor != null && cursor.getCount() > 0) {
				FolderInfo folderInfo = new FolderInfo();
				while (cursor.moveToNext()) {
					MusicInfo musicInfo = new MusicInfo();
					final String file = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_FILE));
					final String name = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_NAME));
					final String path = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_PATH));
					final int favorite = cursor.getInt(cursor
							.getColumnIndex(DBData.MUSIC_FAVORITE));
					final String time = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_TIME));
					final String size = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_SIZE));
					final String artist = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_ARTIST));
					final String format = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_FORMAT));
					final String album = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_ALBUM));
					final String years = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_YEARS));
					final String channels = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_CHANNELS));
					final String genre = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_GENRE));
					final String kbps = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_KBPS));
					final String hz = cursor.getString(cursor
							.getColumnIndex(DBData.MUSIC_HZ));
					final int id = cursor.getInt(cursor
							.getColumnIndex(DBData.MUSIC_ID));
					Flog.d("DBDao-----queryAll()--name--" + name);
					Flog.d("DBDao-----queryAll()--ID--" + id);

					musicInfo.setFile(file);
					musicInfo.setName(name);
					musicInfo.setPath(path);
					musicInfo.setFavorite(favorite == 1 ? true : false);
					musicInfo.setTime(time);
					musicInfo.setSize(size);
					musicInfo.setArtist(artist);
					musicInfo.setFormat(format);
					musicInfo.setAlbum(album);
					musicInfo.setYears(years);
					musicInfo.setChannels(channels);
					musicInfo.setGenre(genre);
					musicInfo.setKbps(kbps);
					musicInfo.setHz(hz);
					musicInfo.setId(id);
					// 加入所有歌曲列表
					MusicList.list.add(musicInfo);
					int position = MusicList.list.indexOf(musicInfo);
					Flog.d("queryAll------position----" + position);
					// 加入文件夹临时列表
					listInfo.add(musicInfo);
					// 加入我的最爱列表
					if (favorite == 1) {
						Flog.d("favorite" + FavoriteList.list.size());
						FavoriteList.list.add(musicInfo);
					}
				}
				// 设置文件夹列表文件夹路径
				folderInfo.setMusicFolder(folder);
				// 设置文件夹列表歌曲信息
				folderInfo.setMusicList(listInfo);
				// 加入文件夹列表
				FolderList.list.add(folderInfo);
			}
		}
		// 查询歌词
		cursor = db.rawQuery("SELECT * FROM " + DBData.LYRIC_TABLENAME, null);
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				final String file = cursor.getString(cursor
						.getColumnIndex(DBData.LYRIC_FILE));
				final String path = cursor.getString(cursor
						.getColumnIndex(DBData.LYRIC_PATH));
				LyricList.map.put(file, path);
			}
		}
		if (cursor != null) {
			cursor.close();
		}
	}

	/**
	 * 删除歌曲信息
	 */
	public void deleteAll() {

		try {
			db.execSQL("delete from " + DBData.MUSIC_TABLENAME + ";");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 根据文件路径来判断是否音乐存在
	 * 
	 * @param filePath
	 *            文件路径
	 */
	public boolean isQuery(String filePath) {
		// List<String> list=new ArrayList<String>();
		// MusicInfo info=new MusicInfo();
		boolean isExist = false;

		// Cursor cursor = db.rawQuery("SELECT * FROM " + DBData.MUSIC_TABLENAME
		// + " where " + DBData.MUSIC_PATH + "=?",
		// new String[] { filePath });
		Cursor cursor = db.query(DBData.MUSIC_TABLENAME, null,
				DBData.MUSIC_PATH + "=?", new String[] { filePath }, null,
				null, null);

		if (cursor.getCount() > 0) {
			isExist = true;
		}
		return isExist;

	}

	public boolean isLryicQuery(String filePath) {
		// List<String> list=new ArrayList<String>();
		// MusicInfo info=new MusicInfo();
		boolean isExist = false;

		// Cursor cursor = db.rawQuery("SELECT * FROM " + DBData.MUSIC_TABLENAME
		// + " where " + DBData.MUSIC_PATH + "=?",
		// new String[] { filePath });
		Cursor cursor = db.query(DBData.LYRIC_TABLENAME, null,
				DBData.LYRIC_PATH + "=?", new String[] { filePath }, null,
				null, null);

		if (cursor.getCount() > 0) {
			isExist = true;
		}
		return isExist;

	}


	/**
	 * 根据文件路径来删除音乐信息
	 * 
	 * @param filePath
	 *            文件路径
	 * @return 成功删除的条数
	 */
	public int delete(String filePath) {

		int result = db.delete(DBData.MUSIC_TABLENAME, DBData.MUSIC_PATH + "='"
				+ filePath + "'", null);
		return result;
	}

	/**
	 * 删除歌词信息表
	 */
	public void deleteLyric() {
		// 可能不存在该表，需要拋异常
		try {

			db.execSQL("delete from " + DBData.LYRIC_TABLENAME + ";");
			db.execSQL("update sqlite_sequence set seq=0 where name='"
					+ DBData.LYRIC_TABLENAME + "';");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 使用完数据库必须关闭
	 */
	public void close() {
		db.close();
		db = null;
	}

}
