package com.geckotechnology.dynatraceConcurrency;

import org.junit.jupiter.api.Test;

public class TestConcurrency {
	
	@Test
	void testLocal1() {
		String args[] = {"-events", "-timeslot", "5", "-loadDQLResultsFileName", ".\\src\\test\\resources\\sampleoutput.json"};
		Concurrency.main(args);
	}

	@Test
	void testLocal2() {
		String args[] = {"-events", "-loadDQLResultsFileName", ".\\src\\test\\resources\\sampleoutput.json"};
		Concurrency.main(args);
	}

	@Test
	void testLocal3() {
		String args[] = {"-events", "-timeslot", "5", "-field.startTime", "messages.processstarttime", "-loadDQLResultsFileName", ".\\src\\test\\resources\\sampleoutput.json", "-saveDQLResultsFileName", "\\tmp\\dqltest3.out"};
		Concurrency.main(args);
	}

	@Test
	void testLocal4() {
		String args[] = {"-events", "-timeslot", "5", "-loadDQLResultsFileName", ".\\src\\test\\resources\\sampleoutputlimited.json"};
		Concurrency.main(args);
	}

	@Test
	void testRemote1() {
		String args[] = {"-timeslot", "5", "-saveDQLResultsFileName", "\\tmp\\dqltest4.out"};
		Concurrency.main(args);
	}

}
