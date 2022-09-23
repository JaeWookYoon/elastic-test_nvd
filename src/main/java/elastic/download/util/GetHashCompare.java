package elastic.download.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.logging.Logger;

public class GetHashCompare {

	private static Logger log=Logger.getAnonymousLogger();
	
	public String getHashcode(byte[]message) throws Exception{
		MessageDigest md=MessageDigest.getInstance("SHA-256");
		
		md.update(message);
		byte[]hash=md.digest();
		System.out.println(hash.toString()+"hashcode");
		StringBuilder str=new StringBuilder();
		for(int i=0;i<hash.length;i++) {
			str.append(String.format("%02x", hash[i]&0xff));
		}
		return str.toString();//hash반환
	}
	
	public byte[] getFileByte(String filepath) throws IOException{
		BufferedInputStream ins=new BufferedInputStream(new FileInputStream(filepath));
		byte[]content=new byte[ins.available()];
		ins.close();
		return content;
	}//파일을 바이트로 변환
	
	public void isModified(String filepath,String hash) throws Exception{
		File f=new File(filepath);
		String filename=f.getName();
		if(filename.contains(".")) {
			filename=filename.substring(0,filename.lastIndexOf("."));
		}
		String hashs=getHashcode(getFileByte(filename));
			
		System.out.println(hashs);
	}
	public static void main(String[]args) {
		
		GetHashCompare com=new GetHashCompare();
		String path="C:\\Users\\Seekers\\Downloads\\nvdcve-1.0-2014.json.zip";
		try{
			String hash=com.getHashcode(com.getFileByte(path));
			System.out.println(hash);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
}
