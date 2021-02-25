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
package org.spongepowered.common.data.provider.generic;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.entity.PlayerTracker;

public final class CreatorTrackedData {

    private CreatorTrackedData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {

        registrator.asMutable(CreatorTrackedBridge.class)
                .create(Keys.NOTIFIER)
                    .get(m -> m.tracked$getNotifierUUID().orElse(null))
                    .set((b, n) -> b.tracked$setTrackedUUID(PlayerTracker.Type.NOTIFIER, n))
                .create(Keys.CREATOR)
                    .get(m -> m.tracked$getCreatorUUID().orElse(null))
                    .set((b, n) -> b.tracked$setTrackedUUID(PlayerTracker.Type.CREATOR, n));

        final ResourceKey dataStoreKey = ResourceKey.sponge("creator_tracked");
        registrator.spongeDataStore(dataStoreKey, CreatorTrackedBridge.class, Keys.NOTIFIER, Keys.CREATOR);

        SpongeDataManager.INSTANCE.registerLegacySpongeData(PlayerTracker.Type.NOTIFIER.compoundKey, dataStoreKey, Keys.NOTIFIER);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(PlayerTracker.Type.CREATOR.compoundKey, dataStoreKey, Keys.CREATOR);
    }
    // @formatter:on
}
