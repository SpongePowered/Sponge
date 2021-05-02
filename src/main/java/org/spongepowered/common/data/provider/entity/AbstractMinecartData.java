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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.common.bridge.world.entity.vehicle.AbstractMinecartBridge;
import org.spongepowered.common.data.ByteToBooleanContentUpdater;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

public final class AbstractMinecartData {

    private AbstractMinecartData() {
    }

    private static final DataContentUpdater MINECART_UPDATER_BYTE_TO_BOOL_FIX = new ByteToBooleanContentUpdater(1, 2, Keys.SLOWS_UNOCCUPIED);

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(AbstractMinecart.class)
                    .create(Keys.BLOCK_STATE)
                        .get(h -> h.hasCustomDisplay() ? (BlockState) h.getDisplayBlockState() : null)
                        .set((h, v) -> h.setDisplayBlockState((net.minecraft.world.level.block.state.BlockState) v))
                        .delete(h -> h.setCustomDisplay(false))
                    .create(Keys.IS_ON_RAIL)
                        .get(h -> {
                            final BlockPos pos = h.blockPosition();
                            if (h.level.getBlockState(pos).is(BlockTags.RAILS)) {
                                return true;
                            }
                            final BlockPos posBelow = pos.offset(0, -1, 0);
                            return h.level.getBlockState(posBelow).is(BlockTags.RAILS);
                        })
                    .create(Keys.MINECART_BLOCK_OFFSET)
                        .get(AbstractMinecart::getDisplayOffset)
                        .setAnd(AbstractMinecartData::setBlockOffset)
                        .deleteAnd(h -> AbstractMinecartData.setBlockOffset(h, h.getDefaultDisplayOffset()))
                .asMutable(AbstractMinecartBridge.class)
                    .create(Keys.AIRBORNE_VELOCITY_MODIFIER)
                        .get(AbstractMinecartBridge::bridge$getAirborneMod)
                        .set(AbstractMinecartBridge::bridge$setAirborneMod)
                    .create(Keys.SLOWS_UNOCCUPIED)
                        .get(AbstractMinecartBridge::bridge$getSlowWhenEmpty)
                        .set(AbstractMinecartBridge::bridge$setSlowWhenEmpty)
                    .create(Keys.DERAILED_VELOCITY_MODIFIER)
                        .get(AbstractMinecartBridge::bridge$getDerailedMod)
                        .set(AbstractMinecartBridge::bridge$setDerailedMod)
                    .create(Keys.POTENTIAL_MAX_SPEED)
                        .get(AbstractMinecartBridge::bridge$getMaxSpeed)
                        .set(AbstractMinecartBridge::bridge$setMaxSpeed)
                    ;
        final ResourceKey minecartDataStoreKey = ResourceKey.sponge("minecart");
        registrator.spongeDataStore(minecartDataStoreKey, 2, new DataContentUpdater[]{AbstractMinecartData.MINECART_UPDATER_BYTE_TO_BOOL_FIX}, AbstractMinecartBridge.class,
                Keys.POTENTIAL_MAX_SPEED, Keys.SLOWS_UNOCCUPIED, Keys.AIRBORNE_VELOCITY_MODIFIER, Keys.DERAILED_VELOCITY_MODIFIER);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Entity.Minecart.MAX_SPEED, minecartDataStoreKey, Keys.POTENTIAL_MAX_SPEED);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Entity.Minecart.SLOW_WHEN_EMPTY, minecartDataStoreKey, Keys.SLOWS_UNOCCUPIED);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Entity.Minecart.AIRBORNE_MODIFIER, minecartDataStoreKey, Keys.AIRBORNE_VELOCITY_MODIFIER);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Entity.Minecart.DERAILED_MODIFIER, minecartDataStoreKey, Keys.DERAILED_VELOCITY_MODIFIER);
    }
    // @formatter:on

    private static boolean setBlockOffset(final AbstractMinecart holder, final Integer value) {
        if (!holder.hasCustomDisplay()) {
            return false;
        }
        holder.setDisplayOffset(value);
        return true;
    }
}
