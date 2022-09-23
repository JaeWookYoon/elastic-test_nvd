package com.seekerslab.nvd.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateUpdate {

	private BufferedReader br;
	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(getClass());

	@SuppressWarnings({ "unchecked", "unused" })
	public JSONObject getRecentJson() { // 최근 recent data의 meta-data read
		URLConnection uCon;
		JSONObject obj = new JSONObject();
		try {
			URL url = new URL("https://static.nvd.nist.gov/feeds/json/cve/1.0/nvdcve-1.0-recent.meta");

			uCon = url.openConnection();
			br = new BufferedReader(new InputStreamReader(uCon.getInputStream()));//recent CVE meta data reading...

			String str = "";
			List<String> list = new ArrayList<String>();

			while ((str = br.readLine()) != null) {
				list.add(str);
			}
			for (int i = 0; i < list.size(); i++) {
				String a1 = list.get(i).substring(0, list.get(i).indexOf(":"));
				String a2 = list.get(i).substring(list.get(i).indexOf(":") + 1, list.get(i).length());
				obj.put(a1, a2);
			}
			String result = (String) obj.get("lastModifiedDate");// last modify update

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			close();
		}
		return obj;// jsonObject return
	}

	@SuppressWarnings({ "unchecked", "unused" })
	public JSONObject getJson(String year) {//입력받은 year의 nvd meta data
		URLConnection uCon;
		JSONObject obj = new JSONObject();
		try {
			URL url = new URL("https://static.nvd.nist.gov/feeds/json/cve/1.0/nvdcve-1.0-" + year + ".meta");
			//meta-data json
			uCon = url.openConnection();
			br = new BufferedReader(new InputStreamReader(uCon.getInputStream()));//url로부터 meta data읽는 중

			String str = "";
			List<String> list = new ArrayList<String>();

			while ((str = br.readLine()) != null) {
				list.add(str);
			}
			for (int i = 0; i < list.size(); i++) {//meta data parsing
				String a1 = list.get(i).substring(0, list.get(i).indexOf(":"));
				String a2 = list.get(i).substring(list.get(i).indexOf(":") + 1, list.get(i).length());
				obj.put(a1, a2);
			}
			String result = (String) obj.get("lastModifiedDate");
			// meta-data
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			close();
		}
		return obj;
	}

	public void close() {
		try {
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
