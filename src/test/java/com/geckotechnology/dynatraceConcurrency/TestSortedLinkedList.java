package com.geckotechnology.dynatraceConcurrency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;

public class TestSortedLinkedList {

	@Test
	void testAdd1() {
		SortedLinkedList<Integer> sll = new SortedLinkedList<Integer>(Comparator.naturalOrder());
		sll.addElementInOrder(1);
		assertEquals(sll.toString(), "[1]");
		sll.addElementInOrder(2);
		assertEquals(sll.toString(), "[1, 2]");
		sll.addElementInOrder(0);
		assertEquals(sll.toString(), "[0, 1, 2]");
		sll.addElementInOrder(1);
		assertEquals(sll.toString(), "[0, 1, 1, 2]");
		sll.addElementInOrder(0);
		assertEquals(sll.toString(), "[0, 0, 1, 1, 2]");
		sll.addElementInOrder(2);
		assertEquals(sll.toString(), "[0, 0, 1, 1, 2, 2]");
		sll.addElementInOrder(10);
		assertEquals(sll.toString(), "[0, 0, 1, 1, 2, 2, 10]");
		sll.addElementInOrder(-1);
		assertEquals(sll.toString(), "[-1, 0, 0, 1, 1, 2, 2, 10]");
	}

	@Test
	void testAdd2() {
		SortedLinkedList<Integer> sll = new SortedLinkedList<Integer>(Comparator.naturalOrder());
		sll.addElementInOrder(1);
		assertEquals(sll.toString(), "[1]");
		sll.addElementInOrder(1);
		assertEquals(sll.toString(), "[1, 1]");
		sll.addElementInOrder(1);
		assertEquals(sll.toString(), "[1, 1, 1]");
	}

	@Test
	void testAdd3() {
		SortedLinkedList<Integer> sll = new SortedLinkedList<Integer>(Comparator.naturalOrder());
		sll.addElementInOrder(1);
		assertEquals(sll.toString(), "[1]");
		sll.addElementInOrder(3);
		assertEquals(sll.toString(), "[1, 3]");
		sll.addElementInOrder(2);
		assertEquals(sll.toString(), "[1, 2, 3]");
	}
	
	@Test
	void testRemove1() {
		SortedLinkedList<Integer> sll = new SortedLinkedList<Integer>(Comparator.naturalOrder());
		assertEquals(sll.toString(), "[]");
		sll.removeStricklyLower(0);
		assertEquals(sll.toString(), "[]");
		sll.addElementInOrder(1);
		assertEquals(sll.toString(), "[1]");
		sll.removeStricklyLower(0);
		assertEquals(sll.toString(), "[1]");
		sll.removeStricklyLower(1);
		assertEquals(sll.toString(), "[1]");
		sll.removeStricklyLower(2);
		assertEquals(sll.toString(), "[]");
	}

	@Test
	void testRemove2() {
		SortedLinkedList<Integer> sll = new SortedLinkedList<Integer>(Comparator.naturalOrder());
		sll.addElementInOrder(1);
		sll.addElementInOrder(1);
		sll.addElementInOrder(1);
		assertEquals(sll.toString(), "[1, 1, 1]");
		sll.removeStricklyLower(0);
		assertEquals(sll.toString(), "[1, 1, 1]");
		sll.removeStricklyLower(1);
		assertEquals(sll.toString(), "[1, 1, 1]");
		sll.removeStricklyLower(2);
		assertEquals(sll.toString(), "[]");
	}

	@Test
	void testRemove3() {
		SortedLinkedList<Integer> sll = new SortedLinkedList<Integer>(Comparator.naturalOrder());
		sll.addElementInOrder(1);
		sll.addElementInOrder(2);
		sll.addElementInOrder(3);
		assertEquals(sll.toString(), "[1, 2, 3]");
		sll.removeStricklyLower(0);
		assertEquals(sll.toString(), "[1, 2, 3]");
		sll.removeStricklyLower(1);
		assertEquals(sll.toString(), "[1, 2, 3]");
		sll.removeStricklyLower(2);
		assertEquals(sll.toString(), "[2, 3]");
		sll.addElementInOrder(1);
		assertEquals(sll.toString(), "[1, 2, 3]");
		sll.removeStricklyLower(3);
		assertEquals(sll.toString(), "[3]");
		sll.addElementInOrder(1);
		sll.addElementInOrder(2);
		assertEquals(sll.toString(), "[1, 2, 3]");
		sll.removeStricklyLower(4);
		assertEquals(sll.toString(), "[]");
	}
}
