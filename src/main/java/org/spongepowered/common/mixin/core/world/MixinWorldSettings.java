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
package org.spongepowered.common.mixin.core.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.difficulty.Difficulties;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.interfaces.world.IMixinWorldSettings;
import org.spongepowered.common.registry.type.world.GeneratorModifierRegistryModule;
import org.spongepowered.common.util.persistence.JsonTranslator;

import java.util.Collection;

@NonnullByDefault
@Mixin(WorldSettings.class)
@Implements(value = @Interface(iface = WorldCreationSettings.class, prefix = "creationSettings$"))
public abstract class MixinWorldSettings implements WorldCreationSettings, IMixinWorldSettings {

    @Shadow private boolean commandsAllowed;

    @Shadow(prefix = "settings$") abstract long settings$getSeed();
    @Shadow abstract boolean isBonusChestEnabled();
    @Shadow abstract WorldSettings.GameType getGameType();
    @Shadow abstract boolean getHardcoreEnabled();
    @Shadow abstract boolean isMapFeaturesEnabled();
    @Shadow abstract WorldType getTerrainType();
    @Shadow(prefix = "settings$") abstract boolean settings$areCommandsAllowed();
    @Shadow abstract String getGeneratorOptions();

    private String id, name;
    private DimensionType dimensionType = DimensionTypes.OVERWORLD;
    private Difficulty difficulty = Difficulties.NORMAL;
    private SerializationBehavior serializationBehavior = SerializationBehaviors.AUTOMATIC;
    private DataContainer generatorSettings = new MemoryDataContainer();
    private boolean isEnabled = true;
    private boolean loadOnStartup = true;
    private boolean keepSpawnLoaded = true;
    private boolean generateSpawnOnLoad = true;
    private boolean pvpEnabled = true;
    private boolean generateBonusChest = false;
    private boolean fromBuilder = false;
    private Collection<WorldGeneratorModifier> generatorModifiers = ImmutableList.of();

    @Inject(method = "<init>(Lnet/minecraft/world/storage/WorldInfo;)V", at = @At(value = "RETURN"))
    public void onConstruct(WorldInfo info, CallbackInfo ci) {
        //Set above: info.getSeed(), info.getGameType(),  info.isMapFeaturesEnabled(), info.isHardcoreModeEnabled(), info.getTerrainType()

        final WorldProperties properties = (WorldProperties) info;
        this.dimensionType = properties.getDimensionType();
        this.difficulty = properties.getDifficulty();
        this.serializationBehavior = properties.getSerializationBehavior();
        this.generatorSettings = properties.getGeneratorSettings();
        this.isEnabled = properties.isEnabled();
        this.loadOnStartup = properties.loadOnStartup();
        this.keepSpawnLoaded = properties.doesKeepSpawnLoaded();
        this.generateSpawnOnLoad = properties.doesGenerateSpawnOnLoad();
        this.pvpEnabled = properties.isPVPEnabled();
        this.generateBonusChest = properties.doesGenerateBonusChest();
        GeneratorModifierRegistryModule.getInstance().checkAllRegistered(properties.getGeneratorModifiers());
        this.generatorModifiers = ImmutableList.copyOf(properties.getGeneratorModifiers());

    }

    @Intrinsic
    public long creationSettings$getSeed() {
        return this.settings$getSeed();
    }

    @Inject(method = "setGeneratorOptions", at = @At(value = "RETURN"))
    public void onSetGeneratorOptions(String generatorOptions, CallbackInfoReturnable<WorldSettings> cir) {
        // Minecraft uses a String, we want to return a fancy DataContainer
        // Parse the world generator settings as JSON
        DataContainer settings = null;
        try {
            settings = JsonTranslator.translateFrom(new JsonParser().parse(generatorOptions).getAsJsonObject());
        } catch (JsonParseException | IllegalStateException ignored) {
        }
        // If null, assume custom
        if (settings == null) {
            settings = new MemoryDataContainer().set(DataQueries.WORLD_CUSTOM_SETTINGS, generatorOptions);
        }
        this.generatorSettings = settings;
    }

    @Override
    public GameMode getGameMode() {
        return (GameMode) (Object) this.getGameType();
    }

    @Override
    public GeneratorType getGeneratorType() {
        return (GeneratorType) this.getTerrainType();
    }

    @Override
    public boolean usesMapFeatures() {
        return this.isMapFeaturesEnabled();
    }

    @Override
    public boolean isHardcore() {
        return this.getHardcoreEnabled();
    }

    @Override
    public boolean areCommandsAllowed() {
        return this.settings$areCommandsAllowed();
    }

    @Override
    public boolean doesGenerateBonusChest() {
        return this.generateBonusChest;
    }

    @Override
    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    @Override
    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    @Override
    public DataContainer getGeneratorSettings() {
        return this.generatorSettings;
    }

    @Override
    public SerializationBehavior getSerializationBehavior() {
        return this.serializationBehavior;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public boolean loadOnStartup() {
        return this.loadOnStartup;
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    @Override
    public boolean doesGenerateSpawnOnLoad() {
        return this.generateSpawnOnLoad;
    }

    @Override
    public boolean isPVPEnabled() {
        return this.pvpEnabled;
    }

    @Override
    public Collection<WorldGeneratorModifier> getGeneratorModifiers() {
        return this.generatorModifiers;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setId(String id) {
        checkNotNull(id);
        if (this.id != null) {
            throw new IllegalStateException("Attempt made to set id twice!");
        }

        this.id = id;
    }

    @Override
    public void setName(String name) {
        checkNotNull(name);
        if (this.name != null) {
            throw new IllegalStateException("Attempt made to set name twice!");
        }

        this.name = name;
    }

    @Override
    public boolean isFromBuilder() {
        return this.fromBuilder;
    }

    @Override
    public void setDimensionType(DimensionType dimensionType) {
        this.dimensionType = dimensionType;
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public void setSerializationBehavior(SerializationBehavior behavior) {
        this.serializationBehavior = behavior;
    }

    @Override
    public void setGeneratorSettings(DataContainer generatorSettings) {
        this.generatorSettings = generatorSettings;
    }

    @Override
    public void setGeneratorModifiers(ImmutableList<WorldGeneratorModifier> generatorModifiers) {
        this.generatorModifiers = generatorModifiers;
    }

    @Override
    public void setEnabled(boolean state) {
        this.isEnabled = state;
    }

    @Override
    public void setLoadOnStartup(boolean state) {
        this.loadOnStartup = state;
    }

    @Override
    public void setKeepSpawnLoaded(boolean state) {
        this.keepSpawnLoaded = state;
    }

    @Override
    public void setGenerateSpawnOnLoad(boolean state) {
        this.generateSpawnOnLoad = state;
    }

    @Override
    public void setPVPEnabled(boolean state) {
        this.pvpEnabled = state;
    }

    @Override
    public void setCommandsAllowed(boolean state) {
        this.commandsAllowed = state;
    }

    @Override
    public void setGenerateBonusChest(boolean state) {
        this.generateBonusChest = state;
    }

    @Override
    public void fromBuilder(boolean state) {
        this.fromBuilder = state;
    }
}
