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
package org.spongepowered.common.mixin.api.mcp.world.storage;

import net.kyori.adventure.text.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.generation.MutableWorldGenerationConfig;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.WorldSettingsAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.storage.ServerWorldInfoBridge;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@Mixin(ServerWorldInfo.class)
@Implements(@Interface(iface = ServerWorldProperties.class, prefix = "serverWorldProperties$"))
public abstract class ServerWorldInfoMixin_API implements ServerWorldProperties {

    // @formatter:off
    @Shadow private UUID wanderingTraderId;
    @Shadow private WorldSettings settings;

    @Shadow public abstract void shadow$setDifficulty(Difficulty difficulty);
    @Shadow public abstract boolean shadow$isInitialized();
    @Shadow public abstract DimensionGeneratorSettings shadow$worldGenSettings();
    // @formatter:on

    @Override
    public ResourceKey getKey() {
        return ((ResourceKeyBridge) this).bridge$getKey();
    }

    @Override
    public Optional<ServerWorld> getWorld() {
        return Optional.ofNullable((ServerWorld) ((ServerWorldInfoBridge) this).bridge$world());
    }

    @Override
    public Optional<Component> displayName() {
        return Optional.ofNullable(((ServerWorldInfoBridge) this).bridge$displayName());
    }

    @Override
    public void displayName(final Component displayName) {
        ((ServerWorldInfoBridge) this).bridge$displayName(Objects.requireNonNull(displayName, "displayName"));
    }

    @Override
    public WorldType worldType() {
        return (WorldType) ((ServerWorldInfoBridge) this).bridge$dimensionType();
    }

    @Override
    public void worldType(final WorldType worldType) {
        ((ServerWorldInfoBridge) this).bridge$dimensionType((DimensionType) Objects.requireNonNull(worldType, "worldType"), true);
    }

    @Override
    public boolean initialized() {
        return this.shadow$isInitialized();
    }

    @Override
    public MutableWorldGenerationConfig worldGenerationSettings() {
        return (MutableWorldGenerationConfig) this.shadow$worldGenSettings();
    }

    @Override
    public void hardcore(final boolean hardcore) {
        ((WorldSettingsAccessor) (Object) this.settings).accessor$hardcode(hardcore);
    }

    @Override
    public boolean pvp() {
        return ((ServerWorldInfoBridge) this).bridge$pvp();
    }

    @Override
    public void pvp(final boolean pvp) {
        ((ServerWorldInfoBridge) this).bridge$pvp(pvp);
    }

    @Override
    public UUID getUniqueId() {
        return ((ServerWorldInfoBridge) this).bridge$uniqueId();
    }

    @Override
    public boolean enabled() {
        return ((ServerWorldInfoBridge) this).bridge$enabled();
    }

    @Override
    public void enabled(final boolean enabled) {
        ((ServerWorldInfoBridge) this).bridge$enabled(enabled);
    }

    @Override
    public boolean keepLoaded() {
        return ((ServerWorldInfoBridge) this).bridge$keepLoaded();
    }

    @Override
    public void keepLoaded(final boolean keepLoaded) {
        ((ServerWorldInfoBridge) this).bridge$keepLoaded(keepLoaded);
    }

    @Override
    public boolean loadOnStartup() {
        return ((ServerWorldInfoBridge) this).bridge$loadOnStartup();
    }

    @Override
    public void loadOnStartup(final boolean loadOnStartup) {
        ((ServerWorldInfoBridge) this).bridge$loadOnStartup(loadOnStartup);
    }

    @Override
    public boolean keepSpawnLoaded() {
        return ((ServerWorldInfoBridge) this).bridge$keepSpawnLoaded();
    }

    @Override
    public void keepSpawnLoaded(final boolean keepSpawnLoaded) {
        ((ServerWorldInfoBridge) this).bridge$keepSpawnLoaded(keepSpawnLoaded);
    }

    @Override
    public boolean generateSpawnOnLoad() {
        return ((ServerWorldInfoBridge) this).bridge$generateSpawnOnLoad();
    }

    @Override
    public void generateSpawnOnLoad(final boolean generateSpawnOnLoad) {
        ((ServerWorldInfoBridge) this).bridge$generateSpawnOnLoad(generateSpawnOnLoad);
    }

    @Override
    public void commands(final boolean commands) {
        ((WorldSettingsAccessor) (Object) this.settings).accessor$allowCommands(commands);
    }

    @Override
    public void difficulty(final org.spongepowered.api.world.difficulty.Difficulty difficulty) {
        this.shadow$setDifficulty((Difficulty) (Object) difficulty);
    }

    @Override
    public SerializationBehavior serializationBehavior() {
        return ((ServerWorldInfoBridge) this).bridge$serializationBehavior();
    }

    @Override
    public void serializationBehavior(final SerializationBehavior behavior) {
        ((ServerWorldInfoBridge) this).bridge$serializationBehavior(behavior);
    }

    @Override
    public Optional<UUID> wanderTraderUniqueId() {
        return Optional.ofNullable(this.wanderingTraderId);
    }
}
