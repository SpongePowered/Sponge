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
package org.spongepowered.common.statistic;

import net.minecraft.stats.StatCrafting;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.common.bridge.stats.StatBaseBridge;
import org.spongepowered.common.mixin.core.stats.StatBaseMixin;

import java.text.NumberFormat;

/**
 * Specifically a default implemented bridge for Sponge added statistics
 * base classes that extend {@link StatBase} and {@link StatCrafting} but
 * cannot specifically reference the implementation provided by
 * {@link StatBaseBridge} and therefor, {@link StatBaseMixin}. It is the
 * premise that the bridge methods are implemented appropriately, and
 * handled the same way as if the statistics were implemented on the
 * classes themselves.
 */
public interface SpongeStatistic extends Statistic {

    @Override
    default String getId() {
        return ((StatBaseBridge) this).bridge$getUnderlyingId();
    }

    @Override
    default NumberFormat getFormat() {
        return ((StatBaseBridge) this).bridge$getFormat();
    }

}
