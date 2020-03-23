package io.gr1d.ic.usage.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Have some Date/Time utilities (and make it standard thru the code to make dates)
 * 
 * @author Rafael M. Lins
 *
 */
public enum DateTimes {
	;
	
	private static final ZoneId UTC = ZoneId.of("UTC");
	
	public static ZonedDateTime startOfMonth(final LocalDate date) {
		return date.withDayOfMonth(1).atStartOfDay(UTC);
	}

	public static ZonedDateTime endOfMonth(final LocalDate date) {
		return date.withDayOfMonth(1).plusMonths(1).atStartOfDay(UTC).minusNanos(1);
	}
	
	public static LocalDate nextWorkDay(final LocalDate start) {
		LocalDate date = start;
		
		while (!isWorkDay(date.getDayOfWeek())) {
			date = date.plusDays(1);
		}
		
		return date;
	}
	
	public static boolean isWorkDay(final DayOfWeek day) {
		return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
	}
}
