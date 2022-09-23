package com.seekerslab.nvd.scheduler;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;

import com.seekerslab.nvd.util.RecentFileDownload;

public class RecentlySchedule {

	public void getTrigger() throws SchedulerException {
		SchedulerFactory scheduleFactory=new StdSchedulerFactory();
		Scheduler scheduler=scheduleFactory.getScheduler();
		try {			
			scheduler.start();//시작			
			JobDetail jobDetail=new JobDetailImpl("job1","group1",RecentFileDownload.class);//schedule			
		Trigger trigger=TriggerBuilder.newTrigger()
				.withIdentity("trigger1","triggerGroup1")
				.withSchedule(CronScheduleBuilder.cronSchedule("0/1 * * * * ?"))//cron등록
				.build();
		
		scheduler.scheduleJob(jobDetail,trigger);//schedule작업
				
		}catch(Exception e) {
			e.printStackTrace();
			scheduler.clear();
			scheduler.shutdown();
		}
	}
}
