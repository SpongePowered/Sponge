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
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.WorldCategory;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.registry.type.world.DimensionTypeRegistryModule;
import org.spongepowered.common.registry.type.world.PortalAgentRegistryModule;
import org.spongepowered.common.registry.type.world.WorldGeneratorModifierRegistryModule;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.WorldManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(WorldInfo.class)
public abstract class WorldInfoMixin implements WorldInfoBridge {

    @Shadow private WorldType terrainType;
    @Shadow private int spawnX;
    @Shadow private int spawnY;
    @Shadow private int spawnZ;
    @Shadow private String levelName;
    @Shadow private GameType gameType;
    @Shadow private boolean hardcore;
    @Shadow private Difficulty difficulty;
    @Shadow public abstract void setDifficulty(Difficulty newDifficulty);

    private final ListNBT impl$playerUniqueIdNbt = new ListNBT();
    private final BiMap<Integer, UUID> impl$playerUniqueIdMap = HashBiMap.create();
    private final List<UUID> impl$pendingUniqueIds = new ArrayList<>();
    private int impl$trackedUniqueIdCount = 0;
    private boolean impl$hasCustomDifficulty = false;
    private boolean impl$isMod = false;
    private boolean impl$generateBonusChest;
    private DimensionType impl$dimensionType = DimensionTypes.OVERWORLD;
    private CompoundNBT impl$spongeRootLevelNbt = new CompoundNBT();
    private CompoundNBT impl$spongeNbt = new CompoundNBT();
    @Nullable private UUID impl$uuid;
    @Nullable private Integer impl$dimensionId;
    @Nullable private SpongeConfig<WorldConfig> impl$worldConfig;
    @Nullable private PortalAgentType impl$portalAgentType;


    //     protected WorldInfo()
    @Inject(method = "<init>", at = @At("RETURN") )
    private void impl$vanillaConstruction(final CallbackInfo ci) {
        this.impl$commonConstructionSetUpSpongeCompounds();
    }

    //     public WorldInfo(NBTTagCompound nbt)
    @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN") )
    private void impl$MaybeClientConstruction(final CompoundNBT nbt, final CallbackInfo ci) {
        if (SpongeImplHooks.isMainThread() && !PhaseTracker.getInstance().getCurrentContext().state.isConvertingMaps()) {
            this.impl$commonConstructionSetUpSpongeCompounds();
        }
    }

    //     public WorldInfo(WorldSettings settings, String name)
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "<init>(Lnet/minecraft/world/WorldSettings;Ljava/lang/String;)V", at = @At("RETURN"))
    private void impl$onFullConstruction(final WorldSettings settings, final String name, final CallbackInfo ci) {
        if (!this.bridge$isValid()) {
            return;
        }

        this.impl$commonConstructionSetUpSpongeCompounds();

        final WorldArchetype archetype = (WorldArchetype) (Object) settings;
        this.bridge$setDimensionType(archetype.getDimensionType());

        this.bridge$createWorldConfig();
        final WorldConfig config = this.bridge$getConfigAdapter().getConfig();
        final WorldCategory worldCat = config.getWorld();
        worldCat.setWorldEnabled(archetype.isEnabled());
        worldCat.setLoadOnStartup(archetype.loadOnStartup());
        if (((WorldSettingsBridge)(Object) settings).bridge$internalKeepSpawnLoaded() != null) {
            worldCat.setKeepSpawnLoaded(archetype.doesKeepSpawnLoaded());
        }
        worldCat.setGenerateSpawnOnLoad(archetype.doesGenerateSpawnOnLoad());
        this.bridge$forceSetDifficulty((Difficulty) (Object) archetype.getDifficulty());
        final Collection<WorldGeneratorModifier> modifiers = WorldGeneratorModifierRegistryModule.getInstance().toModifiers(config.getWorldGenModifiers());
        if (modifiers.isEmpty()) {
            config.getWorldGenModifiers().clear();
            config.getWorldGenModifiers().addAll(WorldGeneratorModifierRegistryModule.getInstance().toIds(archetype.getGeneratorModifiers()));
        } else {
            // use config modifiers
            config.getWorldGenModifiers().clear();
            config.getWorldGenModifiers().addAll(WorldGeneratorModifierRegistryModule.getInstance().toIds(modifiers));
        }
        this.setDoesGenerateBonusChest(archetype.doesGenerateBonusChest());
        ((WorldProperties) this).setSerializationBehavior(archetype.getSerializationBehavior());
    }

    //     public WorldInfo(WorldInfo worldInformation)
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "<init>(Lnet/minecraft/world/storage/WorldInfo;)V", at = @At("RETURN") )
    private void impl$initialConstruction(final WorldInfo worldInformation, final CallbackInfo ci) {
        this.impl$commonConstructionSetUpSpongeCompounds();

        final WorldInfoMixin info = (WorldInfoMixin) (Object) worldInformation;
        this.bridge$getConfigAdapter(); // Create the config now if it has not yet been created.
        this.impl$portalAgentType = info.impl$portalAgentType;
        this.bridge$setDimensionType(info.impl$dimensionType);
    }

    // used in all init methods
    private void impl$commonConstructionSetUpSpongeCompounds() {
        this.impl$spongeNbt.func_74782_a(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, this.impl$playerUniqueIdNbt);
        this.impl$spongeRootLevelNbt.func_74782_a(Constants.Sponge.SPONGE_DATA, this.impl$spongeNbt);
    }

    @Inject(method = "updateTagCompound", at = @At("HEAD"))
    private void impl$ensureLevelNameMatchesDirectory(final CompoundNBT compound, final CompoundNBT player, final CallbackInfo ci) {
        if (this.impl$dimensionId == null) {
            return;
        }

        final String name = WorldManager.getWorldFolderByDimensionId(this.impl$dimensionId).orElse(this.levelName);
        if (!this.levelName.equalsIgnoreCase(name)) {
            this.levelName = name;
        }
    }

    @Override
    public boolean bridge$createWorldConfig() {
        if (this.impl$worldConfig != null) {
             return false;
        }

        if (this.bridge$isValid()) {
            this.impl$worldConfig =
                    new SpongeConfig<>(SpongeConfig.Type.WORLD, ((DimensionTypeBridge) this.impl$dimensionType).bridge$getConfigPath()
                            .resolve(this.levelName)
                            .resolve("world.conf"),
                            SpongeImpl.ECOSYSTEM_ID,
                            ((DimensionTypeBridge) this.impl$dimensionType).bridge$getDimensionConfig(),
                            false);
        } else {
            this.impl$worldConfig = SpongeConfig.newDummyConfig(SpongeConfig.Type.WORLD);
        }

        return true;
    }

    @Override
    public boolean bridge$isValid() {
        return !(this.levelName == null || this.levelName.equals("") || this.levelName.equals("MpServer") || this.levelName.equals("sponge$dummy_world"));
    }

    @Override
    public void bridge$setDimensionType(final DimensionType type) {
        this.impl$dimensionType = type;
        final String modId = SpongeImplHooks.getModIdFromClass(this.impl$dimensionType.getDimensionClass());
        if (!"minecraft".equals(modId)) {
            this.impl$isMod = true;
        }
    }

    @SuppressWarnings("RedundantCast")
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
        WorldManager.getWorlds()
                .stream()
                .filter(world -> world.getWorldInfo() == (WorldInfo) (Object) this)
                .flatMap(world -> world.field_73010_i.stream())
                .filter(player -> player instanceof ServerPlayerEntity)
                .map(player -> (ServerPlayerEntity) player)
                .forEach(player -> player.connection.sendPacket(new SServerDifficultyPacket(this.difficulty, ((WorldInfo) (Object) this).isDifficultyLocked
                        ())));
    }

    private void setDoesGenerateBonusChest(final boolean state) {
        this.impl$generateBonusChest = state;
    }

    @Override
    public void bridge$setDimensionId(final int id) {
        this.impl$dimensionId = id;
    }

    @Override
    public Integer bridge$getDimensionId() {
        return this.impl$dimensionId;
    }

    @Override
    public void bridge$setUniqueId(final UUID uniqueId) {
        this.impl$uuid = uniqueId;
    }

    @Override
    public boolean bridge$getIsMod() {
        return this.impl$isMod;
    }

    @Override
    public void bridge$setIsMod(final boolean flag) {
        this.impl$isMod = flag;
    }

    @Override
    public UUID bridge$getAssignedId() {
        return this.impl$uuid;
    }

    @Override
    public boolean bridge$getSpawnsBonusChest() {
        return this.impl$generateBonusChest;
    }

    @Override
    public PortalAgentType bridge$getPortalAgent() {
        if (this.impl$portalAgentType == null) {
            this.impl$portalAgentType = PortalAgentTypes.DEFAULT;
        }
        return this.impl$portalAgentType;
    }

    @Override
    public SpongeConfig<WorldConfig> bridge$getConfigAdapter() {
        if (this.impl$worldConfig == null) {
            this.bridge$createWorldConfig();
        }
        return this.impl$worldConfig;
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
    public CompoundNBT bridge$getSpongeRootLevelNbt() {
        this.writeSpongeNbt();
        return this.impl$spongeRootLevelNbt;
    }

    @Override
    public void bridge$setSpongeRootLevelNBT(final CompoundNBT nbt) {
        this.impl$spongeRootLevelNbt = nbt;
        if (nbt.contains(Constants.Sponge.SPONGE_DATA)) {
            this.impl$spongeNbt = nbt.getCompound(Constants.Sponge.SPONGE_DATA);
        }
    }

    @Override
    public void bridge$readSpongeNbt(final CompoundNBT nbt) {
        final UUID nbtUniqueId = nbt.getUniqueId(Constants.UUID);
        if (UUID.fromString("00000000-0000-0000-0000-000000000000").equals(nbtUniqueId)) {
            return;
        }
        this.impl$uuid = nbtUniqueId;
        this.impl$dimensionId = nbt.getInt(Constants.Sponge.World.DIMENSION_ID);
        final String dimensionTypeId = nbt.getString(Constants.Sponge.World.DIMENSION_TYPE);
        final DimensionType dimensionType = (org.spongepowered.api.world.DimensionType)(Object) WorldManager.getDimensionType(this.impl$dimensionId).orElse(null);
        this.bridge$setDimensionType(dimensionType != null ? dimensionType : DimensionTypeRegistryModule.getInstance().getById(dimensionTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Could not find a DimensionType registered for world '" + this.getWorldName() + "' with dim id: " + this.impl$dimensionId)));
        this.impl$generateBonusChest = nbt.getBoolean(Constants.World.GENERATE_BONUS_CHEST);
        this.impl$portalAgentType = PortalAgentRegistryModule.getInstance().validatePortalAgent(nbt.getString(Constants.Sponge.World.PORTAL_AGENT_TYPE), this.levelName);
        this.impl$hasCustomDifficulty = nbt.getBoolean(Constants.Sponge.World.HAS_CUSTOM_DIFFICULTY);
        this.impl$trackedUniqueIdCount = 0;
        if (nbt.contains(Constants.Sponge.World.WORLD_SERIALIZATION_BEHAVIOR)) {
            final short saveBehavior = nbt.getShort(Constants.Sponge.World.WORLD_SERIALIZATION_BEHAVIOR);
            if (saveBehavior == 1) {
                ((WorldProperties) this).setSerializationBehavior(SerializationBehaviors.AUTOMATIC);
            } else if (saveBehavior == 0) {
                ((WorldProperties) this).setSerializationBehavior(SerializationBehaviors.MANUAL);
            } else {
                ((WorldProperties) this).setSerializationBehavior(SerializationBehaviors.NONE);
            }
        }
        if (nbt.contains(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, Constants.NBT.TAG_LIST)) {
            final ListNBT playerIdList = nbt.getList(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < playerIdList.func_74745_c(); i++) {
                final CompoundNBT playerId = playerIdList.getCompound(i);
                final UUID playerUuid = playerId.getUniqueId(Constants.UUID);
                final Integer playerIndex = this.impl$playerUniqueIdMap.inverse().get(playerUuid);
                if (playerIndex == null) {
                    this.impl$playerUniqueIdMap.put(this.impl$trackedUniqueIdCount++, playerUuid);
                } else {
                    playerIdList.func_74744_a(i);
                }
            }

        }
    }

    private void writeSpongeNbt() {
        // Never save Sponge data if we have no UUID
        if (this.impl$uuid != null && this.bridge$isValid()) {
            this.impl$spongeNbt.putInt(Constants.Sponge.DATA_VERSION, Constants.Sponge.SPONGE_DATA_VERSION);
            this.impl$spongeNbt.putUniqueId(Constants.UUID, this.impl$uuid);
            this.impl$spongeNbt.putInt(Constants.Sponge.World.DIMENSION_ID, this.impl$dimensionId);
            this.impl$spongeNbt.putString(Constants.Sponge.World.DIMENSION_TYPE, this.impl$dimensionType.getId());
            this.impl$spongeNbt.putBoolean(Constants.World.GENERATE_BONUS_CHEST, this.impl$generateBonusChest);
            if (this.impl$portalAgentType == null) {
                this.impl$portalAgentType = PortalAgentTypes.DEFAULT;
            }
            this.impl$spongeNbt.putString(Constants.Sponge.World.PORTAL_AGENT_TYPE, this.impl$portalAgentType.getPortalAgentClass().getName());
            short saveBehavior = 1;
            if (((WorldProperties) this).getSerializationBehavior() == SerializationBehaviors.NONE) {
                saveBehavior = -1;
            } else if (((WorldProperties) this).getSerializationBehavior() == SerializationBehaviors.MANUAL) {
                saveBehavior = 0;
            }
            this.impl$spongeNbt.putShort(Constants.Sponge.World.WORLD_SERIALIZATION_BEHAVIOR, saveBehavior);
            this.impl$spongeNbt.putBoolean(Constants.Sponge.World.HAS_CUSTOM_DIFFICULTY, this.impl$hasCustomDifficulty);
            final Iterator<UUID> iterator = this.impl$pendingUniqueIds.iterator();
            final ListNBT playerIdList = this.impl$spongeNbt.getList(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, Constants.NBT.TAG_COMPOUND);
            while (iterator.hasNext()) {
                final CompoundNBT compound = new CompoundNBT();
                compound.putUniqueId(Constants.UUID, iterator.next());
                playerIdList.func_74742_a(compound);
                iterator.remove();
            }
        }
    }

    /**
     * @reason Some mods set a null levelName, which is incompatible with Spongne's policy
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
    public void bridge$saveConfig() {
        this.bridge$getConfigAdapter().save();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("levelName", this.levelName)
            .add("terrainType", this.terrainType)
            .add("uuid", this.impl$uuid)
            .add("dimensionId", this.impl$dimensionId)
            .add("dimensionType", this.impl$dimensionType)
            .add("spawnX", this.spawnX)
            .add("spawnY", this.spawnY)
            .add("spawnZ", this.spawnZ)
            .add("gameType", this.gameType)
            .add("hardcore", this.hardcore)
            .add("difficulty", this.difficulty)
            .add("isMod", this.impl$isMod)
            .toString();
    }
}
