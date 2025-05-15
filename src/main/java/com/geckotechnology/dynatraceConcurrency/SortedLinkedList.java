package com.geckotechnology.dynatraceConcurrency;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

public class SortedLinkedList<E> {

	private LinkedList<E> linkedList = new LinkedList<E>();
	private Comparator<E> comparator;
	
    public SortedLinkedList(Comparator<E> comparator) {
        this.comparator = comparator;
    }
    
	public int size() {
		return linkedList.size();
	}
	
    public boolean isEmpty() {
        return linkedList.isEmpty();
    }
	
	public E getFirst() {
		return linkedList.getFirst();
	}
	
	public E removeFist() {
		return linkedList.removeFirst();
	}
	
	public void addElementInOrder(E e) {
		ListIterator<E> iterator = linkedList.listIterator();
		while(iterator.hasNext()) {
			int compare = comparator.compare(iterator.next(), e);
			if(compare == 0) {
				iterator.add(e);
				return;		
			}
			if(compare > 0) {
				iterator.previous();
				iterator.add(e);
				return;
			}
		}
		iterator.add(e);
	}
	
	public void removeStricklyLower(E e) {
		// @TODO optimize by removing until a certain element
		while(!linkedList.isEmpty()) {
			if(comparator.compare(getFirst(), e) < 0) {
				removeFist();
			}
			else break;
		}
	}
	
    @Override
    public String toString() {
        return linkedList.toString();
    }
	
}
