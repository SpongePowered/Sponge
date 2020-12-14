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
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.util.Constants;

import java.util.Iterator;
import java.util.UUID;

@Mixin(IServerWorldInfo.class)
public interface IServerWorldInfoMixin extends ISpawnWorldInfoMixin, WorldInfoBridge, ResourceKeyBridge {

    // @formatter:off
    @Shadow String shadow$getLevelName();
    @Shadow GameType shadow$getGameType();
    // @formatter:on

    @Override
    default boolean bridge$isValid() {
        final String levelName = this.shadow$getLevelName();

        return this.bridge$getKey() != null && !(levelName == null || levelName.equals("") || levelName.equals("MpServer"));
    }

    @Override
    default boolean bridge$isSinglePlayerProperties() {
        return this.shadow$getLevelName() != null && this.shadow$getLevelName().equals("MpServer");
    }

    @Override
    default boolean bridge$isEnabled() {
        return this.bridge$getConfigAdapter().get().getWorld().isWorldEnabled();
    }

    @Override
    default void bridge$setEnabled(final boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setWorldEnabled(state);
    }

    @Override
    default boolean bridge$isPVPEnabled() {
        return this.bridge$getConfigAdapter().get().getWorld().getPVPEnabled();
    }

    @Override
    default void bridge$setPVPEnabled(final boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setPVPEnabled(state);
    }

    @Override
    default boolean bridge$doesGenerateBonusChest() {
        return this.bridge$getConfigAdapter().get().getWorld().getGenerateBonusChest();
    }

    @Override
    default void bridge$setGenerateBonusChest(final boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setGenerateBonusChest(state);
    }

    @Override
    default boolean bridge$doesLoadOnStartup() {
        return this.bridge$getConfigAdapter().get().getWorld().getLoadOnStartup();
    }

    @Override
    default void bridge$setLoadOnStartup(final boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setLoadOnStartup(state);
    }

    @Override
    default boolean bridge$doesKeepSpawnLoaded() {
        return this.bridge$getConfigAdapter().get().getWorld().getKeepSpawnLoaded();
    }

    @Override
    default void bridge$setKeepSpawnLoaded(final boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setKeepSpawnLoaded(state);
    }

    @Override
    default boolean bridge$doesGenerateSpawnOnLoad() {
        return this.bridge$getConfigAdapter().get().getWorld().getGenerateSpawnOnLoad();
    }

    @Override
    default void bridge$setGenerateSpawnOnLoad(final boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setGenerateSpawnOnLoad(state);
    }


    @Override
    default SerializationBehavior bridge$getSerializationBehavior() {
        return this.bridge$getConfigAdapter().get().getWorld().getSerializationBehavior();
    }

    @Override
    default void bridge$setSerializationBehavior(final SerializationBehavior behavior) {
        this.bridge$getConfigAdapter().get().getWorld().setSerializationBehavior(behavior);
    }

    default void impl$updateWorldForDifficultyChange(ServerWorld serverWorld, boolean isLocked) {
        if (serverWorld == null) {
            return;
        }

        final MinecraftServer server = serverWorld.getServer();
        final Difficulty difficulty = this.shadow$getDifficulty();

        if (difficulty == Difficulty.HARD) {
            serverWorld.setSpawnSettings(true, true);
        } else if (server.isSingleplayer()) {
            serverWorld.setSpawnSettings(difficulty != Difficulty.PEACEFUL, true);
        } else {
            serverWorld.setSpawnSettings(((MinecraftServerAccessor) server).invoker$isSpawningMonsters(), server.isSpawningAnimals());
        }

        serverWorld.players().forEach(player -> player.connection.send(new SServerDifficultyPacket(difficulty, isLocked)));
    }

    @Nullable
    @Override
    default ServerWorld bridge$getWorld() {
        if (!Sponge.isServerAvailable()) {
            return null;
        }

        final ServerWorld world = ((SpongeServer) SpongeCommon.getServer()).getWorldManager().getWorld0(this.bridge$getKey());
        if (world == null) {
            return null;
        }

        final IServerWorldInfo levelData = ((ServerWorldBridge) world).bridge$getServerLevelData();
        if (levelData != this) {
            return null;
        }

        return world;
    }

    @Override
    default void bridge$writeSpongeLevelData(final CompoundNBT compound) {
        if (!this.bridge$isValid()) {
            return;
        }

        final CompoundNBT spongeDataCompound = new CompoundNBT();
        spongeDataCompound.putInt(Constants.Sponge.DATA_VERSION, Constants.Sponge.SPONGE_DATA_VERSION);
        spongeDataCompound.putString(Constants.Sponge.World.DIMENSION_TYPE, this.impl$logicType.getKey().toString());
        spongeDataCompound.putUUID(Constants.Sponge.World.UNIQUE_ID, this.bridge$getUniqueId());
        spongeDataCompound.putBoolean(Constants.Sponge.World.IS_MOD_CREATED, this.bridge$isModCreated());
        spongeDataCompound.putBoolean(Constants.Sponge.World.HAS_CUSTOM_DIFFICULTY, this.bridge$hasCustomDifficulty());
        // TODO write custom difficulty
        this.impl$customDifficulty

        this.bridge$writeTrackedPlayerTable(spongeDataCompound);

        compound.put(Constants.Sponge.SPONGE_DATA, spongeDataCompound);
    }

    @Override
    default void bridge$readSpongeLevelData(final CompoundNBT compound) {
        if (!compound.contains(Constants.Sponge.SPONGE_DATA)) {
            // TODO Minecraft 1.15 - Bad Sponge level data...warn/crash?
            return;
        }

        // TODO TODO Minecraft 1.15 - Run DataFixer on the SpongeData compound

        final CompoundNBT spongeDataCompound = compound.getCompound(Constants.Sponge.SPONGE_DATA);

        final String rawDimensionType = spongeDataCompound.getString(Constants.Sponge.World.DIMENSION_TYPE);
        this.impl$logicType = (SpongeDimensionType) SpongeCommon.getRegistry().getCatalogRegistry().get(org.spongepowered.api.world.dimension
                .DimensionType.class, ResourceKey.resolve(rawDimensionType)).orElseGet(() -> {
            SpongeCommon.getLogger().warn("WorldProperties '{}' specifies dimension type '{}' which does not exist, defaulting to '{}'",
                    this.shadow$getLevelName(), rawDimensionType, World.OVERWORLD.location());

            return DimensionTypes.OVERWORLD.get();
        });
        if (spongeDataCompound.hasUUID(Constants.Sponge.World.UNIQUE_ID)) {
            this.bridge$setUniqueId(spongeDataCompound.getUUID(Constants.Sponge.World.UNIQUE_ID));
        } else {
            this.bridge$setUniqueId(UUID.randomUUID());
        }

        if (spongeDataCompound.getBoolean(Constants.Sponge.World.HAS_CUSTOM_DIFFICULTY)) {
            // TODO read custom difficulty
            this.bridge$forceSetDifficulty(this.shadow$getDifficulty());
        }
        this.bridge$setModCreated(spongeDataCompound.getBoolean(Constants.Sponge.World.IS_MOD_CREATED));

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

}
