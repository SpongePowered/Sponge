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
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

@Mixin(WorldInfo.class)
public abstract class WorldInfoMixin implements ResourceKeyBridge, WorldInfoBridge {

    @Shadow private String levelName;
    @Shadow private Difficulty difficulty;
    @Shadow public abstract String shadow$getWorldName();
    @Shadow public abstract WorldType shadow$getGenerator();
    @Shadow public abstract int shadow$getSpawnX();
    @Shadow public abstract int shadow$getSpawnY();
    @Shadow public abstract int shadow$getSpawnZ();
    @Shadow public abstract GameType shadow$getGameType();
    @Shadow public abstract boolean shadow$isHardcore();
    @Shadow public abstract Difficulty shadow$getDifficulty();
    @Shadow public abstract void shadow$populateFromWorldSettings(WorldSettings p_176127_1_);

    @Nullable private ResourceKey impl$key;
    private UUID impl$uniqueId = UUID.randomUUID();

    private SpongeDimensionType impl$logicType;
    private InheritableConfigHandle<WorldConfig> impl$configAdapter = SpongeGameConfigs.createDetached();
    private boolean impl$modCreated;

    private final BiMap<Integer, UUID> impl$playerUniqueIdMap = HashBiMap.create();
    private final List<UUID> impl$pendingUniqueIds = new ArrayList<>();
    private int impl$trackedUniqueIdCount = 0;
    private boolean impl$hasCustomDifficulty = false;

    @Override
    public ResourceKey bridge$getKey() {
        return this.impl$key;
    }

    @Override
    public void bridge$setKey(final ResourceKey key) {
        this.impl$key = key;
    }

    @Nullable
    @Override
    public ServerWorld bridge$getWorld() {
        if (!Sponge.isServerAvailable()) {
            return null;
        }

        final ServerWorld world = ((SpongeServer) SpongeCommon.getServer()).getWorldManager().getWorld0(this.bridge$getKey());
        if (world == null) {
            return null;
        }

        if (world.getWorldInfo() != (Object) this) {
            return null;
        }

        return world;
    }

    @Override
    public boolean bridge$isValid() {
        final String levelName = this.shadow$getWorldName();

        return this.impl$key != null && !(levelName == null || levelName.equals("") || levelName.equals("MpServer"));
    }

    @Override
    public boolean bridge$isSinglePlayerProperties() {
        return this.levelName != null && this.levelName.equals("MpServer");
    }

    @Override
    public SpongeDimensionType bridge$getLogicType() {
        return this.impl$logicType;
    }

    @Override
    public void bridge$setLogicType(final SpongeDimensionType dimensionType, boolean updatePlayers) {
        this.impl$logicType = dimensionType;

        if (updatePlayers) {
            final ServerWorld serverWorld = this.bridge$getWorld();

            if (serverWorld != null) {
                ((WorldBridge) serverWorld).bridge$adjustDimensionLogic(dimensionType);
            }
        }
    }

    @Override
    public UUID bridge$getUniqueId() {
        return this.impl$uniqueId;
    }

    @Override
    public void bridge$setUniqueId(final UUID uniqueId) {
        this.impl$uniqueId = uniqueId;
    }

    @Override
    public boolean bridge$hasCustomDifficulty() {
        return this.impl$hasCustomDifficulty;
    }

    @Override
    public void bridge$forceSetDifficulty(final Difficulty difficulty) {
        this.difficulty = difficulty;

        this.impl$updateWorldForDifficultyChange();
    }

    @Override
    public boolean bridge$isEnabled() {
        return this.bridge$getConfigAdapter().get().getWorld().isWorldEnabled();
    }

    @Override
    public void bridge$setEnabled(final boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setWorldEnabled(state);
    }

    @Override
    public boolean bridge$isPVPEnabled() {
        return this.bridge$getConfigAdapter().get().getWorld().getPVPEnabled();
    }

    @Override
    public void bridge$setPVPEnabled(final boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setPVPEnabled(state);
    }

    @Override
    public boolean bridge$doesGenerateBonusChest() {
        return this.impl$configAdapter.get().getWorld().getGenerateBonusChest();
    }

    @Override
    public void bridge$setGenerateBonusChest(final boolean state) {
        this.impl$configAdapter.get().getWorld().setGenerateBonusChest(state);
    }

    @Override
    public boolean bridge$doesLoadOnStartup() {
        return this.bridge$getConfigAdapter().get().getWorld().getLoadOnStartup();
    }

    @Override
    public void bridge$setLoadOnStartup(final boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setLoadOnStartup(state);
    }

    @Override
    public boolean bridge$doesKeepSpawnLoaded() {
        return this.bridge$getConfigAdapter().get().getWorld().getKeepSpawnLoaded();
    }

    @Override
    public void bridge$setKeepSpawnLoaded(final boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setKeepSpawnLoaded(state);
    }

    @Override
    public boolean bridge$doesGenerateSpawnOnLoad() {
        return this.bridge$getConfigAdapter().get().getWorld().getGenerateSpawnOnLoad();
    }

    @Override
    public void bridge$setGenerateSpawnOnLoad(final boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setGenerateSpawnOnLoad(state);
    }

    @Override
    public SerializationBehavior bridge$getSerializationBehavior() {
        return this.impl$configAdapter.get().getWorld().getSerializationBehavior();
    }

    @Override
    public void bridge$setSerializationBehavior(final SerializationBehavior behavior) {
        this.impl$configAdapter.get().getWorld().setSerializationBehavior(behavior);
    }

    @Override
    public boolean bridge$isModCreated() {
        return this.impl$modCreated;
    }

    @Override
    public void bridge$setModCreated(final boolean state) {
        this.impl$modCreated = state;
    }

    @Override
    public InheritableConfigHandle<WorldConfig> bridge$getConfigAdapter() {
        if (this.impl$configAdapter == null) {
            if (this.bridge$isValid()) {
                this.impl$configAdapter = SpongeGameConfigs.createWorld(this.bridge$getLogicType(), this.bridge$getKey());
            } else {
                this.impl$configAdapter = SpongeGameConfigs.createDetached();
            }
        }
        return this.impl$configAdapter;
    }

    @Override
    public void bridge$setConfigAdapter(final InheritableConfigHandle<WorldConfig> adapter) {
        this.impl$configAdapter = Objects.requireNonNull(adapter, "adapter");
    }

    @Override
    public int bridge$getIndexForUniqueId(final UUID uniqueId) {
        final Integer index = this.impl$playerUniqueIdMap.inverse().get(uniqueId);
        if (index != null) {
            return index;
        }

        this.impl$playerUniqueIdMap.put(this.impl$trackedUniqueIdCount, uniqueId);
        this.impl$pendingUniqueIds.add(uniqueId);
        return this.impl$trackedUniqueIdCount++;
    }

    @Override
    public Optional<UUID> bridge$getUniqueIdForIndex(final int index) {
        return Optional.ofNullable(this.impl$playerUniqueIdMap.get(index));
    }

    @Override
    public void bridge$writeSpongeLevelData(final CompoundNBT compound) {
        if (!this.bridge$isValid()) {
            return;
        }

        final CompoundNBT spongeDataCompound = new CompoundNBT();
        spongeDataCompound.putInt(Constants.Sponge.DATA_VERSION, Constants.Sponge.SPONGE_DATA_VERSION);
        spongeDataCompound.putString(Constants.Sponge.World.DIMENSION_TYPE, this.impl$logicType.getKey().toString());
        spongeDataCompound.putUniqueId(Constants.Sponge.World.UNIQUE_ID, this.impl$uniqueId);
        spongeDataCompound.putBoolean(Constants.Sponge.World.IS_MOD_CREATED, this.impl$modCreated);
        spongeDataCompound.putBoolean(Constants.Sponge.World.HAS_CUSTOM_DIFFICULTY, this.impl$hasCustomDifficulty);

        final Iterator<UUID> iter = this.impl$pendingUniqueIds.iterator();
        final ListNBT playerIdList = spongeDataCompound.getList(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, Constants.NBT.TAG_COMPOUND);
        while (iter.hasNext()) {
            final CompoundNBT playerIdCompound = new CompoundNBT();
            compound.putUniqueId(Constants.UUID, iter.next());
            playerIdList.add(playerIdCompound);
            iter.remove();
        }

        compound.put(Constants.Sponge.SPONGE_DATA, spongeDataCompound);
    }

    @Override
    public void bridge$readSpongeLevelData(final CompoundNBT compound) {
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
                    this.shadow$getWorldName(), rawDimensionType, DimensionTypes.OVERWORLD.get().getKey());

                return DimensionTypes.OVERWORLD.get();
        });
        if (spongeDataCompound.hasUniqueId(Constants.Sponge.World.UNIQUE_ID)) {
            this.impl$uniqueId = spongeDataCompound.getUniqueId(Constants.Sponge.World.UNIQUE_ID);
        } else {
            this.impl$uniqueId = UUID.randomUUID();
        }
        this.impl$hasCustomDifficulty = spongeDataCompound.getBoolean(Constants.Sponge.World.HAS_CUSTOM_DIFFICULTY);
        this.impl$modCreated = spongeDataCompound.getBoolean(Constants.Sponge.World.IS_MOD_CREATED);
        this.impl$trackedUniqueIdCount = 0;
        if (spongeDataCompound.contains(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, Constants.NBT.TAG_LIST)) {
            final ListNBT playerIdList = spongeDataCompound.getList(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, Constants.NBT.TAG_COMPOUND);
            final Iterator<INBT> iter = playerIdList.iterator();
            while (iter.hasNext()) {
                final CompoundNBT playerIdComponent = (CompoundNBT) iter.next();
                final UUID playerUuid = playerIdComponent.getUniqueId(Constants.UUID);
                final Integer playerIndex = this.impl$playerUniqueIdMap.inverse().get(playerUuid);
                if (playerIndex == null) {
                    this.impl$playerUniqueIdMap.put(this.impl$trackedUniqueIdCount++, playerUuid);
                } else {
                    iter.remove();
                }
            }
        }
    }

    @Redirect(method = "<init>(Lnet/minecraft/world/WorldSettings;Ljava/lang/String;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/storage/WorldInfo;populateFromWorldSettings(Lnet/minecraft/world/WorldSettings;)V"
            )
    )
    private void impl$setupBeforeSettingsPopulation(final WorldInfo info, final WorldSettings settings,
            final WorldSettings settingsB, final String levelName) {
        this.levelName = levelName;
        this.shadow$populateFromWorldSettings(settingsB);
    }

    @Inject(method = "populateFromWorldSettings", at = @At("TAIL"))
    private void impl$fillInfo(final WorldSettings settings, final CallbackInfo ci) {
        ((WorldSettingsBridge) (Object) settings).bridge$populateInfo((WorldInfo) (Object) this);
    }

    /**
     * @author zidane - November 27th, 2020 - Minecraft 1.15.2
     * @reason Flag using setDifficulty as an explicit set and mark it as custom
     */
    @Overwrite
    public void setDifficulty(Difficulty newDifficulty) {
        if ((Object) this instanceof DerivedWorldInfo) {
            return;
        }

        this.impl$hasCustomDifficulty = true;
        this.difficulty = newDifficulty;

        this.impl$updateWorldForDifficultyChange();
    }

    public void impl$updateWorldForDifficultyChange() {
        final ServerWorld serverWorld = this.bridge$getWorld();

        if (serverWorld == null) {
            return;
        }

        final MinecraftServer server = serverWorld.getServer();

        if (this.difficulty == Difficulty.HARD) {
            serverWorld.setAllowedSpawnTypes(true, true);
        } else if (server.isSinglePlayer()) {
            serverWorld.setAllowedSpawnTypes(this.difficulty != Difficulty.PEACEFUL, true);
        } else {
            serverWorld.setAllowedSpawnTypes(((MinecraftServerAccessor) server).accessor$isSpawningMonsters(), server.getCanSpawnAnimals());
        }

        serverWorld
                .getPlayers()
                .forEach(player -> player.connection.sendPacket(new SServerDifficultyPacket(this.difficulty, ((WorldInfo) (Object) this)
                        .isDifficultyLocked())));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", WorldInfo.class.getSimpleName() + "[", "]")
                .add("key=" + this.impl$key)
                .add("uniqueId=" + this.impl$uniqueId)
                .add("dimensionType=" + this.impl$logicType)
                .add("generator=" + this.shadow$getGenerator())
                .add("modCreated=" + this.impl$modCreated)
                .add("spawnX=" + this.shadow$getSpawnX())
                .add("spawnY=" + this.shadow$getSpawnY())
                .add("spawnZ=" + this.shadow$getSpawnZ())
                .add("gameType=" + this.shadow$getGameType())
                .add("hardcore=" + this.shadow$isHardcore())
                .add("difficulty=" + this.shadow$getDifficulty())
                .toString();
    }
}
