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
package org.spongepowered.common.data.manipulator.immutable.common;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.util.DataUtil;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * So, considering this is the root of the immutable variants of
 * {@link DataManipulator}, otherwise known as {@link ImmutableDataManipulator}s.
 * The advantage of these types of {@link DataManipulator}s is that they can not be
 * mutated once created. In other words, it's safe to pass around these immutable
 * variants across threads without worry of the underlying values being changed.
 *
 * It may be possible that some commonly used {@link ImmutableDataManipulator}s
 * may be cached for better performance when processing obtaining new
 * {@link ImmutableDataManipulator}s with different values.
 *
 * <p>Note: It is ABSOLUTELY REQUIRED to {@link #registerKeyValue(Key, Supplier)}
 * and {@link #registerFieldGetter(Key, Supplier)} for all possible
 * {@link Key}s and {@link Value}s the {@link DataManipulator} may provide as
 * all of the implementation methods provided here are handled using those. This
 * was done to avoid having to override {@link #getKeys()} and {@link #getValues()},
 * let alone using {@link ValueProcessor}s for simple setters and getters. I believe
 * this to be faster than having to retrieve a processor from a map, checking if
 * the class is an instance of the type implementation to access the setters
 * and getters.</p>
 *
 * @param <I> The immutable data manipulator type
 * @param <M> The mutable manipulator type
 */
@SuppressWarnings("unchecked")
public abstract class AbstractImmutableData<I extends ImmutableDataManipulator<I, M>, M extends DataManipulator<M, I>> implements ImmutableDataManipulator<I, M> {

    private final Class<I> immutableClass;

    // Ok, so, you're probably asking "Why the hell are you doing this type of hackery?"
    // Answer: Because I'd rather have these abstract functions (read method references)
    // to get and set field values according to the key, and get values based on key
    // instead of using a Value/Data Processor. The advantage of course is that
    // in Java 8, this would all be done with lambda expressions, but since we
    // target Java 6, we can't quite do that... so, this is the compromise.
    //
    // There was a possibility for using annotations, but I (gabizou) decided against
    // it since there's a lot of magic that goes on with it, and there is very little
    // customization when it comes to the method calls for setting/getting the values.
    // The largest issue was implementation. Since most fields are simple to get and
    // set, other values, such as ItemStacks require a bit of finer tuning.
    //
    private final Map<Key<?>, Supplier<ImmutableValue<?>>> keyValueMap = new IdentityHashMap<>();
    private final Map<Key<?>, Supplier<?>> keyFieldGetterMap = new IdentityHashMap<>();

    protected AbstractImmutableData(Class<I> immutableClass) {
        this.immutableClass = checkNotNull(immutableClass);
    }

    /**
     * Simple registration method for the keys to value return methods.
     *
     * <p>Note that this is still going to be usable, but will be made simpler
     * when Java 8 is used, as lambda expressions can reference methods. The
     * update won't actually change these registration methods, but the
     * {@link DataManipulator}s calling these registration methods will
     * become single line simplifications.</p>
     *
     * @param key The key for the value return type
     * @param function The function for getting the value
     */
    protected final void registerKeyValue(Key<?> key, Supplier<ImmutableValue<?>> function) {
        this.keyValueMap.put(checkNotNull(key), checkNotNull(function));
    }

    /**
     * Simple registration method for the keys to field getter methods.
     *
     * <p>Note that this is still going to be usable, but will be made simpler
     * when Java 8 is used, as lambda expressions can reference methods. The
     * update won't actually change these registration methods, but the
     * {@link DataManipulator}s calling these registration methods will
     * become single line simplifications.</p>
     *
     * @param key The key for the value return type
     * @param function The function for getting the field
     */
    protected final void registerFieldGetter(Key<?> key, Supplier<?> function) {
        this.keyFieldGetterMap.put(checkNotNull(key), checkNotNull(function));
    }

    protected abstract void registerGetters();

    @Override
    public <E> Optional<I> with(Key<? extends BaseValue<E>> key, E value) {
        // Basic stuff, getting the processor....
        final Optional<DataProcessor<M, I>> processor = DataUtil.getImmutableProcessor(this.immutableClass);
        // We actually need to check that the processor is available, otherwise
        // we throw an IllegalArgumentException, because people don't check for
        // support!!
        checkArgument(processor.isPresent(), "Invalid Key for " + this.immutableClass.getCanonicalName() + ". Use supports(Key) to avoid "
                + "exceptions!");
        // Then we pass it to the processor :)
        return processor.get().with(checkNotNull(key), checkNotNull(value), (I) this);
    }

    @Override
    public final I copy() {
        return (I) this;
    }

    // Beyond this point is involving keyFieldGetters or keyValueGetters. No external
    // implementation required.

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        if (!supports(key)) {
            return Optional.empty();
        }
        return Optional.of((E) this.keyFieldGetterMap.get(key).get());
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        if (!this.keyValueMap.containsKey(key)) {
            return Optional.empty();
        }
        return Optional.of((V) checkNotNull(this.keyValueMap.get(key).get()));
    }

    @Override
    public boolean supports(Key<?> key) {
        return this.keyFieldGetterMap.containsKey(checkNotNull(key));
    }

    @Override
    public Set<Key<?>> getKeys() {
        return ImmutableSet.copyOf(this.keyValueMap.keySet());
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        ImmutableSet.Builder<ImmutableValue<?>> builder = ImmutableSet.builder();
        for (Supplier<ImmutableValue<?>> function : this.keyValueMap.values()) {
            builder.add(checkNotNull(function.get()));
        }
        return builder.build();
    }

    // Then finally traditional java stuff.

    @Override
    public int hashCode() {
        List<Object> objects = Lists.newArrayList();
        objects.addAll(this.keyFieldGetterMap.values().stream().map(Supplier::get).collect(Collectors.toList()));
        return Objects.hashCode(this.immutableClass, objects);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AbstractImmutableData other = (AbstractImmutableData) obj;
        return Objects.equal(this.immutableClass, other.immutableClass);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, getContentVersion());
    }
}
