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
package org.spongepowered.common.data.provider.block.entity;

import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.world.spawner.AbstractSpawner;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.processor.common.SpawnerUtils;
import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.data.provider.DataProviderRegistryBuilder;
import org.spongepowered.common.data.provider.commandblock.CommandBlockLogicDataProviders;
import org.spongepowered.common.mixin.accessor.tileentity.BeaconTileEntityAccessor;
import org.spongepowered.common.mixin.accessor.tileentity.EndGatewayTileEntityAccessor;
import org.spongepowered.common.mixin.accessor.tileentity.MobSpawnerTileEntityAccessor;
import org.spongepowered.common.mixin.accessor.world.spawner.AbstractSpawnerAccessor;
import org.spongepowered.common.util.VecHelper;

public class TileEntityDataProviders extends DataProviderRegistryBuilder {

    public TileEntityDataProviders(DataProviderRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        registerBannerTileEntityData();
        registerCommandBlockTileEntityData();
        registerEndGatewayEntityData();
        registerMobSpawnerEntityData();

        register(new SignTileEntityLinesProvider());
    }

    private void registerBannerTileEntityData() {
        register(new BannerTileEntityBaseColorProvider());
        register(new BannerTileEntityPatternsProvider());

        register(new BeaconTileEntityEffectProvider(Keys.BEACON_PRIMARY_EFFECT.get(),
                BeaconTileEntityAccessor::accessor$getPrimaryEffect,
                BeaconTileEntityAccessor::accessor$setPrimaryEffect));

        register(new BeaconTileEntityEffectProvider(Keys.BEACON_SECONDARY_EFFECT.get(),
                BeaconTileEntityAccessor::accessor$getSecondaryEffect,
                BeaconTileEntityAccessor::accessor$setSecondaryEffect));
    }

    private void registerCommandBlockTileEntityData() {
        new CommandBlockLogicDataProviders<>(this.registry, CommandBlockTileEntity.class,
                CommandBlockTileEntity::getCommandBlockLogic).register();
    }

    private void registerEndGatewayEntityData() {
        register(EndGatewayTileEntityAccessor.class, Keys.END_GATEWAY_AGE,
                EndGatewayTileEntityAccessor::accessor$getAge,
                EndGatewayTileEntityAccessor::accessor$setAge);

        register(EndGatewayTileEntityAccessor.class, Keys.DO_EXACT_TELEPORT,
                EndGatewayTileEntityAccessor::accessor$getExactTeleport,
                EndGatewayTileEntityAccessor::accessor$setExactTeleport);

        register(EndGatewayTileEntityAccessor.class, Keys.EXIT_POSITION,
                (accessor) -> VecHelper.toVector3d(accessor.accessor$getExitPortal()),
                (accessor, value) -> accessor.accessor$setExitPortal(VecHelper.toBlockPos(value)));

        register(EndGatewayTileEntityAccessor.class, Keys.END_GATEWAY_TELEPORT_COOLDOWN,
                EndGatewayTileEntityAccessor::accessor$getTeleportCooldown,
                EndGatewayTileEntityAccessor::accessor$setTeleportCooldown);
    }

    private void registerMobSpawnerEntityData() {
        register(MobSpawnerTileEntityAccessor.class, Keys.SPAWNER_ENTITIES,
                (accessor) -> SpawnerUtils.getEntities(accessor.accessor$getSpawnerLogic()),
                (accessor, value) -> {
                    final AbstractSpawnerAccessor logic = (AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic();
                    SpawnerUtils.setEntities(logic, value);
                    SpawnerUtils.setNextEntity((AbstractSpawner) logic, SpawnerUtils.getNextEntity(logic));
                });

        register(MobSpawnerTileEntityAccessor.class, Keys.SPAWNER_MAXIMUM_DELAY,
                (accessor) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$getMaxSpawnDelay(),
                (accessor, value) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$setMaxSpawnDelay(value));

        register(MobSpawnerTileEntityAccessor.class, Keys.SPAWNER_MINIMUM_DELAY,
                (accessor) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$getMinSpawnDelay(),
                (accessor, value) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$setMinSpawnDelay(value));

        register(MobSpawnerTileEntityAccessor.class, Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES,
                (accessor) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$getMaxNearbyEntities(),
                (accessor, value) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$setMaxNearbyEntities(value));

        register(MobSpawnerTileEntityAccessor.class, Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN,
                (accessor) -> SpawnerUtils.getNextEntity((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()),
                (accessor, value) -> SpawnerUtils.setNextEntity(accessor.accessor$getSpawnerLogic(), value));

        register(MobSpawnerTileEntityAccessor.class, Keys.SPAWNER_REMAINING_DELAY,
                (accessor) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$getSpawnDelay(),
                (accessor, value) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$setSpawnDelay(value));

        register(MobSpawnerTileEntityAccessor.class, Keys.SPAWNER_REQUIRED_PLAYER_RANGE,
                (accessor) -> (double) ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$getActivatingRangeFromPlayer(),
                (accessor, value) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic())
                        .accessor$setActivatingRangeFromPlayer(value.intValue()));

        register(MobSpawnerTileEntityAccessor.class, Keys.SPAWNER_SPAWN_COUNT,
                (accessor) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$getSpawnCount(),
                (accessor, value) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$setSpawnCount(value));

        register(MobSpawnerTileEntityAccessor.class, Keys.SPAWNER_SPAWN_RANGE,
                (accessor) -> (double) ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$getSpawnRange(),
                (accessor, value) -> ((AbstractSpawnerAccessor) accessor.accessor$getSpawnerLogic()).accessor$setSpawnRange(value.intValue()));
    }
}
