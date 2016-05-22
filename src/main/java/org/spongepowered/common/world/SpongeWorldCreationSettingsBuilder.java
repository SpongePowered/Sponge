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
package org.spongepowered.common.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.interfaces.world.IMixinWorldSettings;
import org.spongepowered.common.registry.type.world.GeneratorModifierRegistryModule;

import java.util.Random;

public class SpongeWorldCreationSettingsBuilder implements WorldCreationSettings.Builder {

    private String name;
    private long seed;
    private GameMode gameMode;
    private GeneratorType generatorType;
    private DimensionType dimensionType;
    private PortalAgentType portalAgentType;
    private boolean mapFeaturesEnabled;
    private boolean hardcore;
    private boolean worldEnabled;
    private boolean loadOnStartup;
    private boolean keepSpawnLoaded;
    private boolean generateSpawnOnLoad;
    private boolean isMod;
    private boolean pvpEnabled;
    private int dimensionId; // internal use only
    private DataContainer generatorSettings;
    private ImmutableList<WorldGeneratorModifier> generatorModifiers;

    public SpongeWorldCreationSettingsBuilder() {
        reset();
    }

    public SpongeWorldCreationSettingsBuilder(WorldCreationSettings settings) {
        this.name = settings.getWorldName();
        this.seed = settings.getSeed();
        this.gameMode = settings.getGameMode();
        this.generatorType = settings.getGeneratorType();
        this.generatorModifiers = ImmutableList.copyOf(settings.getGeneratorModifiers());
        this.dimensionType = settings.getDimensionType();
        this.portalAgentType = settings.getPortalAgentType();
        this.mapFeaturesEnabled = settings.usesMapFeatures();
        this.hardcore = settings.isHardcore();
        this.worldEnabled = settings.isEnabled();
        this.loadOnStartup = settings.loadOnStartup();
        this.keepSpawnLoaded = settings.doesKeepSpawnLoaded();
        this.generateSpawnOnLoad = settings.doesGenerateSpawnOnLoad();
        this.pvpEnabled = settings.isPVPEnabled();
    }

    public SpongeWorldCreationSettingsBuilder(WorldProperties properties) {
        this.name = properties.getWorldName();
        this.seed = properties.getSeed();
        this.gameMode = properties.getGameMode();
        this.generatorType = properties.getGeneratorType();
        this.generatorModifiers = ImmutableList.copyOf(properties.getGeneratorModifiers());
        this.dimensionType = properties.getDimensionType();
        this.portalAgentType = properties.getPortalAgentType();
        this.mapFeaturesEnabled = properties.usesMapFeatures();
        this.hardcore = properties.isHardcore();
        this.worldEnabled = properties.isEnabled();
        this.loadOnStartup = properties.loadOnStartup();
        this.keepSpawnLoaded = properties.doesKeepSpawnLoaded();
        this.generateSpawnOnLoad = properties.doesGenerateSpawnOnLoad();
        this.pvpEnabled = properties.isPVPEnabled();
    }

    @Override
    public SpongeWorldCreationSettingsBuilder fill(WorldCreationSettings settings) {
        checkNotNull(settings);
        this.name = settings.getWorldName();
        this.seed = settings.getSeed();
        this.gameMode = settings.getGameMode();
        this.generatorType = settings.getGeneratorType();
        this.dimensionType = settings.getDimensionType();
        this.portalAgentType = settings.getPortalAgentType();
        this.mapFeaturesEnabled = settings.usesMapFeatures();
        this.hardcore = settings.isHardcore();
        this.worldEnabled = settings.isEnabled();
        this.loadOnStartup = settings.loadOnStartup();
        this.keepSpawnLoaded = settings.doesKeepSpawnLoaded();
        this.pvpEnabled = settings.isPVPEnabled();
        this.generateSpawnOnLoad = settings.doesGenerateSpawnOnLoad();
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder fill(WorldProperties properties) {
        checkNotNull(properties);
        this.name = properties.getWorldName();
        this.seed = properties.getSeed();
        this.gameMode = properties.getGameMode();
        this.generatorType = properties.getGeneratorType();
        this.dimensionType = properties.getDimensionType();
        this.mapFeaturesEnabled = properties.usesMapFeatures();
        this.hardcore = properties.isHardcore();
        this.worldEnabled = properties.isEnabled();
        this.loadOnStartup = properties.loadOnStartup();
        this.keepSpawnLoaded = properties.doesKeepSpawnLoaded();
        this.pvpEnabled = properties.isPVPEnabled();
        this.generateSpawnOnLoad = properties.doesGenerateSpawnOnLoad();
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder seed(long seed) {
        this.seed = seed;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder gameMode(GameMode gameMode) {
        this.gameMode = gameMode;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder generator(GeneratorType type) {
        this.generatorType = type;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder dimension(DimensionType type) {
        this.dimensionType = type;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder usesMapFeatures(boolean enabled) {
        this.mapFeaturesEnabled = enabled;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder hardcore(boolean enabled) {
        this.hardcore = enabled;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder enabled(boolean state) {
        this.worldEnabled = state;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder loadsOnStartup(boolean state) {
        this.loadOnStartup = state;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder keepsSpawnLoaded(boolean state) {
        this.keepSpawnLoaded = state;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder generateSpawnOnLoad(boolean state) {
        this.generateSpawnOnLoad = state;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder generatorSettings(DataContainer settings) {
        this.generatorSettings = settings;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder generatorModifiers(WorldGeneratorModifier... modifiers) {
        ImmutableList<WorldGeneratorModifier> defensiveCopy = ImmutableList.copyOf(modifiers);
        GeneratorModifierRegistryModule.getInstance().checkAllRegistered(defensiveCopy);
        this.generatorModifiers = defensiveCopy;
        return this;
    }

    public SpongeWorldCreationSettingsBuilder dimensionId(int id) {
        this.dimensionId = id;
        return this;
    }

    public SpongeWorldCreationSettingsBuilder isMod(boolean flag) {
        this.isMod = flag;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder portalAgent(PortalAgentType type) {
        this.portalAgentType = type;
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder pvp(boolean enabled) {
        this.pvpEnabled = enabled;
        return this;
    }

    @Override
    public WorldCreationSettings.Builder from(WorldCreationSettings value) {
        this.name = value.getWorldName();
        this.seed = value.getSeed();
        this.gameMode = value.getGameMode();
        this.generatorType = value.getGeneratorType();
        this.dimensionType = value.getDimensionType();
        this.mapFeaturesEnabled = value.usesMapFeatures();
        this.hardcore = value.isHardcore();
        this.worldEnabled = value.isEnabled();
        this.loadOnStartup = value.loadOnStartup();
        this.keepSpawnLoaded = value.doesKeepSpawnLoaded();
        this.generatorSettings = value.getGeneratorSettings().copy();
        this.generatorModifiers = ImmutableList.copyOf(value.getGeneratorModifiers());
        this.dimensionId = ((IMixinWorldSettings) value).getDimensionId();
        this.isMod = ((IMixinWorldSettings) value).getIsMod();
        this.pvpEnabled = value.isPVPEnabled();
        this.generateSpawnOnLoad = value.doesGenerateSpawnOnLoad();
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder reset() {
        this.name = null;
        this.seed = new Random().nextLong();
        this.gameMode = GameModes.SURVIVAL;
        this.generatorType = GeneratorTypes.DEFAULT;
        this.dimensionType = DimensionTypes.OVERWORLD;
        this.portalAgentType = PortalAgentTypes.DEFAULT;
        this.mapFeaturesEnabled = true;
        this.hardcore = false;
        this.worldEnabled = true;
        this.loadOnStartup = true;
        this.keepSpawnLoaded = true;
        this.generateSpawnOnLoad = true;
        this.generatorSettings = new MemoryDataContainer();
        this.generatorModifiers = ImmutableList.of();
        this.dimensionId = 0;
        this.isMod = false;
        this.pvpEnabled = true;
        return this;
    }

    @Override
    public WorldCreationSettings build() {
        checkNotNull(this.name, "Building the settings to make a world requires a non-null name!");
        final WorldSettings settings =
                new WorldSettings(this.seed, (WorldSettings.GameType) (Object) this.gameMode, this.mapFeaturesEnabled, this.hardcore,
                        (WorldType) this.generatorType);
        ((IMixinWorldSettings) (Object) settings).setActualWorldName(this.name);
        ((IMixinWorldSettings) (Object) settings).setDimensionType(this.dimensionType);
        ((IMixinWorldSettings) (Object) settings).setPortalAgentType(this.portalAgentType);
        ((IMixinWorldSettings) (Object) settings).setGeneratorSettings(this.generatorSettings);
        ((IMixinWorldSettings) (Object) settings).setGeneratorModifiers(this.generatorModifiers);
        ((IMixinWorldSettings) (Object) settings).setEnabled(this.worldEnabled);
        ((IMixinWorldSettings) (Object) settings).setKeepSpawnLoaded(this.keepSpawnLoaded);
        ((IMixinWorldSettings) (Object) settings).setGenerateSpawnOnLoad(this.generateSpawnOnLoad);
        ((IMixinWorldSettings) (Object) settings).setLoadOnStartup(this.loadOnStartup);
        if (this.dimensionId != 0) {
            ((IMixinWorldSettings) (Object) settings).setDimensionId(this.dimensionId);
            ((IMixinWorldSettings) (Object) settings).setIsMod(this.isMod);
        }
        ((IMixinWorldSettings) (Object) settings).setPVPEnabled(this.pvpEnabled);
        ((IMixinWorldSettings) (Object) settings).fromBuilder(true);
        return (WorldCreationSettings) (Object) settings;
    }
}
