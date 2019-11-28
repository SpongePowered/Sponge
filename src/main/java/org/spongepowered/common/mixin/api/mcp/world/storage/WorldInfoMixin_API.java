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
package org.spongepowered.common.mixin.api.mcp.world.storage;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.GameRulesBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.registry.type.world.WorldGeneratorModifierRegistryModule;
import org.spongepowered.common.util.Constants;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(WorldInfo.class)
@Implements(@Interface(iface = WorldProperties.class, prefix = "worldproperties$"))
public abstract class WorldInfoMixin_API implements WorldProperties {

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
    @Shadow private String levelName;
    @Shadow private int saveVersion;
    @Shadow private int cleanWeatherTime;
    @Shadow private boolean raining;
    @Shadow private int rainTime;
    @Shadow private boolean thundering;
    @Shadow private int thunderTime;
    @Shadow private GameType gameType;
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
    @Shadow private GameRules gameRules;

    @Shadow public abstract void setDifficulty(EnumDifficulty newDifficulty);
    @Shadow public abstract NBTTagCompound cloneNBTCompound(@Nullable NBTTagCompound nbt);
    @Shadow public abstract String shadow$getWorldName();

    private SerializationBehavior api$serializationBehavior = SerializationBehaviors.AUTOMATIC;

    @Override
    public Vector3i getSpawnPosition() {
        return new Vector3i(this.spawnX, this.spawnY, this.spawnZ);
    }

    @Override
    public void setSpawnPosition(final Vector3i position) {
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
    public void setGeneratorType(final GeneratorType type) {
        this.terrainType = (WorldType) type;
    }

    @Intrinsic
    public long worldproperties$getSeed() {
        return this.randomSeed;
    }

    @Override
    public void setSeed(final long seed) {
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
    public void setWorldTime(final long time) {
        this.worldTime = time;
    }

    @Override
    public DimensionType getDimensionType() {
        return ((WorldInfoBridge) this).bridge$getDimensionType();
    }

    @Override
    public PortalAgentType getPortalAgentType() {
        return ((WorldInfoBridge) this).bridge$getPortalAgent();
    }

    @Intrinsic
    public boolean worldproperties$isRaining() {
        return this.raining;
    }

    @Override
    public void setRaining(final boolean state) {
        this.raining = state;
    }

    @Intrinsic
    public int worldproperties$getRainTime() {
        return this.rainTime;
    }

    @Intrinsic
    public void worldproperties$setRainTime(final int time) {
        this.rainTime = time;
    }

    @Intrinsic
    public boolean worldproperties$isThundering() {
        return this.thundering;
    }

    @Intrinsic
    public void worldproperties$setThundering(final boolean state) {
        this.thundering = state;
    }

    @Override
    public int getThunderTime() {
        return this.thunderTime;
    }

    @Override
    public void setThunderTime(final int time) {
        this.thunderTime = time;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public GameMode getGameMode() {
        return (GameMode) (Object) this.gameType;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setGameMode(final GameMode gamemode) {
        this.gameType = (GameType) (Object) gamemode;
    }

    @Override
    public boolean usesMapFeatures() {
        return this.mapFeaturesEnabled;
    }

    @Override
    public void setMapFeaturesEnabled(final boolean state) {
        this.mapFeaturesEnabled = state;
    }

    @Override
    public boolean isHardcore() {
        return this.hardcore;
    }

    @Override
    public void setHardcore(final boolean state) {
        this.hardcore = state;
    }

    @Override
    public boolean areCommandsAllowed() {
        return this.allowCommands;
    }

    @Override
    public void setCommandsAllowed(final boolean state) {
        this.allowCommands = state;
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Difficulty getDifficulty() {
        return (Difficulty) (Object) this.difficulty;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setDifficulty(final Difficulty difficulty) {
        this.setDifficulty((EnumDifficulty) (Object) difficulty);
    }

    @Override
    public boolean isPVPEnabled() {
        return  ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorld().getPVPEnabled();
    }

    @Override
    public void setPVPEnabled(final boolean enabled) {
        ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorld().setPVPEnabled(enabled);
    }

    @Override
    public boolean doesGenerateBonusChest() {
        return ((WorldInfoBridge) this).bridge$getSpawnsBonusChest();
    }

    @Override
    public Vector3d getWorldBorderCenter() {
        return new Vector3d(this.borderCenterX, 0, this.borderCenterZ);
    }

    @Override
    public void setWorldBorderCenter(final double x, final double z) {
        this.borderCenterX = x;
        this.borderCenterZ = z;
    }

    @Override
    public double getWorldBorderDiameter() {
        return this.borderSize;
    }

    @Override
    public void setWorldBorderDiameter(final double diameter) {
        this.borderSize = diameter;
    }

    @Override
    public double getWorldBorderTargetDiameter() {
        return this.borderSizeLerpTarget;
    }

    @Override
    public void setWorldBorderTargetDiameter(final double diameter) {
        this.borderSizeLerpTarget = diameter;
    }

    @Override
    public double getWorldBorderDamageThreshold() {
        return this.borderSafeZone;
    }

    @Override
    public void setWorldBorderDamageThreshold(final double distance) {
        this.borderSafeZone = distance;
    }

    @Override
    public double getWorldBorderDamageAmount() {
        return this.borderDamagePerBlock;
    }

    @Override
    public void setWorldBorderDamageAmount(final double damage) {
        this.borderDamagePerBlock = damage;
    }

    @Override
    public int getWorldBorderWarningTime() {
        return this.borderWarningTime;
    }

    @Override
    public void setWorldBorderWarningTime(final int time) {
        this.borderWarningTime = time;
    }

    @Override
    public int getWorldBorderWarningDistance() {
        return this.borderWarningDistance;
    }

    @Override
    public void setWorldBorderWarningDistance(final int distance) {
        this.borderWarningDistance = distance;
    }

    @Override
    public long getWorldBorderTimeRemaining() {
        return this.borderSizeLerpTime;
    }

    @Override
    public void setWorldBorderTimeRemaining(final long time) {
        this.borderSizeLerpTime = time;
    }

    @Override
    public Optional<String> getGameRule(final String gameRule) {
        checkNotNull(gameRule, "The gamerule cannot be null!");
        if (this.gameRules.func_82765_e(gameRule)) {
            return Optional.of(this.gameRules.func_82767_a(gameRule));
        }
        return Optional.empty();
    }

    @Override
    public Map<String, String> getGameRules() {
        final ImmutableMap.Builder<String, String> ruleMap = ImmutableMap.builder();
        for (final String rule : this.gameRules.func_82763_b()) {
            ruleMap.put(rule, this.gameRules.func_82767_a(rule));
        }
        return ruleMap.build();
    }

    @Override
    public void setGameRule(final String gameRule, final String value) {
        checkNotNull(gameRule, "The gamerule cannot be null!");
        checkNotNull(value, "The gamerule value cannot be null!");
        this.gameRules.func_82764_b(gameRule, value);
    }

    @Override
    public boolean removeGameRule(final String gameRule) {
        checkNotNull(gameRule, "The gamerule cannot be null!");
        return ((GameRulesBridge) this.gameRules).bridge$removeGameRule(gameRule);
    }

    @Override
    public UUID getUniqueId() {
        return ((WorldInfoBridge) this).bridge$getAssignedId();
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return NbtTranslator.getInstance().translateFrom(this.cloneNBTCompound(null));
    }

    @Override
    public boolean isEnabled() {
        return  ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorld().isWorldEnabled();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorld().setWorldEnabled(enabled);
    }

    @Override
    public boolean loadOnStartup() {
        Boolean loadOnStartup =  ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorld().loadOnStartup();
        if (loadOnStartup == null) {
           loadOnStartup = ((DimensionTypeBridge) ((WorldInfoBridge) this).bridge$getDimensionType()).bridge$shouldGenerateSpawnOnLoad();
           this.setLoadOnStartup(loadOnStartup);
        }
        return loadOnStartup;
    }

    @Override
    public void setLoadOnStartup(final boolean state) {
        ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorld().setLoadOnStartup(state);
        ((WorldInfoBridge) this).bridge$saveConfig();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean doesKeepSpawnLoaded() {
        Boolean keepSpawnLoaded =  ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorld().getKeepSpawnLoaded();
        if (keepSpawnLoaded == null) {
            keepSpawnLoaded = ((DimensionTypeBridge) ((WorldInfoBridge) this).bridge$getDimensionType()).bridge$shouldLoadSpawn();
        } else if (((WorldInfoBridge) this).bridge$getIsMod() && !keepSpawnLoaded) { // If disabled and a mod dimension, validate
            final Integer dimensionId = ((WorldInfoBridge) this).bridge$getDimensionId();

            if (dimensionId != null && dimensionId == ((net.minecraft.world.dimension.DimensionType)(Object) ((WorldInfoBridge) this).bridge$getDimensionType()).func_186068_a()) {
                if (((DimensionTypeBridge)((WorldInfoBridge) this).bridge$getDimensionType()).bridge$shouldKeepSpawnLoaded()) {
                    this.setKeepSpawnLoaded(true);
                    keepSpawnLoaded = true;
                }
            }
        }
        return keepSpawnLoaded;
    }

    @Override
    public void setKeepSpawnLoaded(final boolean loaded) {
        ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorld().setKeepSpawnLoaded(loaded);
        ((WorldInfoBridge) this).bridge$saveConfig();
    }

    @Override
    public boolean doesGenerateSpawnOnLoad() {
        Boolean shouldGenerateSpawn =  ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorld().getGenerateSpawnOnLoad();
        if (shouldGenerateSpawn == null) {
            shouldGenerateSpawn = ((DimensionTypeBridge) ((WorldInfoBridge) this).bridge$getDimensionType()).bridge$shouldGenerateSpawnOnLoad();
            this.setGenerateSpawnOnLoad(shouldGenerateSpawn);
        }
        return shouldGenerateSpawn;
    }

    @Override
    public void setGenerateSpawnOnLoad(final boolean state) {
        ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorld().setGenerateSpawnOnLoad(state);
    }

    @Override
    public Collection<WorldGeneratorModifier> getGeneratorModifiers() {
        return WorldGeneratorModifierRegistryModule.getInstance().toModifiers( ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorldGenModifiers());
    }

    @Override
    public void setGeneratorModifiers(final Collection<WorldGeneratorModifier> modifiers) {
        checkNotNull(modifiers, "modifiers");

        ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorldGenModifiers().clear();
        ((WorldInfoBridge) this).bridge$getConfigAdapter().getConfig().getWorldGenModifiers().addAll(WorldGeneratorModifierRegistryModule.getInstance().toIds(modifiers));
    }

    @Override
    public DataContainer getGeneratorSettings() {
        // Minecraft uses a String, we want to return a fancy DataContainer
        // Parse the world generator settings as JSON
        try {
            return DataFormats.JSON.read(this.generatorOptions);
        } catch (JsonParseException | IOException ignored) {
        }
        return DataContainer.createNew().set(Constants.Sponge.World.WORLD_CUSTOM_SETTINGS, this.generatorOptions);
    }

    @Override
    public SerializationBehavior getSerializationBehavior() {
        return this.api$serializationBehavior;
    }

    @Override
    public void setSerializationBehavior(final SerializationBehavior behavior) {
        this.api$serializationBehavior = behavior;
    }

    @Override
    public Optional<DataView> getPropertySection(final DataQuery path) {
        if ( ((WorldInfoBridge) this).bridge$getSpongeRootLevelNbt().func_74764_b(path.toString())) {
            try {
                final NBTTagCompound property = ((WorldInfoBridge) this).bridge$getSpongeRootLevelNbt().func_74775_l(path.toString());
                return Optional.of(NbtTranslator.getInstance().translateFrom(property));
            } catch (Exception e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public void setPropertySection(final DataQuery path, final DataView data) {
        final NBTTagCompound nbt = NbtTranslator.getInstance().translateData(data);
        ((WorldInfoBridge) this).bridge$getSpongeRootLevelNbt().func_74782_a(path.toString(), nbt);
    }

    @Intrinsic
    public String worldproperties$getWorldName() {
        return this.shadow$getWorldName();
    }


    @Override
    public DataContainer getAdditionalProperties() {
        final NBTTagCompound additionalProperties = ((WorldInfoBridge) this).bridge$getSpongeRootLevelNbt().func_74737_b();
        additionalProperties.func_82580_o(SpongeImpl.ECOSYSTEM_NAME);
        return NbtTranslator.getInstance().translateFrom(additionalProperties);
    }

}
