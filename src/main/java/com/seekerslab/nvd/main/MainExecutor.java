package com.seekerslab.nvd.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.seekerslab.nvd.scheduler.CommonSchedule;

@SuppressWarnings("unused")
public class MainExecutor {

	private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private static Logger log=Logger.getAnonymousLogger();
	
	@SuppressWarnings({"resource","unused"})
	public static void main(String[] args) {					
			AbstractApplicationContext context = new AnnotationConfigApplicationContext(CommonSchedule.class);//schedule 실행				
	}
	public boolean matchObjectSize(int startObject,int endObject) {
		if(startObject==endObject) {
			return true;
		}else {
			return false;
		}
	}
}
