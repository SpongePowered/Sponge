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
package org.spongepowered.common.mixin.core.world.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.storage.IServerWorldInfoBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;

import java.util.Optional;
import java.util.UUID;

@Mixin(IServerWorldInfo.class)
public interface IServerWorldInfoMixin extends IServerWorldInfoBridge, ResourceKeyBridge {

    // @formatter:off
    @Shadow String shadow$getLevelName();
    @Shadow GameType shadow$getGameType();
    // @formatter:on

    @Override
    default ResourceKey bridge$getKey() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$setKey(ResourceKey key) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default boolean bridge$isValid() {
        final String levelName = this.shadow$getLevelName();

        return this.bridge$getKey() != null && !(levelName == null || levelName.equals("") || levelName.equals("MpServer"));
    }

    @Override
    default boolean bridge$hasCustomDifficulty() {
        return false;
    }

    @Override
    default void bridge$forceSetDifficulty(Difficulty difficulty) {

    }

    @Override
    default DimensionType bridge$getDimensionType() {
        return SpongeCommon.getServer().registryAccess().dimensionTypes().get(DimensionType.OVERWORLD_LOCATION);
    }

    @Override
    default void bridge$setDimensionType(final DimensionType type, final boolean updatePlayers) {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties can set dimension types!!");
    }

    @Override
    default UUID bridge$getUniqueId() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$setUniqueId(UUID uniqueId) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Nullable
    @Override
    default ServerWorld bridge$getWorld() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$writeSpongeLevelData(final CompoundNBT compound) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$readSpongeLevelData(final CompoundNBT compound) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default int bridge$getIndexForUniqueId(UUID uuid) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default Optional<UUID> bridge$getUniqueIdForIndex(int index) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Nullable
    @Override
    default InheritableConfigHandle<WorldConfig> bridge$getConfigAdapter() {
        return SpongeGameConfigs.createDetached();
    }

    @Override
    default void bridge$setConfigAdapter(final InheritableConfigHandle<WorldConfig> adapter) {

    }

    @Override
    default void bridge$writeTrackedPlayerTable(CompoundNBT spongeDataCompound) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }
}
