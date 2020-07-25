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
package org.spongepowered.common.command.selector;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.selector.SelectorSortAlgorithm;

import java.util.List;
import java.util.function.BiConsumer;

public final class SpongeSelectorSortAlgorithm implements SelectorSortAlgorithm {

    private final BiConsumer<Vec3d, List<? extends Entity>> sortAlgorithm;
    private final ResourceKey resourceKey;

    public SpongeSelectorSortAlgorithm(
            final BiConsumer<Vec3d, List<? extends Entity>> sortAlgorithm, final ResourceKey resourceKey) {
        this.sortAlgorithm = sortAlgorithm;
        this.resourceKey = resourceKey;
    }

    @Override
    public final ResourceKey getKey() {
        return this.resourceKey;
    }

    public final BiConsumer<Vec3d, List<? extends Entity>> getSortAlgorithm() {
        return this.sortAlgorithm;
    }

}
