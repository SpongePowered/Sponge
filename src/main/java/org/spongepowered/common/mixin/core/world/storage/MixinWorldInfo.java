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

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gamerule.GameRule;
import org.spongepowered.api.world.gen.GeneratorType;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.teleport.PortalAgentType;
import org.spongepowered.api.world.teleport.PortalAgentTypes;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.WorldCategory;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.world.IMixinDimensionType;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.registry.type.world.PortalAgentRegistryModule;
import org.spongepowered.common.world.WorldLoader;

import java.time.Duration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(WorldInfo.class)
@Implements(@Interface(iface = WorldProperties.class, prefix = "prop$"))
public abstract class MixinWorldInfo implements IMixinWorldInfo {

    @Shadow private String levelName;
    @Shadow private boolean initialized;
    @Shadow private int spawnX;
    @Shadow private int spawnY;
    @Shadow private int spawnZ;
    @Shadow private WorldType generator;
    @Shadow public long randomSeed;
    @Shadow private long gameTime;
    @Shadow private long dayTime;
    @Shadow private boolean mapFeaturesEnabled;
    @Shadow private boolean hardcore;
    @Shadow private boolean allowCommands;
    @Shadow public EnumDifficulty difficulty;
    private Set<UUID> pendingPlayerUniqueIds = new HashSet<>();

    @Shadow protected abstract void updateTagCompound(NBTTagCompound compound, NBTTagCompound spSaveCompound);
    @Shadow public abstract void setDifficulty(EnumDifficulty difficulty);
    @Shadow public abstract boolean isDifficultyLocked();

    private final BiMap<Integer, UUID> playerUniqueIdMap = HashBiMap.create();
    private Integer trackedUniqueIdCount;

    @Nullable private DimensionType dimensionType;
    @Nullable private UUID uniqueId;
    private GameType gameType;
    private PortalAgentType portalAgentType = PortalAgentTypes.DEFAULT;
    private SpongeConfig<WorldConfig> config;
    private SerializationBehavior serializationBehavior = SerializationBehaviors.AUTOMATIC;
    private boolean isFake;
    private boolean createdByMod;
    private boolean hasCustomDifficulty;

    public boolean prop$isInitialized() {
        return this.initialized;
    }

    public String prop$getFolderName() {
        return null;
    }

    public String prop$getWorldName() {
        return this.levelName;
    }

    public boolean prop$isEnabled() {
        return this.getConfig().getConfig().getWorld().isWorldEnabled();
    }

    public void prop$setEnabled(boolean state) {
        this.getConfig().getConfig().getWorld().setWorldEnabled(state);
    }

    public boolean prop$doesLoadOnStartup() {
        return this.getConfig().getConfig().getWorld().doesLoadOnStartup();
    }

    public void prop$setLoadOnStartup(boolean state) {
        this.getConfig().getConfig().getWorld().setLoadOnStartup(state);
    }

    public boolean prop$doesKeepSpawnLoaded() {
        return this.getConfig().getConfig().getWorld().doesKeepSpawnLoaded();
    }

    public void prop$setKeepSpawnLoaded(boolean state) {
        this.getConfig().getConfig().getWorld().setKeepSpawnLoaded(state);
    }

    public boolean prop$doesGenerateSpawnOnLoad() {
        return this.getConfig().getConfig().getWorld().doesGenerateSpawnOnLoad();
    }

    public void prop$setGenerateSpawnOnLoad(boolean state) {
        this.getConfig().getConfig().getWorld().setGenerateSpawnOnLoad(state);
    }

    public Vector3i prop$getSpawnPosition() {
        return new Vector3i(this.spawnX, this.spawnY, this.spawnZ);
    }

    public void prop$setSpawnPosition(Vector3i position) {
        this.spawnX = position.getX();
        this.spawnY = position.getY();
        this.spawnZ = position.getZ();
    }

    public GeneratorType prop$getGeneratorType() {
        return (GeneratorType) (Object) this.generator;
    }

    public void prop$setGeneratorType(GeneratorType type) {
        this.generator = (WorldType) (Object) type;
    }

    public long prop$getSeed() {
        return this.randomSeed;
    }

    public void prop$setSeed(long seed) {
        this.randomSeed = seed;
    }

    public Duration prop$getTotalTime() {
        // TODO (1.13)
        return Duration.ofSeconds(this.gameTime);
    }

    public Duration prop$getWorldTime() {
        // TODO (1.13) - Since this is in ticks, this seems really weird..
        return Duration.ofSeconds(this.dayTime);
    }

    public void prop$setWorldTime(Duration time) {
        // TODO (1.13) - Since this is in ticks, this seems really weird..
        this.dayTime = time.getSeconds();
    }

    public PortalAgentType prop$getPortalAgentType() {
        return this.portalAgentType;
    }

    public boolean prop$isPVPEnabled() {
        return this.getConfig().getConfig().getWorld().isPVPEnabled();
    }

    public void prop$setPVPEnabled(boolean enabled) {
        this.getConfig().getConfig().getWorld().setPVPEnabled(enabled);
    }

    public GameMode prop$getGameMode() {
        return (GameMode) (Object) this.gameType;
    }

    public void prop$setGameMode(GameMode gamemode) {
        this.gameType = (GameType) (Object) gamemode;
    }

    public boolean prop$areStructuresEnabled() {
        return this.mapFeaturesEnabled;
    }

    public void prop$setStructuresEnabled(boolean state) {
        this.mapFeaturesEnabled = state;
    }

    public boolean prop$isHardcore() {
        return this.hardcore;
    }

    public void prop$setHardcore(boolean state) {
        this.hardcore = state;
    }

    public boolean prop$areCommandsAllowed() {
        return this.allowCommands;
    }

    public void prop$setCommandsAllowed(boolean state) {
        this.allowCommands = state;
    }

    public Difficulty prop$getDifficulty() {
        return (Difficulty) (Object) this.difficulty;
    }

    public void prop$setDifficulty(Difficulty difficulty) {
        this.setDifficulty((EnumDifficulty) (Object) difficulty);
    }

    public WorldBorder prop$getWorldBorder() {
        // TODO (1.13)
        return null;
    }

    public SerializationBehavior prop$getSerializationBehavior() {
        return this.serializationBehavior;
    }

    public void prop$setSerializationBehavior(SerializationBehavior behavior) {
        this.serializationBehavior = behavior;
    }

    public int prop$getContentVersion() {
        // TODO (1.13)
        return 0;
    }

    public DataContainer prop$toContainer() {
        final NBTTagCompound compound = new NBTTagCompound();
        // We don't care about SSP save data
        this.updateTagCompound(compound, null);
        this.writeSpongeCompound(compound);
        return NbtTranslator.getInstance().translate(compound);
    }

    public <V> V prop$getGameRule(GameRule<V> gameRule) {
        // TODO (1.13)
        return null;
    }

    public <V> void prop$setGameRule(GameRule<V> gameRule, V value) {
        // TODO (1.13)
    }

    public Map<GameRule<?>, ?> prop$getGameRules() {
        // TODO (1.13)
        return null;
    }

    public Weather prop$getWeather() {
        // TODO (1.13)

        return null;
    }

    public Duration prop$getRemainingWeatherDuration() {
        // TODO (1.13) - Since this is in ticks, this seems really weird..
        return null;
    }

    public Duration prop$getRunningWeatherDuration() {
        // TODO (1.13) - Since this is in ticks, this seems really weird..
        return null;
    }

    public void prop$setWeather(Weather weather) {
        // TODO (1.13)
    }

    public void prop$setWeather(Weather weather, Duration duration) {
        // TODO (1.13)
    }

    // IMixinWorldInfo
    @Override
    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    @Override
    public void setDimensionType(@Nullable DimensionType dimensionType) {
        this.dimensionType = dimensionType;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public void setUniqueId(@Nullable UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public boolean isFake() {
        return this.isFake;
    }

    @Override
    public boolean isCreatedByMod() {
        return this.createdByMod;
    }

    @Override
    public void setCreatedByMod(boolean createdByMod) {
        this.createdByMod = createdByMod;
    }

    @Override
    public void setPortalAgentType(PortalAgentType type) {
        checkNotNull(type);
        this.portalAgentType = type;
    }

    @Override
    public boolean hasCustomDifficulty() {
        return this.hasCustomDifficulty;
    }

    @Override
    public void setNonCustomDifficulty(EnumDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    @Nullable
    @Override
    public Integer getOrCreateIndexForPlayerUniqueId(UUID uniqueId) {
        checkNotNull(uniqueId);
        Integer index = this.playerUniqueIdMap.inverse().get(uniqueId);
        if (index != null) {
            return index;
        }

        this.playerUniqueIdMap.put(this.trackedUniqueIdCount, uniqueId);
        this.pendingPlayerUniqueIds.add(uniqueId);
        return this.trackedUniqueIdCount++;
    }

    @Nullable
    @Override
    public UUID getPlayerUniqueIdForIndex(int index) {
        return this.playerUniqueIdMap.get(index);
    }

    @Override
    public SpongeConfig<WorldConfig> createConfig() {
        final DimensionType dimensionType = this.getDimensionType();

        if (dimensionType == null) {
            return null;
        }

        if (this.config == null) {
            this.config =
                new SpongeConfig<>(SpongeConfig.Type.WORLD, ((IMixinDimensionType) this.dimensionType).getGlobalDimensionType().getConfigPath()
                    .resolve(this.levelName)
                    .resolve("world.conf"),
                    SpongeImpl.ECOSYSTEM_ID,
                    ((IMixinDimensionType) dimensionType).getGlobalDimensionType().getConfig());
        }

        return this.config;
    }

    @Nullable
    @Override
    public SpongeConfig<WorldConfig> getConfig() {
        return this.config;
    }

    @Override
    public void readSpongeCompound(NBTTagCompound compound) {
        checkNotNull(compound);

        if (this.isFake) {
            return;
        }

        this.portalAgentType = PortalAgentRegistryModule.getInstance().validatePortalAgent(compound.getString(NbtDataUtil.PORTAL_AGENT_TYPE), this.levelName);
        this.hasCustomDifficulty = compound.getBoolean(NbtDataUtil.HAS_CUSTOM_DIFFICULTY);
        this.trackedUniqueIdCount = 0;
        if (compound.contains(NbtDataUtil.WORLD_SERIALIZATION_BEHAVIOR)) {
            short saveBehavior = compound.getShort(NbtDataUtil.WORLD_SERIALIZATION_BEHAVIOR);
            if (saveBehavior == 1) {
                this.serializationBehavior = SerializationBehaviors.AUTOMATIC;
            } else if (saveBehavior == 0) {
                this.serializationBehavior = SerializationBehaviors.MANUAL;
            } else {
                this.serializationBehavior = SerializationBehaviors.NONE;
            }
        }
        if (compound.contains(NbtDataUtil.SPONGE_PLAYER_UUID_TABLE, NbtDataUtil.TAG_LIST)) {
            final NBTTagList playerIdList = compound.getList(NbtDataUtil.SPONGE_PLAYER_UUID_TABLE, NbtDataUtil.TAG_COMPOUND);
            for (int i = 0; i < playerIdList.size(); i++) {
                final NBTTagCompound playerId = playerIdList.getCompound(i);
                final UUID playerUuid = playerId.getUniqueId(NbtDataUtil.UUID);
                final Integer playerIndex = this.playerUniqueIdMap.inverse().get(playerUuid);
                if (playerIndex == null) {
                    this.playerUniqueIdMap.put(this.trackedUniqueIdCount++, playerUuid);
                } else {
                    playerIdList.removeTag(i);
                }
            }

        }
    }

    @Override
    public NBTTagCompound writeSpongeCompound(NBTTagCompound compound) {
        checkNotNull(compound);

        if (this.isFake) {
            return compound;
        }

        compound.putInt(NbtDataUtil.DATA_VERSION, DataUtil.DATA_VERSION);
        compound.putUniqueId(NbtDataUtil.UUID, this.uniqueId);
        compound.putInt(NbtDataUtil.DIMENSION_ID, this.dimensionType.getId());
        compound.putString(NbtDataUtil.DIMENSION_TYPE, ((IMixinDimensionType) this.dimensionType).getGlobalDimensionType().getKey().toString());
        compound.putString(NbtDataUtil.PORTAL_AGENT_TYPE, this.portalAgentType.getPortalAgentClass().getName());
        short saveBehavior = 1;
        if (this.serializationBehavior == SerializationBehaviors.NONE) {
            saveBehavior = -1;
        } else if (this.serializationBehavior == SerializationBehaviors.MANUAL) {
            saveBehavior = 0;
        }
        compound.putShort(NbtDataUtil.WORLD_SERIALIZATION_BEHAVIOR, saveBehavior);
        compound.putBoolean(NbtDataUtil.HAS_CUSTOM_DIFFICULTY, this.hasCustomDifficulty);
        final Iterator<UUID> iterator = this.pendingPlayerUniqueIds.iterator();
        final NBTTagList playerIdList = new NBTTagList();
        while (iterator.hasNext()) {
            final NBTTagCompound playerUniqueIdCompound = new NBTTagCompound();
            playerUniqueIdCompound.putUniqueId(NbtDataUtil.UUID, iterator.next());
            playerIdList.add(compound);
            iterator.remove();
        }

        compound.put(NbtDataUtil.SPONGE_PLAYER_UUID_TABLE, playerIdList);
        return compound;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/WorldSettings;Ljava/lang/String;)V", at = @At("RETURN"))
    public void onConstruct(WorldSettings settings, String levelName, CallbackInfo ci) {
        if (levelName.equals("MpServer") || levelName.equals("sponge$dummy_world")) {
            this.isFake = true;
            return;
        }

        final WorldArchetype archetype = (WorldArchetype) (Object) settings;

        final SpongeConfig<WorldConfig> config = this.createConfig();
        final WorldCategory category = config.getConfig().getWorld();
        category.setWorldEnabled(archetype.isEnabled());
        category.setLoadOnStartup(archetype.doesLoadOnStartup());
        category.setKeepSpawnLoaded(archetype.doesKeepSpawnLoaded());
        category.setGenerateSpawnOnLoad(archetype.doesGenerateSpawnOnLoad());
        category.setPVPEnabled(archetype.isPVPEnabled());

        // TODO (1.13) - Determine the following as candidacy for configs.
        this.gameType = (GameType) (Object) archetype.getGameMode();
        this.generator = (WorldType) (Object) archetype.getGeneratorType();
        this.mapFeaturesEnabled = archetype.areStructuresEnabled();
        this.hardcore = archetype.isHardcore();
        this.allowCommands = archetype.areCommandsAllowed();
        this.serializationBehavior = archetype.getSerializationBehavior();

        // Since the Archetype wants the difficulty set, we want it counted as a custom one
        this.setDifficulty((EnumDifficulty) (Object) archetype.getDifficulty());

        // TODO (1.13) - Review Portal Agents
        this.portalAgentType = archetype.getPortalAgentType();

        this.getConfig().save();
    }

    @Inject(method = "setDifficulty", at = @At("HEAD"), cancellable = true)
    private void onSetDifficultyVanilla(EnumDifficulty newDifficulty, CallbackInfo ci) {
        this.hasCustomDifficulty = true;
        if (newDifficulty == null) {
            // This is an error from someone
            new PrettyPrinter(60).add("Null Difficulty being set!").centre().hr()
                .add("Someone (not Sponge) is attempting to set a null difficulty to the properties of a world! Please report to the mod/plugin "
                    + "author!")
                .add()
                .addWrapped(60, " %s : %s", "WorldInfo", this)
                .add()
                .add(new Exception("Stacktrace"))
                .log(SpongeImpl.getLogger(), Level.ERROR);
            ci.cancel();
            return;
        }

        if (Sponge.isServerAvailable()) {
            final WorldLoader manager = ((IMixinMinecraftServer) SpongeImpl.getServer()).getWorldLoader();
            manager.getWorlds()
                .stream()
                .filter(world -> world.getWorldInfo() == (WorldInfo) (Object) this)
                .flatMap(world -> world.playerEntities.stream())
                .filter(player -> player instanceof EntityPlayerMP)
                .map(player -> (EntityPlayerMP) player)
                .forEach(player -> player.connection.sendPacket(new SPacketServerDifficulty(newDifficulty, this.isDifficultyLocked())));
        }
    }
}
