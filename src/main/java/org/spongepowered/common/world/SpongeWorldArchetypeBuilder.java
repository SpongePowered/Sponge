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

import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.api.world.gen.GeneratorModifierType;
import org.spongepowered.api.world.gen.GeneratorModifierTypes;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.world.dimension.SpongeDimensionType;

import java.util.Objects;
import java.util.Random;

public final class SpongeWorldArchetypeBuilder implements WorldArchetype.Builder {

    public static final Random RANDOM = new Random();

    private ResourceKey key;
    private SpongeDimensionType dimensionType = (SpongeDimensionType) DimensionTypes.OVERWORLD.get();
    private GeneratorModifierType generatorModifier = GeneratorModifierTypes.NONE.get();
    private Difficulty difficulty = Difficulties.NORMAL.get();
    private GameMode gameMode = GameModes.SURVIVAL.get();
    private SerializationBehavior serializationBehavior = SerializationBehavior.AUTOMATIC;
    private DataContainer generatorSettings = DataContainer.createNew();

    private long seed;
    private boolean structuresEnabled;
    private boolean hardcore;
    private boolean enabled;
    private boolean loadOnStartup;
    private boolean keepSpawnLoaded;
    private boolean generateSpawnOnLoad;
    private boolean pvpEnabled;
    private boolean commandsEnabled;
    private boolean generateBonusChest = false;
    private boolean randomizedSeed;

    public SpongeWorldArchetypeBuilder() {
        this.reset();
    }

    @Override
    public WorldArchetype.Builder key(final ResourceKey key) {
        this.key = Objects.requireNonNull(key);
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder seed(final long seed) {
        this.seed = seed;
        this.randomizedSeed = false;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder randomSeed() {
        this.seed = SpongeWorldArchetypeBuilder.RANDOM.nextLong();
        this.randomizedSeed = true;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder gameMode(final GameMode gameMode) {
        Objects.requireNonNull(gameMode);
        this.gameMode = gameMode;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generatorModifierType(final GeneratorModifierType modifier) {
        Objects.requireNonNull(modifier);
        this.generatorModifier = modifier;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder dimensionType(final DimensionType type) {
        Objects.requireNonNull(type);
        this.dimensionType = (SpongeDimensionType) type;
        return this;
    }

    @Override
    public WorldArchetype.Builder difficulty(final Difficulty difficulty) {
        Objects.requireNonNull(difficulty);
        this.difficulty = difficulty;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generateStructures(boolean state) {
        this.structuresEnabled = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder hardcore(boolean state) {
        this.hardcore = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder enabled(boolean state) {
        this.enabled = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder loadOnStartup(boolean state) {
        this.loadOnStartup = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder keepSpawnLoaded(boolean state) {
        this.keepSpawnLoaded = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generateSpawnOnLoad(boolean state) {
        this.generateSpawnOnLoad = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder pvpEnabled(boolean state) {
        this.pvpEnabled = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder commandsEnabled(boolean state) {
        this.commandsEnabled = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generateBonusChest(boolean state) {
        this.generateBonusChest = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder serializationBehavior(final SerializationBehavior behavior) {
        Objects.requireNonNull(behavior);
        this.serializationBehavior = behavior;
        return this;
    }

    @Override
    public WorldArchetype.Builder generatorSettings(final DataContainer generatorSettings) {
        Objects.requireNonNull(generatorSettings);
        this.generatorSettings = generatorSettings;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder from(final WorldArchetype value) {
        Objects.requireNonNull(value);

        this.key = null;
        this.dimensionType(value.getDimensionType());
        this.generatorModifierType(value.getGeneratorModifier());
        this.gameMode(value.getGameMode());
        this.difficulty(value.getDifficulty());
        this.serializationBehavior(value.getSerializationBehavior());
        this.seed = value.getSeed();
        this.randomizedSeed = value.isSeedRandomized();
        this.structuresEnabled = value.areStructuresEnabled();
        this.hardcore = value.isHardcore();
        this.enabled = value.isEnabled();
        this.loadOnStartup = value.doesLoadOnStartup();
        this.keepSpawnLoaded = value.doesKeepSpawnLoaded();
        this.pvpEnabled = value.isPVPEnabled();
        this.generateSpawnOnLoad = value.doesGenerateSpawnOnLoad();
        this.commandsEnabled = value.areCommandsEnabled();
        this.generateBonusChest = value.doesGenerateBonusChest();
        this.generatorSettings = value.getGeneratorSettings().copy();
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder from(final WorldProperties value) {
        Objects.requireNonNull(value);

        this.key = null;
        this.dimensionType(value.getDimensionType());
        this.generatorModifierType(value.getGeneratorModifierType());
        this.gameMode(value.getGameMode());
        this.difficulty(value.getDifficulty());
        this.serializationBehavior(value.getSerializationBehavior());
        this.seed = value.getSeed();
        this.randomizedSeed = false;
        this.structuresEnabled = value.areStructuresEnabled();
        this.hardcore = value.isHardcore();
        this.enabled = value.isEnabled();
        this.loadOnStartup = value.doesLoadOnStartup();
        this.keepSpawnLoaded = value.doesKeepSpawnLoaded();
        this.pvpEnabled = value.isPVPEnabled();
        this.generateSpawnOnLoad = value.doesGenerateSpawnOnLoad();
        this.commandsEnabled = value.areCommandsEnabled();
        this.generateBonusChest = value.doesGenerateBonusChest();
        this.generatorSettings = value.getGeneratorSettings().copy();
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder reset() {
        this.key = null;
        this.dimensionType = (SpongeDimensionType) DimensionTypes.OVERWORLD.get();
        this.generatorModifier = GeneratorModifierTypes.NONE.get();
        this.gameMode = GameModes.SURVIVAL.get();
        this.difficulty = Difficulties.NORMAL.get();
        this.serializationBehavior = SerializationBehavior.AUTOMATIC;
        this.seed = SpongeWorldArchetypeBuilder.RANDOM.nextLong();
        this.randomizedSeed = true;
        this.structuresEnabled = true;
        this.hardcore = false;
        this.enabled = true;
        this.loadOnStartup = true;
        this.keepSpawnLoaded = false;
        this.generateSpawnOnLoad = PlatformHooks.getInstance().getDimensionHooks().doesGenerateSpawnOnLoad(this.dimensionType);
        this.generatorSettings = DataContainer.createNew();
        this.pvpEnabled = true;
        this.commandsEnabled = true;
        this.generateBonusChest = false;
        this.generatorSettings = this.generatorModifier.getDefaultGeneratorSettings().copy();
        return this;
    }

    @Override
    public WorldArchetype build() throws IllegalArgumentException {
        Objects.requireNonNull(this.key);

        final WorldSettings settings = new WorldSettings(this.seed, (GameType) (Object) this.gameMode, this.structuresEnabled, this.hardcore,
            (WorldType) this.generatorModifier);
        final WorldSettingsBridge settingsBridge = (WorldSettingsBridge) (Object) settings;
        ((ResourceKeyBridge) (Object) settings).bridge$setKey(this.key);
        settingsBridge.bridge$setLogicType(this.dimensionType);
        settingsBridge.bridge$setDifficulty((net.minecraft.world.Difficulty) (Object) this.difficulty);
        settingsBridge.bridge$setSerializationBehavior(this.serializationBehavior);
        settingsBridge.bridge$setGeneratorSettings(this.generatorSettings);
        settingsBridge.bridge$setEnabled(this.enabled);
        settingsBridge.bridge$setLoadOnStartup(this.loadOnStartup);
        settingsBridge.bridge$setKeepSpawnLoaded(this.keepSpawnLoaded);
        settingsBridge.bridge$setGenerateSpawnOnLoad(this.generateSpawnOnLoad);
        settingsBridge.bridge$setPVPEnabled(this.pvpEnabled);
        settingsBridge.bridge$setCommandsEnabled(this.commandsEnabled);
        settingsBridge.bridge$setGenerateBonusChest(this.generateBonusChest);
        settingsBridge.bridge$setRandomSeed(this.randomizedSeed);
        settingsBridge.bridge$setGeneratorSettings(this.generatorSettings);

        return (WorldArchetype) (Object) settings;
    }
}
