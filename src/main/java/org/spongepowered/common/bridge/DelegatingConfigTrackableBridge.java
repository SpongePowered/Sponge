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
package org.spongepowered.common.bridge;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Allows delegating the particular attributes, such as if blocks or entities are to be tracked,
 * to a delegating {@link TrackableBridge}. Used for types such as {@link BlockEntity} or {@link Entity}
 * that leave it up to their respective types.
 */
public interface DelegatingConfigTrackableBridge extends TrackableBridge {

    TrackableBridge bridge$trackingConfigDelegate();

    @Override
    default boolean bridge$allowsBlockBulkCaptures() {
        return this.bridge$trackingConfigDelegate().bridge$allowsBlockBulkCaptures();
    }

    @Override
    default void bridge$setAllowsBlockBulkCaptures(final boolean allowsBlockBulkCaptures) {
        this.bridge$trackingConfigDelegate().bridge$setAllowsBlockBulkCaptures(allowsBlockBulkCaptures);
    }

    @Override
    default boolean bridge$allowsBlockEventCreation() {
        return this.bridge$trackingConfigDelegate().bridge$allowsBlockEventCreation();
    }

    @Override
    default void bridge$setAllowsBlockEventCreation(final boolean allowsBlockEventCreation) {
        this.bridge$trackingConfigDelegate().bridge$setAllowsBlockEventCreation(allowsBlockEventCreation);
    }

    @Override
    default boolean bridge$allowsEntityBulkCaptures() {
        return this.bridge$trackingConfigDelegate().bridge$allowsEntityBulkCaptures();
    }

    @Override
    default void bridge$setAllowsEntityBulkCaptures(final boolean allowsEntityBulkCaptures) {
        this.bridge$trackingConfigDelegate().bridge$setAllowsEntityBulkCaptures(allowsEntityBulkCaptures);
    }

    @Override
    default boolean bridge$allowsEntityEventCreation() {
        return this.bridge$trackingConfigDelegate().bridge$allowsEntityEventCreation();
    }

    @Override
    default void bridge$setAllowsEntityEventCreation(final boolean allowsEntityEventCreation) {
        this.bridge$trackingConfigDelegate().bridge$setAllowsEntityEventCreation(allowsEntityEventCreation);
    }

    @Override
    default void bridge$refreshTrackerStates() {
        this.bridge$trackingConfigDelegate().bridge$refreshTrackerStates();
    }
}
