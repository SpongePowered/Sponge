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
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.registry.CatalogTypeAlreadyRegisteredException;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.registry.type.world.WorldArchetypeRegistryModule;
import org.spongepowered.common.registry.type.world.WorldGeneratorModifierRegistryModule;

import javax.annotation.Nullable;

public class SpongeWorldArchetypeBuilder implements WorldArchetype.Builder {

    private DimensionType dimensionType;
    private GeneratorType generatorType;
    private Difficulty difficulty;
    private GameMode gameMode;
    private SerializationBehavior serializationBehavior;
    private long seed;
    private boolean mapFeaturesEnabled;
    private boolean hardcore;
    private boolean worldEnabled;
    private boolean loadOnStartup;
    @Nullable
    private Boolean keepSpawnLoaded;
    private boolean generateSpawnOnLoad;
    private boolean pvpEnabled;
    private boolean commandsAllowed;
    private boolean generateBonusChest;
    private DataContainer generatorSettings;
    private ImmutableList<WorldGeneratorModifier> generatorModifiers;
    private PortalAgentType portalAgentType;
    private boolean seedRandomized;

    public SpongeWorldArchetypeBuilder() {
        reset();
    }

    @Override
    public SpongeWorldArchetypeBuilder seed(long seed) {
        this.seed = seed;
        this.seedRandomized = false;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder randomSeed() {
        this.seed = SpongeImpl.random.nextLong();
        this.seedRandomized = true;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder gameMode(GameMode gameMode) {
        this.gameMode = gameMode;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generator(GeneratorType type) {
        this.generatorType = type;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder dimension(DimensionType type) {
        this.dimensionType = type;
        return this;
    }

    @Override
    public WorldArchetype.Builder difficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder usesMapFeatures(boolean enabled) {
        this.mapFeaturesEnabled = enabled;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder hardcore(boolean enabled) {
        this.hardcore = enabled;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder enabled(boolean state) {
        this.worldEnabled = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder loadsOnStartup(boolean state) {
        this.loadOnStartup = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder keepsSpawnLoaded(boolean state) {
        this.keepSpawnLoaded = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generateSpawnOnLoad(boolean state) {
        this.generateSpawnOnLoad = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generatorSettings(DataContainer settings) {
        this.generatorSettings = settings;
        return this;
    }

    @Override
    public WorldArchetype.Builder portalAgent(PortalAgentType type) {
        this.portalAgentType = checkNotNull(type);
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generatorModifiers(WorldGeneratorModifier... modifiers) {
        ImmutableList<WorldGeneratorModifier> defensiveCopy = ImmutableList.copyOf(modifiers);
        WorldGeneratorModifierRegistryModule.getInstance().checkAllRegistered(defensiveCopy);
        this.generatorModifiers = defensiveCopy;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder pvp(boolean state) {
        this.pvpEnabled = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder commandsAllowed(boolean state) {
        this.commandsAllowed = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generateBonusChest(boolean state) {
        this.generateBonusChest = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder serializationBehavior(SerializationBehavior behavior) {
        this.serializationBehavior = behavior;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder from(WorldArchetype value) {
        checkNotNull(value);
        this.dimensionType = value.getDimensionType();
        this.generatorType = value.getGeneratorType();
        this.gameMode = value.getGameMode();
        this.difficulty = value.getDifficulty();
        this.serializationBehavior = value.getSerializationBehavior();
        this.seed = value.getSeed();
        this.seedRandomized = value.isSeedRandomized();
        this.mapFeaturesEnabled = value.usesMapFeatures();
        this.hardcore = value.isHardcore();
        this.worldEnabled = value.isEnabled();
        this.loadOnStartup = value.loadOnStartup();
        this.keepSpawnLoaded = value.doesKeepSpawnLoaded();
        this.generatorSettings = value.getGeneratorSettings();
        this.generatorModifiers = ImmutableList.copyOf(value.getGeneratorModifiers());
        this.pvpEnabled = value.isPVPEnabled();
        this.generateSpawnOnLoad = value.doesGenerateSpawnOnLoad();
        this.commandsAllowed = value.areCommandsAllowed();
        this.generateBonusChest = value.doesGenerateBonusChest();
        this.portalAgentType = value.getPortalAgentType();
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder from(WorldProperties value) {
        checkNotNull(value);
        this.dimensionType = value.getDimensionType();
        this.generatorType = value.getGeneratorType();
        this.gameMode = value.getGameMode();
        this.difficulty = value.getDifficulty();
        this.serializationBehavior = value.getSerializationBehavior();
        this.seed = value.getSeed();
        this.seedRandomized = false;
        this.mapFeaturesEnabled = value.usesMapFeatures();
        this.hardcore = value.isHardcore();
        this.worldEnabled = value.isEnabled();
        this.loadOnStartup = value.loadOnStartup();
        this.keepSpawnLoaded = value.doesKeepSpawnLoaded();
        this.generatorSettings = value.getGeneratorSettings();
        this.generatorModifiers = ImmutableList.copyOf(value.getGeneratorModifiers());
        this.pvpEnabled = value.isPVPEnabled();
        this.generateSpawnOnLoad = value.doesGenerateSpawnOnLoad();
        this.commandsAllowed = value.areCommandsAllowed();
        this.generateBonusChest = value.doesGenerateBonusChest();
        this.portalAgentType = value.getPortalAgentType();
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public WorldArchetype build(String id, String name) throws IllegalArgumentException, CatalogTypeAlreadyRegisteredException {
        WorldArchetypeRegistryModule.getInstance().getById(id).ifPresent(w -> {
            throw new CatalogTypeAlreadyRegisteredException(id);
        });
        final WorldSettings settings =
                new WorldSettings(this.seed, (GameType) (Object) this.gameMode, this.mapFeaturesEnabled, this.hardcore,
                        (WorldType) this.generatorType);
        WorldSettingsBridge spongeSettings = (WorldSettingsBridge) (Object) settings;
        spongeSettings.bridge$setId(id);
        spongeSettings.bridge$setName(name);
        spongeSettings.bridge$setDimensionType(this.dimensionType);
        spongeSettings.bridge$setDifficulty(this.difficulty);
        spongeSettings.bridge$setSerializationBehavior(this.serializationBehavior);
        spongeSettings.bridge$setGeneratorSettings(this.generatorSettings);
        spongeSettings.bridge$setGeneratorModifiers(this.generatorModifiers);
        spongeSettings.bridge$setEnabled(this.worldEnabled);
        spongeSettings.bridge$setLoadOnStartup(this.loadOnStartup);
        spongeSettings.bridge$setKeepSpawnLoaded(this.keepSpawnLoaded);
        spongeSettings.bridge$setGenerateSpawnOnLoad(this.generateSpawnOnLoad);
        spongeSettings.bridge$setPVPEnabled(this.pvpEnabled);
        spongeSettings.bridge$setCommandsAllowed(this.commandsAllowed);
        spongeSettings.bridge$setGenerateBonusChest(this.generateBonusChest);
        spongeSettings.bridge$setPortalAgentType(this.portalAgentType);
        spongeSettings.bridge$setRandomSeed(this.seedRandomized);
        Sponge.getRegistry().register(WorldArchetype.class, (WorldArchetype) (Object) settings);
        return (WorldArchetype) (Object) settings;
    }

    @Override
    public SpongeWorldArchetypeBuilder reset() {
        this.dimensionType = DimensionTypes.OVERWORLD;
        this.generatorType = GeneratorTypes.DEFAULT;
        this.gameMode = GameModes.SURVIVAL;
        this.difficulty = Difficulties.NORMAL;
        this.serializationBehavior = SerializationBehaviors.AUTOMATIC;
        this.seed = SpongeImpl.random.nextLong();
        this.seedRandomized = true;
        this.mapFeaturesEnabled = true;
        this.hardcore = false;
        this.worldEnabled = true;
        this.loadOnStartup = true;
        this.keepSpawnLoaded = null;
        this.generateSpawnOnLoad = ((DimensionTypeBridge) this.dimensionType).bridge$shouldGenerateSpawnOnLoad();
        this.generatorSettings = DataContainer.createNew();
        this.generatorModifiers = ImmutableList.of();
        this.pvpEnabled = true;
        this.commandsAllowed = true;
        this.generateBonusChest = false;
        this.portalAgentType = PortalAgentTypes.DEFAULT;
        return this;
    }
}
