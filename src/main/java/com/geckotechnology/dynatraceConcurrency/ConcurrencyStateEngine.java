package com.geckotechnology.dynatraceConcurrency;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;

public class ConcurrencyStateEngine {
	
	private int timeslotDurationInSeconds;
	private Event previousEvent = null;
	private TimeslotStatistics currentTimeslotStatistics = null;
	private TimeslotStatistics previousTimeslotStatistics = null;
	private SortedLinkedList<ZonedDateTime> eventEndDateTimeSortedList = new SortedLinkedList<ZonedDateTime>(Comparator.naturalOrder());
	private int maxConcurrency = 0; //for the entire event set
	
	public ConcurrencyStateEngine() {
		this.timeslotDurationInSeconds = -1;
	}
	
	public ConcurrencyStateEngine(int timeslotDurationInSeconds) {
		if(timeslotDurationInSeconds <= 0)
			throw new RuntimeException("timeslotDurationInSeconds must be stricly greater than 0");
		this.timeslotDurationInSeconds = timeslotDurationInSeconds;
	}
	
	private void processTimeslotStatistics(Event event) {
		//ZonedDateTime eventZonedDateTimeTimeslot = event.getStartDateTime().withNano(0).minusSeconds(event.getStartDateTime().toEpochSecond() % timeslotDurationInSeconds);
		long eventStartDateTimeEpoch = event.getStartDateTime().toEpochSecond();
		long eventZonedDateTimeTimeslotEpoch = eventStartDateTimeEpoch - eventStartDateTimeEpoch % timeslotDurationInSeconds;
		ZonedDateTime eventZonedDateTimeTimeslot = ZonedDateTime.ofInstant(Instant.ofEpochSecond(eventZonedDateTimeTimeslotEpoch), event.getStartDateTime().getZone());
		
		if(currentTimeslotStatistics == null) {
			//this is the very first event
			currentTimeslotStatistics = new TimeslotStatistics(eventZonedDateTimeTimeslot);
			currentTimeslotStatistics.newEventReceived(event);
		}
		else {
			//this is not the first event
			//check if new event is the same timeslot as the current timeslot
			if(currentTimeslotStatistics.isTimeslotStartEqual(eventZonedDateTimeTimeslot)) {
				currentTimeslotStatistics.newEventReceived(event);
			}
			else {
				//this new event falls under a new timeslot
				if(previousTimeslotStatistics != null)
					throw new RuntimeException("Previous Timeslot has not been cleared");
				previousTimeslotStatistics = currentTimeslotStatistics;
				currentTimeslotStatistics = new TimeslotStatistics(eventZonedDateTimeTimeslot);
				currentTimeslotStatistics.newEventReceived(event);		
			}
		}
	}
	
	public void receiveEvent(Event event) {
		//STEP-1: check if previous event is before this new event
		if(previousEvent != null) {
			if((previousEvent.getStartDateTime().compareTo(event.getStartDateTime())) > 0)
				throw new RuntimeException("New event is after the previous event. They need to be sorted in ASC start time ordder");
		}
		
		//STEP-2: calculate concurrency for this event
		//remove from eventsTreeSet all events where endDateTime is strictly before this event startDateTime
		eventEndDateTimeSortedList.removeStricklyLower(event.getStartDateTime());
		eventEndDateTimeSortedList.addElementInOrder(event.getEndDateTime());
		int concurrency = eventEndDateTimeSortedList.size();
		if(concurrency > maxConcurrency)
			maxConcurrency = concurrency;
		event.setConcurrency(concurrency);
		
		//STEP-3: check if there is a currentTimeslotStatistics
		if(timeslotDurationInSeconds > 0)
			processTimeslotStatistics(event);
			
		//STEP-3: keep current event to ensure next event is chronologically after
		previousEvent = event;
	}
	
	public boolean hasPreviousTimeslowStatistics() {
		return previousTimeslotStatistics != null;
	}
	
	public TimeslotStatistics getPreviousTimeslotStatisticsAndClear() {
		TimeslotStatistics tss = previousTimeslotStatistics;
		previousTimeslotStatistics = null;
		return tss;
	}
	
	public TimeslotStatistics getCurrentTimeslowStatistics() {
		return currentTimeslotStatistics;
	}

	public int getMaxConcurrency() {
		return maxConcurrency;
	}

}
