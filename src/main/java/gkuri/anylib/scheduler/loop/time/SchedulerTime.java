package gkuri.anylib.scheduler.loop.time;

import java.time.DayOfWeek;
import java.util.concurrent.TimeUnit;

public class SchedulerTime {

	private int month;
	private int day;
	private int date;
	private int hour;
	private int min;
	private int sec;
	private int milisec;
	private DayOfWeek dayOfWeek;
	
	public SchedulerTime() {
		this.month = 0;
		this.day = 0;
		this.date = 0;
		this.hour = 0;
		this.min = 0;
		this.sec = 0;
		this.milisec = 0;
		this.dayOfWeek = null;
	}
	
	public int getMonth() {
		return this.month;
	}
	
	public SchedulerTime setMonth(int month) {
		this.month = Math.max(0, month);
		return this;
	}
	
	public int getDayOfWeekNum() {
		return this.day;
	}
	
	public SchedulerTime setDayOfWeekNum(int dayOfWeek) {
		this.day = Math.max(0, dayOfWeek);
		this.dayOfWeek = null;
		return this;
	}
	
	public DayOfWeek getDayOfWeek() {
		return this.dayOfWeek;
	}
	
	public SchedulerTime setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
		this.day = 0;
		return this;
	}
	
	public int getDay() {
		return this.date;
	}
	
	public SchedulerTime setDay(int day) {
		this.date = Math.max(0, day);
		return this;
	}
	
	public int getHour() {
		return this.hour;
	}
	
	public SchedulerTime setHour(int hour) {
		this.hour = Math.max(0, hour);
		return this;
	}
	
	public int getMin() {
		return this.min;
	}
	
	public SchedulerTime setMin(int min) {
		this.min = Math.max(0, min);
		return this;
	}
	
	public int getSec() {
		return this.sec;
	}
	
	public SchedulerTime setSec(int sec) {
		this.sec = Math.max(0, sec);
		return this;
	}
	
	public int getMillSec() {
		return this.milisec;
	}
	
	public SchedulerTime setMillSec(int millSec) {
		this.milisec = Math.max(0, millSec);
		return this;
	}
	
	/**
	 * 暫定時間用。
	 * @param schedulerMode
	 * @return
	 */
	@Deprecated
	protected long getSpanMS(TimeSchedulerMode schedulerMode) {
		long result = this.milisec;
		if(schedulerMode == TimeSchedulerMode.EVERY_SEC) {
			return Math.max(result, 1);
		}
		
		result += TimeUnit.MILLISECONDS.convert(this.sec, TimeUnit.SECONDS);
		if(schedulerMode == TimeSchedulerMode.EVERY_MIN) {
			return Math.max(result, TimeSchedulerMode.EVERY_SEC.getRateMS());
		}
		
		result += TimeUnit.MILLISECONDS.convert(this.min, TimeUnit.MINUTES);
		if(schedulerMode == TimeSchedulerMode.EVERY_HOUR) {
			return Math.max(result, TimeSchedulerMode.EVERY_HOUR.getRateMS());
		}
		
		result += TimeUnit.MILLISECONDS.convert(this.hour, TimeUnit.HOURS);
		if(schedulerMode == TimeSchedulerMode.EVERY_DAY) {
			return Math.max(result, TimeSchedulerMode.EVERY_DAY.getRateMS());
		}
		
		if(schedulerMode == TimeSchedulerMode.EVERY_WEEK) {
			if(this.dayOfWeek != null) {
				result += TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);
			} else {
				result += TimeUnit.MILLISECONDS.convert(this.day * 7, TimeUnit.DAYS);
			}
			
			return Math.max(result, TimeSchedulerMode.EVERY_WEEK.getRateMS());
		}
		
		result += TimeUnit.MILLISECONDS.convert(this.date, TimeUnit.DAYS);
		if(schedulerMode == TimeSchedulerMode.EVERY_MONTH) {
			return Math.max(result, TimeSchedulerMode.EVERY_MONTH.getRateMS());
		}
		
		result += TimeUnit.MILLISECONDS.convert(this.month * 30, TimeUnit.DAYS);
		
		return Math.max(result, TimeSchedulerMode.EVERY_YEAR.getRateMS());
	}

}
