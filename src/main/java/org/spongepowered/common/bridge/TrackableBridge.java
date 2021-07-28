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
import org.spongepowered.common.config.tracker.TrackerConfig;

public interface TrackableBridge {

    /**
     * Gets whether this entity has been added to a World's tracked entity lists
     * @return True if this entity is being tracked in a world's chunk lists.
     */
    default boolean bridge$isWorldTracked() {
        return false;
    }

    /**
     * Sets an entity to be tracked or untracked. Specifically used in
     * Level#add(Entity) and
     * {@link net.minecraft.server.level.ServerLevel#onEntityRemoved(Entity)}
     *
     * @param tracked Tracked
     */
    default void bridge$setWorldTracked(boolean tracked) {
    }

    default boolean bridge$shouldTick() {
        return true;
    }

    boolean bridge$allowsBlockBulkCaptures();

    void bridge$setAllowsBlockBulkCaptures(boolean allowsBlockBulkCaptures);

    boolean bridge$allowsBlockEventCreation();

    void bridge$setAllowsBlockEventCreation(boolean allowsBlockEventCreation);

    boolean bridge$allowsEntityBulkCaptures();

    void bridge$setAllowsEntityBulkCaptures(boolean allowsEntityBulkCaptures);

    boolean bridge$allowsEntityEventCreation();

    void bridge$setAllowsEntityEventCreation(boolean allowsEntityEventCreation);

    /**
     * Tells this trackable to refresh it's tracker states.
     * Usually these states are only needing to be refreshed
     * when the {@link TrackerConfig}.
     */
    default void bridge$refreshTrackerStates() {
    }
}
