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
package org.spongepowered.common.bridge.entity;

import net.minecraft.entity.Entity;

/**
 * Bridge methods designed as hooks for various methods called on an {@link Entity}
 * where a platform would want to adjust logic
 */
public interface PlatformEntityBridge {

    /**
     * Called when the {@link Entity} is to be not marked as removed.
     */
    default void bridge$revive() {
        ((Entity) this).removed = false;
    }

    /**
     * Called when the {@link Entity} is to be marked to be removed.
     *
     * @param keepData Specify to the platform that it should keep any specific
     * data added to this entity when removing
     */
    default void bridge$remove(boolean keepData) {
        ((Entity) this).remove();
    }

    default boolean bridge$isFakePlayer() {
        return false;
    }
}
