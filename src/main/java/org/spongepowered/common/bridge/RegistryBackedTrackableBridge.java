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

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.common.config.tracker.NamespacedCategory;
import org.spongepowered.common.config.tracker.TrackerCategory;

public interface RegistryBackedTrackableBridge<T> extends TrackableBridge {

    TrackerCategory bridge$trackerCategory();

    Registry<T> bridge$trackerRegistryBacking();

    void bridge$saveTrackerConfig();

    default void bridge$refreshTrackerStates() {
        final ResourceLocation key = this.bridge$trackerRegistryBacking().getKey((T) (Object) this);
        final String namespace = key.getNamespace();
        final String path = key.getPath();
        final NamespacedCategory namespacedCategory = this.bridge$trackerCategory().namespacedOrCreate(namespace);
        final NamespacedCategory.ValueCategory valueCategory = namespacedCategory.valueOrCreate(path);

        if (!namespacedCategory.enabled()) {
            this.bridge$setAllowsBlockBulkCaptures(false);
            this.bridge$setAllowsBlockEventCreation(false);
            this.bridge$setAllowsEntityBulkCaptures(false);
            this.bridge$setAllowsEntityEventCreation(false);
            valueCategory.setAllowBlockEvents(false);
            valueCategory.setAllowEntityEvents(false);
            valueCategory.setCaptureBlocksInBulk(false);
            valueCategory.setCaptureEntitiesInBulk(false);
        } else {
            this.bridge$setAllowsBlockEventCreation(valueCategory.allowsBlockEvents());
            this.bridge$setAllowsEntityEventCreation(valueCategory.allowsEntityEvents());
            this.bridge$setAllowsBlockBulkCaptures(valueCategory.capturesBlocksInBulk());
            this.bridge$setAllowsEntityBulkCaptures(valueCategory.capturesEntitiesInBulk());
        }

        if (namespacedCategory.enabled() && this.bridge$trackerCategory().autoPopulate()) {
            this.bridge$saveTrackerConfig();
        }
    }
}
