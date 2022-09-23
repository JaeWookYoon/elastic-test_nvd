package com.seekerslab.nvd.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileDownload {

	final static int SIZE = 2048;//byte array SIZE
	private Logger logger = LoggerFactory.getLogger(getClass());

	public int fileUrlReadDownload(String fileaddress, String localFileName, String downloadDir)
			throws Exception {
		OutputStream out = null;
		InputStream in = null;
		URLConnection uCon = null;		
		int byteWritten = 0;
		try {
			URL url;
			byte[] buf;
			int byteRead;

			url = new URL(fileaddress);
			out = new BufferedOutputStream(new FileOutputStream(downloadDir + "/" + localFileName));

			uCon = url.openConnection();

			in = uCon.getInputStream();//다운로드 준비

			buf = new byte[SIZE];

			while ((byteRead = in.read(buf)) != -1) {//read Start
				out.write(buf, 0, byteRead);//buf byte array 애 byteRead만큼 담는 중.
				byteWritten += byteRead;//작성된 사이즈 기록 중
				logger.info(byteWritten + "download");
			}
			logger.info("Download Over");

		} catch (FileNotFoundException fe) {
			logger.info("FileNotFound");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException ie) {
				logger.error(ie.getMessage());
			}			
		}
		return byteWritten;
	}

	public Map<String, Object> fileUrlDownload(String year) throws Exception {//해당 년도를 입력받아 다운로드 실행		
		int result = 0;
		Map<String, Object> map = new HashMap<String, Object>();
		String fileAddress = "https://static.nvd.nist.gov/feeds/json/cve/1.0/nvdcve-1.0-" + year + ".json.zip";//경로 url
		
		File f = new File(Constant.filePath);//down 경로
		int slashIndex = fileAddress.lastIndexOf('/');
		int periodIndex = fileAddress.lastIndexOf('.');
		
		String fileName = fileAddress.substring(slashIndex + 1);//fileName - fileAddress 에서 Last / 부터 substring
		if (periodIndex >= 1 && slashIndex >= 0 && slashIndex < fileAddress.length() - 1) { // slashIndex 는 파일 주소 길이보다 짧고, . /의 last Index는 존재해야 한다.
			logger.info(fileName + "filename");
			if(!f.exists()) {//download경로 dir이 존재하지 않을 경우,
				f.mkdirs();//생성
			}
			result = fileUrlReadDownload(fileAddress, fileName, Constant.filePath);//파싱한 파일 주소와 인덱스
			map.put("address", Constant.filePath + fileName);//최종 경로
			map.put("big", new Integer(result));//다운받은 파일 사이즈
		} else {
			logger.info("path or file name NG.");
		}
		return map;
	}

}
