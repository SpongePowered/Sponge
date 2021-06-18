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
package org.spongepowered.common.mixin.api.minecraft.world.level.storage;

import net.kyori.adventure.text.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.generation.config.WorldGenerationConfig;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.level.LevelSettingsAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@Mixin(PrimaryLevelData.class)
@Implements(@Interface(iface = ServerWorldProperties.class, prefix = "serverWorldProperties$"))
public abstract class PrimaryLevelDataMixin_API implements ServerWorldProperties {

    // @formatter:off
    @Shadow private UUID wanderingTraderId;
    @Shadow private LevelSettings settings;

    @Shadow public abstract void shadow$setDifficulty(Difficulty difficulty);
    @Shadow public abstract boolean shadow$isInitialized();
    @Shadow public abstract WorldGenSettings shadow$worldGenSettings();
    // @formatter:on

    @Override
    public ResourceKey key() {
        return ((ResourceKeyBridge) this).bridge$getKey();
    }

    @Override
    public Optional<ServerWorld> world() {
        return Optional.ofNullable((ServerWorld) ((PrimaryLevelDataBridge) this).bridge$world());
    }

    @Override
    public Optional<Component> displayName() {
        return ((PrimaryLevelDataBridge) this).bridge$displayName();
    }

    @Override
    public void setDisplayName(final Component displayName) {
        ((PrimaryLevelDataBridge) this).bridge$setDisplayName(Objects.requireNonNull(displayName, "displayName"));
    }

    @Override
    public WorldType worldType() {
        return (WorldType) ((PrimaryLevelDataBridge) this).bridge$dimensionType();
    }

    @Override
    public void setWorldType(final WorldType worldType) {
        ((PrimaryLevelDataBridge) this).bridge$dimensionType((DimensionType) Objects.requireNonNull(worldType, "worldType"), true);
    }

    @Override
    public boolean initialized() {
        return this.shadow$isInitialized();
    }

    @Override
    public WorldGenerationConfig.Mutable worldGenerationConfig() {
        return (WorldGenerationConfig.Mutable) this.shadow$worldGenSettings();
    }

    @Override
    public void setHardcore(final boolean hardcore) {
        ((LevelSettingsAccessor) (Object) this.settings).accessor$hardcode(hardcore);
    }

    @Override
    public boolean pvp() {
        return ((PrimaryLevelDataBridge) this).bridge$pvp().orElse(BootstrapProperties.pvp);
    }

    @Override
    public void setPvp(final boolean pvp) {
        ((PrimaryLevelDataBridge) this).bridge$setPvp(pvp);
    }

    @Override
    public UUID uniqueId() {
        return ((PrimaryLevelDataBridge) this).bridge$uniqueId();
    }

    @Override
    public boolean loadOnStartup() {
        return ((PrimaryLevelDataBridge) this).bridge$loadOnStartup();
    }

    @Override
    public void setLoadOnStartup(final boolean loadOnStartup) {
        ((PrimaryLevelDataBridge) this).bridge$setLoadOnStartup(loadOnStartup);
    }

    @Override
    public boolean performsSpawnLogic() {
        return ((PrimaryLevelDataBridge) this).bridge$performsSpawnLogic();
    }

    @Override
    public void setPerformsSpawnLogic(final boolean keepLoaded) {
        ((PrimaryLevelDataBridge) this).bridge$setPerformsSpawnLogic(keepLoaded);
    }

    @Override
    public void setCommands(final boolean commands) {
        ((LevelSettingsAccessor) (Object) this.settings).accessor$allowCommands(commands);
    }

    @Override
    public int viewDistance() {
        return ((PrimaryLevelDataBridge) this).bridge$viewDistance().orElse(BootstrapProperties.viewDistance);
    }

    @Override
    public void setViewDistance(@Nullable Integer viewDistance) {
        ((PrimaryLevelDataBridge) this).bridge$setViewDistance(viewDistance);
    }

    @Override
    public void setDifficulty(final org.spongepowered.api.world.difficulty.Difficulty difficulty) {
        this.shadow$setDifficulty((Difficulty) (Object) difficulty);
    }

    @Override
    public SerializationBehavior serializationBehavior() {
        return ((PrimaryLevelDataBridge) this).bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC);
    }

    @Override
    public void setSerializationBehavior(final SerializationBehavior behavior) {
        ((PrimaryLevelDataBridge) this).bridge$setSerializationBehavior(behavior);
    }

    @Override
    public Optional<UUID> wanderTraderUniqueId() {
        return Optional.ofNullable(this.wanderingTraderId);
    }
}
