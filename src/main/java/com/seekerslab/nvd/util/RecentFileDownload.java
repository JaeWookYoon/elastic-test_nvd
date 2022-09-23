package com.seekerslab.nvd.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.seekerslab.nvd.es.ElasticsearchConnection;

public class RecentFileDownload implements Job {

	final static int SIZE = 2048;
	private static Logger log=Logger.getAnonymousLogger();
	@SuppressWarnings("unused")
	public int fileUrlReadDownload(String fileaddress, String localFileName, String downloadDir)
			throws Exception {
		int byteWritten = 0;
		OutputStream out = null;
		InputStream in = null;
		URLConnection uCon = null;
		String releaseFolder = "D:/RECENT";
		try {

			URL url;
			byte[] buf;
			int byteRead;

			url = new URL(fileaddress);
			out = new BufferedOutputStream(new FileOutputStream(downloadDir + "/" + localFileName));

			uCon = url.openConnection();

			in = uCon.getInputStream();

			buf = new byte[SIZE];

			while ((byteRead = in.read(buf)) != -1) {
				out.write(buf, 0, byteRead);
				byteWritten += byteRead;
				log.info(byteWritten + "download");
			}
			log.info("Download Over");

			ValidateUpdate val = new ValidateUpdate();

			JSONObject obj = val.getRecentJson();

			int zipsize = Integer.valueOf((String) obj.get("zipSize"));
			log.info(zipsize + "SizeFile.");


		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			in.close();
			out.close();

		}
		return byteWritten;
	}

	public Map<String, Object> fileUrlDownload() throws Exception {
		String fileAddress = "https://static.nvd.nist.gov/feeds/json/cve/1.0/nvdcve-1.0-recent.json.zip";
		String downloadDir = "C:\\RECENT";
		Map<String, Object> map = new HashMap<String, Object>();
		int slashIndex = fileAddress.lastIndexOf('/');
		int periodIndex = fileAddress.lastIndexOf('.');

		String fileName = fileAddress.substring(slashIndex + 1);
		if (periodIndex >= 1 && slashIndex >= 0 && slashIndex < fileAddress.length() - 1) {
			int result = fileUrlReadDownload(fileAddress, fileName, downloadDir);
			map.put("recentAddress", downloadDir + "/" + fileName);
			map.put("recent", result);

		} else {
			log.info("path or file name NG.");
		}
		return map;
	}

	public boolean getDif(String filepath) throws Exception {
		ValidateUpdate val = new ValidateUpdate();

		JSONObject obj = val.getRecentJson();

		String recentDate = (String) obj.get("lastModifiedDate");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
		String str = "";
		StringBuilder strr = new StringBuilder();
		Object ob = "";
		File f=new File(filepath);
		
		String in=String.valueOf(f.hashCode());
		
		MessageDigest digest=MessageDigest.getInstance("SHA-256");
		
		byte[]hash=digest.digest(in.getBytes("UTF-8"));
		StringBuilder hexString=new StringBuilder();
		
		for(int i=0;i<hash.length;i++) {
			String hex=Integer.toHexString(0xff & hash[i]);
			if(hex.length()==1) {
				hexString.append("0");
			}
		}
		
		while ((str = br.readLine()) != null) {

			strr.append(str);

		}

		JSONParser parser = new JSONParser();
		ob = parser.parse(strr.toString());

		JSONObject jsonob = (JSONObject) ob;

		String date = (String) jsonob.get("CVE_data_timestamp");
		
		br.close();		
		
		if (date.split("T")[0].equals(recentDate.split("T"))) {// date format�� 1�� ������ format ���� ��� true
			return true;
		} else {
			return false;
		}

	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		RecentFileDownload re = new RecentFileDownload();
		Map<String, Object> map = null;
		File f = new File("D:/RECENT/nvdcve-1.0-recent.json");
		try {
			if (f.exists()) {// json�� �ִ°� ����
				if (re.getDif("D:/RECENT/nvdcve-1.0-recent.json")) {// json�� ������¥�� meta-data��
					log.info("It's Same");
				} else {
					log.info("It's Def");// ������ Exception �߻�
					map = re.fileUrlDownload();
					
					ValidateUpdate val = new ValidateUpdate();

					JSONObject obj = val.getRecentJson();

					int zipsize = Integer.valueOf((String) obj.get("zipSize"));

					int recent = (int) map.get("recent");
									
					if (zipsize == recent) {// �ٿ�ް� ���� ���� ������ ũ�� ��

						String str=CompressionExtractor.extractZip((String) map.get("recentAddress"), "D:/RECENT");
						ElasticsearchConnection cont = new ElasticsearchConnection();
						if(cont.indexExist("recent")) {
							cont.deleteIndex("recent");
						}
						cont.createIndex("recent");
						cont.insertData(str, "recent");
					}

				}
			} else {
				map = fileUrlDownload();
				ValidateUpdate val = new ValidateUpdate();

				JSONObject obj = val.getRecentJson();

				int zipsize = Integer.valueOf((String) obj.get("zipSize"));

				int recent = (int) map.get("recent");

				
				if (zipsize == recent) {// �ٿ�ް� ���� ���� ������ ũ�� ��

					CompressionExtractor.extractZip((String) map.get("recentAddress"), "D:/RECENT");

				}
			}
		} catch (FileNotFoundException fe) {
			log.info("File Not Found");
			try {
				map = re.fileUrlDownload();
				
				ValidateUpdate val = new ValidateUpdate();

				JSONObject obj = val.getRecentJson();

				int zipsize = Integer.valueOf((String) obj.get("zipSize"));

				int recent = (int) map.get("recent");

				
				if (zipsize == recent) {// �ٿ�ް� ���� ���� ������ ũ�� ��

					CompressionExtractor.extractZip((String) map.get("recentAddress"), "D:/RECENT");

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			fe.printStackTrace();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
