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
package org.spongepowered.common.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@FunctionalInterface
public interface PairStream<K,V> {
    static <K,V> PairStream<K,V> from(Map<K,V> map) {
        return from(map.entrySet().stream());
    }
    static <K,V> PairStream<K,V> from(Stream<Map.Entry<K,V>> s) {
        return ()->s;
    }
    static <K,V> PairStream<K,V> from(Stream<K> s, Function<? super K, ? extends V> f) {
        return ()->s.map(k->new AbstractMap.SimpleImmutableEntry<>(k, f.apply(k)));
    }

    default PairStream<K,V> distinct() {
        return from(this.entries().distinct());
    }
    default PairStream<K,V> peek(BiConsumer<? super K, ? super V> action) {
        return from(this.entries().peek(e->action.accept(e.getKey(), e.getValue())));
    }
    default PairStream<K,V> skip(long n) {
        return from(this.entries().skip(n));
    }
    default PairStream<K,V> limit(long maxSize) {
        return from(this.entries().limit(maxSize));
    }
    default PairStream<K,V> filterKey(Predicate<? super K> mapper) {
        return from(this.entries().filter(e->mapper.test(e.getKey())));
    }
    default PairStream<K,V> filterValue(Predicate<? super V> mapper) {
        return from(this.entries().filter(e->mapper.test(e.getValue())));
    }
    default PairStream<K,V> filter(BiPredicate<? super K, ? super V> mapper) {
        return from(this.entries().filter(e->mapper.test(e.getKey(), e.getValue())));
    }
    default <R> PairStream<R,V> mapKey(Function<? super K,? extends R> mapper) {
        return from(this.entries().map(e->new AbstractMap.SimpleImmutableEntry<>(
            mapper.apply(e.getKey()), e.getValue()
        )));
    }
    default <R> PairStream<K,R> mapValue(Function<? super V,? extends R> mapper) {
        return from(this.entries().map(e->new AbstractMap.SimpleImmutableEntry<>(
            e.getKey(), mapper.apply(e.getValue())
        )));
    }
    default <R> Stream<R> map(BiFunction<? super K, ? super V,? extends R> mapper) {
        return this.entries().map(e->mapper.apply(e.getKey(), e.getValue()));
    }
    default DoubleStream mapToDouble(ToDoubleBiFunction<? super K, ? super V> mapper) {
        return this.entries().mapToDouble(e->mapper.applyAsDouble(e.getKey(), e.getValue()));
    }
    default IntStream mapToInt(ToIntBiFunction<? super K, ? super V> mapper) {
        return this.entries().mapToInt(e->mapper.applyAsInt(e.getKey(), e.getValue()));
    }
    default LongStream mapToLong(ToLongBiFunction<? super K, ? super V> mapper) {
        return this.entries().mapToLong(e->mapper.applyAsLong(e.getKey(), e.getValue()));
    }
    default <RK,RV> PairStream<RK,RV> flatMap(
        BiFunction<? super K, ? super V,? extends PairStream<RK,RV>> mapper) {
        return from(this.entries().flatMap(
            e->mapper.apply(e.getKey(), e.getValue()).entries()));
    }
    default <R> Stream<R> flatMapToObj(
        BiFunction<? super K, ? super V,? extends Stream<R>> mapper) {
        return this.entries().flatMap(e->mapper.apply(e.getKey(), e.getValue()));
    }
    default DoubleStream flatMapToDouble(
        BiFunction<? super K, ? super V,? extends DoubleStream> mapper) {
        return this.entries().flatMapToDouble(e->mapper.apply(e.getKey(), e.getValue()));
    }
    default IntStream flatMapToInt(
        BiFunction<? super K, ? super V,? extends IntStream> mapper) {
        return this.entries().flatMapToInt(e->mapper.apply(e.getKey(), e.getValue()));
    }
    default LongStream flatMapToLong(
        BiFunction<? super K, ? super V,? extends LongStream> mapper) {
        return this.entries().flatMapToLong(e->mapper.apply(e.getKey(), e.getValue()));
    }
    default PairStream<K,V> sortedByKey(Comparator<? super K> comparator) {
        return from(this.entries().sorted(Map.Entry.comparingByKey(comparator)));
    }
    default PairStream<K,V> sortedByValue(Comparator<? super V> comparator) {
        return from(this.entries().sorted(Map.Entry.comparingByValue(comparator)));
    }

    default boolean allMatch(BiPredicate<? super K,? super V> predicate) {
        return this.entries().allMatch(e->predicate.test(e.getKey(), e.getValue()));
    }
    default boolean anyMatch(BiPredicate<? super K,? super V> predicate) {
        return this.entries().anyMatch(e->predicate.test(e.getKey(), e.getValue()));
    }
    default boolean noneMatch(BiPredicate<? super K,? super V> predicate) {
        return this.entries().noneMatch(e->predicate.test(e.getKey(), e.getValue()));
    }
    default long count() {
        return this.entries().count();
    }

    Stream<Map.Entry<K,V>> entries();
    default Stream<K> keys() {
        return this.entries().map(Map.Entry::getKey);
    }
    default Stream<V> values() {
        return this.entries().map(Map.Entry::getValue);
    }
    default Optional<Map.Entry<K,V>> maxByKey(Comparator<? super K> comparator) {
        return this.entries().max(Map.Entry.comparingByKey(comparator));
    }
    default Optional<Map.Entry<K,V>> maxByValue(Comparator<? super V> comparator) {
        return this.entries().max(Map.Entry.comparingByValue(comparator));
    }
    default Optional<Map.Entry<K,V>> minByKey(Comparator<? super K> comparator) {
        return this.entries().min(Map.Entry.comparingByKey(comparator));
    }
    default Optional<Map.Entry<K,V>> minByValue(Comparator<? super V> comparator) {
        return this.entries().min(Map.Entry.comparingByValue(comparator));
    }
    default void forEach(BiConsumer<? super K, ? super V> action) {
        this.entries().forEach(e->action.accept(e.getKey(), e.getValue()));
    }
    default void forEachOrdered(BiConsumer<? super K, ? super V> action) {
        this.entries().forEachOrdered(e->action.accept(e.getKey(), e.getValue()));
    }

    default Map<K,V> toMap() {
        return this.entries().collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    default Map<K,V> toMap(BinaryOperator<V> valAccum) {
        return this.entries().collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, valAccum));
    }
}