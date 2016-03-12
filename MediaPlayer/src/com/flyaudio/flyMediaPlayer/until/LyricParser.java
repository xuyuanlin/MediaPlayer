package com.flyaudio.flyMediaPlayer.until;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.flyaudio.flyMediaPlayer.objectInfo.LyricItem;

/*eeeaandroid.jar    chardet.jar    cpdetector.jar
 */public class LyricParser {

	private String lyricPath = null;

	private List<LyricItem> lyricList = null;

	public static StringBuilder stringBuilder;
	
	private String lrcORtrc;

	public LyricParser(String lyricPath) {
		// TODO Auto-generated constructor stub
		this.lyricPath = lyricPath;
		this.lyricList = new ArrayList<LyricItem>();
	}

	public List<LyricItem> parser() throws Exception {
		Flog.d("LyricParser---parser()--start");
		String encode = "UTF-8";
		File file = new File(lyricPath);
		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
		detector.add(new ParsingDetector(false));
		detector.add(JChardetFacade.getInstance());
		detector.add(ASCIIDetector.getInstance());
		detector.add(UnicodeDetector.getInstance());
		Charset set = null;
		try {
			set = detector.detectCodepage(file.toURI().toURL());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (set != null) {
			encode = set.name();
		}
		lrcORtrc = lyricPath.substring(lyricPath.indexOf(".") + 1,
				lyricPath.length());
		InputStream inputStream = new FileInputStream(file);
		InputStreamReader inputReader = new InputStreamReader(inputStream,
				encode);
		BufferedReader bufferedReader = new BufferedReader(inputReader);

		String temp = null;
		// 行数
		int line = 0;
		// 每一行的时间点数
		int count = 0;
		// 每一行存在的时间的文字长度
		int timeLength = 0;

		// 暂存时间点的数据，因为有的一行有多个时间，需要先提取所有时间，最后提取歌词
		ArrayList<Integer> timeTemp = new ArrayList<Integer>();

		// 创建正则表达式[00:00.00]/[00:00:00]/[00:00]
		Pattern p = Pattern
				.compile("\\[\\s*[0-9]{1,2}\\s*:\\s*[0-5][0-9]\\s*[\\.:]?\\s*[0-9]?[0-9]?\\s*\\]");
		String msg = null;
		stringBuilder = new StringBuilder();
		// 都去每行
		while ((temp = bufferedReader.readLine()) != null) {
			line++;
			// 计算前听零
			count = 0;
			// 清除暂存列表
			timeTemp.clear();
			// 对一行进行匹配
			Matcher m = p.matcher(temp);

			while (m.find()) {
				count++;
				// 获取匹配到的字段
				String timeStr = m.group();
				timeStr = timeStr.substring(1, timeStr.length() - 1);
				timeLength = timeStr.length() + 2;
				// 根据匹配的字段计算处时间
				int timeMill = time2ms(timeStr);
				Flog.d("LyricParser---parser--timeMill--"+timeMill);
				// 加入列表
				timeTemp.add(timeMill);
			}
			// 如果存在时间点数据
			if (count > 0) {
				// 按从小到大的插入时间点
				for (int j = 0; j < timeTemp.size(); j++) {
					LyricItem item = new LyricItem();
					// 如果列表为空直接添加
					if (lyricList.size() == 0) {
						// 时间点数据添加到列表
						item.setTime(timeTemp.get(j));
						// 获取歌词的字符串
						if (line == 1) {
							msg = temp.substring(timeLength * count + 1);
						} else {
							msg = temp.substring(timeLength * count);
						}
						// 歌词添加的列表
//						item.setLyric(msg);
//						lyricList.add(item);
						if (lrcORtrc.equals("trc")) {
							Flog.d("LyricParser-----------------1-----------------trc");
							String a = getTrc(msg);
							String wordTime = oneTime(msg);
							item.setOnetime(wordTime);
							item.setLyric(a);
							lyricList.add(item);
						} else {
							Flog.d("LyricParser-----------------1-----------------lrc");
							item.setLyric(msg);
							lyricList.add(item);
						}
						
						
					}
					// 如果时间大于列表最后一个时间直接添加到尾部
					else if (timeTemp.get(j) > lyricList.get(
							lyricList.size() - 1).getTime()) {
						item.setTime(timeTemp.get(j));
						if (line == 1) {
							msg = temp.substring(timeLength * count + 1);
						} else {
							msg = temp.substring(timeLength * count);
						}

						if (lrcORtrc.equals("trc")) {
							Flog.d("LyricParser-----------------2-----------------trc");
							String trc = getTrc(msg);
							Flog.d("LyricParser--------2----trc--"+trc);
							String wordTime = oneTime(msg);
							Flog.d("LyricParser--------wordTime----wordTime--"+wordTime);
							item.setOnetime(wordTime);
							item.setLyric(trc);
							lyricList.add(item);
						} else {
							Flog.d("LyricParser-----------------2-----------------lrc");
							item.setLyric(msg);
							lyricList.add(item);
						}
						
					}
					// 否则安大小插入

					else {
						for (int index = 0; index < lyricList.size(); index++) {
							if (timeTemp.get(j) <= lyricList.get(index)
									.getTime()) {
								item.setTime(timeTemp.get(j));
								if (line == 1) {
									msg = temp
											.substring(timeLength * count + 1);
								} else {
									msg = temp.substring(timeLength * count);
								}
								if (lrcORtrc.equals("trc")) {
									Flog.d("LyricParser-----------------3-----------------trc");
									String a = getTrc(msg);
									String wordTime = oneTime(msg);
									item.setOnetime(wordTime);
									item.setLyric(a);
									lyricList.add(item);
								} else {
									Flog.d("LyricParser-----------------3-----------------lrc");
									item.setLyric(msg);
									lyricList.add(item);
								}
								
								break;
							}
						}
					}
				}
			}
			

//			stringBuilder.append(msg+"#" );
		}
		
		Flog.d("LyricParser----end---msg--" + msg);
//		Flog.d("LyricParser----end---stringBuilder--" + stringBuilder.toString());
		bufferedReader.close();
		inputReader.close();
		inputStream.close();
		
		Flog.d("LyricParser--parser()--end" );
		
		return lyricList;

	}
	
	public String getTrc(String msg) {
		List<String> ls = new ArrayList<String>();

		Pattern pattern = Pattern.compile("[\u4e00-\u9fa5a-zA-Z]");

//		Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");

		Matcher matcher = pattern.matcher(msg);
		while (matcher.find())

		ls.add(matcher.group());
		String lrc = ls.toString().substring(1, ls.toString().length() - 1);

		lrc = lrc.replaceAll(",","");

//		lrc = lrc.replace(",","");

		return lrc;

	}
	
	public String oneTime(String msg) {
		String str2 = "";
		for (int i = 0; i < msg.toString().length(); i++) {
			if (msg.charAt(i) >= 48 && msg.charAt(i) <= 57) {
				str2 += msg.charAt(i);
			}
		}
	  return str2;

	}


	public static int time2ms(String timeStr) {
		String s[] = timeStr.split(":");
		int min = Integer.parseInt(s[0]);
		int sec = 0;
		int mill = 0;
		// mm:ss:ms
		if (s.length > 2) {
			sec = Integer.parseInt(s[1]);
			mill = Integer.parseInt(s[2]);
		} else {
			String ss[] = s[1].split("\\.");
			// Ϊmm:ss.ms
			if (ss.length > 1) {
				sec = Integer.parseInt(ss[0]);
				mill = Integer.parseInt(ss[1]);
			}
			// Ϊmm:ss
			else {
				sec = Integer.parseInt(ss[0]);
				mill = 0;
			}
		}
		return min * 60 * 1000 + sec * 1000 + mill*10;
	}

}
