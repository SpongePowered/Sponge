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
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.storage.IServerWorldInfoBridge;
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
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$forceSetDifficulty(Difficulty difficulty) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default boolean bridge$isSinglePlayerProperties() {
        return this.shadow$getLevelName() != null && this.shadow$getLevelName().equals("MpServer");
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

    @Override
    default boolean bridge$isEnabled() {
        return this.bridge$getConfigAdapter().get().world.enabled;
    }

    @Override
    default void bridge$setEnabled(final boolean state) {
        this.bridge$getConfigAdapter().get().world.enabled = state;
    }

    @Override
    default boolean bridge$isPVPEnabled() {
        return this.bridge$getConfigAdapter().get().world.pvp;
    }

    @Override
    default void bridge$setPVPEnabled(final boolean state) {
        this.bridge$getConfigAdapter().get().world.pvp = state;
    }

    @Override
    default boolean bridge$doesLoadOnStartup() {
        return this.bridge$getConfigAdapter().get().world.loadOnStartup;
    }

    @Override
    default void bridge$setLoadOnStartup(final boolean state) {
        this.bridge$getConfigAdapter().get().world.loadOnStartup = state;
    }

    @Override
    default boolean bridge$doesKeepSpawnLoaded() {
        return this.bridge$getConfigAdapter().get().world.keepSpawnLoaded;
    }

    @Override
    default void bridge$setKeepSpawnLoaded(final boolean state) {
        this.bridge$getConfigAdapter().get().world.keepSpawnLoaded = state;
    }

    @Override
    default boolean bridge$doesGenerateSpawnOnLoad() {
        return this.bridge$getConfigAdapter().get().world.generateSpawnOnLoad;
    }

    @Override
    default void bridge$setGenerateSpawnOnLoad(final boolean state) {
        this.bridge$getConfigAdapter().get().world.generateSpawnOnLoad = state;
    }

    @Override
    default SerializationBehavior bridge$getSerializationBehavior() {
        return this.bridge$getConfigAdapter().get().world.serializationBehavior;
    }

    @Override
    default void bridge$setSerializationBehavior(final SerializationBehavior behavior) {
        this.bridge$getConfigAdapter().get().world.serializationBehavior = behavior;
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
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$setConfigAdapter(final InheritableConfigHandle<WorldConfig> adapter) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$writeTrackedPlayerTable(CompoundNBT spongeDataCompound) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }
}
