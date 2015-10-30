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
package org.spongepowered.common.data.value.immutable;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutablePatternListValue;
import org.spongepowered.api.data.value.mutable.PatternListValue;
import org.spongepowered.common.data.meta.SpongePatternLayer;
import org.spongepowered.common.data.value.mutable.SpongePatternListValue;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class ImmutableSpongePatternListValue extends ImmutableSpongeListValue<PatternLayer> implements ImmutablePatternListValue {

    public ImmutableSpongePatternListValue(Key<? extends BaseValue<List<PatternLayer>>> key, List<PatternLayer> actualValue) {
        super(key, ImmutableList.copyOf(actualValue));
    }

    @Override
    public ImmutablePatternListValue with(List<PatternLayer> value) {
        return new ImmutableSpongePatternListValue(getKey(), ImmutableList.copyOf(checkNotNull(value)));
    }

    @Override
    public ImmutablePatternListValue transform(Function<List<PatternLayer>, List<PatternLayer>> function) {
        return new ImmutableSpongePatternListValue(getKey(), ImmutableList.copyOf(checkNotNull(checkNotNull(function).apply(this.actualValue))));
    }

    @Override
    public PatternListValue asMutable() {
        final List<PatternLayer> list = Lists.newArrayList();
        list.addAll(this.actualValue);
        return new SpongePatternListValue(getKey(), list);
    }

    @Override
    public ImmutablePatternListValue withElement(PatternLayer elements) {
        return new ImmutableSpongePatternListValue(getKey(), ImmutableList.<PatternLayer>builder().addAll(this.actualValue).add(elements).build());
    }

    @Override
    public ImmutablePatternListValue withAll(Iterable<PatternLayer> elements) {
        return new ImmutableSpongePatternListValue(getKey(), ImmutableList.<PatternLayer>builder().addAll(this.actualValue).addAll(elements).build());
    }

    @Override
    public ImmutablePatternListValue without(PatternLayer element) {
        final ImmutableList.Builder<PatternLayer> builder = ImmutableList.builder();
        this.actualValue.stream().filter(existingElement -> !existingElement.equals(element)).forEach(builder::add);
        return new ImmutableSpongePatternListValue(getKey(), builder.build());

    }

    @Override
    public ImmutablePatternListValue withoutAll(Iterable<PatternLayer> elements) {
        final ImmutableList.Builder<PatternLayer> builder = ImmutableList.builder();
        this.actualValue.stream().filter(existingElement -> !Iterables.contains(elements, existingElement)).forEach(builder::add);
        return new ImmutableSpongePatternListValue(getKey(), builder.build());
    }

    @Override
    public ImmutablePatternListValue withoutAll(Predicate<PatternLayer> predicate) {
        final ImmutableList.Builder<PatternLayer> builder = ImmutableList.builder();
        this.actualValue.stream().filter(existing -> checkNotNull(predicate).test(existing)).forEach(builder::add);
        return new ImmutableSpongePatternListValue(getKey(), builder.build());
    }

    @Override
    public ImmutablePatternListValue with(int index, PatternLayer value) {
        final ImmutableList.Builder<PatternLayer> builder = ImmutableList.builder();
        for (final ListIterator<PatternLayer> iterator = this.actualValue.listIterator(); iterator.hasNext(); ) {
            if (iterator.nextIndex() - 1 == index) {
                builder.add(checkNotNull(value));
                iterator.next();
            } else {
                builder.add(iterator.next());
            }
        }
        return new ImmutableSpongePatternListValue(getKey(), builder.build());
    }

    @Override
    public ImmutablePatternListValue with(int index, Iterable<PatternLayer> values) {
        final ImmutableList.Builder<PatternLayer> builder = ImmutableList.builder();
        for (final ListIterator<PatternLayer> iterator = this.actualValue.listIterator(); iterator.hasNext(); ) {
            if (iterator.nextIndex() -1 == index) {
                builder.addAll(values);
            }
            builder.add(iterator.next());
        }
        return new ImmutableSpongePatternListValue(getKey(), builder.build());
    }

    @Override
    public ImmutablePatternListValue without(int index) {
        final ImmutableList.Builder<PatternLayer> builder = ImmutableList.builder();
        for (final ListIterator<PatternLayer> iterator = this.actualValue.listIterator(); iterator.hasNext(); ) {
            if (iterator.nextIndex() - 1 != index) {
                builder.add(iterator.next());
            }
        }
        return new ImmutableSpongePatternListValue(getKey(), builder.build());
    }

    @Override
    public ImmutablePatternListValue set(int index, PatternLayer element) {
        final ImmutableList.Builder<PatternLayer> builder = ImmutableList.builder();
        for (final ListIterator<PatternLayer> iterator = this.actualValue.listIterator(); iterator.hasNext(); ) {
            if (iterator.nextIndex() -1 == index) {
                builder.add(checkNotNull(element));
                iterator.next();
            } else {
                builder.add(iterator.next());
            }
        }
        return new ImmutableSpongePatternListValue(getKey(), builder.build());
   }

    @Override
    public ImmutablePatternListValue with(BannerPatternShape patternShape, DyeColor color) {
        return withElement(new SpongePatternLayer(patternShape, color));
    }

    @Override
    public ImmutablePatternListValue with(int index, BannerPatternShape patternShape, DyeColor color) {
        return with(index, new SpongePatternLayer(patternShape, color));
    }

    @Override
    public ImmutablePatternListValue set(int index, BannerPatternShape patternShape, DyeColor color) {
        return set(index, new SpongePatternLayer(patternShape, color));
    }
}
