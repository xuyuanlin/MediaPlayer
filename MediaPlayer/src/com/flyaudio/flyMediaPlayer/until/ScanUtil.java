package com.flyaudio.flyMediaPlayer.until;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import com.flyaudio.flyMediaPlayer.data.DBDao;
import com.flyaudio.flyMediaPlayer.objectInfo.FolderInfo;
import com.flyaudio.flyMediaPlayer.objectInfo.MusicInfo;
import com.flyaudio.flyMediaPlayer.objectInfo.ScanInfo;
import com.flyaudio.flyMediaPlayer.perferences.FolderList;
import com.flyaudio.flyMediaPlayer.perferences.LyricList;
import com.flyaudio.flyMediaPlayer.perferences.MusicList;

public class ScanUtil {

	private Context context;
	private DBDao db;
	private List<String> filelist = new ArrayList<String>();
	String[] str = { "mp3", "ape", "atrial", "dts", "ac3", "mp2", "flac", "ra",
			"vorbis", "aac", "hevc", "divx", "flv1", "vc1", "vpx", "h264",
			"rv", "wav", "mpeg4", "h263", "mpeg2v", "m4a", "flac", "flv",
			"mpg", "vob", "wmv" };
	String[] audioString = { "aac", "mp3", "vorbis", "wma", "ra", "flac",
			"mp2", "ac3", "ape", "dts", "atrial", "m4a" };
	String[] videosStrings = { "mpeg2v", "h263", "h264", "mpeg4", "wmv", "rv",
			"vpx", "vc", "flv1", "divx", "hevc", "vtrial" };
	private String path;

	public ScanUtil(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	/**
	 * 通过系统媒体资源，进行文件遍历扫描
	 * @return
	 */
	public List<ScanInfo> searchAllDirectory() {
		Flog.d("ScanUtil--searchAllDirectory");
		List<ScanInfo> list = new ArrayList<ScanInfo>();
		StringBuffer sb = new StringBuffer();
		String[] projection = { MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DATA };
		//获取原生系统媒体的cursor
		Cursor cr = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, MediaStore.Audio.Media.DISPLAY_NAME);

		String displayName = null;
		String data = null;
		if (cr == null) {
			return list;
		}
		if (cr.moveToFirst()) {
			while (cr.moveToNext()) {
				displayName = cr.getString(cr
						.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
				Flog.d("ScanUtil--searchAllDirectory--displayName--"
						+ displayName);
				data = cr.getString(cr
						.getColumnIndex(MediaStore.Audio.Media.DATA));
				Flog.d("ScanUtil--searchAllDirectory--data1---" + data);
				if (displayName != null) {
					data = data.replace(displayName, "");
				}
				Flog.d("ScanUtil--searchAllDirectory--data2---" + data);
				if (!sb.toString().contains(data)) {
					list.add(new ScanInfo(data, true));
					// list.add(new ScanInfo("/Android/data/class", true));
					sb.append(data);
				}
				Flog.d("ScanUtil--searchAllDirectory--" + sb.toString());
			}
		}
		cr.close();
		Flog.d("ScanUtil--searchAllDirectory--end");
		return list;
	}

	public void scanMusicFromSD(List<String> folderList, Handler handler) {
		Flog.d("ScanUtil--scanMusicFromSD---start");
		int count = 0;// 统计新增的数
		db = new DBDao(context);
		db.deleteLyric();// 不做歌词是否存在的判断，全部删除后重新扫描
		db.deleteAll();
		final int size = folderList.size();
		Flog.d("ScanUtil--scanMusicFromSD----size--" + size);
		MusicList.list.clear();
		for (int i = 0; i < size; i++) {
			final String folder = folderList.get(i);
			Flog.d("ScanUtil--scanMusicFromSD---folder--" + folder);
			File file[] = new File(folder).listFiles();
			if (file == null) {
				continue;
			}
			Flog.d("ScanUtil--scanMusicFromSD---file[]--" + file.length);
			FolderInfo folderInfo = new FolderInfo();
			List<MusicInfo> listInfo = new ArrayList<MusicInfo>();
			for (File temp : file) {
				// 是文件才保存，里面还有文件夹再判断
				String fileName;

				if (temp.isFile()) {
					fileName = temp.getName();
					Flog.d("--ScanUtil---fileName---1---" + fileName);
					path = temp.getPath();
					Flog.d("--ScanUtil---path----" + path);
					String end = fileName.substring(
							fileName.lastIndexOf(".") + 1, fileName.length());
					String music = fileName.substring(
							fileName.lastIndexOf("/") + 1, fileName.length());

					for (int j = 0; j < audioString.length; j++) {
						if (end.equalsIgnoreCase(audioString[j])) {
							MusicInfo musicInfo = scanMusicTag(fileName, path);
							Flog.d("--ScanUtil---fileName---for---" + fileName);
							Flog.d("--ScanUtil---fileName---for---" + path);

							Flog.d("--ScanUtil---fileName---for---" + music);

							// 先写定一个，解析不了的就报未知
							if (musicInfo.getKbps() == null) {
								musicInfo.setSize("未知");
								musicInfo.setTime("未知");
								musicInfo.setFormat("未知");
								musicInfo.setChannels("声道: 未知");
								musicInfo.setKbps("未知");
								musicInfo.setArtist("未知艺术家");
								musicInfo.setAlbum("未知");
								musicInfo.setYears("未知");
								musicInfo.setGenre("未知");
								musicInfo.setHz("未知");
							}

							if (musicInfo.getName() == null
									|| musicInfo.getName().equals("")) {
								musicInfo.setName(fileName.substring(0,
										fileName.lastIndexOf(".") - 1));
							}
							if (musicInfo.getArtist() == null
									|| musicInfo.getArtist().equals("")
									|| musicInfo.getArtist().equals("未知艺术家")) {
								if (fileName.contains("-")) {
									musicInfo.setArtist(fileName.substring(0,
											fileName.lastIndexOf("-") - 1));
								}
							}

							Flog.d("---------ScanUtil----------"
									+ musicInfo.toString());
							db.add(fileName, musicInfo.getName(), path, folder,
									false, musicInfo.getTime(),
									musicInfo.getSize(), musicInfo.getArtist(),
									musicInfo.getFormat(),
									musicInfo.getAlbum(), musicInfo.getYears(),
									musicInfo.getChannels(),
									musicInfo.getGenre(), musicInfo.getKbps(),
									musicInfo.getHz());

							musicInfo.setPath(path);
							MusicList.list.add(musicInfo);
							listInfo.add(musicInfo);
							count++;

							if (handler != null) {
								Message msg = handler.obtainMessage();
								msg.obj = fileName;

								msg.sendToTarget();
							}

						}
					}

				}
			}
			String subPath = folder.substring(0, folder.lastIndexOf("/"));
			Flog.d("ScanUtil--scanMusicFromSD---lrc---subpath---" + subPath);
			String subPath1 = subPath.substring(0, subPath.lastIndexOf("/"));
			Flog.d("ScanUtil--scanMusicFromSD---lrc---subpath1---" + subPath1);
			String lrcsubPath = subPath1 + "/lyric";
			Flog.d("ScanUtil--scanMusicFromSD---lrc---lrcsubPath---"
					+ lrcsubPath);
			List<String> listLrc = findFileList(lrcsubPath);
			// 歌词的处理 相同文件的出来方法///////////////////////////////////////
			// if (end.equalsIgnoreCase("lrc")) {
			if (listLrc == null) {
				continue;
			} else {

				for (int j = 0; j < listLrc.size(); j++) {
					Flog.d("ScanUtil--scanMusicFromSD---lrc---for");
					String lrcPath = listLrc.get(j);
					Flog.d("ScanUtil--scanMusicFromSD---lrc---for---lrcPath---"
							+ lrcPath);
					String subLrcPath = lrcPath.substring(
							lrcPath.lastIndexOf("/") + 1,
							lrcPath.lastIndexOf(".")).replaceAll(" ", "");

					Flog.d("ScanUtil--scanMusicFromSD---lrc---for---subLrcPath---"
							+ subLrcPath);
					if (db.isLryicQuery(lrcPath)) {
						// return;

					} else {
						db.addLyric(subLrcPath, lrcPath);
						LyricList.map.put(subLrcPath, lrcPath);

					}
				}
				Flog.d("ScanUtil--scanMusicFromSD---lrc---for---LyricList.map.size()---"
						+ LyricList.map.size());
				Flog.d("ScanUtil--scanMusicFromSD---lrc---for---LyricList---"
						+ LyricList.map.toString());
			}

			if (listInfo.size() > 0) {
				boolean exists = false;
				for (int j = 0; j < FolderList.list.size(); j++) {
					// 做对比，同名合并，新增添加
					if (folder.equals(FolderList.list.get(j).getMusicFolder())) {

						FolderList.list.get(j).getMusicList().addAll(listInfo);
						exists = true;
						break;
					}
				}
				if (!exists) {// 不存在同名添加
					// 文件夹的路径
					folderInfo.setMusicFolder(folder);
					// 文件夹歌曲的列表信息
					folderInfo.setMusicList(listInfo);
					// 加入到文件夹列表
					FolderList.list.add(folderInfo);
				}
			}
		}
		if (handler != null) {
			Message msg = handler.obtainMessage();
			Flog.d("----ScanUtil--scanMusicFromSD--count--" + count);
			msg.obj = "扫描完成，总共" + count + "首";

			msg.sendToTarget();
		}
		db.close();

		Flog.d("----ScanUtil--scanMusicFromSD--end");
	}

	// 遍历文件
	public List<String> findFileList(String strPath) {
		Flog.d("ScanUtil---findFileList()--satrt");
		String strFileName = null;
		String end = null;
		File file = new File(strPath);
		if (!file.exists()) {
			return null;
		}
		Flog.d("scanutil---findfilelist()--file--" + file.toString());
		File[] files = file.listFiles();
		if (files == null) {
			return null;
		}

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				findFileList(files[i].getAbsolutePath());
			} else {
				strFileName = files[i].getAbsolutePath();
				end = strFileName.substring(strFileName.lastIndexOf(".") + 1,
						strFileName.length());
				Flog.d("scanutil---findfilelist()---strFileName---"
						+ strFileName);
				if (end.equalsIgnoreCase("lrc") || end.equalsIgnoreCase("trc")) {
					filelist.add(files[i].getAbsolutePath());
				}

			}
		}
		Flog.d("scanutil---findfilelist()---list---" + filelist.toString());
		Flog.d("scanutil---findfilelist()---end");
		return filelist;

	}

	/**
	 * 从数据库获取媒体信息
	 */
	public void scanMusicFromDB() {
		Flog.d("ScanUtil--scanMusicFromDB");
		db = new DBDao(context);
		// db.deleteAll();
		db.queryAll(searchAllDirectory());
		db.close();
	}

	// 获取音乐的信息
	public MusicInfo scanMusicTag(String fileName, String path) {
		Flog.d("ScanUtil--scanMusicTag");

		Flog.d("ScanUtil--scanMusicTag---fileName---" + fileName + "---" + path);
		File file = new File(path);
		MusicInfo info = new MusicInfo();

		if (file.exists()) {
			try {
				AudioFile mp3File = AudioFileIO.read(file);
				Flog.d("-------scanMusicTag-------" + mp3File.toString());
				AudioHeader header = mp3File.getAudioHeader();
				Flog.d("-------scanMusicTag-------" + header.toString());
				info.setPath(path);

				info.setFile(fileName);
				// 时间
				info.setTime(FormatUtil.formatTime((int) (header
						.getTrackLength() * 1000)));
				info.setSize(FormatUtil.formatSize(file.length()));

				info.setFormat(header.getEncodingType());
				final String channels = header.getChannels();
				if (channels.equals("Joint Stereo")) {
					info.setChannels("声道：立体声");
				} else {
					info.setChannels("声道: " + header.getChannels());
				}
				info.setKbps(header.getBitRate() + "Kbps");
				info.setHz(header.getSampleRate() + "Hz");

				/* if (mp3File.) { */
				Tag tag = mp3File.getTag();
				try {
					// 设置歌曲名
					final String tempName = tag.getFirst(FieldKey.TITLE);
					Flog.d("--ScanUtil---tempName----" + tempName);
					Flog.d("---ScanUtil---fileName----" + fileName);
					if (tempName == null || tempName.equals("")) {
						info.setName(fileName);

					} else {
						// info.setName((tempName));
						info.setName(tempName);
					}
				} catch (KeyNotFoundException e) {
					// TODO Auto-generated catch block
					info.setName(fileName);
				}

				try {
					// 设置歌手
					final String tempArtist = tag.getFirst(FieldKey.ARTIST);
					Flog.d("-----tempArtist----" + tempArtist);
					if (tempArtist == null || tempArtist.equals("")) {
						info.setArtist("未知艺术家");
					} else {
						info.setArtist(tempArtist);
					}
				} catch (KeyNotFoundException e) {
					// TODO Auto-generated catch block
					info.setArtist("未知艺术家");
				}

				try {
					// 设置专辑
					final String tempAlbum = tag.getFirst(FieldKey.ALBUM);
					if (tempAlbum == null || tempAlbum.equals("")) {
						info.setAlbum("未知");
					} else {
						info.setAlbum((tempAlbum));
					}
				} catch (KeyNotFoundException e) {
					// TODO Auto-generated catch block
					info.setAlbum("未知");
				}

				try {
					// 设置年代
					final String tempYears = tag.getFirst(FieldKey.YEAR);
					if (tempYears == null || tempYears.equals("")) {
						info.setYears("未知");
					} else {
						info.setYears(tempYears);

					}
				} catch (KeyNotFoundException e) {
					// TODO Auto-generated catch block
					info.setYears("未知");
				}

				try {
					// 设置风格
					final String tempGener = tag.getFirst(FieldKey.GENRE);
					if (tempGener == null || tempGener.equals("")) {
						info.setGenre("未知");
					} else {
						info.setGenre(tempGener);
					}
				} catch (KeyNotFoundException e) {
					// TODO Auto-generated catch block
					info.setGenre("未知");
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Flog.d("ScanUtil--scanMusicTag" + info.toString());

		return info;
	}
}

/*
 * public void scanMusic(String file, Handler handler) {
 * Flog.d("ScanUtil--scanMusic"); int count = 0;// 统计新增的数 db = new
 * DBDao(context); db.deleteLyric();// 不做歌词是否存在的判断，全部删除后重新扫描 db.deleteAll();
 * 
 * MusicList.list.clear();
 * 
 * for (int i = 0; i < size; i++) { final String folder = files.get(i); File
 * file[] = new File(folder).listFiles(); if (file == null) { continue; }
 * 
 * File files[] = new File(file).listFiles();
 * Flog.d("ScanUtil--scanMusic--files.length----" + files.length); if (files ==
 * null) { return; } FolderInfo folderInfo = new FolderInfo(); List<MusicInfo>
 * listInfo = new ArrayList<MusicInfo>(); for (File temp : files) { //
 * 是文件才保存，里面还有文件夹再判断 if (temp.isFile()) { String fileName = temp.getName();
 * Flog.d("--ScanUtil---fileName---1---" + fileName); path = temp.getPath();
 * Flog.d("--ScanUtil---path----" + path); final String end =
 * fileName.substring( fileName.lastIndexOf(".") + 1, fileName.length());
 * 
 * String name = fileName.substring(0, fileName.lastIndexOf("."));
 * Flog.d("--ScanUtil---name------" + name); for (int j = 0; j <
 * audioString.length; j++) { if (end.equalsIgnoreCase(audioString[j])) { // if
 * (!db.queryExist(fileName, folder)) { MusicInfo musicInfo =
 * scanMusicTag(fileName, path);
 * 
 * musicInfo.setPath(path); musicInfo.setName(name); // 先写定一个，解析不了的就报未知 if
 * (musicInfo.getKbps() == null) { musicInfo.setSize("未知");
 * musicInfo.setTime("未知"); musicInfo.setFormat("未知");
 * musicInfo.setChannels("声道: 未知"); musicInfo.setKbps("未知");
 * musicInfo.setArtist("未知艺术家"); musicInfo.setAlbum("未知");
 * musicInfo.setYears("未知"); musicInfo.setGenre("未知"); musicInfo.setHz("未知"); }
 * 
 * Flog.d("---------ScanUtil----------" + musicInfo.toString());
 * db.add(fileName, musicInfo.getName(), musicInfo.getPath(), file, false,
 * musicInfo.getTime(), musicInfo.getSize(), musicInfo.getArtist(),
 * musicInfo.getFormat(), musicInfo.getAlbum(), musicInfo.getYears(),
 * musicInfo.getChannels(), musicInfo.getGenre(), musicInfo.getKbps(),
 * musicInfo.getHz());
 * 
 * MusicList.list.add(musicInfo); int position =
 * MusicList.list.indexOf(musicInfo); Flog.d("MusicList------" +
 * MusicList.list.size()); Flog.d("MusicList---position---" + position);
 * 
 * listInfo.add(musicInfo); count++; // db.updateMusic(musicInfo.getId()); // }
 * 
 * if (handler != null) { Message msg = handler.obtainMessage(); msg.obj =
 * fileName;
 * 
 * msg.sendToTarget(); }
 * 
 * } } // 歌词的处理 if (end.equalsIgnoreCase("lrc")) { db.addLyric(fileName, path);
 * LyricList.map.put(fileName, path); } } } if (listInfo.size() > 0) { boolean
 * exists = false; for (int j = 0; j < FolderList.list.size(); j++) { //
 * 做对比，同名合并，新增添加 if (file.equals(FolderList.list.get(j).getMusicFolder())) {
 * 
 * FolderList.list.get(j).getMusicList().addAll(listInfo); exists = true; break;
 * } } if (!exists) {// 不存在同名添加 // 文件夹的路径 folderInfo.setMusicFolder(file); //
 * 文件夹歌曲的列表信息 folderInfo.setMusicList(listInfo); // 加入到文件夹列表
 * FolderList.list.add(folderInfo); } }
 * 
 * if (handler != null) { Message msg = handler.obtainMessage(); msg.obj =
 * "扫描完成，总共" + count + "首";
 * 
 * msg.sendToTarget(); } db.close();
 * 
 * Flog.d("----ScanUtil--scanMusicFromSD--end"); }
 * 
 * }
 */