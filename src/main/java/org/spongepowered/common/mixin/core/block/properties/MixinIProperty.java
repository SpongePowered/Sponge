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
package org.spongepowered.common.mixin.core.block.properties;

import net.minecraft.state.AbstractProperty;
import net.minecraft.state.IProperty;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.util.Functional;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.registry.provider.BlockPropertyIdProvider;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * This default implements the {@link BlockTrait} methods directly
 * into {@link IProperty} such that regardless of whether a mod extends
 * {@link AbstractProperty} or only implements {@link IProperty}, we can
 * still cast back and forth between the implementation interface and
 * API interface. Of course, this is Java 8 only functionality, but
 * still, as with all other Mixin classes, this mixin class cannot be
 * directly referenced in any manner.
 *
 * @param <T> The type of comparable.
 */
@Mixin(value = IProperty.class)
@Implements(@Interface(iface = BlockTrait.class, prefix = "trait$"))
public interface MixinIProperty<T extends Comparable<T>> extends IProperty<T> {



    default String trait$getId() {
        return BlockPropertyIdProvider.getInstance().get(this).map(CatalogKey::toString)
            .orElseThrow(() -> new IllegalStateException("Unknown registration for Property: " + this));
    }

    @Intrinsic
    default String trait$getName() {
        return getName();
    }

    default Collection<T> trait$getPossibleValues() {
        return getAllowedValues();
    }

    @Intrinsic
    default Class<T> trait$getValueClass() {
        return getValueClass();
    }

    default Predicate<T> trait$getPredicate() {
        return Functional.predicateIn(getAllowedValues());
    }
}
