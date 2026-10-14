package gkuri.anylib.scheduler.loop.time;

import java.util.concurrent.TimeUnit;

public enum TimeSchedulerMode {

	EVERY_YEAR("every year", TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS)),
	EVERY_MONTH("every month", TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS)),
	EVERY_WEEK("every week", TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)),
	EVERY_DAY("every day", TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)),
	EVERY_HOUR("every hour", TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)),
	EVERY_MIN("every min", TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES)),
	EVERY_SEC("every sec", TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS));
	
	
	private final String rateName;
	private final long rateMS;
	
	private TimeSchedulerMode(String rateName, long rateMS) {
		this.rateName = rateName;
		this.rateMS = rateMS;
	}
	
	public long getRateMS() {
		return this.rateMS;
	}
	
	@Override
	public String toString() {
		return "TimeSchedulerMode@%s".formatted(this.rateName).replace(' ', '-');
	}
}
