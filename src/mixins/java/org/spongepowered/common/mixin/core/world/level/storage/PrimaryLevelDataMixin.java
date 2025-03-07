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
package org.spongepowered.common.mixin.core.world.level.storage;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.world.level.LevelSettingsAccessor;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.data.DataUtil;
import org.spongepowered.common.data.fixer.LegacyUUIDCodec;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MapUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.server.SpongeServerLevelData;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataMixin implements ServerLevelData, WorldData, PrimaryLevelDataBridge, DataCompoundHolder {

    // @formatter:off
    @Shadow private LevelSettings settings;
    @Shadow private BlockPos spawnPos;
    @Shadow private float spawnAngle;

    @Shadow public abstract boolean shadow$isDifficultyLocked();
    @Shadow public abstract void shadow$setSpawn(BlockPos p_176143_1_, float p_176143_2_);
    // @formatter:on

    @Shadow
    private boolean difficultyLocked;
    private final SpongeServerLevelData impl$spongeData = new SpongeServerLevelData();

    private DimensionType impl$dimensionType;
    private ChunkGenerator impl$chunkGenerator;

    private final BiMap<Integer, UUID> impl$playerUniqueIdMap = HashBiMap.create();

    private boolean impl$customDifficulty = false, impl$customGameType = false, impl$customSpawnPosition = false;

    private BiMap<Integer, UUID> impl$mapUUIDIndex = HashBiMap.create();
    private @Nullable CompoundTag impl$compound;

    @Override
    public SpongeServerLevelData bridge$spongeData() {
        return this.impl$spongeData;
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
        this.settings = this.settings.withDifficulty(difficulty);
        final ServerLevel level = this.bridge$level();
        if (level != null) {
            this.impl$updateWorldForDifficultyChange(level, this.shadow$isDifficultyLocked());
        }
    }

    @Override
    public Optional<Boolean> bridge$pvp() {
        return Optional.ofNullable(this.impl$spongeData.configAdapter().get().world.pvpEnabled);
    }

    @Override
    public void bridge$setPvp(@Nullable final Boolean pvp) {
        this.impl$spongeData.configAdapter().get().world.pvpEnabled = pvp;
    }

    @Override
    public boolean bridge$performsSpawnLogic() {
        return this.impl$spongeData.configAdapter().get().world.keepSpawnLoaded;
    }

    @Override
    public void bridge$setPerformsSpawnLogic(final boolean performsSpawnLogic) {
        this.impl$spongeData.configAdapter().get().world.keepSpawnLoaded = performsSpawnLogic;
    }

    @Override
    public boolean bridge$loadOnStartup() {
        return this.impl$spongeData.configAdapter().get().world.loadOnStartup;
    }

    @Override
    public void bridge$setLoadOnStartup(final boolean loadOnStartup) {
        this.impl$spongeData.configAdapter().get().world.loadOnStartup = loadOnStartup;
    }

    @Override
    public Optional<SerializationBehavior> bridge$serializationBehavior() {
        return Optional.ofNullable(this.impl$spongeData.configAdapter().get().world.serializationBehavior);
    }

    @Override
    public void bridge$setSerializationBehavior(@Nullable final SerializationBehavior behavior) {
        this.impl$spongeData.configAdapter().get().world.serializationBehavior = behavior;
    }

    @Override
    public Optional<Component> bridge$displayName() {
        return Optional.ofNullable(this.impl$spongeData.configAdapter().get().world.displayName);
    }

    @Override
    public void bridge$setDisplayName(@Nullable final Component displayName) {
        this.impl$spongeData.configAdapter().get().world.displayName = displayName;
    }

    @Override
    public Optional<Integer> bridge$viewDistance() {
        return Optional.ofNullable(this.impl$spongeData.configAdapter().get().world.viewDistance);
    }

    @Override
    public void bridge$setViewDistance(@Nullable final Integer viewDistance) {
        this.impl$spongeData.configAdapter().get().world.viewDistance = viewDistance;
        this.bridge$triggerViewDistanceLogic();
    }

    public void bridge$populateFromLevelStem(final LevelStem dimension) {
        this.impl$dimensionType = dimension.type().value();
        this.impl$chunkGenerator = dimension.generator();

        // Legacy back compat
        final LevelStemBridge bridge = (LevelStemBridge) (Object) dimension;
        if (!bridge.bridge$hasLegacyData()) {
            return;
        }
        Optional.ofNullable(bridge.bridge$displayName()).ifPresent(this::bridge$setDisplayName);
        final Difficulty difficulty = bridge.bridge$difficulty();
        final GameType gameType = bridge.bridge$gameMode();
        final Boolean isHardcore = bridge.bridge$hardcore();
        final Boolean allowCommands = bridge.bridge$allowCommands();
        if (difficulty != null) {
            this.impl$customDifficulty = true;
        }
        if (gameType != null) {
            this.impl$customGameType = true;
        }
        this.settings = new LevelSettings(
            this.settings.levelName(),
            gameType == null ? this.settings.gameType() : gameType,
            isHardcore == null ? this.settings.hardcore() : isHardcore,
            difficulty == null ? this.settings.difficulty() : difficulty,
            allowCommands == null ? this.settings.allowCommands() : allowCommands,
            this.settings.gameRules(),
            this.settings.getDataConfiguration());

        final Vector3i spawnPos = bridge.bridge$spawnPosition();
        if (spawnPos != null) {
            this.shadow$setSpawn(VecHelper.toBlockPos(spawnPos), this.spawnAngle);
            this.impl$customSpawnPosition = true;
        }

        Optional.ofNullable(bridge.bridge$serializationBehavior()).ifPresent(this::bridge$setSerializationBehavior);
        Optional.ofNullable(bridge.bridge$pvp()).ifPresent(this::bridge$setPvp);
        this.bridge$setLoadOnStartup(bridge.bridge$loadOnStartup());
        this.bridge$setPerformsSpawnLogic(bridge.bridge$performsSpawnLogic());
        this.bridge$setViewDistance(bridge.bridge$viewDistance());
    }

    @Override
    public BiMap<Integer, UUID> bridge$getMapUUIDIndex() {
        return this.impl$mapUUIDIndex;
    }

    @Override
    public int bridge$getIndexForUniqueId(final UUID uuid) {
        final Integer index = this.impl$playerUniqueIdMap.inverse().get(uuid);
        if (index != null) {
            return index;
        }

        final int newIndex = this.impl$playerUniqueIdMap.size();
        this.impl$playerUniqueIdMap.put(newIndex, uuid);
        return newIndex;
    }

    @Override
    public Optional<UUID> bridge$getUniqueIdForIndex(final int index) {
        return Optional.ofNullable(this.impl$playerUniqueIdMap.get(index));
    }

    void impl$updateWorldForDifficultyChange(final ServerLevel level, final boolean isLocked) {
        final MinecraftServer server = level.getServer();
        final Difficulty difficulty = this.getDifficulty();

        if (difficulty == Difficulty.HARD) {
            level.setSpawnSettings(true); // set spawn enemies true
        } else if (server.isSingleplayer()) {
            level.setSpawnSettings(difficulty != Difficulty.PEACEFUL);
        } else {
            level.setSpawnSettings(((MinecraftServerAccessor) server).invoker$isSpawningMonsters());
        }

        level.players().forEach(player -> player.connection.send(new ClientboundChangeDifficultyPacket(difficulty, isLocked)));
    }

    @Override
    public void bridge$hardcore(final boolean hardcore) {
        ((LevelSettingsAccessor) (Object) this.settings).accessor$hardcore(hardcore);
    }

    @Override
    public void bridge$allowCommands(final boolean allowCommands) {
        ((LevelSettingsAccessor) (Object) this.settings).accessor$allowCommands(allowCommands);
    }

    @Override
    @SuppressWarnings("deprecated")
    public void bridge$readSpongeLevelData(final Dynamic<Tag> dynamic) {
        dynamic.get(Constants.Sponge.Data.V2.SPONGE_DATA).get().ifSuccess(v2 -> {
            v2.get(Constants.Sponge.World.UNIQUE_ID).read(UUIDUtil.CODEC).result().ifPresent(this.impl$spongeData::setUniqueId);

            v2.get(Constants.Map.MAP_UUID_INDEX).readMap(Codec.STRING, UUIDUtil.CODEC).result().ifPresent(value -> {
                final BiMap<Integer, UUID> mapIndex = HashBiMap.create();
                for (final Pair<String, UUID> pair : value) {
                    final int id = Integer.parseInt(pair.getFirst());
                    mapIndex.put(id, pair.getSecond());
                }
                this.impl$mapUUIDIndex = mapIndex;
            });

            // TODO Move this to Schema
            v2.get(Constants.Sponge.LEGACY_SPONGE_PLAYER_UUID_TABLE).readList(LegacyUUIDCodec.CODEC).result().orElseGet(() ->
                v2.get(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE).readList(UUIDUtil.CODEC).result().orElse(Collections.emptyList())
            ).forEach(uuid -> this.impl$playerUniqueIdMap.inverse().putIfAbsent(uuid, this.impl$playerUniqueIdMap.size()));
        });

        this.data$setCompound((CompoundTag) dynamic.getValue());
        DataUtil.syncTagToData(this);
        this.data$setCompound(null);
    }

    @Override
    public CompoundTag bridge$writeSpongeLevelData() {
        final CompoundTag data = new CompoundTag();
        data.putUUID(Constants.Sponge.World.UNIQUE_ID, this.impl$spongeData.uniqueId());

        // Map Storage
        final CompoundTag mapUUIDIndexTag = new CompoundTag();
        MapUtil.saveMapUUIDIndex(mapUUIDIndexTag, this.impl$mapUUIDIndex);
        data.put(Constants.Map.MAP_UUID_INDEX, mapUUIDIndexTag);

        final ListTag playerIdList = new ListTag();
        data.put(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, playerIdList);
        this.impl$playerUniqueIdMap.values().forEach(uuid -> playerIdList.add(new IntArrayTag(UUIDUtil.uuidToIntArray(uuid))));

        return data;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PrimaryLevelData.class.getSimpleName() + "[", "]")
                .add("key=" + this.impl$spongeData.key())
                .add("worldType=" + this.impl$dimensionType)
                .add("uniqueId=" + this.impl$spongeData.uniqueId())
                .add("spawn=" + VecHelper.toVector3i(this.spawnPos))
                .add("gameType=" + this.getGameType())
                .add("hardcore=" + this.isHardcore())
                .add("difficulty=" + this.getDifficulty())
                .toString();
    }

    @Inject(method = "isRaining", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onIsRaining(final CallbackInfoReturnable<Boolean> cir) {
        if (this.impl$dimensionType.hasCeiling()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isThundering", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onIsThundering(final CallbackInfoReturnable<Boolean> cir) {
        if (this.impl$dimensionType.hasCeiling()) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public CompoundTag data$getCompound() {
        return this.impl$compound;
    }

    @Override
    public void data$setCompound(final CompoundTag nbt) {
        this.impl$compound = nbt;
    }

    @Redirect(method = "setTagData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldGenSettings;encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/core/RegistryAccess;)Lcom/mojang/serialization/DataResult;"))
    private DataResult<?> impl$onEncodeWorldGenSettings(final DynamicOps<?> $$0, final WorldOptions $$1, final RegistryAccess $$2) {
        final Map<ResourceKey<LevelStem>, LevelStem> dimensions = $$2.lookupOrThrow(Registries.LEVEL_STEM)
            .listElements()
            .collect(Collectors.toMap(Holder.Reference::key, Holder.Reference::value));
        if (this.impl$dimensionType != null && this.impl$chunkGenerator != null) {
            dimensions.computeIfAbsent(ResourceKey.create(Registries.LEVEL_STEM, (ResourceLocation) (Object) this.impl$spongeData.key()),
                $ -> new LevelStem($$2.lookupOrThrow(Registries.DIMENSION_TYPE).wrapAsHolder(this.impl$dimensionType), this.impl$chunkGenerator));
        }
        return WorldGenSettings.encode($$0, $$1, new WorldDimensions(dimensions));
    }
}
