/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.IntFunction;

@SuppressWarnings("unchecked")
public class DelayQueueAsRunnable<E extends Delayed & Runnable>
        implements BlockingQueue<Runnable> {

    private final BlockingQueue<E> delayQueue;

    public DelayQueueAsRunnable(BlockingQueue<E> delayQueue) {
        this.delayQueue = delayQueue;
    }
    @Override
    public Runnable poll() {
        return this.delayQueue.poll();
    }

    @Override
    public boolean add(Runnable runnable) {
        return this.delayQueue.add((E) runnable);
    }

    @Override
    public boolean offer(Runnable runnable) {
        return this.delayQueue.offer((E) runnable);
    }

    @Override
    public void put(Runnable runnable) throws InterruptedException {
        this.delayQueue.put((E) runnable);
    }

    @Override
    public boolean offer(Runnable runnable, long timeout, TimeUnit unit) throws InterruptedException {
        return this.delayQueue.offer((E)runnable, timeout, unit);
    }

    @Override
    public @NotNull E take() throws InterruptedException {
        return this.delayQueue.take();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return this.delayQueue.poll(timeout, unit);
    }

    @Override
    public E remove() {
        return this.delayQueue.remove();
    }

    @Override
    public E peek() {
        return this.delayQueue.peek();
    }

    @Override
    public int size() {
        return this.delayQueue.size();
    }

    @Override
    public void clear() {
        this.delayQueue.clear();
    }

    @Override
    public int remainingCapacity() {
        return this.delayQueue.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        return this.delayQueue.remove(o);
    }

    @Override
    public E element() {
        return this.delayQueue.element();
    }

    @Override
    public boolean isEmpty() {
        return this.delayQueue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.delayQueue.contains(o);
    }


    @Override
    public int drainTo(Collection<? super Runnable> c) {
        return this.delayQueue.drainTo(c);
    }

    @Override
    public int drainTo(Collection<? super Runnable> c, int maxElements) {
        return this.delayQueue.drainTo(c, maxElements);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.delayQueue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Runnable> c) {
        Objects.requireNonNull(c);
        if (c == this)
            throw new IllegalArgumentException();
        boolean modified = false;
        for (Runnable e : c)
            if (add(e))
                modified = true;
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.delayQueue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.delayQueue.retainAll(c);
    }

    @Override
    public Object[] toArray() {
        return this.delayQueue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.delayQueue.toArray(a);
    }
    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return this.delayQueue.toArray(generator);
    }

    @Override
    public Iterator<Runnable> iterator() {
        return new Itr<>(this.delayQueue.iterator());
    }

    @Override
    public boolean equals(Object o) {
        return this.delayQueue.equals(o);
    }
    @Override
    public int hashCode() {
        return this.delayQueue.hashCode();
    }

    @Override
    public String toString() {
        return this.delayQueue.toString();
    }


    private record Itr<E extends Runnable>(
            Iterator<E> src
    ) implements Iterator<Runnable> {

        @Override
        public boolean hasNext() {
            return this.src.hasNext();
        }

        @Override
        public Runnable next() {
            return this.src.next();
        }

        @Override
        public void remove() {
            this.src.remove();
        }

        @Override
        public void forEachRemaining(Consumer<? super Runnable> action) {
            this.src.forEachRemaining(action);
        }

    }
}
