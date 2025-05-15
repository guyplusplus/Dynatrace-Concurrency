package com.geckotechnology.dynatraceConcurrency;

import java.time.ZonedDateTime;

public class TimeslotStatistics {

	private ZonedDateTime timeslotStart;
	private int eventCount = 0;
	private int maxConcurrency = 0; //for that timeslot
	
	public int getEventCount() {
		return eventCount;
	}

	public int getMaxConcurrency() {
		return maxConcurrency;
	}

	public ZonedDateTime getTimeslotStart() {
		return timeslotStart;
	}

	public TimeslotStatistics(ZonedDateTime timeslotStart) {
		this.timeslotStart = timeslotStart;
	}
	
	public boolean isTimeslotStartEqual(ZonedDateTime aTimeslotStart) {
		return timeslotStart.equals(aTimeslotStart);
	}
	
	public void newEventReceived(Event event) {
		eventCount++;
		if(event.getConcurrency() > maxConcurrency)
			maxConcurrency = event.getConcurrency();
	}

	@Override
	public String toString() {
		return "TimeslotStatistics [timeslotStart=" + timeslotStart + ", eventCount=" + eventCount + ", maxConcurrency=" + maxConcurrency
				+ "]";
	}	
}
