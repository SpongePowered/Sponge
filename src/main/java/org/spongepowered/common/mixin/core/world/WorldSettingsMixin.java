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
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.registry.type.world.WorldGeneratorModifierRegistryModule;
import org.spongepowered.common.util.Constants;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(WorldSettings.class)
public abstract class WorldSettingsMixin implements WorldSettingsBridge {

    @Shadow private boolean commandsAllowed;
    @Shadow private String generatorOptions;

    @Shadow public abstract GameType getGameType();

    @Nullable private String id;
    @Nullable private String name;
    private DimensionType dimensionType = DimensionTypes.OVERWORLD;
    private Difficulty difficulty = Difficulties.NORMAL;
    private SerializationBehavior serializationBehavior = SerializationBehaviors.AUTOMATIC;
    private DataContainer generatorSettings = DataContainer.createNew();
    private boolean isEnabled = true;
    private boolean loadOnStartup = true;
    @Nullable private Boolean keepSpawnLoaded = null;
    private boolean generateSpawnOnLoad = false;
    private boolean pvpEnabled = true;
    private boolean generateBonusChest = false;
    @Nullable private PortalAgentType portalAgentType;
    private Collection<WorldGeneratorModifier> generatorModifiers = ImmutableList.of();
    private boolean seedRandomized = false;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "<init>(Lnet/minecraft/world/storage/WorldInfo;)V", at = @At(value = "RETURN"))
    private void impl$reAssignValuesFromIncomingInfo(WorldInfo info, CallbackInfo ci) {
        final WorldProperties properties = (WorldProperties) info;
        if (((WorldInfoBridge) info).bridge$getConfigAdapter() != null) {
            this.dimensionType = properties.getDimensionType();
            this.difficulty = properties.getDifficulty();
            this.serializationBehavior = properties.getSerializationBehavior();
            this.generatorSettings = properties.getGeneratorSettings().copy();
            this.isEnabled = properties.isEnabled();
            this.loadOnStartup = properties.loadOnStartup();
            this.keepSpawnLoaded = properties.doesKeepSpawnLoaded();
            this.generateSpawnOnLoad = properties.doesGenerateSpawnOnLoad();
            this.pvpEnabled = properties.isPVPEnabled();
            this.generateBonusChest = properties.doesGenerateBonusChest();
            WorldGeneratorModifierRegistryModule.getInstance().checkAllRegistered(properties.getGeneratorModifiers());
            this.generatorModifiers = ImmutableList.copyOf(properties.getGeneratorModifiers());
        }
    }

    @Override
    public String bridge$getId() {
        return this.id;
    }

    @Override
    public String bridge$getName() {
        return this.name;
    }

    @Override
    public boolean bridge$isSeedRandomized() {
        return this.seedRandomized;
    }

    @Override
    public void bridge$setRandomSeed(boolean state) {
        this.seedRandomized = state;
    }

    @Inject(method = "setGeneratorOptions", at = @At(value = "RETURN"))
    private void onSetGeneratorOptions(String generatorOptions, CallbackInfoReturnable<WorldSettings> cir) {
        // Minecraft uses a String, we want to return a fancy DataContainer
        // Parse the world generator settings as JSON
        DataContainer settings = null;
        try {
            settings = DataFormats.JSON.read(generatorOptions);
        } catch (JsonParseException | IOException ignored) {
        }
        // If null, assume custom
        if (settings == null) {
            settings = DataContainer.createNew().set(Constants.Sponge.World.WORLD_CUSTOM_SETTINGS, generatorOptions);
        }
        this.generatorSettings = settings;
    }


    @Override
    public boolean bridge$getGeneratesBonusChest() {
        return this.generateBonusChest;
    }

    @Override
    public DimensionType bridge$getDimensionType() {
        return this.dimensionType;
    }

    @Override
    public Difficulty bridge$getDifficulty() {
        return this.difficulty;
    }

    @Override
    public PortalAgentType bridge$getPortalAgentType() {
        if (this.portalAgentType == null) {
            this.portalAgentType = PortalAgentTypes.DEFAULT;
        }
        return this.portalAgentType;
    }

    @Override
    public void bridge$setPortalAgentType(PortalAgentType type) {
        this.portalAgentType = type;
    }

    @Override
    public DataContainer bridge$getGeneratorSettings() {
        return this.generatorSettings;
    }

    @Override
    public SerializationBehavior bridge$getSerializationBehavior() {
        return this.serializationBehavior;
    }

    @Override
    public boolean bridge$isEnabled() {
        return this.isEnabled;
    }

    @Override
    public boolean bridge$loadOnStartup() {
        return this.loadOnStartup;
    }

    @Override
    public boolean bridge$doesKeepSpawnLoaded() {
        if (this.keepSpawnLoaded == null) {
            this.keepSpawnLoaded = this.dimensionType == DimensionTypes.OVERWORLD;
        }
        return this.keepSpawnLoaded;
    }

    @Override
    public boolean bridge$generateSpawnOnLoad() {
        return this.generateSpawnOnLoad;
    }

    @Override
    public boolean bridge$isPVPEnabled() {
        return this.pvpEnabled;
    }

    @Override
    public Collection<WorldGeneratorModifier> bridge$getGeneratorModifiers() {
        return this.generatorModifiers;
    }

    @Override
    public void bridge$setId(String id) {
        checkNotNull(id);
        if (this.id != null) {
            throw new IllegalStateException("Attempt made to set id twice!");
        }

        this.id = id;
    }

    @Override
    public void bridge$setName(String name) {
        checkNotNull(name);
        if (this.name != null) {
            throw new IllegalStateException("Attempt made to set name twice!");
        }

        this.name = name;
    }

    @Override
    public void bridge$setDimensionType(DimensionType dimensionType) {
        this.dimensionType = dimensionType;
    }

    @Override
    public void bridge$setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public void bridge$setSerializationBehavior(SerializationBehavior behavior) {
        this.serializationBehavior = behavior;
    }

    @Override
    public void bridge$setGeneratorSettings(DataContainer generatorSettings) {
        // Update the generatorOptions string
        Optional<String> optCustomSettings = generatorSettings.getString(Constants.Sponge.World.WORLD_CUSTOM_SETTINGS);
        if (optCustomSettings.isPresent()) {
            this.generatorOptions = optCustomSettings.get();
        } else {
            try {
                this.generatorOptions = DataFormats.JSON.write(generatorSettings);
            } catch (IOException e) {
                // ignore
            }
        }
        this.generatorSettings = generatorSettings;
    }

    @Override
    public void bridge$setGeneratorModifiers(ImmutableList<WorldGeneratorModifier> generatorModifiers) {
        this.generatorModifiers = generatorModifiers;
    }

    @Override
    public void bridge$setEnabled(boolean state) {
        this.isEnabled = state;
    }

    @Override
    public void bridge$setLoadOnStartup(boolean state) {
        this.loadOnStartup = state;
    }

    @Override
    public void bridge$setKeepSpawnLoaded(@Nullable Boolean state) {
        this.keepSpawnLoaded = state;
    }

    @Override
    public void bridge$setGenerateSpawnOnLoad(boolean state) {
        this.generateSpawnOnLoad = state;
    }

    @Override
    public void bridge$setPVPEnabled(boolean state) {
        this.pvpEnabled = state;
    }

    @Override
    public void bridge$setCommandsAllowed(boolean state) {
        this.commandsAllowed = state;
    }

    @Override
    public void bridge$setGenerateBonusChest(boolean state) {
        this.generateBonusChest = state;
    }

    @Override
    public Boolean bridge$internalKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }
}
