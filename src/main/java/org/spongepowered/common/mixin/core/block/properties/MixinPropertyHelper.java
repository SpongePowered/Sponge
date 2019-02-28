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

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyHelper;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.interfaces.block.IMixinPropertyHolder;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is retained solely for simplification not having to perform any
 * lookups to the {@link BlockTypeRegistryModule#getIdFor(IProperty)}.
 *
 * @param <T> The type of comparable
 */
@Mixin(value = PropertyHelper.class)
public abstract class MixinPropertyHelper<T extends Comparable<T>> implements BlockTrait<T>, IMixinPropertyHolder {

    // cache
    private static AtomicInteger hashId = new AtomicInteger(1);
    private final int hashCode = 92821 * hashId.getAndIncrement();
    private String idString;

    @Override
    public String getId() {
        return this.idString;
    }

    @Override
    public void setId(String id) {
        this.idString = id;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Optional<T> parseValue(String value) {
        return Optional.ofNullable((T) ((IProperty) this).parseValue(value).orNull());
    }

    /**
     * @author blood - February 28th, 2019
     * @reason Block properties are only initialized once per class so
     * we can simply check the instance and avoid checking values.
     *
     * @return True if this object equals other
     */
    @Overwrite
    public boolean equals(Object other) {
        return this == other;
    }

    /**
     * @author blood - February 28th, 2019
     * @reason Block properties are immutable so there is no reason
     * to recompute the hashCode repeatedly. To improve performance, we
     * compute the hashCode once then cache the result.
     *
     * @return The cached hashCode
     */
    @Overwrite
    public int hashCode() {
        return this.hashCode;
    }
}
