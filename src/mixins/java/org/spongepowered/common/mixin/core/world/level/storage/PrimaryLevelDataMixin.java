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
import net.kyori.adventure.text.Component;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.server.SpongeServerLevelData;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataMixin implements ServerLevelData, WorldData, PrimaryLevelDataBridge, DataCompoundHolder {

    // @formatter:off
    @Shadow private LevelSettings settings;
    @Shadow private LevelData.RespawnData respawnData;

    @Shadow public abstract boolean shadow$isDifficultyLocked();
    @Shadow public abstract void shadow$setSpawn(LevelData.RespawnData $$0);
    // @formatter:on

    private SpongeServerLevelData impl$spongeData = new SpongeServerLevelData();

    private DimensionType impl$dimensionType;
    private ChunkGenerator impl$chunkGenerator;

    private boolean impl$customDifficulty = false, impl$customGameType = false, impl$customSpawnPosition = false;
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
    public @Nullable ChunkGenerator bridge$chunkGenerator() {
        return this.impl$chunkGenerator;
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
        final var difficultySettings = new LevelSettings.DifficultySettings(
            difficulty == null ? this.settings.difficultySettings().difficulty() : difficulty,
            isHardcore == null ? this.settings.difficultySettings().hardcore() : isHardcore,
            false
        );
        this.settings = new LevelSettings(
            this.settings.levelName(),
            gameType == null ? this.settings.gameType() : gameType,
            difficultySettings,
            allowCommands == null ? this.settings.allowCommands() : allowCommands,
            this.settings.dataConfiguration());

        final Vector3i spawnPos = bridge.bridge$spawnPosition();
        if (spawnPos != null) {
            this.shadow$setSpawn(new RespawnData(new GlobalPos(this.respawnData.dimension(), VecHelper.toBlockPos(spawnPos)),this.respawnData.pitch(), this.respawnData.yaw()));
            this.impl$customSpawnPosition = true;
        }

        Optional.ofNullable(bridge.bridge$serializationBehavior()).ifPresent(this::bridge$setSerializationBehavior);
        Optional.ofNullable(bridge.bridge$pvp()).ifPresent(this::bridge$setPvp);
        this.bridge$setLoadOnStartup(bridge.bridge$loadOnStartup());
        this.bridge$setPerformsSpawnLogic(bridge.bridge$performsSpawnLogic());
        this.bridge$setViewDistance(bridge.bridge$viewDistance());
    }

    @Override
    public int bridge$getIndexForUniqueId(final UUID uuid) {
        final BiMap<Integer, UUID> playerUniqueIds = this.impl$spongeData.playerUniqueIds();
        final Integer index = playerUniqueIds.inverse().get(uuid);
        if (index != null) {
            return index;
        }

        final int newIndex = playerUniqueIds.size();
        playerUniqueIds.put(newIndex, uuid);
        return newIndex;
    }

    @Override
    public Optional<UUID> bridge$getUniqueIdForIndex(final int index) {
        return Optional.ofNullable(this.impl$spongeData.playerUniqueIds().get(index));
    }

    void impl$updateWorldForDifficultyChange(final ServerLevel level, final boolean isLocked) {
        final MinecraftServer server = level.getServer();
        final Difficulty difficulty = this.getDifficulty();

        if (difficulty == Difficulty.HARD) {
            level.setSpawnSettings(true); // set spawn enemies true
        } else if (server.isSingleplayer()) {
            level.setSpawnSettings(difficulty != Difficulty.PEACEFUL);
        } else {
            level.setSpawnSettings(server.getGameRules().get(GameRules.SPAWN_MONSTERS));
        }

        level.players().forEach(player -> player.connection.send(new ClientboundChangeDifficultyPacket(difficulty, isLocked)));
    }

    @Override
    public void bridge$hardcore(final boolean hardcore) {
        final var oldDifficulty = this.settings.difficultySettings();
        final var newDifficulty = new LevelSettings.DifficultySettings(oldDifficulty.difficulty(), hardcore, oldDifficulty.locked());
        this.settings = new LevelSettings(
            this.settings.levelName(),
            this.settings.gameType(),
            newDifficulty,
            this.settings.allowCommands(),
            this.settings.dataConfiguration()
        );
    }

    @Override
    public void bridge$allowCommands(final boolean allowCommands) {
        final var oldDifficulty = this.settings.difficultySettings();
        this.settings = new LevelSettings(
            this.settings.levelName(),
            this.settings.gameType(),
            oldDifficulty,
            allowCommands,
            this.settings.dataConfiguration()
        );
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PrimaryLevelData.class.getSimpleName() + "[", "]")
                .add("key=" + this.impl$spongeData.key())
                .add("worldType=" + this.impl$dimensionType)
                .add("uniqueId=" + this.impl$spongeData.uniqueId())
                .add("spawn=" + this.respawnData.toString())
                .add("gameType=" + this.getGameType())
                .add("hardcore=" + this.isHardcore())
                .add("difficulty=" + this.getDifficulty())
                .toString();
    }
    @Override
    public CompoundTag data$getCompound() {
        return this.impl$compound;
    }

    @Override
    public void data$setCompound(final CompoundTag nbt) {
        this.impl$compound = nbt;
    }

    // LevelStem persistence for runtime-created worlds is now handled by SpongeWorldManager.createLevel()
    // which adds the LevelStem to WorldGenSettings.dimensions() (a SavedData in server-level SavedDataStorage).
}
