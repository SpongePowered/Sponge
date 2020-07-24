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

import com.google.common.base.MoreObjects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.InheritableConfigHandle;
import org.spongepowered.common.config.SpongeConfigs;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.dimension.SpongeDimensionType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    @Nullable private DimensionType impl$dimensionType;

    private UUID impl$uniqueId;
    private SpongeDimensionType impl$logicType;
    private InheritableConfigHandle<WorldConfig> impl$configAdapter;
    private boolean impl$generateBonusChest;
    private boolean impl$modCreated;
    @Nullable private SerializationBehavior impl$serializationBehavior;

    private final BiMap<Integer, UUID> impl$playerUniqueIdMap = HashBiMap.create();
    private final List<UUID> impl$pendingUniqueIds = new ArrayList<>();
    private int impl$trackedUniqueIdCount = 0;
    private boolean impl$hasCustomDifficulty = false;

    @Redirect(method = "<init>(Lnet/minecraft/world/WorldSettings;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;populateFromWorldSettings(Lnet/minecraft/world/WorldSettings;)V"))
    private void impl$setupBeforeSettingsPopulation(WorldInfo info, WorldSettings settings, WorldSettings settingsB, String levelName) {
        this.levelName = levelName;
        this.shadow$populateFromWorldSettings(settings);
    }

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

        final ServerWorld world = (ServerWorld) Sponge.getServer().getWorldManager().getWorld(this.bridge$getKey()).orElse(null);
        if (world == null) {
            return null;
        }

        if (world.getWorldInfo() != (WorldInfo) (Object) this) {
            return null;
        }

        return world;
    }

    @Override
    public void bridge$changeDimensionLogicType(org.spongepowered.api.world.dimension.DimensionType dimensionType) {
        ((DimensionTypeBridge) this.bridge$getDimensionType()).bridge$setSpongeDimensionType((SpongeDimensionType) dimensionType);
        ((WorldInfoBridge) this).bridge$setLogicType(dimensionType);

        final ServerWorld world = this.bridge$getWorld();
        if (world != null) {
            ((WorldBridge) world).bridge$changeDimension((SpongeDimensionType) dimensionType);

            for (final ServerPlayerEntity player : world.getPlayers()) {
                ((ServerPlayerEntityBridge) player).bridge$sendViewerEnvironment(dimensionType);
            }
        }
    }

    @Override
    public boolean bridge$isValid() {
        final String levelName = this.shadow$getWorldName();

        return this.impl$key != null && this.impl$dimensionType != null && !(levelName == null || levelName.equals("") || levelName.equals("MpServer") || levelName.equals("sponge$dummy_world"));
    }

    @Override
    public void bridge$setDimensionType(final DimensionType type) {
        this.impl$dimensionType = type;
        this.impl$logicType = ((DimensionTypeBridge) this.impl$dimensionType).bridge$getSpongeDimensionType();
    }

    @Override
    public SpongeDimensionType bridge$getLogicType() {
        return this.impl$logicType;
    }

    @Override
    public void bridge$setLogicType(final org.spongepowered.api.world.dimension.DimensionType type) {
        this.impl$logicType = (SpongeDimensionType) type;
    }

    @Override
    public void bridge$setUniqueId(UUID uniqueId) {
        this.impl$uniqueId = uniqueId;
    }

    @Inject(method = "setDifficulty", at = @At("HEAD"), cancellable = true)
    private void impl$onSetDifficultyVanilla(@Nullable final Difficulty newDifficulty, final CallbackInfo ci) {
        if (newDifficulty == null) {
            // This is an error from someone
            new PrettyPrinter(60).add("Null Difficulty being set!").centre().hr()
                .add("Someone (not Sponge) is attempting to set a null difficulty to this WorldInfo setup! Please report to the mod/plugin author!")
                .add()
                .addWrapped(60, " %s : %s", "WorldInfo", this)
                .add()
                .add(new Exception("Stacktrace"))
                .log(SpongeCommon.getLogger(), Level.ERROR);
            ci.cancel(); // We cannot let the null set the field.
            return;
        }

        this.impl$hasCustomDifficulty = true;
        this.difficulty = newDifficulty;

        //this.bridge$updatePlayersForDifficulty();
    }

    @Override
    public boolean bridge$hasCustomDifficulty() {
        return this.impl$hasCustomDifficulty;
    }

    @Override
    public void bridge$forceSetDifficulty(final Difficulty difficulty) {
        this.difficulty = difficulty;

        this.bridge$updatePlayersForDifficulty();
    }

    @Override
    public void bridge$updatePlayersForDifficulty() {
        ServerWorld serverWorld = null;
        for (final org.spongepowered.api.world.server.ServerWorld world : Sponge.getServer().getWorldManager().getWorlds()) {
            final net.minecraft.world.World mcWorld = (net.minecraft.world.World) world;
            if (!((WorldBridge) mcWorld).bridge$isFake() && mcWorld.getWorldInfo() == (Object) this) {
                serverWorld = (ServerWorld) world;
                break;
            }
        }

        if (serverWorld == null) {
            return;
        }

        serverWorld
            .getPlayers()
            .forEach(player -> player.connection.sendPacket(new SServerDifficultyPacket(this.difficulty, ((WorldInfo) (Object) this).isDifficultyLocked())));
    }

    @Override
    public boolean bridge$isEnabled() {
        return this.bridge$getConfigAdapter().get().getWorld().isWorldEnabled();
    }

    @Override
    public void bridge$setEnabled(boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setWorldEnabled(state);
    }

    @Override
    public boolean bridge$isPVPEnabled() {
        return this.bridge$getConfigAdapter().get().getWorld().getPVPEnabled();
    }

    @Override
    public void bridge$setPVPEnabled(boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setPVPEnabled(state);
    }

    @Override
    public boolean bridge$doesGenerateBonusChest() {
        return this.impl$generateBonusChest;
    }

    @Override
    public void bridge$setGenerateBonusChest(boolean state) {
        this.impl$generateBonusChest = state;
    }

    @Override
    public boolean bridge$doesLoadOnStartup() {
        return this.bridge$getConfigAdapter().get().getWorld().getLoadOnStartup();
    }

    @Override
    public void bridge$setLoadOnStartup(boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setLoadOnStartup(state);
    }

    @Override
    public boolean bridge$doesKeepSpawnLoaded() {
        return this.bridge$getConfigAdapter().get().getWorld().getKeepSpawnLoaded();
    }

    @Override
    public void bridge$setKeepSpawnLoaded(boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setKeepSpawnLoaded(state);
    }

    @Override
    public boolean bridge$doesGenerateSpawnOnLoad() {
        return this.bridge$getConfigAdapter().get().getWorld().getGenerateSpawnOnLoad();
    }

    @Override
    public void bridge$setGenerateSpawnOnLoad(boolean state) {
        this.bridge$getConfigAdapter().get().getWorld().setGenerateSpawnOnLoad(state);
    }

    @Override
    public SerializationBehavior bridge$getSerializationBehavior() {
        return this.impl$serializationBehavior;
    }

    @Override
    public void bridge$setSerializationBehavior(SerializationBehavior behavior) {
        this.impl$serializationBehavior = behavior;
    }

    @Override
    public boolean bridge$isModCreated() {
        return this.impl$modCreated;
    }

    @Override
    public void bridge$setModCreated(boolean state) {
        this.impl$modCreated = state;
    }

    @Override
    public InheritableConfigHandle<WorldConfig> bridge$getConfigAdapter() {
        if (this.impl$configAdapter == null) {
            if (this.bridge$isValid()) {
                return SpongeConfigs.createWorld(this.bridge$getLogicType(), this.bridge$getKey());
            } else {
                return SpongeConfigs.createDetached();
            }
        }
        return this.impl$configAdapter;
    }

    @Override
    public void bridge$setConfigAdapter(InheritableConfigHandle<WorldConfig> adapter) {
        this.impl$configAdapter = Objects.requireNonNull(adapter, "adapter");
    }

    @Override
    public DimensionType bridge$getDimensionType() {
        return this.impl$dimensionType;
    }

    @Override
    public int bridge$getIndexForUniqueId(final UUID uuid) {
        final Integer index = this.impl$playerUniqueIdMap.inverse().get(uuid);
        if (index != null) {
            return index;
        }

        this.impl$playerUniqueIdMap.put(this.impl$trackedUniqueIdCount, uuid);
        this.impl$pendingUniqueIds.add(uuid);
        return this.impl$trackedUniqueIdCount++;
    }

    @Override
    public Optional<UUID> bridge$getUniqueIdForIndex(final int index) {
        return Optional.ofNullable(this.impl$playerUniqueIdMap.get(index));
    }

    @Override
    public void bridge$saveConfig() {
        if (this.impl$configAdapter != null) {
            this.impl$configAdapter.save();
        }
    }

    @Override
    public void bridge$writeSpongeLevelData(final CompoundNBT compound) {
        if (!this.bridge$isValid()) {
            return;
        }

        final CompoundNBT spongeDataCompound = new CompoundNBT();
        spongeDataCompound.putInt(Constants.Sponge.DATA_VERSION, Constants.Sponge.SPONGE_DATA_VERSION);
        spongeDataCompound.putString(Constants.Sponge.World.KEY, this.impl$key.getFormatted());
        spongeDataCompound.putInt(Constants.Sponge.World.DIMENSION_ID, this.impl$dimensionType.getId());
        spongeDataCompound.putString(Constants.Sponge.World.DIMENSION_TYPE, this.impl$logicType.getKey().toString());
        spongeDataCompound.putUniqueId(Constants.Sponge.World.UNIQUE_ID, this.impl$uniqueId);
        spongeDataCompound.putBoolean(Constants.World.GENERATE_BONUS_CHEST, this.impl$generateBonusChest);
//        if (this.impl$portalAgentType == null) {
//            this.impl$portalAgentType = PortalAgentTypes.DEFAULT.get();
//        }
//        spongeDataCompound.putString(Constants.Sponge.World.PORTAL_AGENT_TYPE, this.impl$portalAgentType.getPortalAgentClass().getName());
        short saveBehavior = 1;
        if (this.impl$serializationBehavior == SerializationBehaviors.NONE.get()) {
            saveBehavior = -1;
        } else if (this.impl$serializationBehavior == SerializationBehaviors.MANUAL.get()) {
            saveBehavior = 0;
        }
        spongeDataCompound.putShort(Constants.Sponge.World.WORLD_SERIALIZATION_BEHAVIOR, saveBehavior);
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
            // TODO 1.14 - Bad Sponge level data...warn/crash?
            return;
        }

        // TODO 1.14 - Run DataFixer on the SpongeData compound

        final CompoundNBT spongeDataCompound = compound.getCompound(Constants.Sponge.SPONGE_DATA);

        if (!spongeDataCompound.contains(Constants.Sponge.World.KEY)) {
            // TODO 1.14 - Bad Sponge level data...warn/crash?
            return;
        }

        if (!spongeDataCompound.contains(Constants.Sponge.World.DIMENSION_ID)) {
            // TODO 1.14 - Bad Sponge level data...warn/crash?
            return;
        }

        if (!spongeDataCompound.hasUniqueId(Constants.Sponge.World.UNIQUE_ID)) {
            // TODO 1.14 - Bad Sponge level data...warn/crash?
            return;
        }

        // Ha ha, Vanilla!
        this.impl$dimensionType = DimensionType.getById(spongeDataCompound.getInt(Constants.Sponge.World.DIMENSION_ID) + 1);
        final String rawDimensionType = spongeDataCompound.getString(Constants.Sponge.World.DIMENSION_TYPE);
        this.impl$logicType = (SpongeDimensionType) SpongeCommon.getRegistry().getCatalogRegistry().get(org.spongepowered.api.world.dimension
                .DimensionType.class, ResourceKey.resolve(rawDimensionType)).orElseGet(() -> {
                SpongeCommon.getLogger().warn("WorldProperties '{}' specifies dimension type '{}' which does not exist, defaulting to '{}'",
                    this.shadow$getWorldName(), rawDimensionType, DimensionTypes.OVERWORLD.get().getKey());

                return DimensionTypes.OVERWORLD.get();
        });
        this.impl$uniqueId = spongeDataCompound.getUniqueId(Constants.Sponge.World.UNIQUE_ID);
        this.impl$generateBonusChest = spongeDataCompound.getBoolean(Constants.World.GENERATE_BONUS_CHEST);
        // TODO - Zidane.
//        this.impl$portalAgentType = PortalAgentRegistryModule.getInstance()
//            .validatePortalAgent(spongeDataCompound.getString(Constants.Sponge.World.PORTAL_AGENT_TYPE), this.levelName);
        this.impl$hasCustomDifficulty = spongeDataCompound.getBoolean(Constants.Sponge.World.HAS_CUSTOM_DIFFICULTY);
        this.impl$serializationBehavior = SerializationBehaviors.AUTOMATIC.get();
        this.impl$modCreated = spongeDataCompound.getBoolean(Constants.Sponge.World.IS_MOD_CREATED);
        if (spongeDataCompound.contains(Constants.Sponge.World.WORLD_SERIALIZATION_BEHAVIOR)) {
            final short saveBehavior = spongeDataCompound.getShort(Constants.Sponge.World.WORLD_SERIALIZATION_BEHAVIOR);
            if (saveBehavior == 0) {
                this.impl$serializationBehavior = SerializationBehaviors.MANUAL.get();
            } else {
                this.impl$serializationBehavior = SerializationBehaviors.NONE.get();
            }
        }

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("key", this.impl$key)
            .add("dimensionType", this.impl$logicType)
            .add("generator", this.shadow$getGenerator())
            .add("modCreated", this.impl$modCreated)
            .add("spawnX", this.shadow$getSpawnX())
            .add("spawnY", this.shadow$getSpawnY())
            .add("spawnZ", this.shadow$getSpawnZ())
            .add("gameType", this.shadow$getGameType())
            .add("hardcore", this.shadow$isHardcore())
            .add("difficulty", this.shadow$getDifficulty())
            .toString();
    }
}
