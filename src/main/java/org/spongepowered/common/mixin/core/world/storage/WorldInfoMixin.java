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
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.teleport.PortalAgentType;
import org.spongepowered.api.world.teleport.PortalAgentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.WorldCategory;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.registry.type.world.PortalAgentRegistryModule;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(WorldInfo.class)
public abstract class WorldInfoMixin implements WorldInfoBridge {

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

    @Nullable private DimensionType impl$dimensionType;
    @Nullable private UUID impl$uniqueId;
    @Nullable private String impl$worldName;
    @Nullable private SpongeConfig<WorldConfig> impl$configAdapter;
    private boolean impl$enabled;
    private boolean impl$pvp;
    private boolean impl$loadOnStartup;
    private boolean impl$keepSpawnLoaded;
    private boolean impl$generateSpawnOnLoad;
    private boolean impl$generateBonusChest;
    private boolean impl$modCreated;
    @Nullable private PortalAgentType impl$portalAgentType;
    @Nullable private SerializationBehavior impl$serializationBehavior;

    private final BiMap<Integer, UUID> impl$playerUniqueIdMap = HashBiMap.create();
    private final List<UUID> impl$pendingUniqueIds = new ArrayList<>();
    private int impl$trackedUniqueIdCount = 0;
    private boolean impl$hasCustomDifficulty = false;
    private boolean impl$isConstructing = false;

    @Redirect(method = "<init>(Lnet/minecraft/world/WorldSettings;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;populateFromWorldSettings(Lnet/minecraft/world/WorldSettings;)V"))
    private void impl$setupBeforeSettingsPopulation(WorldInfo info, WorldSettings settings, String levelName) {
        this.levelName = levelName;
        this.impl$isConstructing = true;

        if (this.bridge$isValid()) {
            this.shadow$populateFromWorldSettings(settings);
        }

        this.impl$isConstructing = false;
    }

    @Inject(method = "populateFromWorldSettings", at = @At("RETURN"))
    private void populatePropertiesFromArchetype(WorldSettings settings, CallbackInfo ci) {
        if (!this.bridge$isValid() || this.impl$isConstructing) {
            return;
        }

        ((WorldSettingsBridge) (Object) settings).bridge$populateInfo((WorldInfo) (Object) this);
    }

    @Override
    public boolean bridge$createWorldConfig() {
        if (this.impl$configAdapter != null) {
             return false;
        }

        if (this.bridge$isValid()) {
            this.impl$configAdapter =
                    new SpongeConfig<>(SpongeConfig.Type.WORLD, ((DimensionTypeBridge) this.impl$dimensionType).bridge$getSpongeDimensionType().getConfigPath()
                            .resolve(this.levelName)
                            .resolve("world.conf"),
                            SpongeImpl.ECOSYSTEM_ID,
                            ((DimensionTypeBridge) this.impl$dimensionType).bridge$getSpongeDimensionType().getConfigAdapter(),
                            false);
        } else {
            this.impl$configAdapter = SpongeConfig.newDummyConfig(SpongeConfig.Type.WORLD);
        }

        return true;
    }

    @Override
    public boolean bridge$isValid() {
        final String levelName = this.shadow$getWorldName();

        return !(levelName == null || levelName.equals("") || levelName.equals("MpServer") || levelName.equals("sponge$dummy_world"));
    }

    @Override
    public void bridge$setDimensionType(final DimensionType type) {
        this.impl$dimensionType = type;
    }

    @Inject(method = "setDifficulty", at = @At("HEAD"), cancellable = true)
    private void onSetDifficultyVanilla(@Nullable final Difficulty newDifficulty, final CallbackInfo ci) {
        if (newDifficulty == null) {
            // This is an error from someone
            new PrettyPrinter(60).add("Null Difficulty being set!").centre().hr()
                .add("Someone (not Sponge) is attempting to set a null difficulty to this WorldInfo setup! Please report to the mod/plugin author!")
                .add()
                .addWrapped(60, " %s : %s", "WorldInfo", this)
                .add()
                .add(new Exception("Stacktrace"))
                .log(SpongeImpl.getLogger(), Level.ERROR);
            ci.cancel(); // We cannot let the null set the field.
            return;
        }

        this.impl$hasCustomDifficulty = true;
        this.difficulty = newDifficulty;

        this.bridge$updatePlayersForDifficulty();
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
        SpongeImpl.getWorldManager().getWorlds()
            .stream()
            .map(world -> (ServerWorld) world)
            .filter(world -> world.getWorldInfo() == (WorldInfo) (Object) this)
            .flatMap(world -> world.getPlayers().stream())
            .forEach(player -> player.connection.sendPacket(new SServerDifficultyPacket(this.difficulty, ((WorldInfo) (Object) this)
                .isDifficultyLocked())));
    }

    @Override
    public void bridge$setUniqueId(final UUID uniqueId) {
        this.impl$uniqueId = uniqueId;
    }

    @Override
    public String bridge$getWorldName() {
        return this.impl$worldName;
    }

    @Override
    public void bridge$setWorldName(String worldName) {
        this.impl$worldName = worldName;
    }

    @Override
    public boolean bridge$isEnabled() {
        return this.impl$enabled;
    }

    @Override
    public void bridge$setEnabled(boolean state) {
        this.impl$enabled = state;
    }

    @Override
    public boolean bridge$isPVPEnabled() {
        return this.impl$pvp;
    }

    @Override
    public void bridge$setPVPEnabled(boolean state) {
        this.impl$pvp = state;
    }

    @Override
    public UUID bridge$getUniqueId() {
        return this.impl$uniqueId;
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
        return this.impl$loadOnStartup;
    }

    @Override
    public void bridge$setLoadOnStartup(boolean state) {
        this.impl$loadOnStartup = state;
    }

    @Override
    public boolean bridge$doesKeepSpawnLoaded() {
        return this.impl$keepSpawnLoaded;
    }

    @Override
    public void bridge$setKeepSpawnLoaded(boolean state) {
        this.impl$keepSpawnLoaded = state;
    }

    @Override
    public boolean bridge$doesGenerateSpawnOnLoad() {
        return this.impl$generateSpawnOnLoad;
    }

    @Override
    public void bridge$setGenerateSpawnOnLoad(boolean state) {
        this.impl$generateSpawnOnLoad = state;
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
    public PortalAgentType bridge$getPortalAgent() {
        if (this.impl$portalAgentType == null) {
            this.impl$portalAgentType = PortalAgentTypes.DEFAULT;
        }
        return this.impl$portalAgentType;
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
    public SpongeConfig<WorldConfig> bridge$getConfigAdapter() {
        if (this.impl$configAdapter == null) {
            this.bridge$createWorldConfig();
        }
        return this.impl$configAdapter;
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
        final WorldCategory worldCat = this.impl$configAdapter.getConfig().getWorld();
        worldCat.setWorldEnabled(this.impl$enabled);
        worldCat.setLoadOnStartup(this.impl$loadOnStartup);
        worldCat.setGenerateSpawnOnLoad(this.impl$generateSpawnOnLoad);
        worldCat.setKeepSpawnLoaded(this.impl$keepSpawnLoaded);
        worldCat.setPVPEnabled(this.impl$pvp);
        this.impl$configAdapter.save();
    }

    @Override
    public void bridge$writeSpongeLevelData(CompoundNBT compound) {

        if (this.impl$uniqueId == null || !this.bridge$isValid()) {
            return;
        }

        final CompoundNBT spongeDataCompound = new CompoundNBT();
        spongeDataCompound.putInt(Constants.Sponge.DATA_VERSION, Constants.Sponge.SPONGE_DATA_VERSION);
        spongeDataCompound.putUniqueId(Constants.UUID, this.impl$uniqueId);
        spongeDataCompound.putInt(Constants.Sponge.World.DIMENSION_ID, this.impl$dimensionType.getId());
        spongeDataCompound.putString(Constants.Sponge.World.DIMENSION_TYPE,
            ((DimensionTypeBridge) this.impl$dimensionType).bridge$getSpongeDimensionType().getKey().toString());
        spongeDataCompound.putBoolean(Constants.World.GENERATE_BONUS_CHEST, this.impl$generateBonusChest);
        if (this.impl$portalAgentType == null) {
            this.impl$portalAgentType = PortalAgentTypes.DEFAULT;
        }
        spongeDataCompound.putString(Constants.Sponge.World.PORTAL_AGENT_TYPE, this.impl$portalAgentType.getPortalAgentClass().getName());
        short saveBehavior = 1;
        if (this.impl$serializationBehavior == SerializationBehaviors.NONE) {
            saveBehavior = -1;
        } else if (this.impl$serializationBehavior == SerializationBehaviors.MANUAL) {
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
    }

    @Override
    public void bridge$readSpongeLevelData(CompoundNBT compound) {
        if (!compound.contains(Constants.Sponge.SPONGE_DATA)) {
            // TODO 1.14 - Bad Sponge level data...warn/crash?
            return;
        }

        // TODO 1.14 - Run DataFixer on the SpongeData compound

        final CompoundNBT spongeDataCompound = compound.getCompound(Constants.Sponge.SPONGE_DATA);
        if (!spongeDataCompound.contains(Constants.UUID_MOST) || !spongeDataCompound.contains(Constants.UUID_LEAST)) {
            // TODO 1.14 - Bad Sponge level data...warn/crash?
            return;
        }

        this.impl$uniqueId = spongeDataCompound.getUniqueId(Constants.UUID);
        this.impl$dimensionType = net.minecraft.world.dimension.DimensionType.getById(spongeDataCompound.getInt(Constants.Sponge.World.DIMENSION_ID));
        this.impl$generateBonusChest = spongeDataCompound.getBoolean(Constants.World.GENERATE_BONUS_CHEST);
        this.impl$portalAgentType = PortalAgentRegistryModule.getInstance()
            .validatePortalAgent(spongeDataCompound.getString(Constants.Sponge.World.PORTAL_AGENT_TYPE), this.levelName);
        this.impl$hasCustomDifficulty = spongeDataCompound.getBoolean(Constants.Sponge.World.HAS_CUSTOM_DIFFICULTY);
        this.impl$serializationBehavior = SerializationBehaviors.AUTOMATIC;
        this.impl$modCreated = spongeDataCompound.getBoolean(Constants.Sponge.World.IS_MOD_CREATED);
        if (spongeDataCompound.contains(Constants.Sponge.World.WORLD_SERIALIZATION_BEHAVIOR)) {
            final short saveBehavior = spongeDataCompound.getShort(Constants.Sponge.World.WORLD_SERIALIZATION_BEHAVIOR);
            if (saveBehavior == 0) {
                this.impl$serializationBehavior = SerializationBehaviors.MANUAL;
            } else {
                this.impl$serializationBehavior = SerializationBehaviors.NONE;
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

    /**
     * @reason Some mods set a null levelName, which is incompatible with Sponge's policy
     * of never returning null from our API. Since this method has the same deobfuscated
     * name as an API method, I'm choosing to overwrite it to keep development and production
     * consistent. If we end up breaking mods because of this, we'll probably need to switch
     * to using a wrapepr type for WorldInfo
     *
     * @author Aaron1011 - August 9th, 2018
     */
    @Overwrite
    public String getWorldName() {
        if (this.levelName == null) {
            this.levelName = "";
        }
        return this.levelName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("directoryName", this.shadow$getWorldName())
            .add("worldName", this.impl$worldName)
            .add("uuid", this.impl$uniqueId)
            .add("dimensionType", this.impl$dimensionType)
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
