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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.registry.CatalogTypeAlreadyRegisteredException;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.interfaces.world.IMixinWorldSettings;
import org.spongepowered.common.registry.type.world.GeneratorModifierRegistryModule;
import org.spongepowered.common.registry.type.world.WorldCreationSettingsRegistryModule;

import java.util.Optional;
import java.util.Random;

public class SpongeWorldCreationSettingsBuilder implements WorldCreationSettings.Builder {

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
    private boolean keepSpawnLoaded;
    private boolean generateSpawnOnLoad;
    private boolean pvpEnabled;
    private boolean commandsAllowed;
    private boolean generateBonusChest;
    private DataContainer generatorSettings;
    private ImmutableList<WorldGeneratorModifier> generatorModifiers;

    public SpongeWorldCreationSettingsBuilder() {
        reset();
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
    public WorldCreationSettings.Builder difficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
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

    @Override
    public SpongeWorldCreationSettingsBuilder pvp(boolean state) {
        this.pvpEnabled = state;
        return this;
    }

    @Override
    public WorldCreationSettings.Builder commandsAllowed(boolean state) {
        this.commandsAllowed = state;
        return this;
    }

    @Override
    public WorldCreationSettings.Builder generateBonusChest(boolean state) {
        this.generateBonusChest = state;
        return this;
    }

    @Override
    public WorldCreationSettings.Builder serializationBehavior(SerializationBehavior behavior) {
        this.serializationBehavior = behavior;
        return this;
    }

    @Override
    public WorldCreationSettings.Builder from(WorldCreationSettings value) {
        checkNotNull(value);
        this.dimensionType = value.getDimensionType();
        this.generatorType = value.getGeneratorType();
        this.gameMode = value.getGameMode();
        this.difficulty = value.getDifficulty();
        this.serializationBehavior = value.getSerializationBehavior();
        this.seed = value.getSeed();
        this.mapFeaturesEnabled = value.usesMapFeatures();
        this.hardcore = value.isHardcore();
        this.worldEnabled = value.isEnabled();
        this.loadOnStartup = value.loadOnStartup();
        this.keepSpawnLoaded = value.doesKeepSpawnLoaded();
        this.generatorSettings = value.getGeneratorSettings().copy();
        this.generatorModifiers = ImmutableList.copyOf(value.getGeneratorModifiers());
        this.pvpEnabled = value.isPVPEnabled();
        this.generateSpawnOnLoad = value.doesGenerateSpawnOnLoad();
        this.commandsAllowed = value.areCommandsAllowed();
        this.generateBonusChest = value.doesGenerateBonusChest();
        return this;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder from(WorldProperties properties) {
        checkNotNull(properties);
        this.dimensionType = properties.getDimensionType();
        this.generatorType = properties.getGeneratorType();
        this.gameMode = properties.getGameMode();
        this.difficulty = properties.getDifficulty();
        this.serializationBehavior = properties.getSerializationBehavior();
        this.seed = properties.getSeed();
        this.mapFeaturesEnabled = properties.usesMapFeatures();
        this.hardcore = properties.isHardcore();
        this.worldEnabled = properties.isEnabled();
        this.loadOnStartup = properties.loadOnStartup();
        this.keepSpawnLoaded = properties.doesKeepSpawnLoaded();
        this.generatorSettings = properties.getGeneratorSettings().copy();
        this.generatorModifiers = ImmutableList.copyOf(properties.getGeneratorModifiers());
        this.pvpEnabled = properties.isPVPEnabled();
        this.generateSpawnOnLoad = properties.doesGenerateSpawnOnLoad();
        this.commandsAllowed = properties.areCommandsAllowed();
        this.generateBonusChest = properties.doesGenerateBonusChest();
        return this;
    }

    @Override
    public WorldCreationSettings build(String id, String name) throws IllegalArgumentException, CatalogTypeAlreadyRegisteredException {
        final Optional<WorldCreationSettings> optWorldCreationSettings = WorldCreationSettingsRegistryModule.getInstance().getById(id);
        if (optWorldCreationSettings.isPresent()) {
            throw new CatalogTypeAlreadyRegisteredException(id);
        }
        final WorldSettings settings =
                new WorldSettings(this.seed, (WorldSettings.GameType) (Object) this.gameMode, this.mapFeaturesEnabled, this.hardcore,
                        (WorldType) this.generatorType);
        IMixinWorldSettings spongeSettings = (IMixinWorldSettings) (Object) settings;
        spongeSettings.setId(id);
        spongeSettings.setName(name);
        // TODO Register as catalog type
        spongeSettings.setDimensionType(this.dimensionType);
        spongeSettings.setDifficulty(this.difficulty);
        spongeSettings.setSerializationBehavior(this.serializationBehavior);
        spongeSettings.setGeneratorSettings(this.generatorSettings);
        spongeSettings.setGeneratorModifiers(this.generatorModifiers);
        spongeSettings.setEnabled(this.worldEnabled);
        spongeSettings.setLoadOnStartup(this.loadOnStartup);
        spongeSettings.setKeepSpawnLoaded(this.keepSpawnLoaded);
        spongeSettings.setGenerateSpawnOnLoad(this.generateSpawnOnLoad);
        spongeSettings.setPVPEnabled(this.pvpEnabled);
        spongeSettings.setCommandsAllowed(this.commandsAllowed);
        spongeSettings.setGenerateBonusChest(this.generateBonusChest);
        spongeSettings.fromBuilder(true);
        Sponge.getRegistry().register(WorldCreationSettings.class, (WorldCreationSettings) (Object) settings);
        return (WorldCreationSettings) (Object) settings;
    }

    @Override
    public SpongeWorldCreationSettingsBuilder reset() {
        this.dimensionType = DimensionTypes.OVERWORLD;
        this.generatorType = GeneratorTypes.DEFAULT;
        this.gameMode = GameModes.SURVIVAL;
        this.difficulty = Difficulties.NORMAL;
        this.serializationBehavior = SerializationBehaviors.AUTOMATIC;
        this.seed = new Random().nextLong();
        this.mapFeaturesEnabled = true;
        this.hardcore = false;
        this.worldEnabled = true;
        this.loadOnStartup = true;
        this.keepSpawnLoaded = true;
        this.generateSpawnOnLoad = true;
        this.generatorSettings = new MemoryDataContainer();
        this.generatorModifiers = ImmutableList.of();
        this.pvpEnabled = true;
        this.commandsAllowed = true;
        this.generateBonusChest = false;
        return this;
    }
}
