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
package org.spongepowered.common.block;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockStateMatcher;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.property.PropertyMatcher;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeBlockStateMatcher implements BlockStateMatcher {
    final BlockType type;
    final BlockTrait<?>[] traits;
    final Object[] values;
    final List<PropertyMatcher<?>> propertyMatchers;
    @Nullable private ImmutableList<BlockState> compatibleStates; // Lazily constructed

    SpongeBlockStateMatcher(BlockType type, BlockTrait<?>[] traits, Object[] values,
            List<PropertyMatcher<?>> propertyMatchers) {
        this.type = type;
        this.traits = new BlockTrait<?>[traits.length];
        this.propertyMatchers = propertyMatchers;
        System.arraycopy(traits, 0, this.traits, 0, traits.length);
        this.values = new Object[values.length];
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    private ImmutableList<BlockState> computeCompatibleStates() {
        return this.type.getAllBlockStates()
                .stream()
                .filter(this::matches)
                .collect(ImmutableList.toImmutableList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(BlockState state) {
        if (this.type != state.getType()) {
            return false;
        }
        for (int i = 0; i < this.traits.length; i++) {
            final BlockTrait<?> trait = this.traits[i];
            final Object value = this.values[i];
            final Optional<?> traitValue = state.getTraitValue(trait);
            if (!traitValue.isPresent()) { // If for any reason this fails, that means there's another problem, but alas, just in case
                return false;
            }
            final Object o = traitValue.get();
            if (!value.equals(o)) {
                return false;
            }
        }
        for (PropertyMatcher propertyMatcher : this.propertyMatchers) {
            final Object value = state.getProperty(propertyMatcher.getProperty()).orElse(null);
            if (!propertyMatcher.matches(value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<BlockState> getCompatibleStates() {
        if (this.compatibleStates == null) {
            this.compatibleStates = computeCompatibleStates();
        }
        return this.compatibleStates;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", this.type)
                .add("traits", this.traits)
                .add("values", this.values)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SpongeBlockStateMatcher that = (SpongeBlockStateMatcher) o;
        return Objects.equal(this.type, that.type)
                && Objects.equal(this.traits, that.traits)
                && Objects.equal(this.values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.type, this.traits, this.values);
    }

}
