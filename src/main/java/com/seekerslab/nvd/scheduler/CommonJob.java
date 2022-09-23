package com.seekerslab.nvd.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;

import com.seekerslab.nvd.es.ElasticsearchConnection;
import com.seekerslab.nvd.main.MainExecutor;
import com.seekerslab.nvd.util.CompressionExtractor;
import com.seekerslab.nvd.util.Constant;
import com.seekerslab.nvd.util.FileDownload;
import com.seekerslab.nvd.util.ValidateUpdate;

public class CommonJob {

	private static int startObjectSize;
	private static int endObjectSize;
	private MainExecutor main = new MainExecutor();
	@SuppressWarnings("unused")
	private ValidateUpdate val = new ValidateUpdate();	

	@SuppressWarnings({ "deprecation", "unused" })
	@Scheduled(fixedDelay = 1000 * 60 * 60 * 24) // ms 단위 60초 * 60 * 24 = Daily
	public void scheduleJob() {
		List<String> years = new ArrayList<String>();
		Date date = new Date();
		int now = date.getYear() + 1900;
		
		for (int i = 2002; i <= now; i++) {// 2002년도 부터 현재까지.
			years.add(String.valueOf(i));
		} // end for
		years.add("recent");
				
		for (String s : years) {
			realJob(s);
		}
	}
	public void realJob(String year) {		
		FileDownload auto = new FileDownload();
		try {
			Map<String, Object> map = auto.fileUrlDownload(year);// 파일 다운로드
			JSONObject obj = val.getJson(year);// NVDLIST meta-data json data
			startObjectSize = Integer.valueOf(String.valueOf(map.get("big")));// Download ZIP Size

			endObjectSize = Integer.valueOf((String) obj.get("zipSize"));// meta-data NVD zipSize
			if (main.matchObjectSize(startObjectSize, endObjectSize)) {// download ZIP Size 와 meta-data Zip size의 동일성을 통해, 무결성 검증
				String fileAddress = CompressionExtractor.extractZip((String) map.get("address"), Constant.filePath);// 경로지정 다운로드
				ElasticsearchConnection esConnection = new ElasticsearchConnection();
				if (esConnection.indexExist(year)) {// 해당 년도 존재 할 경우.
					esConnection.deleteIndex(year);// 해당 년도 index 삭제
				}
				esConnection.createIndex(year); // index 생성
				esConnection.insertData(fileAddress, year); // index bulk
			} // end match If
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
