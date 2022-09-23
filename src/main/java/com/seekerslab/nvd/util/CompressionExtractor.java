package com.seekerslab.nvd.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressionExtractor {
	private static Logger logger = LoggerFactory.getLogger(CompressionExtractor.class);
		
	public static String extractZip(String filepath, String unzipPath) throws IOException{
		String zipFilePath=filepath;//파일 path는 zip의 위치
		File zipFile=new File(zipFilePath);
		ZipInputStream zis=new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry ze=zis.getNextEntry();//zip 파일의 다음 entry zip
		String entryName="";
		while(ze!=null) {
			entryName=ze.getName();//zip의 원본 명
			String[]name=entryName.split("/");
			entryName=name[name.length-1];//
			
			File file=new File(unzipPath+"/"+entryName);
			file.getParentFile().mkdirs();
			FileOutputStream fos=new FileOutputStream(file);
			int len;
			byte[] buf=new byte[1024];
			while((len=zis.read(buf))>0) {
				fos.write(buf, 0, len);		//해제 후, len만큼 buf byte array에 data 넣기		
			}
			fos.close();
			ze=zis.getNextEntry();
		}//end of while
		zis.closeEntry();
		zis.close();				
		logger.info("unzip complete");				
		return unzipPath + entryName;//unzip path 및 파일 이름		
	}				
}
