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

import com.google.gson.JsonElement;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.data.persistence.NBTTranslator;

import javax.annotation.Nullable;

@Mixin(WorldSettings.class)
public abstract class WorldSettingsMixin implements ResourceKeyBridge, WorldSettingsBridge {

    // @formatter:off
    @Shadow private boolean commandsAllowed;
    @Shadow private boolean bonusChestEnabled;
    @Shadow private JsonElement generatorOptions;
    // @formatter:on

    private ResourceKey impl$key;
    // Sponge Start - Vanilla registry catalogs cannot be default set here, causes chain classloading
    private SpongeDimensionType impl$logicType;
    private Difficulty impl$difficulty;
    // Sponge End
    private SerializationBehavior impl$serializationBehavior = SerializationBehavior.AUTOMATIC;
    private DataContainer impl$generatorSettings = DataContainer.createNew();
    private boolean impl$isEnabled = true;
    private boolean impl$loadOnStartup = true;
    private boolean impl$keepSpawnLoaded = false;
    private boolean impl$generateSpawnOnLoad = false;
    private boolean impl$pvpEnabled = true;
    private boolean impl$seedRandomized = false;
    private InheritableConfigHandle<WorldConfig> impl$configAdapter;
    private boolean impl$configExists = false;

    @Override
    public ResourceKey bridge$getKey() {
        return this.impl$key;
    }

    @Override
    public void bridge$setKey(final ResourceKey key) {
        this.impl$key = key;
    }

    @Override
    public boolean bridge$isSeedRandomized() {
        return this.impl$seedRandomized;
    }

    @Override
    public void bridge$setRandomSeed(final boolean state) {
        this.impl$seedRandomized = state;
    }

    @Override
    public SpongeDimensionType bridge$getLogicType() {
        return this.impl$logicType;
    }

    @Override
    public Difficulty bridge$getDifficulty() {
        return this.impl$difficulty;
    }

    @Override
    public DataContainer bridge$getGeneratorSettings() {
        return this.impl$generatorSettings;
    }

    @Override
    public SerializationBehavior bridge$getSerializationBehavior() {
        return this.impl$serializationBehavior;
    }

    @Override
    public boolean bridge$isEnabled() {
        return this.impl$isEnabled;
    }

    @Override
    public boolean bridge$loadOnStartup() {
        return this.impl$loadOnStartup;
    }

    @Override
    public boolean bridge$doesKeepSpawnLoaded() {
        return this.impl$keepSpawnLoaded;
    }

    @Override
    public boolean bridge$generateSpawnOnLoad() {
        return this.impl$generateSpawnOnLoad;
    }

    @Override
    public boolean bridge$isPVPEnabled() {
        return this.impl$pvpEnabled;
    }

    @Override
    public void bridge$setLogicType(final SpongeDimensionType dimensionType) {
        this.impl$logicType = dimensionType;
    }

    @Override
    public void bridge$setDifficulty(final Difficulty difficulty) {
        this.impl$difficulty = difficulty;
    }

    @Override
    public void bridge$setSerializationBehavior(final SerializationBehavior behavior) {
        this.impl$serializationBehavior = behavior;
    }

    @Override
    public void bridge$setGeneratorSettings(final DataContainer generatorSettings) {
        final CompoundNBT nbt = NBTTranslator.getInstance().translate(generatorSettings);
        this.generatorOptions = Dynamic.convert(NBTDynamicOps.INSTANCE, JsonOps.INSTANCE, nbt);
    }

    @Override
    public void bridge$setEnabled(final boolean state) {
        this.impl$isEnabled = state;
    }

    @Override
    public void bridge$setLoadOnStartup(final boolean state) {
        this.impl$loadOnStartup = state;
    }

    @Override
    public void bridge$setKeepSpawnLoaded(@Nullable final Boolean state) {
        this.impl$keepSpawnLoaded = state;
    }

    @Override
    public void bridge$setGenerateSpawnOnLoad(final boolean state) {
        this.impl$generateSpawnOnLoad = state;
    }

    @Override
    public void bridge$setPVPEnabled(final boolean state) {
        this.impl$pvpEnabled = state;
    }

    @Override
    public void bridge$setCommandsEnabled(final boolean state) {
        this.commandsAllowed = state;
    }

    @Override
    public void bridge$setGenerateBonusChest(final boolean state) {
        this.bonusChestEnabled = state;
    }

    @Override
    public void bridge$setInfoConfigAdapter(final InheritableConfigHandle<WorldConfig> configAdapter) {
        this.impl$configAdapter = configAdapter;
    }

    @Override
    public void bridge$setConfigExists(boolean configExists) {
        this.impl$configExists = configExists;
    }

    @Override
    public void bridge$populateInfo(final WorldInfo info) {
        final WorldInfoBridge infoBridge = (WorldInfoBridge) info;

        if (infoBridge.bridge$isSinglePlayerProperties()) {
            return;
        }

        if (this.impl$configAdapter != null) {
            infoBridge.bridge$setConfigAdapter(this.impl$configAdapter);
            this.impl$configAdapter = null;
        }

        if (this.impl$configExists) {
            this.impl$configExists = false;
            return;
        }

        infoBridge.bridge$setEnabled(this.impl$isEnabled);
        infoBridge.bridge$setLogicType(this.impl$logicType, false);
        infoBridge.bridge$setLoadOnStartup(this.impl$loadOnStartup);
        infoBridge.bridge$setGenerateSpawnOnLoad(this.impl$generateSpawnOnLoad);
        infoBridge.bridge$setKeepSpawnLoaded(this.impl$keepSpawnLoaded);
        infoBridge.bridge$setGenerateBonusChest(this.bonusChestEnabled);
        infoBridge.bridge$setSerializationBehavior(this.impl$serializationBehavior);
        infoBridge.bridge$forceSetDifficulty((net.minecraft.world.Difficulty) (Object) this.impl$difficulty);
        infoBridge.bridge$setPVPEnabled(this.impl$pvpEnabled);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/storage/WorldInfo;)V", at = @At(value = "RETURN"))
    private void impl$populateSettings(WorldInfo info, CallbackInfo ci) {
        if (!((WorldInfoBridge) info).bridge$isValid()) {
            return;
        }

        final WorldProperties properties = (WorldProperties) info;

        this.impl$isEnabled = properties.isEnabled();
        this.impl$logicType = (SpongeDimensionType) properties.getDimensionType();
        this.impl$loadOnStartup = properties.doesLoadOnStartup();
        this.impl$generateSpawnOnLoad = properties.doesGenerateSpawnOnLoad();
        this.impl$keepSpawnLoaded = properties.doesKeepSpawnLoaded();
        // Sponge Start - Bonus chest status is in the Vanilla world data but not read from the properties.
        this.bonusChestEnabled = properties.doesGenerateBonusChest();
        // Sponge End
        this.impl$serializationBehavior = properties.getSerializationBehavior();
        this.impl$difficulty = (Difficulty) (Object) properties.getDifficulty();
        this.impl$pvpEnabled = properties.isPVPEnabled();
        this.impl$generatorSettings = properties.getGeneratorSettings().copy();
    }

    @Inject(method = "setGeneratorOptions", at = @At(value = "RETURN"))
    private void impl$onSetGeneratorOptions(JsonElement element, CallbackInfoReturnable<WorldSettings> cir) {
        // TODO 1.14 - JsonElement -> DataContainer
    }
}
