/*
 * Copyright 2012 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.queues;


import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public final class OneToOneConcurrentArrayQueue<E>
implements Queue<E>
{
    private final int capacity;
    private final E[] buffer;

    private volatile long tail = 0;
    private volatile long head = 0;

    @SuppressWarnings("unchecked")
    public OneToOneConcurrentArrayQueue(final int capacity)
    {
        this.capacity = capacity;
        buffer = (E[])new Object[this.capacity];
    }

    public boolean add(final E e)
    {
        if (offer(e))
        {
            return true;
        }

        throw new IllegalStateException("Queue is full");
    }

    public boolean offer(final E e)
    {
        if (null == e)
        {
            throw new NullPointerException("Null is not a valid element");
        }

        final long currentTail = tail;
        final long wrapPoint = currentTail - capacity;
        if (head <= wrapPoint)
        {
            return false;
        }

        buffer[(int)(currentTail % buffer.length)] = e;
        tail = currentTail + 1;

        return true;
    }

    public E poll()
    {
        final long currentHead = head;
        if (currentHead >= tail)
        {
            return null;
        }

        final E element = removeSequence(currentHead);
        head = currentHead + 1;

        return element;
    }

    public E remove()
    {
        final E e = poll();
        if (null == e)
        {
            throw new NoSuchElementException("Queue is empty");
        }

        return e;
    }

    public E element()
    {
        final E e = peek();
        if (null == e)
        {
            throw new NoSuchElementException("Queue is empty");
        }

        return e;
    }

    public E peek()
    {
        return buffer[(int)(head % buffer.length)];
    }

    public int size()
    {
        return (int)(tail - head);
    }

    public boolean isEmpty()
    {
        return tail == head;
    }

    public boolean contains(final Object o)
    {
        if (null == o)
        {
            return false;
        }

        for (long i = head, limit = tail + 1; i < limit; i++)
        {
            final E e = buffer[(int)(i % buffer.length)];
            if (o.equals(e))
            {
                return true;
            }
        }

        return false;
    }

    public Iterator<E> iterator()
    {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray()
    {
        throw new UnsupportedOperationException();
    }

    public <T> T[] toArray(final T[] a)
    {
        throw new UnsupportedOperationException();
    }

    public boolean remove(final Object o)
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(final Collection<?> c)
    {
        for (Object o : c)
        {
            if (!contains(o))
            {
                return false;
            }
        }

        return true;
    }

    public boolean addAll(final Collection<? extends E> c)
    {
        for (E o : c)
        {
            add(o);
        }

        return true;
    }

    public boolean removeAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {
        Object value;
        do
        {
            value = poll();
        }
        while (null != value);
    }

    private E removeSequence(final long sequence)
    {
        final int removeIndex = (int)(sequence % buffer.length);
        final E element = buffer[removeIndex];
        buffer[removeIndex] = null;

        return element;
    }
}
