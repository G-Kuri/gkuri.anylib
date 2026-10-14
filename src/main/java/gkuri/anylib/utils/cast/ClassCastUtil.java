package gkuri.anylib.utils.cast;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gkuri.anylib.utils.multiValue.Duo;

public class ClassCastUtil {
	
	private static final List<Duo<Long, DateTimeFormatter>> DATE_FORMATTS = new LinkedList<Duo<Long,DateTimeFormatter>>(List.of(
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yyMMdd")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yy-MM-dd")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yy/MM/dd")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yy.MM.dd")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yy MM dd")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yy年MM月dd日")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yyyyMMdd")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yyyy/MM/dd")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yyyy.MM.dd")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yyyy MM dd")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("MM-dd-yyyy")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("MM/dd/yyyy")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("MM.dd.yyyy")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("MM dd yyyy")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
																				));
	
	private static final List<Duo<Long, DateTimeFormatter>> TIME_FORMATTS = new LinkedList<Duo<Long,DateTimeFormatter>>(List.of(
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HH")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HHmm")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HH:mm")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HH.mm")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HH mm")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HH時mm分")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HHmmss")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HH:mm:ss")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HH.mm.ss")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HH mm ss")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HH時mm分ss秒")),
																					new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern("HH:mm:ss.SSSS"))
																				));
	
	private static final List<Duo<Long, DateTimeFormatter>> DATE_TIME_FORMATTS = Stream.of(
																					"yyyyMMdd'T'HHmm",
																					"yyyy-MM-dd'T'HH:mm",
																					"yyyy/MM/dd'T'HH:mm",
																					"yyyy.MM.dd'T'HH:mm",
																					"yyyy MM dd'T'HH:mm",
																					"yyyy年MM月dd日'T'HH時mm分",
																					"yyyyMMdd HHmm",
																					"yyyy-MM-dd HH:mm",
																					"yyyy/MM/dd HH:mm",
																					"yyyy.MM.dd HH:mm",
																					"yyyy MM dd HH:mm",
																					"yyyy年MM月dd日 HH時mm分",
																					"yyyy-MM-dd'T'HH mm",
																					"yyyy/MM/dd'T'HH mm",
																					"yyyy.MM.dd'T'HH mm",
																					"yyyy MM dd'T'HH mm",
																					"yyyy-MM-dd HH.mm",
																					"yyyy/MM/dd HH.mm",
																					"yyyy.MM.dd HH.mm",
																					"yyyy MM dd HH.mm",
																					"yyyy-MM-dd HH mm",
																					"yyyy/MM/dd HH mm",
																					"yyyy.MM.dd HH mm",
																					"yyyy MM dd HH mm",
																					"yyyyMMdd'T'HHmmss",
																					"yyyy-MM-dd'T'HH:mm:ss",
																					"yyyy/MM/dd'T'HH:mm:ss",
																					"yyyy.MM.dd'T'HH:mm:ss",
																					"yyyy MM dd'T'HH:mm:ss",
																					"yyyy年MM月dd日'T'HH時mm分ss秒",
																					"yyyyMMdd HHmmss",
																					"yyyy-MM-dd HH:mm:ss",
																					"yyyy/MM/dd HH:mm:ss",
																					"yyyy.MM.dd HH:mm:ss",
																					"yyyy MM dd HH:mm:ss",
																					"yyyy年MM月dd日 HH時mm分ss秒",
																					"yyyy-MM-dd'T'HH mm ss",
																					"yyyy/MM/dd'T'HH mm ss",
																					"yyyy.MM.dd'T'HH mm ss",
																					"yyyy MM dd'T'HH mm ss",
																					"yyyy-MM-dd'T'HH.mm.ss",
																					"yyyy/MM/dd'T'HH.mm.ss",
																					"yyyy.MM.dd'T'HH.mm.ss",
																					"yyyy MM dd'T'HH.mm.ss",
																					"yyyy-MM-dd HH.mm.ss",
																					"yyyy/MM/dd HH.mm.ss",
																					"yyyy.MM.dd HH.mm.ss",
																					"yyyy MM dd HH.mm.ss",
																					"yyyy-MM-dd HH mm ss",
																					"yyyy/MM/dd HH mm ss",
																					"yyyy.MM.dd HH mm ss",
																					"yyyy MM dd HH mm ss"
																				).map(pattern -> new Duo<Long, DateTimeFormatter>(0L, DateTimeFormatter.ofPattern(pattern))).collect(Collectors.toCollection(LinkedList::new));
	
	public static Number stringToNumber(String number){
		if(number == null) return null;
		number = number.replace("_", "");
		String lowerNumber = number.toLowerCase();
		boolean isMuinus = number.startsWith("-");
		if(isMuinus){
			number = number.substring(1);
			lowerNumber = lowerNumber.substring(1);
		}

		if(lowerNumber.contains(".")) {
			if(lowerNumber.endsWith("f")) {
				float result = Float.parseFloat(number.substring(0, number.length() - 1));
				return isMuinus ? result * -1 : result;
			}
			double result = Double.parseDouble(number);
			return isMuinus ? result * -1 : result;
		}
		
		if(lowerNumber.startsWith("0b")) {
			if(lowerNumber.endsWith("l")) {
				long result = Long.parseLong(number.substring(2, number.length() - 1), 2);
				return isMuinus ? result * -1 : result;
			}
			int result = Integer.parseInt(number.substring(2), 2);
			return isMuinus ? result * -1 : result;
		}
		
		if(lowerNumber.startsWith("0x")) {
			if(lowerNumber.endsWith("l")) {
				long result = Long.parseLong(number.substring(2, number.length() - 1), 16);
				return isMuinus ? result * -1 : result;
			}
			int result = Integer.parseInt(number.substring(2), 16);
			return isMuinus ? result * -1 : result;
		}

		if(lowerNumber.startsWith("0")) {
			if(lowerNumber.length() == 1) return 0;
			
			if(lowerNumber.endsWith("l")) {
				long result =  Long.parseLong(number.substring(1, number.length() - 1), 8);
				return isMuinus ? result * -1 : result;
			}
			int result =  Integer.parseInt(number.substring(1), 8);
			return isMuinus ? result * -1 : result;
		}
		
		if(lowerNumber.endsWith("l")) {
			long result =  Long.parseLong(number.substring(0, number.length() - 1), 10);
			return isMuinus ? result * -1 : result;
		}
		for(char c : lowerNumber.toCharArray()) {
			if(c < '0' || '9' < c) throw new NumberFormatException(number);
		}
		int result =  Integer.parseInt(number, 10);
		return isMuinus ? result * -1 : result;
	}
	
	public static LocalDate stringToDate(String stringDate) {
		stringDate = stringDate.trim();
		stringDate = Normalizer.normalize(stringDate, Normalizer.Form.NFKC);
		
		if(stringDate.length() < 6) return null;
		
		for(int i = 0; i < DATE_FORMATTS.size(); i++) {
			Duo<Long, DateTimeFormatter> f = DATE_FORMATTS.get(i);
			try {
				LocalDate date = LocalDate.parse(stringDate, f.getSecond());
				if(f.getFirst() < Long.MAX_VALUE) f.setFirst(f.getFirst() + 1);
				DATE_FORMATTS.set(i, f);
				DATE_FORMATTS.sort((a, b) -> Long.compare(b.getFirst(), a.getFirst()));
				return date;
			} catch (DateTimeParseException e) {
				// TODO: handle exception
			}
		}
		
		return null;
	}
	
	public static Boolean stringToBoolean(String bool) {
		if(bool == null) return null;
		if(bool.equalsIgnoreCase("true")) return true;
		if(bool.equalsIgnoreCase("false")) return false;
		return null;
	}
	
	public static LocalTime stringToTime(String stringTime) {
		stringTime = stringTime.trim();
		stringTime = Normalizer.normalize(stringTime, Normalizer.Form.NFKC);
		
		if(stringTime.length() < 2) return null;
		
		for(int i = 0; i < TIME_FORMATTS.size(); i++) {
			Duo<Long, DateTimeFormatter> f = TIME_FORMATTS.get(i);
			try {
				LocalTime date = LocalTime.parse(stringTime, f.getSecond());
				if(f.getFirst() < Long.MAX_VALUE) f.setFirst(f.getFirst() + 1);
				TIME_FORMATTS.set(i, f);
				TIME_FORMATTS.sort((a, b) -> Long.compare(b.getFirst(), a.getFirst()));
				return date;
			} catch (DateTimeParseException e) {
				// TODO: handle exception
			}
		}
		
		return null;
	}
	
	public static LocalDateTime stringToDateTime(String stringDateTime) {
		stringDateTime = stringDateTime.trim();
		stringDateTime = Normalizer.normalize(stringDateTime, Normalizer.Form.NFKC);
		
		if(stringDateTime.length() < 2) return null;
		
		for(int i = 0; i < DATE_TIME_FORMATTS.size(); i++) {
			Duo<Long, DateTimeFormatter> f = DATE_TIME_FORMATTS.get(i);
			try {
				LocalDateTime date = LocalDateTime.parse(stringDateTime, f.getSecond());
				if(f.getFirst() < Long.MAX_VALUE) f.setFirst(f.getFirst() + 1);
				DATE_TIME_FORMATTS.set(i, f);
				DATE_TIME_FORMATTS.sort((a, b) -> Long.compare(b.getFirst(), a.getFirst()));
				return date;
			} catch (DateTimeParseException e) {
				// TODO: handle exception
			}
		}
		
		return null;
	}
	
	public static LocalDateTime stringToDateTime(String dateStr, String timeStr) {
	    LocalDate date = stringToDate(dateStr);
	    LocalTime time = stringToTime(timeStr);

	    if (date == null || time == null) {
	        return null;
	    }

	    return LocalDateTime.of(date, time);
	}
	
	public static <T> T cast(Object object, Class<T> clazz) {
		return cast(object, clazz, false);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object object, Class<T> clazz, boolean throwException) {
		if(object == null) {
			if(throwException) {
				throw new ClassCastException("object is null");
			}
			return null;
		}
		
		if(clazz.isInstance(object)) {
			return clazz.cast(object);
		}
		
		if(Castable.class.isAssignableFrom(clazz)) {
			try {
				Castable<?> castable = (Castable<?>) clazz.getDeclaredConstructor().newInstance();
				return (T) castable.cast(object);
			} catch (Exception e) {
				if(throwException) {
					throw new ClassCastException(e.getMessage());
				}
				return null;
			}
		}
		
		if(throwException) {
			throw new ClassCastException(object + "is not " + clazz + " class");
		}
		return null;
	}
}
