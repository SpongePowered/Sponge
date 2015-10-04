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
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
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
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.IMixinWorldInfo;
import org.spongepowered.common.service.persistence.NbtTranslator;
import org.spongepowered.common.world.gen.WorldGeneratorRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@NonnullByDefault
@Mixin(WorldInfo.class)
@Implements(@Interface(iface = WorldProperties.class, prefix = "worldproperties$"))
public abstract class MixinWorldInfo implements WorldProperties, IMixinWorldInfo {

    private UUID uuid;
    private boolean worldEnabled;
    private DimensionType dimensionType;
    private boolean loadOnStartup;
    private boolean keepSpawnLoaded;
    private boolean isMod;
    private ImmutableCollection<String> generatorModifiers;
    private NBTTagCompound spongeRootLevelNbt;
    private NBTTagCompound spongeNbt;

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

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruction(CallbackInfo ci) {
        this.worldEnabled = true;
        this.spongeRootLevelNbt = new NBTTagCompound();
        this.spongeNbt = new NBTTagCompound();
        this.spongeRootLevelNbt.setTag(Sponge.ECOSYSTEM_NAME, this.spongeNbt);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstruction(WorldSettings settings, String name, CallbackInfo ci) {
        this.worldEnabled = true;
        this.spongeRootLevelNbt = new NBTTagCompound();
        this.spongeNbt = new NBTTagCompound();
        this.spongeRootLevelNbt.setTag(Sponge.ECOSYSTEM_NAME, this.spongeNbt);

        WorldCreationSettings creationSettings = (WorldCreationSettings) (Object) settings;
        this.dimensionType = creationSettings.getDimensionType();
        this.generatorModifiers = WorldGeneratorRegistry.getInstance().toIds(creationSettings.getGeneratorModifiers());
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstruction(NBTTagCompound nbt, CallbackInfo ci) {
        this.worldEnabled = true;
        this.spongeRootLevelNbt = new NBTTagCompound();
        this.spongeNbt = new NBTTagCompound();
        this.spongeRootLevelNbt.setTag(Sponge.ECOSYSTEM_NAME, this.spongeNbt);
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

    public String worldproperties$getWorldName() {
        return this.levelName;
    }

    @Override
    public void setWorldName(String name) {
        this.levelName = name;
    }

    public boolean worldproperties$isRaining() {
        return this.raining;
    }

    @Override
    public void setRaining(boolean state) {
        this.raining = state;
    }

    public int worldproperties$getRainTime() {
        return this.rainTime;
    }

    public void worldproperties$setRainTime(int time) {
        this.rainTime = time;
    }

    public boolean worldproperties$isThundering() {
        return this.thundering;
    }

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
        this.theGameType = Sponge.getSpongeRegistry().getGameType(gamemode);
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
            return Optional.of(this.theGameRules.getGameRuleStringValue(gameRule));
        }
        return Optional.empty();
    }

    @Override
    public Map<String, String> getGameRules() {
        ImmutableMap.Builder<String, String> ruleMap = ImmutableMap.builder();
        for (String rule : this.theGameRules.getRules()) {
            ruleMap.put(rule, this.theGameRules.getGameRuleStringValue(rule));
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
        return this.worldEnabled;
    }

    @Override
    public void setEnabled(boolean state) {
        this.worldEnabled = state;
    }

    @Override
    public boolean loadOnStartup() {
        return this.loadOnStartup;
    }

    @Override
    public void setLoadOnStartup(boolean state) {
        this.loadOnStartup = state;
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    @Override
    public void setKeepSpawnLoaded(boolean state) {
        this.keepSpawnLoaded = state;
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
    public boolean getIsMod() {
        return this.isMod;
    }

    @Override
    public Collection<WorldGeneratorModifier> getGeneratorModifiers() {
        if (this.generatorModifiers == null) {
            return ImmutableList.of();
        }
        return WorldGeneratorRegistry.getInstance().toModifiers(this.generatorModifiers);
    }

    @Override
    public void setGeneratorModifiers(Collection<WorldGeneratorModifier> modifiers) {
        checkNotNull(modifiers, "modifiers");

        this.generatorModifiers = WorldGeneratorRegistry.getInstance().toIds(modifiers);
    }

    @Override
    public DataContainer getGeneratorSettings() {
        // Minecraft uses a String, we want to return a fancy DataContainer

        // Parse the world generator settings as JSON
        try {
            NBTTagCompound nbt = JsonToNBT.getTagFromJson(this.generatorOptions);
            return NbtTranslator.getInstance().translateFrom(nbt);
        } catch (NBTException ignored) {
        }
        return new MemoryDataContainer().set(DataQuery.of("customSettings"), this.generatorOptions);
    }

    @Override
    public Optional<DataView> getPropertySection(DataQuery path) {
        if (this.spongeRootLevelNbt.hasKey(path.toString())) {
            return Optional
                    .<DataView> of(NbtTranslator.getInstance().translateFrom(this.spongeRootLevelNbt.getCompoundTag(path.toString())));
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
    public NBTTagCompound getSpongeRootLevelNbt() {
        updateSpongeNbt();
        return this.spongeRootLevelNbt;
    }

    @Override
    public NBTTagCompound getSpongeNbt() {
        updateSpongeNbt();
        return this.spongeNbt;
    }

    @Override
    public void readSpongeNbt(NBTTagCompound nbt) {
        this.dimension = nbt.getInteger("dimensionId");
        this.uuid = new UUID(nbt.getLong("uuid_most"), nbt.getLong("uuid_least"));
        this.worldEnabled = nbt.getBoolean("enabled");
        this.keepSpawnLoaded = nbt.getBoolean("keepSpawnLoaded");
        this.loadOnStartup = nbt.getBoolean("loadOnStartup");
        this.isMod = nbt.getBoolean("isMod");
        for (DimensionType type : Sponge.getSpongeRegistry().dimensionClassMappings.values()) {
            if (type.getDimensionClass().getCanonicalName().equalsIgnoreCase(nbt.getString("dimensionType"))) {
                this.dimensionType = type;
            }
        }

        // Read generator modifiers
        NBTTagList generatorModifiersNbt = nbt.getTagList("generatorModifiers", 8);
        ImmutableList.Builder<String> ids = ImmutableList.builder();
        for (int i = 0; i < generatorModifiersNbt.tagCount(); i++) {
            ids.add(generatorModifiersNbt.getStringTagAt(i));
        }
        this.generatorModifiers = ids.build();
    }

    @Override
    public DataContainer getAdditionalProperties() {
        NBTTagCompound additionalProperties = (NBTTagCompound) this.spongeRootLevelNbt.copy();
        additionalProperties.removeTag(Sponge.ECOSYSTEM_NAME);
        return NbtTranslator.getInstance().translateFrom(additionalProperties);
    }

    @Override
    public void setSpongeRootLevelNBT(NBTTagCompound nbt) {
        this.spongeRootLevelNbt = nbt;
        if (nbt.hasKey(Sponge.ECOSYSTEM_NAME)) {
            this.spongeNbt = nbt.getCompoundTag(Sponge.ECOSYSTEM_NAME);
        } else {
            this.spongeRootLevelNbt.setTag(Sponge.ECOSYSTEM_NAME, this.spongeNbt);
        }
    }

    private void updateSpongeNbt() {
        if (this.levelName != null) {
            this.spongeNbt.setString("LevelName", this.levelName);
        }
        this.spongeNbt.setInteger("dimensionId", this.dimension);
        if (this.dimensionType != null) {
            this.spongeNbt.setString("dimensionType", this.dimensionType.getDimensionClass().getName());
        }
        if (this.uuid != null) {
            this.spongeNbt.setLong("uuid_most", this.uuid.getMostSignificantBits());
            this.spongeNbt.setLong("uuid_least", this.uuid.getLeastSignificantBits());
        }
        this.spongeNbt.setBoolean("enabled", this.worldEnabled);
        this.spongeNbt.setBoolean("keepSpawnLoaded", this.keepSpawnLoaded);
        this.spongeNbt.setBoolean("loadOnStartup", this.loadOnStartup);
        if (this.isMod) {
            this.spongeNbt.setBoolean("isMod", this.isMod);
        }

        if (this.generatorModifiers != null) {
            NBTTagList generatorModifierNbt = new NBTTagList();
            for (String generatorModifierId : this.generatorModifiers) {
                generatorModifierNbt.appendTag(new NBTTagString(generatorModifierId));
            }
            this.spongeNbt.setTag("generatorModifiers", generatorModifierNbt);
        }
    }
}
