// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Implements a {@code List<T>} with the express purpose of reuse,
 * meaning there is no new memory allocation when you need to wrap
 * a {@code T[]} with a {@code List<T>} and access via {@code Iterator<T>}.
 * This abstraction is not meant for general purpose use because it is
 * built for one special use case, to make read-only access to {@code List<T>}
 * as fast as possible and produce very little garbage. Specifically this
 * fits use within {@code Actor} implementations while handling a single
 * message delivery. All other uses are at your own risk.
 * 
 * @param <T> the type of elements
 */
public class ResettableReadOnlyList<T> implements List<T> {
  private T[] all;
  private final ResettableReadOnlyIterator iterator;
  private final T[] one;

  @SuppressWarnings("unchecked")
  public ResettableReadOnlyList() {
    this.one = (T[]) new Object[1];
    this.iterator = new ResettableReadOnlyIterator();
  }

  @SuppressWarnings("unchecked")
  public <E> List<E> asList() {
    return (List<E>) this;
  }

  public void wrap(final T[] all) {
    this.all = all;
    this.iterator.current = 0;
  }

  public void wrap(final T all) {
    one[0] = all;
    this.all = one;
    this.iterator.current = 0;
  }

  @Override
  public int size() {
    return all.length;
  }

  @Override
  public boolean isEmpty() {
    return all.length == 0;
  }

  @Override
  public boolean contains(final Object o) {
    throw new UnsupportedOperationException("Must override");
  }

  @Override
  public Iterator<T> iterator() {
    this.iterator.current = 0;
    return iterator;
  }

  @Override
  public Object[] toArray() {
    return all;
  }

  @Override
  public <A> A[] toArray(final A[] a) {
    System.arraycopy(all, 0, a, 0, Math.min(all.length, a.length));
    return a;
  }

  @Override
  public boolean add(final T e) {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    throw new UnsupportedOperationException("Must override");
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public boolean addAll(final int index, final Collection<? extends T> c) {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public T get(final int index) {
    return all[index];
  }

  @Override
  public T set(final int index, final T element) {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public void add(final int index, final T element) {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public T remove(final int index) {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public int indexOf(final Object o) {
    throw new UnsupportedOperationException("Must override");
  }

  @Override
  public int lastIndexOf(final Object o) {
    throw new UnsupportedOperationException("Must override");
  }

  @Override
  public ListIterator<T> listIterator() {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public ListIterator<T> listIterator(final int index) {
    throw new UnsupportedOperationException("Read-only access");
  }

  @Override
  public List<T> subList(final int fromIndex, final int toIndex) {
    throw new UnsupportedOperationException("Must override");
  }

  private final class ResettableReadOnlyIterator implements Iterator<T> {
    private int current;

    @Override
    public boolean hasNext() {
      return current < size();
    }

    @Override
    public T next() {
      return get(current++);
    }

    private ResettableReadOnlyIterator() {
      this.current = 0;
    }
  }
}
