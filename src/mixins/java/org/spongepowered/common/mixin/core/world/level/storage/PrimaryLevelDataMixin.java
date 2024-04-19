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
import com.mojang.serialization.Dynamic;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.data.fixer.LegacyUUIDCodec;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MapUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataMixin implements WorldData, PrimaryLevelDataBridge, ResourceKeyBridge {

    // @formatter:off
    @Shadow private LevelSettings settings;
    @Shadow private int xSpawn;
    @Shadow private int ySpawn;
    @Shadow private int zSpawn;
    @Shadow private float spawnAngle;

    @Shadow public abstract boolean shadow$isDifficultyLocked();
    @Shadow public abstract void shadow$setSpawn(BlockPos p_176143_1_, float p_176143_2_);
    // @formatter:on

    @Nullable private ResourceKey impl$key;
    private DimensionType impl$dimensionType;
    @Nullable private SerializationBehavior impl$serializationBehavior;
    @Nullable private Component impl$displayName;
    @Nullable private Integer impl$viewDistance;
    private UUID impl$uniqueId = UUID.randomUUID();
    private Boolean impl$pvp;
    private InheritableConfigHandle<WorldConfig> impl$configAdapter;

    private final BiMap<Integer, UUID> impl$playerUniqueIdMap = HashBiMap.create();

    private boolean impl$customDifficulty = false, impl$customGameType = false, impl$customSpawnPosition = false, impl$loadOnStartup, impl$performsSpawnLogic;

    private BiMap<Integer, UUID> impl$mapUUIDIndex = HashBiMap.create();

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

    @Override
    public @Nullable ServerLevel bridge$world() {
        if (!Sponge.isServerAvailable()) {
            return null;
        }

        final ServerLevel world = SpongeCommon.server().getLevel(SpongeWorldManager.createRegistryKey(this.impl$key));
        if (world == null) {
            return null;
        }

        final ServerLevelData levelData = (ServerLevelData) world.getLevelData();
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
        this.settings = this.settings.withDifficulty(difficulty);
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
        this.bridge$triggerViewDistanceLogic();
    }

    @Override
    public void bridge$triggerViewDistanceLogic() {
        final ServerLevel world = this.bridge$world();
        if (world != null) {
            final int actual = this.impl$viewDistance == null ? world.getServer().getPlayerList().getViewDistance() : this.impl$viewDistance;
            world.getChunkSource().setViewDistance(actual);
            final ClientboundSetChunkCacheRadiusPacket packet = new ClientboundSetChunkCacheRadiusPacket(actual);

            world.players().forEach(p -> p.connection.send(packet));
        }
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
    public void bridge$populateFromLevelStem(final LevelStem dimension) {
        final LevelStemBridge levelStemBridge = (LevelStemBridge) (Object) dimension;
        this.impl$dimensionType = dimension.type().value();
        this.impl$displayName = levelStemBridge.bridge$displayName();
        final Difficulty difficulty = levelStemBridge.bridge$difficulty();
        final GameType gameType = levelStemBridge.bridge$gameMode();
        final Boolean isHardcore = levelStemBridge.bridge$hardcore();
        if (difficulty != null) {
            this.impl$customDifficulty = true;
        }
        if (gameType != null) {
            this.impl$customGameType = true;
        }
        this.settings = new LevelSettings(this.settings.levelName(),
                gameType == null ? this.settings.gameType() : gameType,
                this.settings.allowCommands(),
                difficulty == null ? this.settings.difficulty() : difficulty,
                isHardcore == null ? this.settings.hardcore() : isHardcore,
                this.settings.gameRules(),
                this.settings.getDataConfiguration());

        final Vector3i spawnPos = levelStemBridge.bridge$spawnPosition();
        if (spawnPos != null) {
            this.shadow$setSpawn(VecHelper.toBlockPos(spawnPos), this.spawnAngle);
            this.impl$customSpawnPosition = true;
        }

        this.impl$serializationBehavior = levelStemBridge.bridge$serializationBehavior();
        this.impl$pvp = levelStemBridge.bridge$pvp();
        this.impl$loadOnStartup = levelStemBridge.bridge$loadOnStartup();
        this.impl$performsSpawnLogic = levelStemBridge.bridge$performsSpawnLogic();
        this.impl$viewDistance = levelStemBridge.bridge$viewDistance();
    }

    @Override
    public void bridge$setMapUUIDIndex(final BiMap<Integer, UUID> index) {
        this.impl$mapUUIDIndex = index;
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

    @Override
    public ServerLevelData overworldData() {
        if (Level.OVERWORLD.location().equals(this.impl$key)) {
            return (ServerLevelData) this;
        }

        return (ServerLevelData) SpongeCommon.server().getLevel(Level.OVERWORLD).getLevelData();
    }

    void impl$updateWorldForDifficultyChange(final ServerLevel world, final boolean isLocked) {
        if (world == null) {
            return;
        }

        final MinecraftServer server = world.getServer();
        final Difficulty difficulty = ((LevelData) this).getDifficulty();

        if (difficulty == Difficulty.HARD) {
            world.setSpawnSettings(true, true);
        } else if (server.isSingleplayer()) {
            world.setSpawnSettings(difficulty != Difficulty.PEACEFUL, true);
        } else {
            world.setSpawnSettings(((MinecraftServerAccessor) server).invoker$isSpawningMonsters(), server.isSpawningAnimals());
        }

        world.players().forEach(player -> player.connection.send(new ClientboundChangeDifficultyPacket(difficulty, isLocked)));
    }

    @Override
    @SuppressWarnings("deprecated")
    public void bridge$readSpongeLevelData(final Dynamic<Tag> dynamic) {
        if (dynamic == null) {
            this.bridge$setUniqueId(UUID.randomUUID());
            return;
        }

        this.bridge$setUniqueId(dynamic.get(Constants.Sponge.World.UNIQUE_ID).read(UUIDUtil.CODEC).result().orElse(UUID.randomUUID()));

        final List<Pair<String, UUID>> mapIndexList = dynamic.get(Constants.Map.MAP_UUID_INDEX).readMap(Codec.STRING, UUIDUtil.CODEC).result().orElse(Collections.emptyList());
        final BiMap<Integer, UUID> mapIndex = HashBiMap.create();
        for (final Pair<String, UUID> pair : mapIndexList) {
            final int id = Integer.parseInt(pair.getFirst());
            mapIndex.put(id, pair.getSecond());
        }
        this.bridge$setMapUUIDIndex(mapIndex);

        // TODO Move this to Schema
        dynamic.get(Constants.Sponge.LEGACY_SPONGE_PLAYER_UUID_TABLE).readList(LegacyUUIDCodec.CODEC).result().orElseGet(() ->
            dynamic.get(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE).readList(UUIDUtil.CODEC).result().orElse(Collections.emptyList())
        ).forEach(uuid -> this.impl$playerUniqueIdMap.inverse().putIfAbsent(uuid, this.impl$playerUniqueIdMap.size()));
    }

    @Override
    public CompoundTag bridge$writeSpongeLevelData() {
        final CompoundTag data = new CompoundTag();
        data.putUUID(Constants.Sponge.World.UNIQUE_ID, this.bridge$uniqueId());

        // Map Storage
        final CompoundTag mapUUIDIndexTag = new CompoundTag();
        MapUtil.saveMapUUIDIndex(mapUUIDIndexTag, this.bridge$getMapUUIDIndex());
        data.put(Constants.Map.MAP_UUID_INDEX, mapUUIDIndexTag);

        final ListTag playerIdList = new ListTag();
        data.put(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, playerIdList);
        this.impl$playerUniqueIdMap.values().forEach(uuid -> playerIdList.add(new IntArrayTag(UUIDUtil.uuidToIntArray(uuid))));

        return data;
    }

    @Override
    public void bridge$hardcore(final boolean hardcore) {
        this.settings = new LevelSettings(this.settings.levelName(), this.settings.gameType(), hardcore, this.settings.difficulty(), this.settings.allowCommands(), this.settings.gameRules(), this.settings.getDataConfiguration());
    }

    @Override
    public void bridge$allowCommands(final boolean commands) {
        this.settings = new LevelSettings(this.settings.levelName(), this.settings.gameType(), this.settings.allowCommands(), this.settings.difficulty(), commands, this.settings.gameRules(), this.settings.getDataConfiguration());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PrimaryLevelData.class.getSimpleName() + "[", "]")
                .add("key=" + this.impl$key)
                .add("worldType=" + this.impl$dimensionType)
                .add("uniqueId=" + this.impl$uniqueId)
                .add("spawn=" + new Vector3i(this.xSpawn, this.ySpawn, this.zSpawn))
                .add("gameType=" + ((ServerLevelData) this).getGameType())
                .add("hardcore=" + ((LevelData) this).isHardcore())
                .add("difficulty=" + ((LevelData) this).getDifficulty())
                .toString();
    }
}
