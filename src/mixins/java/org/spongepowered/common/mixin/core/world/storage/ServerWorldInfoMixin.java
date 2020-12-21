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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraft.world.storage.ServerWorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.world.WorldSettingsAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.storage.IServerWorldInfoBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.server.SpongeWorldManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

@Mixin(ServerWorldInfo.class)
public abstract class ServerWorldInfoMixin implements IServerConfiguration, IServerWorldInfoBridge, ResourceKeyBridge {

    // @formatter:off
    @Shadow public abstract boolean shadow$isDifficultyLocked();
    // @formatter:on

    @Shadow private WorldSettings settings;
    @Nullable private ResourceKey impl$key;
    private String impl$rawDimensionType;
    private DimensionType impl$dimensionType;
    private UUID impl$uniqueId = UUID.randomUUID();
    private boolean impl$hasCustomDifficulty = false;

    private InheritableConfigHandle<WorldConfig> impl$configAdapter = SpongeGameConfigs.createDetached();
    private final BiMap<Integer, UUID> impl$playerUniqueIdMap = HashBiMap.create();
    private final List<UUID> impl$pendingUniqueIds = new ArrayList<>();
    private int impl$trackedUniqueIdCount = 0;

    // ResourceKeyBridge

    public ResourceKey bridge$getKey() {
        return this.impl$key;
    }

    public void bridge$setKey(final ResourceKey key) {
        this.impl$key = key;
    }

    @Nullable
    public ServerWorld bridge$getWorld() {
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
    public DimensionType bridge$getDimensionType() {
        if (this.impl$dimensionType == null) {
            this.impl$dimensionType = SpongeCommon.getServer().registryAccess().dimensionTypes().getOptional(new ResourceLocation(this.impl$rawDimensionType))
                    .orElseGet(() -> {
                        SpongeCommon.getLogger().warn("Level data '{}' specifies dimension type '{}' which does not exist, defaulting to '{}'",
                                ((IServerWorldInfo) this).getLevelName(), this.impl$dimensionType, World.OVERWORLD.location());

                        return SpongeCommon.getServer().registryAccess().dimensionTypes().get(DimensionType.OVERWORLD_LOCATION);
                    });
        }
        return this.impl$dimensionType;
    }

    @Override
    public void bridge$setDimensionType(final DimensionType type, boolean updatePlayers) {
        this.impl$dimensionType = type;
    }

    // WorldInfoBridge

    public UUID bridge$getUniqueId() {
        return this.impl$uniqueId;
    }

    public void bridge$setUniqueId(final UUID uniqueId) {
        this.impl$uniqueId = uniqueId;
    }

    public boolean bridge$hasCustomDifficulty() {
        return this.impl$hasCustomDifficulty;
    }

    public void bridge$forceSetDifficulty(final Difficulty difficulty) {
        this.impl$hasCustomDifficulty = true;
        ((WorldSettingsAccessor) (Object) this.settings).accessor$difficulty(difficulty);
        this.impl$updateWorldForDifficultyChange(this.bridge$getWorld(), this.shadow$isDifficultyLocked());
    }

    public InheritableConfigHandle<WorldConfig> bridge$getConfigAdapter() {
        if (this.impl$configAdapter == null) {
            if (this.bridge$isValid()) {
                this.impl$configAdapter = SpongeGameConfigs.createWorld(null, this.bridge$getKey());
            } else {
                this.impl$configAdapter = SpongeGameConfigs.createDetached();
            }
        }
        return this.impl$configAdapter;
    }

    public void bridge$setConfigAdapter(final InheritableConfigHandle<WorldConfig> adapter) {
        this.impl$configAdapter = Objects.requireNonNull(adapter, "adapter");
    }

    public int bridge$getIndexForUniqueId(final UUID uniqueId) {
        final Integer index = this.impl$playerUniqueIdMap.inverse().get(uniqueId);
        if (index != null) {
            return index;
        }

        this.impl$playerUniqueIdMap.put(this.impl$trackedUniqueIdCount, uniqueId);
        this.impl$pendingUniqueIds.add(uniqueId);
        return this.impl$trackedUniqueIdCount++;
    }

    public Optional<UUID> bridge$getUniqueIdForIndex(final int index) {
        return Optional.ofNullable(this.impl$playerUniqueIdMap.get(index));
    }

    public void bridge$writeTrackedPlayerTable(CompoundNBT spongeDataCompound) {
        final Iterator<UUID> iter = this.impl$pendingUniqueIds.iterator();
        final ListNBT playerIdList = spongeDataCompound.getList(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, Constants.NBT.TAG_COMPOUND);
        while (iter.hasNext()) {
            final CompoundNBT playerIdCompound = new CompoundNBT();
            playerIdCompound.putUUID(Constants.UUID, iter.next());
            playerIdList.add(playerIdCompound);
            iter.remove();
        }
    }

    public void bridge$writeSpongeLevelData(final CompoundNBT compound) {
        if (!this.bridge$isValid()) {
            return;
        }

        final CompoundNBT spongeDataCompound = new CompoundNBT();
        spongeDataCompound.putInt(Constants.Sponge.DATA_VERSION, Constants.Sponge.SPONGE_DATA_VERSION);
        final DimensionType dimensionType = this.bridge$getDimensionType();
        final ResourceLocation dimensionTypeKey = SpongeCommon.getServer().registryAccess().dimensionTypes().getKey(this.impl$dimensionType);
        spongeDataCompound.putString(Constants.Sponge.World.DIMENSION_TYPE, dimensionTypeKey.toString());
        spongeDataCompound.putUUID(Constants.Sponge.World.UNIQUE_ID, this.bridge$getUniqueId());
        spongeDataCompound.putBoolean(Constants.Sponge.World.HAS_CUSTOM_DIFFICULTY, this.bridge$hasCustomDifficulty());

        this.bridge$writeTrackedPlayerTable(spongeDataCompound);

        compound.put(Constants.Sponge.SPONGE_DATA, spongeDataCompound);
    }

    public void bridge$readSpongeLevelData(final CompoundNBT compound) {
        if (!compound.contains(Constants.Sponge.SPONGE_DATA)) {
            // TODO Minecraft 1.16 - Bad Sponge level data...warn/crash?
            return;
        }

        // TODO Minecraft 1.16 - Run DataFixer on the SpongeData compound

        final CompoundNBT spongeDataCompound = compound.getCompound(Constants.Sponge.SPONGE_DATA);

        this.impl$rawDimensionType = spongeDataCompound.getString(Constants.Sponge.World.DIMENSION_TYPE);

        if (spongeDataCompound.hasUUID(Constants.Sponge.World.UNIQUE_ID)) {
            this.bridge$setUniqueId(spongeDataCompound.getUUID(Constants.Sponge.World.UNIQUE_ID));
        } else {
            this.bridge$setUniqueId(UUID.randomUUID());
        }

        if (spongeDataCompound.getBoolean(Constants.Sponge.World.HAS_CUSTOM_DIFFICULTY)) {
            this.bridge$forceSetDifficulty(((IWorldInfo) this).getDifficulty());
        }

        this.impl$trackedUniqueIdCount = 0;
        if (spongeDataCompound.contains(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, Constants.NBT.TAG_LIST)) {
            final ListNBT playerIdList = spongeDataCompound.getList(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, Constants.NBT.TAG_COMPOUND);
            final Iterator<INBT> iter = playerIdList.iterator();
            while (iter.hasNext()) {
                final CompoundNBT playerIdComponent = (CompoundNBT) iter.next();
                final UUID playerUuid = playerIdComponent.getUUID(Constants.UUID);
                final Integer playerIndex = this.impl$playerUniqueIdMap.inverse().get(playerUuid);
                if (playerIndex == null) {
                    this.impl$playerUniqueIdMap.put(this.impl$trackedUniqueIdCount++, playerUuid);
                } else {
                    iter.remove();
                }
            }
        }
    }

    @Override
    public IServerWorldInfo overworldData() {
        if (World.OVERWORLD.location().equals(this.impl$key)) {
            return (IServerWorldInfo) this;
        }

        return (IServerWorldInfo) SpongeCommon.getServer().getLevel(World.OVERWORLD).getLevelData();
    }

    void impl$updateWorldForDifficultyChange(final ServerWorld serverWorld, final boolean isLocked) {
        if (serverWorld == null) {
            return;
        }

        final MinecraftServer server = serverWorld.getServer();
        final Difficulty difficulty = ((IWorldInfo) this).getDifficulty();

        if (difficulty == Difficulty.HARD) {
            serverWorld.setSpawnSettings(true, true);
        } else if (server.isSingleplayer()) {
            serverWorld.setSpawnSettings(difficulty != Difficulty.PEACEFUL, true);
        } else {
            serverWorld.setSpawnSettings(((MinecraftServerAccessor) server).invoker$isSpawningMonsters(), server.isSpawningAnimals());
        }

        serverWorld.players().forEach(player -> player.connection.send(new SServerDifficultyPacket(difficulty, isLocked)));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ServerWorldInfo.class.getSimpleName() + "[", "]")
                .add("key=" + this.impl$key)
                .add("dimensionType=" + this.impl$dimensionType)
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
