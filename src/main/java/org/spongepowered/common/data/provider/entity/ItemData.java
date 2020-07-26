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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.ItemEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.bridge.entity.item.ItemEntityBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.item.util.ItemStackUtil;

public final class ItemData {

    private ItemData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemEntity.class)
                    .create(Keys.ITEM_STACK_SNAPSHOT)
                        .get(h -> ItemStackUtil.snapshotOf(h.getItem()))
                        .set((h, v) -> h.setItem(ItemStackUtil.fromSnapshotToNative(v)))
                .asMutable(ItemEntityBridge.class)
                    .create(Keys.DESPAWN_DELAY)
                        .get(ItemEntityBridge::bridge$getDespawnDelay)
                        .set((h, v) -> h.bridge$setDespawnDelay(v, false))
                    .create(Keys.INFINITE_DESPAWN_DELAY)
                        .get(ItemEntityBridge::bridge$infiniteDespawnDelay)
                        .set((h, v) -> h.bridge$setDespawnDelay(h.bridge$getDespawnDelay(), v))
                    .create(Keys.INFINITE_PICKUP_DELAY)
                        .get(ItemEntityBridge::bridge$infinitePickupDelay)
                        .set((h, v) -> h.bridge$setPickupDelay(h.bridge$getPickupDelay(), v))
                    .create(Keys.PICKUP_DELAY)
                        .get(ItemEntityBridge::bridge$getPickupDelay)
                        .set((h, v) -> h.bridge$setPickupDelay(v, false));
    }
    // @formatter:on
}
