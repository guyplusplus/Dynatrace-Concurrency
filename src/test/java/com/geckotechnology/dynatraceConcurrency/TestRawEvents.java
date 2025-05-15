package com.geckotechnology.dynatraceConcurrency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TestRawEvents {

	/**
	 * 1 ----------
	 * 2  ----------
	 * 3   --
	 * 4       ----------
	 * 5             -----
	 * 6             ------
	 * 7                    ---
	 * 8                       --
	 * 9                        -
	 *10                         -
	 *11                           --
	 */
	@Test
	void testLongTimeSlot() {
		ConcurrencyStateEngine engine = new ConcurrencyStateEngine(1000000);
		ZonedDateTime zdt = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneId.of("GMT+2"));
		
		Event event = null;
		
		// event 1
		event = new Event(zdt, 10000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 1);
		
		// event 2
		zdt = zdt.plusSeconds(1);
		event = new Event(zdt, 10000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 2);

		// event 3
		zdt = zdt.plusSeconds(1);
		event = new Event(zdt, 2000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 3);

		// event 4
		zdt = zdt.plusSeconds(4);
		event = new Event(zdt, 10000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 3);

		// event 5
		zdt = zdt.plusSeconds(6);
		event = new Event(zdt, 5000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 2);

		// event 6
		event = new Event(zdt, 6000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 3);

		// event 7
		zdt = zdt.plusSeconds(7);
		event = new Event(zdt, 3000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 1);

		// event 8
		zdt = zdt.plusSeconds(3);
		event = new Event(zdt, 2000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 2);

		// event 9
		zdt = zdt.plusSeconds(1);
		event = new Event(zdt, 1000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 2);

		// event 10
		zdt = zdt.plusSeconds(1);
		event = new Event(zdt, 1000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 3);

		// event 11
		zdt = zdt.plusSeconds(2);
		event = new Event(zdt, 2000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 1);
		
		//check TimeslowStatistics
		assertEquals(engine.hasPreviousTimeslowStatistics(), false);
		assertEquals(engine.getMaxConcurrency(), 3);
		assertEquals(engine.getCurrentTimeslowStatistics().getEventCount(), 11);
		assertEquals(engine.getCurrentTimeslowStatistics().getMaxConcurrency(), 3);
	}

	/**
	 * S +               +
	 * 1 ----------
	 * 2  ----------
	 * 3   --
	 * 4       ----------
	 * 5             -----
	 * 6             ------
	 *&6             ------
	 * S +               +
	 * 7                    ---
	 * 8                       --
	 * 9                        -
	 *10                         -
	 *11                           --
	 * S +               +
	 */
	@Test
	void testTimeSlot15s() {
		ConcurrencyStateEngine engine = new ConcurrencyStateEngine(15);
		ZonedDateTime zdt = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneId.of("GMT+2"));
		
		Event event = null;
		
		// event 1
		event = new Event(zdt, 10000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 1);
		
		// event 2
		zdt = zdt.plusSeconds(1);
		event = new Event(zdt, 10000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 2);

		// event 3
		zdt = zdt.plusSeconds(1);
		event = new Event(zdt, 2000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 3);

		// event 4
		zdt = zdt.plusSeconds(4);
		event = new Event(zdt, 10000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 3);

		// event 5
		zdt = zdt.plusSeconds(6);
		event = new Event(zdt, 5000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 2);

		// event 6
		event = new Event(zdt, 6000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 3);
		
		// event 6 again
		event = new Event(zdt, 6000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 4);
		
		assertEquals(engine.hasPreviousTimeslowStatistics(), false);

		// event 7
		zdt = zdt.plusSeconds(7);
		event = new Event(zdt, 3000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 1);
		
		assertEquals(engine.hasPreviousTimeslowStatistics(), true);
		TimeslotStatistics stats = engine.getPreviousTimeslotStatisticsAndClear();
		assertEquals(engine.hasPreviousTimeslowStatistics(), false);
		assertEquals(stats.getEventCount(), 7);
		assertEquals(stats.getMaxConcurrency(), 4);

		// event 8
		zdt = zdt.plusSeconds(3);
		event = new Event(zdt, 2000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 2);

		// event 9
		zdt = zdt.plusSeconds(1);
		event = new Event(zdt, 1000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 2);

		// event 10
		zdt = zdt.plusSeconds(1);
		event = new Event(zdt, 1000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 3);

		// event 11
		zdt = zdt.plusSeconds(2);
		event = new Event(zdt, 2000);
		engine.receiveEvent(event);
		assertEquals(event.getConcurrency(), 1);
		
		//check TimeslowStatistics
		assertEquals(engine.hasPreviousTimeslowStatistics(), false);
		assertEquals(engine.getMaxConcurrency(), 4);
		assertEquals(engine.getCurrentTimeslowStatistics().getEventCount(), 5);
		assertEquals(engine.getCurrentTimeslowStatistics().getMaxConcurrency(), 3);
	}
}
