package com.geckotechnology.dynatraceConcurrency;

import java.time.ZonedDateTime;
import java.util.Objects;

public class Event {
	
	private ZonedDateTime startDateTime;
	private ZonedDateTime endDateTime;
	private int durationInMs = -1;
	private int concurrency = -1; //undefined at first
	
	public Event(ZonedDateTime startDateTime, int durationInMs) {
		this.startDateTime = startDateTime;
		this.durationInMs = durationInMs;
		this.endDateTime = startDateTime.plusNanos((long)durationInMs*1000000);
	}
	
	/**
	 * This constructor does not validate that endDateTime is after startDateTime
	 * @param startDateTime
	 * @param endDateTime
	 */
	public Event(ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
	}
	
	public int getConcurrency() {
		if(concurrency == -1)
			throw new RuntimeException("Concurrency has not been set yet for this event");
		return concurrency;
	}

	public void setConcurrency(int concurrency) {
		if(concurrency == -1)
			throw new RuntimeException("Concurrency can only be set once on an event");
		this.concurrency = concurrency;
	}
	
	public int getDurationInMs() {
		if(durationInMs == -1)
			throw new RuntimeException("durationInMs has not been set yet for this event");
		return durationInMs;
	}

	public ZonedDateTime getStartDateTime() {
		return startDateTime;
	}
	
	public ZonedDateTime getEndDateTime() {
		return endDateTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(startDateTime);
	}

	@Override
	public String toString() {
		if(durationInMs == -1)
			return "Event [startDateTime=" + startDateTime + ", endDateTime=" + endDateTime + ", concurrency=" + concurrency + "]";
		return "Event [startDateTime=" + startDateTime + ", durationInMs=" + durationInMs + ", concurrency=" + concurrency + "]";
	}
}
