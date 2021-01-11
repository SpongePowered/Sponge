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
import net.minecraft.util.math.BlockPos;
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
import org.spongepowered.common.server.BootstrapProperties;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

@Mixin(ServerWorldInfo.class)
public abstract class ServerWorldInfoMixin implements IServerConfiguration, ServerWorldInfoBridge, ResourceKeyBridge {

    // @formatter:off
    @Shadow private WorldSettings settings;
    @Shadow private int xSpawn;
    @Shadow private int ySpawn;
    @Shadow private int zSpawn;

    @Shadow public abstract boolean shadow$isDifficultyLocked();
    // @formatter:on

    @Shadow public abstract void setSpawn(BlockPos p_176143_1_, float p_176143_2_);

    @Shadow private float spawnAngle;
    @Nullable private ResourceKey impl$key;
    private DimensionType impl$dimensionType;
    @Nullable private SerializationBehavior impl$serializationBehavior;
    @Nullable private Component impl$displayName;
    @Nullable private Integer impl$viewDistance;
    private UUID impl$uniqueId = UUID.randomUUID();
    private Boolean impl$pvp;
    private InheritableConfigHandle<WorldConfig> impl$configAdapter;

    private boolean impl$customDifficulty = false, impl$customGameType = false, impl$customSpawnPosition = false, impl$enabled,
            impl$loadOnStartup, impl$performsSpawnLogic;

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
    public void bridge$dimensionType(final DimensionType type, final boolean updatePlayers) {
        this.impl$dimensionType = type;
    }

    @Override
    public UUID bridge$uniqueId() {
        return this.impl$uniqueId;
    }

    @Override
    public void bridge$setUniqueId(final UUID uniqueId) {
        this.impl$uniqueId = uniqueId;
    }

    @Override
    public boolean bridge$customDifficulty() {
        return this.impl$customDifficulty;
    }

    @Override
    public boolean bridge$customGameType() {
        return this.impl$customGameType;
    }

    @Override
    public boolean bridge$customSpawnPosition() {
        return this.impl$customSpawnPosition;
    }

    @Override
    public void bridge$forceSetDifficulty(final Difficulty difficulty) {
        this.impl$customDifficulty = true;
        ((WorldSettingsAccessor) (Object) this.settings).accessor$difficulty(difficulty);
        this.impl$updateWorldForDifficultyChange(this.bridge$world(), this.shadow$isDifficultyLocked());
    }

    @Override
    public Optional<Boolean> bridge$pvp() {
        return Optional.ofNullable(this.impl$pvp);
    }

    @Override
    public void bridge$setPvp(@Nullable final Boolean pvp) {
        this.impl$pvp = pvp;
    }

    @Override
    public boolean bridge$enabled() {
        return this.impl$enabled;
    }

    @Override
    public void bridge$setEnabled(final boolean enabled) {
        this.impl$enabled = enabled;
    }

    @Override
    public boolean bridge$performsSpawnLogic() {
        return this.impl$performsSpawnLogic;
    }

    @Override
    public void bridge$setPerformsSpawnLogic(final boolean performsSpawnLogic) {
        this.impl$performsSpawnLogic = performsSpawnLogic;
    }

    @Override
    public boolean bridge$loadOnStartup() {
        return this.impl$loadOnStartup;
    }

    @Override
    public void bridge$setLoadOnStartup(final boolean loadOnStartup) {
        this.impl$loadOnStartup = loadOnStartup;
    }

    @Override
    public Optional<SerializationBehavior> bridge$serializationBehavior() {
        return Optional.ofNullable(this.impl$serializationBehavior);
    }

    @Override
    public void bridge$setSerializationBehavior(@Nullable final SerializationBehavior behavior) {
        this.impl$serializationBehavior = behavior;
    }

    @Override
    public Optional<Component> bridge$displayName() {
        return Optional.ofNullable(this.impl$displayName);
    }

    @Override
    public void bridge$setDisplayName(@Nullable final Component displayName) {
        this.impl$displayName = displayName;
    }

    @Override
    public Optional<Integer> bridge$viewDistance() {
        return Optional.ofNullable(this.impl$viewDistance);
    }

    @Override
    public void bridge$setViewDistance(@Nullable final Integer viewDistance) {
        this.impl$viewDistance = viewDistance;
    }

    @Override
    public InheritableConfigHandle<WorldConfig> bridge$configAdapter() {
        return this.impl$configAdapter;
    }

    @Override
    public void bridge$configAdapter(final InheritableConfigHandle<WorldConfig> adapter) {
        this.impl$configAdapter = adapter;
    }

    @Override
    public void bridge$populateFromDimension(final Dimension dimension) {
        final DimensionBridge dimensionBridge = (DimensionBridge) (Object) dimension;
        this.impl$key = ((ResourceKeyBridge) (Object) dimension).bridge$getKey();
        this.impl$dimensionType = dimension.type();
        this.impl$displayName = dimensionBridge.bridge$displayName().orElse(null);
        dimensionBridge.bridge$difficulty().ifPresent(v -> {
            ((WorldSettingsAccessor) (Object) this.settings).accessor$difficulty(RegistryTypes.DIFFICULTY.get().value((ResourceKey) (Object) v));
            this.impl$customDifficulty = true;
        });
        dimensionBridge.bridge$gameMode().ifPresent(v -> {
            ((WorldSettingsAccessor) (Object) this.settings).accessor$gameType(RegistryTypes.GAME_MODE.get().value((ResourceKey) (Object) v));
            this.impl$customGameType = true;
        });
        dimensionBridge.bridge$spawnPosition().ifPresent(v -> {
            this.setSpawn(VecHelper.toBlockPos(v), this.spawnAngle);
            this.impl$customSpawnPosition = true;
        });
        dimensionBridge.bridge$hardcore().ifPresent(v -> ((WorldSettingsAccessor) (Object) this.settings).accessor$hardcode(v));
        this.impl$serializationBehavior = dimensionBridge.bridge$serializationBehavior().orElse(null);
        this.impl$pvp = dimensionBridge.bridge$pvp().orElse(null);
        this.impl$enabled = dimensionBridge.bridge$enabled();
        this.impl$loadOnStartup = dimensionBridge.bridge$loadOnStartup();
        this.impl$performsSpawnLogic = dimensionBridge.bridge$performsSpawnLogic();
        this.impl$viewDistance = dimensionBridge.bridge$viewDistance().orElse(null);
    }

    @Override
    public IServerWorldInfo overworldData() {
        if (World.OVERWORLD.location().equals(this.impl$key)) {
            return (IServerWorldInfo) this;
        }

        return (IServerWorldInfo) SpongeCommon.getServer().getLevel(World.OVERWORLD).getLevelData();
    }

    @Redirect(method = "setTagData", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;encodeStart(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", ordinal = 0))
    private DataResult<Object> impl$ignorePluginDimensionsWhenWritingWorldGenSettings(final Codec codec, final DynamicOps<Object> ops, final Object input) {
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

    @Override
    public String toString() {
        return new StringJoiner(", ", ServerWorldInfo.class.getSimpleName() + "[", "]")
                .add("key=" + this.impl$key)
                .add("worldType=" + this.impl$dimensionType)
                .add("uniqueId=" + this.impl$uniqueId)
                .add("spawn=" + new Vector3i(this.xSpawn, this.ySpawn, this.zSpawn))
                .add("gameType=" + ((IServerWorldInfo) this).getGameType())
                .add("hardcore=" + ((IWorldInfo) this).isHardcore())
                .add("difficulty=" + ((IWorldInfo) this).getDifficulty())
                .toString();
    }
}
