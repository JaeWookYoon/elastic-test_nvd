package com.seekerslab.nvd.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.seekerslab.nvd.util.Constant;
import com.seekerslab.nvd.util.OSSpecifier;
@Configuration
@EnableScheduling
public class CommonSchedule {
	@Bean // 등록 bean scheduler 호출시 자동 호출
	public CommonJob start() {
		String os = System.getProperty("os.name");
		OSSpecifier oss = new OSSpecifier();
		if(oss.isWindows(os)) {
			Constant.filePath = "D:TEST/";
		}else {
			Constant.filePath = "/home/user/TEST/";
		}
		return new CommonJob();
	}
}
