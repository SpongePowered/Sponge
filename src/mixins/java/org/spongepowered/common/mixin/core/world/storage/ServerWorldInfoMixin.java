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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.kyori.adventure.text.Component;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraft.world.storage.ServerWorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.world.WorldSettingsAccessor;
import org.spongepowered.common.accessor.world.gen.DimensionGeneratorSettingsAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.DimensionBridge;
import org.spongepowered.common.bridge.world.gen.DimensionGeneratorSettingsBridge;
import org.spongepowered.common.bridge.world.storage.ServerWorldInfoBridge;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.server.SpongeWorldManager;

import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

@Mixin(ServerWorldInfo.class)
public abstract class ServerWorldInfoMixin implements IServerConfiguration, ServerWorldInfoBridge, ResourceKeyBridge {

    // @formatter:off
    @Shadow private WorldSettings settings;
    @Shadow public abstract boolean shadow$isDifficultyLocked();
    // @formatter:on

    @Nullable private ResourceKey impl$key;
    private DimensionType impl$dimensionType;
    private SerializationBehavior impl$serializationBehavior;
    @Nullable private Component impl$displayName;
    private UUID impl$uniqueId;
    private InheritableConfigHandle<WorldConfig> impl$configAdapter;

    private boolean impl$hasCustomDifficulty = false, impl$keepLoaded, impl$pvp, impl$enabled, impl$loadOnStartup, impl$keepSpawnLoaded,
            impl$generateSpawnOnLoad;

    @Override
    public ResourceKey bridge$getKey() {
        return this.impl$key;
    }

    @Override
    public void bridge$setKey(final ResourceKey key) {
        this.impl$key = key;
    }

    @Override
    public boolean bridge$valid() {
        return this.impl$key != null;
    }

    @Nullable
    public ServerWorld bridge$world() {
        if (!Sponge.isServerAvailable()) {
            return null;
        }

        final ServerWorld world = SpongeCommon.getServer().getLevel(SpongeWorldManager.createRegistryKey(this.impl$key));
        if (world == null) {
            return null;
        }

        final IServerWorldInfo levelData = (IServerWorldInfo) world.getLevelData();
        if (levelData != this) {
            return null;
        }

        return world;
    }

    @Override
    public DimensionType bridge$dimensionType() {
        return this.impl$dimensionType;
    }

    @Override
    public void bridge$dimensionType(final DimensionType type, boolean updatePlayers) {
        this.impl$dimensionType = type;
    }

    @Override
    public UUID bridge$uniqueId() {
        return this.impl$uniqueId;
    }

    @Override
    public boolean bridge$customDifficulty() {
        return this.impl$hasCustomDifficulty;
    }

    @Override
    public void bridge$forceSetDifficulty(final Difficulty difficulty) {
        this.impl$hasCustomDifficulty = true;
        ((WorldSettingsAccessor) (Object) this.settings).accessor$difficulty(difficulty);
        this.impl$updateWorldForDifficultyChange(this.bridge$world(), this.shadow$isDifficultyLocked());
    }

    @Override
    public boolean bridge$pvp() {
        return this.impl$pvp;
    }

    @Override
    public void bridge$pvp(final boolean pvp) {
        this.impl$pvp = pvp;
    }

    @Override
    public boolean bridge$enabled() {
        return this.impl$enabled;
    }

    @Override
    public void bridge$enabled(final boolean enabled) {
        this.impl$enabled = enabled;
    }

    @Override
    public boolean bridge$keepLoaded() {
        return this.impl$keepLoaded;
    }

    @Override
    public void bridge$keepLoaded(final boolean keepLoaded) {
        this.impl$keepLoaded = keepLoaded;
    }

    @Override
    public boolean bridge$loadOnStartup() {
        return this.impl$loadOnStartup;
    }

    @Override
    public void bridge$loadOnStartup(final boolean loadOnStartup) {
        this.impl$loadOnStartup = loadOnStartup;
    }

    @Override
    public boolean bridge$keepSpawnLoaded() {
        return this.impl$keepSpawnLoaded;
    }

    @Override
    public void bridge$keepSpawnLoaded(final boolean keepSpawnLoaded) {
        this.impl$keepSpawnLoaded = keepSpawnLoaded;
    }

    @Override
    public boolean bridge$generateSpawnOnLoad() {
        return this.impl$generateSpawnOnLoad;
    }

    @Override
    public void bridge$generateSpawnOnLoad(final boolean generateSpawnOnLoad) {
        this.impl$generateSpawnOnLoad = generateSpawnOnLoad;
    }

    @Override
    public SerializationBehavior bridge$serializationBehavior() {
        return this.impl$serializationBehavior;
    }

    @Override
    public void bridge$serializationBehavior(final SerializationBehavior behavior) {
        this.impl$serializationBehavior = behavior;
    }

    @Override
    public @Nullable Component bridge$displayName() {
        return this.impl$displayName;
    }

    @Override
    public void bridge$displayName(Component displayName) {
        this.impl$displayName = displayName;
    }

    @Override
    public InheritableConfigHandle<WorldConfig> bridge$configAdapter() {
        return this.impl$configAdapter;
    }

    @Override
    public void bridge$configAdapter(InheritableConfigHandle<WorldConfig> adapter) {
        this.impl$configAdapter = adapter;
    }

    @Override
    public void bridge$populateFromDimension(final Dimension dimension) {
        final DimensionBridge dimensionBridge = (DimensionBridge) (Object) dimension;
        this.impl$key = ((ResourceKeyBridge) (Object) dimension).bridge$getKey();
        this.impl$dimensionType = dimension.type();
        this.impl$serializationBehavior = dimensionBridge.bridge$serializationBehavior();
        this.impl$displayName = dimensionBridge.bridge$displayName();
        this.impl$uniqueId = dimensionBridge.bridge$uniqueId();
        this.impl$hasCustomDifficulty = dimensionBridge.bridge$difficulty() != null;
        if (this.impl$hasCustomDifficulty) {
            ((WorldSettingsAccessor) (Object) this.settings).accessor$difficulty(Sponge.getGame().registries().registry(RegistryTypes.DIFFICULTY).value((ResourceKey) (Object) dimensionBridge.bridge$difficulty()));
        }
        this.impl$pvp = dimensionBridge.bridge$pvp();
        this.impl$enabled = dimensionBridge.bridge$enabled();
        this.impl$keepLoaded = dimensionBridge.bridge$keepLoaded();
        this.impl$loadOnStartup = dimensionBridge.bridge$loadOnStartup();
        this.impl$keepSpawnLoaded = dimensionBridge.bridge$keepSpawnLoaded();
        this.impl$generateSpawnOnLoad = dimensionBridge.bridge$generateSpawnOnLoad();
    }

    @Override
    public IServerWorldInfo overworldData() {
        if (World.OVERWORLD.location().equals(this.impl$key)) {
            return (IServerWorldInfo) this;
        }

        return (IServerWorldInfo) SpongeCommon.getServer().getLevel(World.OVERWORLD).getLevelData();
    }

    void impl$updateWorldForDifficultyChange(final ServerWorld world, final boolean isLocked) {
        if (world == null) {
            return;
        }

        final MinecraftServer server = world.getServer();
        final Difficulty difficulty = ((IWorldInfo) this).getDifficulty();

        if (difficulty == Difficulty.HARD) {
            world.setSpawnSettings(true, true);
        } else if (server.isSingleplayer()) {
            world.setSpawnSettings(difficulty != Difficulty.PEACEFUL, true);
        } else {
            world.setSpawnSettings(((MinecraftServerAccessor) server).invoker$isSpawningMonsters(), server.isSpawningAnimals());
        }

        world.players().forEach(player -> player.connection.send(new SServerDifficultyPacket(difficulty, isLocked)));
    }

    @Redirect(method = "setTagData", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;encodeStart(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", ordinal = 0))
    private DataResult<Object> impl$ignorePluginDimensionsWhenWritingWorldGenSettings(Codec codec, DynamicOps<Object> ops, Object input) {
        DimensionGeneratorSettings dimensionGeneratorSettings = (DimensionGeneratorSettings) input;
        // Sub levels will have an empty dimensions registry so it is an easy toggle off
        if (dimensionGeneratorSettings.dimensions().entrySet().size() == 0) {
            return codec.encodeStart(ops, dimensionGeneratorSettings);
        }
        dimensionGeneratorSettings = ((DimensionGeneratorSettingsBridge) input).bridge$copy();
        final SimpleRegistry<Dimension> registry = new SimpleRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable());
        ((org.spongepowered.api.registry.Registry<Dimension>) (Object) dimensionGeneratorSettings.dimensions()).streamEntries().forEach(entry -> {
            if (Constants.MINECRAFT.equals(entry.key().getNamespace())) {
                ((org.spongepowered.api.registry.Registry<Dimension>) (Object) registry).register(entry.key(), entry.value());
            }
        });
        ((DimensionGeneratorSettingsAccessor) dimensionGeneratorSettings).accessor$dimensions(registry);
        return codec.encodeStart(ops, dimensionGeneratorSettings);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ServerWorldInfo.class.getSimpleName() + "[", "]")
                .add("key=" + this.impl$key)
                .add("worldType=" + this.impl$dimensionType)
                .add("uniqueId=" + this.impl$uniqueId)
                .add("spawnX=" + ((IWorldInfo) this).getXSpawn())
                .add("spawnY=" + ((IWorldInfo) this).getYSpawn())
                .add("spawnZ=" + ((IWorldInfo) this).getZSpawn())
                .add("gameType=" + ((IServerWorldInfo) this).getGameType())
                .add("hardcore=" + ((IWorldInfo) this).isHardcore())
                .add("difficulty=" + ((IWorldInfo) this).getDifficulty())
                .toString();
    }
}
