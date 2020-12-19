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

import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.api.world.gen.WorldGenerationSettings;
import org.spongepowered.api.world.server.ServerWorldProperties;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.hooks.PlatformHooks;

import java.util.Objects;

public final class SpongeWorldArchetypeBuilder implements WorldArchetype.Builder {

    private DimensionType dimensionType = DimensionTypes.OVERWORLD.get(Sponge.getServer().registries());
    private Difficulty difficulty = Difficulties.NORMAL.get();
    private GameMode gameMode = GameModes.SURVIVAL.get();
    private SerializationBehavior serializationBehavior = SerializationBehavior.AUTOMATIC;

    private boolean hardcore;
    private boolean enabled;
    private boolean loadOnStartup;
    private boolean keepSpawnLoaded = false;
    private boolean generateSpawnOnLoad = false;
    private boolean pvpEnabled;
    private boolean commandsEnabled;
    private WorldGenerationSettings worldGenerationSettings;

    public SpongeWorldArchetypeBuilder() {
        this.reset();
    }

    @Override
    public SpongeWorldArchetypeBuilder gameMode(final GameMode gameMode) {
        this.gameMode = Objects.requireNonNull(gameMode, "game mode");
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder dimensionType(final DimensionType type) {
        this.dimensionType = Objects.requireNonNull(type, "type");
        return this;
    }

    @Override
    public WorldArchetype.Builder difficulty(final Difficulty difficulty) {
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
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
    public WorldArchetype.Builder worldGenerationSettings(final WorldGenerationSettings worldGenerationSettings) {
        this.worldGenerationSettings = worldGenerationSettings;
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
    public SpongeWorldArchetypeBuilder serializationBehavior(final SerializationBehavior behavior) {
        this.serializationBehavior = Objects.requireNonNull(behavior);
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder from(final WorldArchetype value) {
        Objects.requireNonNull(value);

        this.dimensionType = value.getDimensionType();
        this.worldGenerationSettings = value.getWorldGeneratorSettings();
        this.gameMode = value.getGameMode();
        this.difficulty = value.getDifficulty();
        this.serializationBehavior = value.getSerializationBehavior();
        this.hardcore = value.isHardcore();
        this.enabled = value.isEnabled();
        this.loadOnStartup = value.doesLoadOnStartup();
        this.keepSpawnLoaded = value.doesKeepSpawnLoaded();
        this.pvpEnabled = value.isPVPEnabled();
        this.generateSpawnOnLoad = value.doesGenerateSpawnOnLoad();
        this.commandsEnabled = value.areCommandsEnabled();
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder from(final ServerWorldProperties value) {
        Objects.requireNonNull(value, "value");

        this.dimensionType(value.getDimensionType());
        this.worldGenerationSettings = value.getWorldGenerationSettings();
        this.gameMode(value.getGameMode());
        this.difficulty(value.getDifficulty());
        this.serializationBehavior(value.getSerializationBehavior());
        this.hardcore = value.isHardcore();
        this.enabled = value.isEnabled();
        this.loadOnStartup = value.doesLoadOnStartup();
        this.keepSpawnLoaded = value.doesKeepSpawnLoaded();
        this.pvpEnabled = value.isPVPEnabled();
        this.generateSpawnOnLoad = value.doesGenerateSpawnOnLoad();
        this.commandsEnabled = value.areCommandsEnabled();
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder reset() {
        this.dimensionType = DimensionTypes.OVERWORLD.get(Sponge.getServer().registries());
        this.worldGenerationSettings = null;
        this.gameMode = GameModes.SURVIVAL.get();
        this.difficulty = Difficulties.NORMAL.get();
        this.serializationBehavior = SerializationBehavior.AUTOMATIC;
        this.hardcore = false;
        this.enabled = true;
        this.loadOnStartup = true;
        this.keepSpawnLoaded = false;
        this.generateSpawnOnLoad = PlatformHooks.INSTANCE.getDimensionHooks().doesGenerateSpawnOnLoad((net.minecraft.world.DimensionType) this.dimensionType);
        this.pvpEnabled = true;
        this.commandsEnabled = true;
        return this;
    }

    @Override
    public WorldArchetype build() throws IllegalArgumentException {
        Objects.requireNonNull(this.worldGenerationSettings, "world generation settings");

        final WorldSettings settings = new WorldSettings("", (GameType) (Object) this.gameMode, this.hardcore,
            (net.minecraft.world.Difficulty) (Object) this.difficulty, this.commandsEnabled, new GameRules(), DatapackCodec.DEFAULT);

        final WorldSettingsBridge settingsBridge = (WorldSettingsBridge) (Object) settings;
        settingsBridge.bridge$setDimensionType((net.minecraft.world.DimensionType) this.dimensionType);
        settingsBridge.bridge$setWorldGenerationSettings(this.worldGenerationSettings);
        settingsBridge.bridge$setSerializationBehavior(this.serializationBehavior);
        settingsBridge.bridge$setEnabled(this.enabled);
        settingsBridge.bridge$setLoadOnStartup(this.loadOnStartup);
        settingsBridge.bridge$setKeepSpawnLoaded(this.keepSpawnLoaded);
        settingsBridge.bridge$setGenerateSpawnOnLoad(this.generateSpawnOnLoad);
        settingsBridge.bridge$setPVPEnabled(this.pvpEnabled);
        settingsBridge.bridge$setCommandsEnabled(this.commandsEnabled);

        return (WorldArchetype) (Object) settings;
    }
}
