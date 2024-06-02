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

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.bridge.world.entity.item.ItemEntityBridge;
import org.spongepowered.common.data.ByteToBooleanContentUpdater;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;

public final class ItemData {

    public static final Key<Value<Boolean>> INFINITE_DESPAWN_DELAY = Key.builder().key(ResourceKey.sponge("infinite_despawn_delay")).elementType(Boolean.class).build();
    public static final Key<Value<Boolean>> INFINITE_PICKUP_DELAY = Key.builder().key(ResourceKey.sponge("infinite_pickup_delay")).elementType(Boolean.class).build();

    private static final DataContentUpdater INFINITE_DELAYS_UPDATER_BYTE_TO_BOOL_FIX = new ByteToBooleanContentUpdater(1, 2, ItemData.INFINITE_PICKUP_DELAY, ItemData.INFINITE_DESPAWN_DELAY);

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
                        .get(h -> SpongeTicks.ticksOrInfinite(h.bridge$getDespawnDelay(), Constants.Entity.Item.MAGIC_NO_DESPAWN))
                        .setAnd((h, v) -> {
                            final int ticks = SpongeTicks.toSaturatedIntOrInfinite(v, Constants.Entity.Item.MAGIC_NO_DESPAWN);
                            if (!v.isInfinite() && ticks < 0) {
                                return false;
                            }
                            h.bridge$setDespawnDelay(ticks);
                            return true;
                        })
                    .create(Keys.PICKUP_DELAY)
                        .get(h -> SpongeTicks.ticksOrInfinite(h.bridge$getPickupDelay(), Constants.Entity.Item.MAGIC_NO_PICKUP))
                        .set((h, v) -> h.bridge$setPickupDelay(SpongeTicks.toSaturatedIntOrInfinite(v, Constants.Entity.Item.MAGIC_NO_PICKUP)))
                    // Only for internal use
                    .create(ItemData.INFINITE_DESPAWN_DELAY)
                        .get(h -> h.bridge$getDespawnDelay() == Constants.Entity.Item.MAGIC_NO_DESPAWN)
                        .set((h, v) -> h.bridge$setDespawnDelay(Constants.Entity.Item.MAGIC_NO_DESPAWN))
                    .create(ItemData.INFINITE_PICKUP_DELAY)
                        .get(h -> h.bridge$getPickupDelay() == Constants.Entity.Item.MAGIC_NO_PICKUP)
                        .set((h, v) -> h.bridge$setPickupDelay(Constants.Entity.Item.MAGIC_NO_PICKUP))
        ;
        final ResourceKey item = ResourceKey.sponge("item");
        registrator.spongeDataStore(item, 2, new DataContentUpdater[]{ItemData.INFINITE_DELAYS_UPDATER_BYTE_TO_BOOL_FIX}, ItemEntityBridge.class,
                ItemData.INFINITE_PICKUP_DELAY, ItemData.INFINITE_DESPAWN_DELAY);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Sponge.Entity.Item.INFINITE_PICKUP_DELAY, item, ItemData.INFINITE_PICKUP_DELAY);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Sponge.Entity.Item.INFINITE_DESPAWN_DELAY, item, ItemData.INFINITE_DESPAWN_DELAY);
    }
    // @formatter:on
}
