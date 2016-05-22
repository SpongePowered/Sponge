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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.SpongeConfig.WorldConfig;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldSettings;
import org.spongepowered.common.registry.type.entity.GameModeRegistryModule;
import org.spongepowered.common.registry.type.world.DimensionRegistryModule;
import org.spongepowered.common.registry.type.world.GeneratorModifierRegistryModule;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.persistence.JsonTranslator;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@NonnullByDefault
@Mixin(WorldInfo.class)
@Implements(@Interface(iface = WorldProperties.class, prefix = "worldproperties$"))
public abstract class MixinWorldInfo implements WorldProperties, IMixinWorldInfo {

    private UUID uuid;
    private DimensionType dimensionType;
    private PortalAgentType portalAgentType = PortalAgentTypes.DEFAULT;
    private boolean isMod;
    private NBTTagCompound spongeRootLevelNbt;
    private NBTTagCompound spongeNbt;
    private NBTTagList playerUniqueIdNbt;
    private BiMap<Integer, UUID> playerUniqueIdMap = HashBiMap.create();
    private List<UUID> pendingUniqueIds = new ArrayList<>();
    private int trackedUniqueIdCount = 0;
    private SpongeConfig<SpongeConfig.WorldConfig> worldConfig;
    private ServerScoreboard scoreboard;
    private boolean isValid = true;

    @Shadow private long randomSeed;
    @Shadow private WorldType terrainType;
    @Shadow private String generatorOptions;
    @Shadow private int spawnX;
    @Shadow private int spawnY;
    @Shadow private int spawnZ;
    @Shadow private long totalTime;
    @Shadow private long worldTime;
    @Shadow private long lastTimePlayed;
    @Shadow private long sizeOnDisk;
    @Shadow private NBTTagCompound playerTag;
    @Shadow private int dimension;
    @Shadow private String levelName;
    @Shadow private int saveVersion;
    @Shadow private int cleanWeatherTime;
    @Shadow private boolean raining;
    @Shadow private int rainTime;
    @Shadow private boolean thundering;
    @Shadow private int thunderTime;
    @Shadow private WorldSettings.GameType theGameType;
    @Shadow private boolean mapFeaturesEnabled;
    @Shadow private boolean hardcore;
    @Shadow private boolean allowCommands;
    @Shadow private boolean initialized;
    @Shadow private EnumDifficulty difficulty;
    @Shadow private boolean difficultyLocked;
    @Shadow private double borderCenterX;
    @Shadow private double borderCenterZ;
    @Shadow private double borderSize;
    @Shadow private long borderSizeLerpTime;
    @Shadow private double borderSizeLerpTarget;
    @Shadow private double borderSafeZone;
    @Shadow private double borderDamagePerBlock;
    @Shadow private int borderWarningDistance;
    @Shadow private int borderWarningTime;
    @Shadow private GameRules theGameRules;
    @Shadow public abstract NBTTagCompound getNBTTagCompound();

    @Inject(method = "<init>", at = @At("RETURN") )
    public void onConstruction(CallbackInfo ci) {
        this.spongeRootLevelNbt = new NBTTagCompound();
        this.spongeNbt = new NBTTagCompound();
        this.playerUniqueIdNbt = new NBTTagList();
        this.spongeNbt.setTag(NbtDataUtil.SPONGE_PLAYER_UUID_TABLE, this.playerUniqueIdNbt);
        this.spongeRootLevelNbt.setTag(NbtDataUtil.SPONGE_DATA, this.spongeNbt);

        if (this.dimensionType == null) {
            this.dimensionType = DimensionTypes.OVERWORLD;
        }
    }

    @Inject(method = "<init>*", at = @At("RETURN") )
    public void onConstruction(WorldSettings settings, String name, CallbackInfo ci) {
        if (name.equals("MpServer") || name.equals("sponge$dummy_world")) {
            this.isValid = false;
            return;
        }

        WorldCreationSettings creationSettings = (WorldCreationSettings) (Object) settings;
        setDimensionType(creationSettings.getDimensionType());
        if (((IMixinWorldSettings) (Object) settings).getDimensionId() != null) {
            this.dimension = ((IMixinWorldSettings) (Object) settings).getDimensionId();
        }
        // make sure to set dimensionType and dimension id before attempting to generate world config
        setDimensionType(creationSettings.getDimensionType());
        onConstruction(ci);
        boolean configPreviouslyExisted = Files.exists(SpongeImpl.getSpongeConfigDir()
                .resolve("worlds")
                .resolve(this.dimensionType.getId())
                .resolve(this.levelName)
                .resolve("world.conf"));
        createWorldConfig();
        this.worldConfig.getConfig().getWorld().setWorldEnabled(creationSettings.isEnabled());
        this.worldConfig.getConfig().getWorld().setKeepSpawnLoaded(creationSettings.doesKeepSpawnLoaded());
        this.worldConfig.getConfig().getWorld().setLoadOnStartup(creationSettings.loadOnStartup());
        this.worldConfig.getConfig().getWorld().setGenerateSpawnOnLoad(creationSettings.doesGenerateSpawnOnLoad());
        if (!creationSettings.getGeneratorModifiers().isEmpty()) {
            this.worldConfig.getConfig().getWorldGenModifiers().clear();
            this.worldConfig.getConfig().getWorldGenModifiers()
                    .addAll(GeneratorModifierRegistryModule.getInstance().toIds(creationSettings.getGeneratorModifiers()));
        }

        // Mark configs enabled if coming from WorldCreationSettings builder and config didn't previously exist.
        if (!configPreviouslyExisted && ((IMixinWorldSettings) (Object) settings).isFromBuilder()) {
            this.worldConfig.getConfig().setConfigEnabled(true);
        }
        this.worldConfig.save();
    }

    @Inject(method = "<init>*", at = @At("RETURN") )
    public void onConstruction(NBTTagCompound nbt, CallbackInfo ci) {
        if (!StaticMixinHelper.convertingMapFormat) {
            onConstruction(ci);
        }
    }

    @Inject(method = "<init>*", at = @At("RETURN") )
    public void onConstruction(WorldInfo worldInformation, CallbackInfo ci) {
        onConstruction(ci);

        MixinWorldInfo info = (MixinWorldInfo) (Object) worldInformation;
        this.dimensionType = info.dimensionType;
        this.isMod = info.isMod;
    }

    @Override
    public void createWorldConfig() {
        if (this.worldConfig != null) {
            return;
        }

        this.worldConfig =
                new SpongeConfig<>(SpongeConfig.Type.WORLD,
                        SpongeImpl.getSpongeConfigDir()
                                .resolve("worlds")
                                .resolve(this.dimensionType.getId())
                                .resolve(this.levelName)
                                .resolve("world.conf"),
                        SpongeImpl.ECOSYSTEM_ID);
    }

    @Override
    public boolean isValid() {
        return this.isValid;
    }

    @Override
    public Vector3i getSpawnPosition() {
        return new Vector3i(this.spawnX, this.spawnY, this.spawnZ);
    }

    @Override
    public void setSpawnPosition(Vector3i position) {
        checkNotNull(position);
        this.spawnX = position.getX();
        this.spawnY = position.getY();
        this.spawnZ = position.getZ();
    }

    @Override
    public GeneratorType getGeneratorType() {
        return (GeneratorType) this.terrainType;
    }

    @Override
    public void setGeneratorType(GeneratorType type) {
        this.terrainType = (WorldType) type;
    }

    @Intrinsic
    public long worldproperties$getSeed() {
        return this.randomSeed;
    }

    @Override
    public void setSeed(long seed) {
        this.randomSeed = seed;
    }

    @Override
    public long getTotalTime() {
        return this.totalTime;
    }

    @Intrinsic
    public long worldproperties$getWorldTime() {
        return this.worldTime;
    }

    @Override
    public void setWorldTime(long time) {
        this.worldTime = time;
    }

    @Override
    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    @Override
    public void setDimensionType(DimensionType type) {
        this.dimensionType = type;
    }

    @Override
    public PortalAgentType getPortalAgentType() {
        return this.portalAgentType;
    }

    @Override
    public void setPortalAgentType(PortalAgentType type) {
        this.portalAgentType = type;
    }

    @Intrinsic
    public String worldproperties$getWorldName() {
        return this.levelName;
    }

    @Override
    public void setWorldName(String name) {
        this.levelName = name;
    }

    @Intrinsic
    public boolean worldproperties$isRaining() {
        return this.raining;
    }

    @Override
    public void setRaining(boolean state) {
        this.raining = state;
    }

    @Intrinsic
    public int worldproperties$getRainTime() {
        return this.rainTime;
    }

    @Intrinsic
    public void worldproperties$setRainTime(int time) {
        this.rainTime = time;
    }

    @Intrinsic
    public boolean worldproperties$isThundering() {
        return this.thundering;
    }

    @Intrinsic
    public void worldproperties$setThundering(boolean state) {
        this.thundering = state;
    }

    @Override
    public int getThunderTime() {
        return this.thunderTime;
    }

    @Override
    public void setThunderTime(int time) {
        this.thunderTime = time;
    }

    @Override
    public GameMode getGameMode() {
        return (GameMode) (Object) this.theGameType;
    }

    @Override
    public void setGameMode(GameMode gamemode) {
        this.theGameType = GameModeRegistryModule.toGameType(gamemode);
    }

    @Override
    public boolean usesMapFeatures() {
        return this.mapFeaturesEnabled;
    }

    @Override
    public void setMapFeaturesEnabled(boolean state) {
        this.mapFeaturesEnabled = state;
    }

    @Override
    public boolean isHardcore() {
        return this.hardcore;
    }

    @Override
    public void setHardcore(boolean state) {
        this.hardcore = state;
    }

    @Override
    public boolean areCommandsAllowed() {
        return this.allowCommands;
    }

    @Override
    public void setCommandsAllowed(boolean state) {
        this.allowCommands = state;
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public Difficulty getDifficulty() {
        return (Difficulty) (Object) this.difficulty;
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = (EnumDifficulty) (Object) difficulty;
    }

    @Override
    public Vector3d getWorldBorderCenter() {
        return new Vector3d(this.borderCenterX, 0, this.borderCenterZ);
    }

    @Override
    public void setWorldBorderCenter(double x, double z) {
        this.borderCenterX = x;
        this.borderCenterZ = z;
    }

    @Override
    public double getWorldBorderDiameter() {
        return this.borderSize;
    }

    @Override
    public void setWorldBorderDiameter(double diameter) {
        this.borderSize = diameter;
    }

    @Override
    public double getWorldBorderTargetDiameter() {
        return this.borderSizeLerpTarget;
    }

    @Override
    public void setWorldBorderTargetDiameter(double diameter) {
        this.borderSizeLerpTarget = diameter;
    }

    @Override
    public double getWorldBorderDamageThreshold() {
        return this.borderSafeZone;
    }

    @Override
    public void setWorldBorderDamageThreshold(double distance) {
        this.borderSafeZone = distance;
    }

    @Override
    public double getWorldBorderDamageAmount() {
        return this.borderDamagePerBlock;
    }

    @Override
    public void setWorldBorderDamageAmount(double damage) {
        this.borderDamagePerBlock = damage;
    }

    @Override
    public int getWorldBorderWarningTime() {
        return this.borderWarningTime;
    }

    @Override
    public void setWorldBorderWarningTime(int time) {
        this.borderWarningTime = time;
    }

    @Override
    public int getWorldBorderWarningDistance() {
        return this.borderWarningDistance;
    }

    @Override
    public void setWorldBorderWarningDistance(int distance) {
        this.borderWarningDistance = distance;
    }

    @Override
    public long getWorldBorderTimeRemaining() {
        return this.borderSizeLerpTime;
    }

    @Override
    public void setWorldBorderTimeRemaining(long time) {
        this.borderSizeLerpTime = time;
    }

    @Override
    public Optional<String> getGameRule(String gameRule) {
        if (this.theGameRules.hasRule(gameRule)) {
            return Optional.of(this.theGameRules.getString(gameRule));
        }
        return Optional.empty();
    }

    @Override
    public Map<String, String> getGameRules() {
        ImmutableMap.Builder<String, String> ruleMap = ImmutableMap.builder();
        for (String rule : this.theGameRules.getRules()) {
            ruleMap.put(rule, this.theGameRules.getString(rule));
        }
        return ruleMap.build();
    }

    @Override
    public void setGameRule(String gameRule, String value) {
        this.theGameRules.setOrCreateGameRule(gameRule, value);
    }

    @Override
    public void setDimensionId(int id) {
        this.dimension = id;
    }

    @Override
    public int getDimensionId() {
        return this.dimension;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public DataContainer toContainer() {
        return NbtTranslator.getInstance().translateFrom(getNBTTagCompound());
    }

    @Override
    public boolean isEnabled() {
        if (!this.worldConfig.getConfig().isConfigEnabled()) {
            return SpongeHooks.getActiveConfig(this.dimensionType.getId(), this.getWorldName()).getConfig().getWorld().isWorldEnabled();
        }
        return this.worldConfig.getConfig().getWorld().isWorldEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.worldConfig.getConfig().getWorld().setWorldEnabled(enabled);
    }

    @Override
    public boolean loadOnStartup() {
        if (!this.worldConfig.getConfig().isConfigEnabled()) {
            return SpongeHooks.getActiveConfig(this.dimensionType.getId(), this.getWorldName()).getConfig().getWorld().loadOnStartup();
        }
        return this.worldConfig.getConfig().getWorld().loadOnStartup();
    }

    @Override
    public void setLoadOnStartup(boolean state) {
        this.worldConfig.getConfig().getWorld().setLoadOnStartup(state);
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        if (!this.worldConfig.getConfig().isConfigEnabled()) {
            return SpongeHooks.getActiveConfig(this.dimensionType.getId(), this.getWorldName()).getConfig().getWorld().getKeepSpawnLoaded();
        }
        return this.worldConfig.getConfig().getWorld().getKeepSpawnLoaded();
    }

    @Override
    public void setKeepSpawnLoaded(boolean loaded) {
        this.worldConfig.getConfig().getWorld().setKeepSpawnLoaded(loaded);
    }

    @Override
    public boolean doesGenerateSpawnOnLoad() {
        return SpongeHooks.getActiveConfig(this.dimensionType.getId(), this.getWorldName()).getConfig().getWorld().getGenerateSpawnOnLoad();
    }

    @Override
    public void setGenerateSpawnOnLoad(boolean state) {
        this.worldConfig.getConfig().getWorld().setGenerateSpawnOnLoad(state);
    }

    @Override
    public boolean isPVPEnabled() {
        return !this.worldConfig.getConfig().isConfigEnabled() || this.worldConfig.getConfig().getWorld().getPVPEnabled();
    }

    @Override
    public void setPVPEnabled(boolean enabled) {
        this.worldConfig.getConfig().getWorld().setPVPEnabled(enabled);
    }

    @Override
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void setIsMod(boolean flag) {
        this.isMod = flag;
    }

    @Override
    public void setScoreboard(ServerScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public boolean getIsMod() {
        return this.isMod;
    }

    @Override
    public SpongeConfig<WorldConfig> getWorldConfig() {
        return this.worldConfig;
    }

    @Override
    public Collection<WorldGeneratorModifier> getGeneratorModifiers() {
        return GeneratorModifierRegistryModule.getInstance().toModifiers(this.worldConfig.getConfig().getWorldGenModifiers());
    }

    @Override
    public void setGeneratorModifiers(Collection<WorldGeneratorModifier> modifiers) {
        checkNotNull(modifiers, "modifiers");

        this.worldConfig.getConfig().getWorldGenModifiers().clear();
        this.worldConfig.getConfig().getWorldGenModifiers().addAll(GeneratorModifierRegistryModule.getInstance().toIds(modifiers));
    }

    @Override
    public DataContainer getGeneratorSettings() {
        // Minecraft uses a String, we want to return a fancy DataContainer
        // Parse the world generator settings as JSON
        try {
            return JsonTranslator.translateFrom(new JsonParser().parse(this.generatorOptions).getAsJsonObject());
        } catch (JsonParseException | IllegalStateException ignored) {
        }
        return new MemoryDataContainer().set(DataQueries.WORLD_CUSTOM_SETTINGS, this.generatorOptions);
    }

    @Override
    public Optional<DataView> getPropertySection(DataQuery path) {
        if (this.spongeRootLevelNbt.hasKey(path.toString())) {
            return Optional
                    .<DataView>of(NbtTranslator.getInstance().translateFrom(this.spongeRootLevelNbt.getCompoundTag(path.toString())));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void setPropertySection(DataQuery path, DataView data) {
        NBTTagCompound nbt = NbtTranslator.getInstance().translateData(data);
        this.spongeRootLevelNbt.setTag(path.toString(), nbt);
    }

    @Override
    public int getIndexForUniqueId(UUID uuid) {
        if (this.playerUniqueIdMap.inverse().get(uuid) == null) {
            this.playerUniqueIdMap.put(this.trackedUniqueIdCount, uuid);
            this.pendingUniqueIds.add(uuid);
            return this.trackedUniqueIdCount++;
        } else {
            return this.playerUniqueIdMap.inverse().get(uuid);
        }
    }

    @Override
    public Optional<UUID> getUniqueIdForIndex(int index) {
        return Optional.ofNullable(this.playerUniqueIdMap.get(index));
    }

    @Override
    public NBTTagCompound getSpongeRootLevelNbt() {
        writeSpongeNbt();
        return this.spongeRootLevelNbt;
    }

    @Override
    public NBTTagCompound getSpongeNbt() {
        writeSpongeNbt();
        return this.spongeNbt;
    }

    @Override
    public void setSpongeRootLevelNBT(NBTTagCompound nbt) {
        this.spongeRootLevelNbt = nbt;
        if (nbt.hasKey(NbtDataUtil.SPONGE_DATA)) {
            this.spongeNbt = nbt.getCompoundTag(NbtDataUtil.SPONGE_DATA);
            if (this.spongeNbt.hasKey(NbtDataUtil.SPONGE_PLAYER_UUID_TABLE)) {
                this.playerUniqueIdNbt = this.spongeNbt.getTagList(NbtDataUtil.SPONGE_PLAYER_UUID_TABLE, 10);
            } else {
                this.spongeNbt.setTag(NbtDataUtil.SPONGE_PLAYER_UUID_TABLE, this.playerUniqueIdNbt);
            }
        } else {
            // Migrate old NBT data to new location
            // TODO Remove later
            if (nbt.hasKey(SpongeImpl.ECOSYSTEM_NAME)) {
                this.spongeNbt = nbt.getCompoundTag(SpongeImpl.ECOSYSTEM_NAME);
            }
            this.spongeRootLevelNbt.setTag(NbtDataUtil.SPONGE_DATA, this.spongeNbt);
        }
    }

    @Override
    public void readSpongeNbt(NBTTagCompound nbt) {
        this.dimension = nbt.getInteger(NbtDataUtil.DIMENSION_ID);
        this.uuid = new UUID(nbt.getLong(NbtDataUtil.WORLD_UUID_MOST), nbt.getLong(NbtDataUtil.WORLD_UUID_LEAST));
        this.isMod = nbt.getBoolean(NbtDataUtil.IS_MOD);
        DimensionRegistryModule.getInstance().getAll().stream().filter(type -> type.getId().equalsIgnoreCase(nbt.getString(NbtDataUtil.DIMENSION_TYPE)))
                .forEach(type -> this.dimensionType = type);
        this.trackedUniqueIdCount = 0;
        for (int i = 0; i < this.playerUniqueIdNbt.tagCount(); i++) {
            NBTTagCompound valueNbt = this.playerUniqueIdNbt.getCompoundTagAt(i);
            UUID uuid = new UUID(valueNbt.getLong(NbtDataUtil.WORLD_UUID_MOST), valueNbt.getLong(NbtDataUtil.WORLD_UUID_LEAST));
            this.playerUniqueIdMap.put(this.trackedUniqueIdCount, uuid);
            this.trackedUniqueIdCount++;
        }
    }

    private void writeSpongeNbt() {
        this.spongeNbt.setInteger(NbtDataUtil.DIMENSION_ID, this.dimension);
        if (this.dimensionType != null) {
            this.spongeNbt.setString(NbtDataUtil.DIMENSION_TYPE, this.dimensionType.getId());
        }
        if (this.uuid != null) {
            this.spongeNbt.setLong(NbtDataUtil.WORLD_UUID_MOST, this.uuid.getMostSignificantBits());
            this.spongeNbt.setLong(NbtDataUtil.WORLD_UUID_LEAST, this.uuid.getLeastSignificantBits());
        }
        if (this.isMod) {
            this.spongeNbt.setBoolean(NbtDataUtil.IS_MOD, true);
        }

        Iterator<UUID> iterator = this.pendingUniqueIds.iterator();
        while (iterator.hasNext()) {
            UUID uuidToAdd = iterator.next();
            NBTTagCompound valueNbt = new NBTTagCompound();
            valueNbt.setLong(NbtDataUtil.WORLD_UUID_MOST, uuidToAdd.getMostSignificantBits());
            valueNbt.setLong(NbtDataUtil.WORLD_UUID_LEAST, uuidToAdd.getLeastSignificantBits());
            this.playerUniqueIdNbt.appendTag(valueNbt);
            iterator.remove();
        }
    }

    @Override
    public DataContainer getAdditionalProperties() {
        NBTTagCompound additionalProperties = (NBTTagCompound) this.spongeRootLevelNbt.copy();
        additionalProperties.removeTag(SpongeImpl.ECOSYSTEM_NAME);
        return NbtTranslator.getInstance().translateFrom(additionalProperties);
    }
}
