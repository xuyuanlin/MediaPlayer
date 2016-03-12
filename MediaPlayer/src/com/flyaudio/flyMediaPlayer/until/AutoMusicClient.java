package com.flyaudio.flyMediaPlayer.until;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.flyaudio.flyMediaPlayer.objectInfo.LoadMusicInfo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class AutoMusicClient {
	private Context mContext;

	private static final String MAX_MUSIC_RESULT = "";

	private static final String TAG = "get_CityClient";

	public AutoMusicClient(Context Context) {

		this.mContext = Context;
	}

	public static String SOSO_GET_URL = "http://cgi.music.soso.com/fcgi-bin/fcg_search_xmldata.q?";// 定义固定地址

	public List<LoadMusicInfo> getMusicList(String singer) {
		List<LoadMusicInfo> result = new ArrayList<LoadMusicInfo>();

		try {
			String query = makeQueryCityURL(singer);
			Flog.d("get_musicclient---------------------------------------------------"
					+ query);

			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(query);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			int code = httpResponse.getStatusLine().getStatusCode();
			if (code != HttpStatus.SC_OK) {
				Log.e("getCityList", "sorry,ti is erro!");

			}
			HttpEntity entity = httpResponse.getEntity();
			InputStream content = entity.getContent();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					content, "GBK"));
			StringBuffer sbf = new StringBuffer();
			String line = null;
			while ((line = br.readLine()) != null) {
				sbf.append(line);
			}

			br.close();

			String jsonString = sbf.toString();
			Log.e(TAG, "-----getCityList----" + jsonString);
			result = getMusicInfos("list", jsonString);
			Log.e(TAG, "------getMusicInfos----" + result.toString());

		} catch (Exception e) {
			// Toast.makeText(this, "输入有误！", 3000)

			e.printStackTrace();

		}
		return result;

	}

	public static List<LoadMusicInfo> getMusicInfos(String key,
			String jsonString) {
		List<LoadMusicInfo> musicInfos = new ArrayList<LoadMusicInfo>();
		try {
			String jsonString2 = jsonString.substring(jsonString.indexOf("{"),
					jsonString.lastIndexOf("}") + 1);
			Log.e(TAG, "------getMusicInfos---jsonString2--" + jsonString2);
			JSONObject jsonObject = new JSONObject(jsonString2);
			JSONArray jsonArray = jsonObject.getJSONArray(key);
			for (int i = 0; i < jsonArray.length(); i++) {
				LoadMusicInfo musicInfo = new LoadMusicInfo();
				JSONObject jsonObject2 = jsonArray.getJSONObject(i);
				musicInfo.setSongid(jsonObject2.getInt("songid"));
				musicInfo.setSongname(jsonObject2.getString("songname"));
				musicInfo.setSingername(jsonObject2.getString("singername"));
				musicInfo.setAblumname(jsonObject2.getString("albumname"));
				musicInfo.setPubtime(jsonObject2.getInt("pubtime"));
				musicInfos.add(musicInfo);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		Log.e(TAG,
				"------getMusicInfos---musicInfos----" + musicInfos.toString());
		return musicInfos;
	}

	// 对地址进行组合
	private String makeQueryCityURL(String singer) {

		return SOSO_GET_URL + "source=10&w=" + singer + "&perpage="
				+ MAX_MUSIC_RESULT + "&ie=utf-8";
	}
}
