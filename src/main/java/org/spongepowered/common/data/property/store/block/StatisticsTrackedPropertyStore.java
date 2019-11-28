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
package org.spongepowered.common.data.property.store.block;

import org.spongepowered.api.data.property.block.StatisticsTrackedProperty;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.data.property.store.common.AbstractBlockPropertyStore;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;

public class StatisticsTrackedPropertyStore extends AbstractBlockPropertyStore<StatisticsTrackedProperty> {

    private static final StatisticsTrackedProperty TRUE = new StatisticsTrackedProperty(true);
    private static final StatisticsTrackedProperty FALSE = new StatisticsTrackedProperty(false);

    public StatisticsTrackedPropertyStore() {
        super(true);
    }

    @Override
    protected Optional<StatisticsTrackedProperty> getForBlock(@Nullable Location<?> location, BlockState block) {
        return Optional.of(block.getBlock().getEnableStats() ? TRUE : FALSE);
    }

}
