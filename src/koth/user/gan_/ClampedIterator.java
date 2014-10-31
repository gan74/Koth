package koth.user.gan_;

import java.util.Iterator;

public class ClampedIterator<E> implements Iterator<E> {
	private Iterator<E> iterator;
	private int clamp;
	private int index;
	
	public ClampedIterator(Iterator<E> it, int cl) {
		iterator = it;
		clamp = cl;
	}

	@Override
	public boolean hasNext() {
		return index < clamp && iterator.hasNext();
	}

	@Override
	public E next() {
		index++;
		return iterator.next();
	}

	@Override
	public void remove() {
		iterator.remove();
	}

}
