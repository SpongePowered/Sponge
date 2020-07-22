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
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.teleport.PortalAgentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.world.dimension.SpongeDimensionType;

import java.util.function.Supplier;

import javax.annotation.Nullable;

@Mixin(WorldSettings.class)
public abstract class WorldSettingsMixin implements ResourceKeyBridge, WorldSettingsBridge {

    @Shadow private boolean commandsAllowed;
    @Shadow private boolean bonusChestEnabled;

    @Nullable private ResourceKey key;
    @Nullable private SpongeDimensionType impl$logicType;
    @Nullable private Difficulty impl$difficulty;
    @Nullable private SerializationBehavior impl$serializationBehavior;
    private boolean impl$isEnabled = true;
    private boolean impl$loadOnStartup = true;
    @Nullable private Boolean impl$keepSpawnLoaded = null;
    private boolean impl$generateSpawnOnLoad = false;
    private boolean impl$pvpEnabled = true;
    @Nullable private PortalAgentType portalAgentType;
    private boolean seedRandomized = false;
    @Nullable private DataContainer impl$generatorSettings;

    @Inject(method = "<init>(Lnet/minecraft/world/storage/WorldInfo;)V", at = @At(value = "RETURN"))
    private void impl$reAssignValuesFromIncomingInfo(WorldInfo info, CallbackInfo ci) {
        final WorldProperties properties = (WorldProperties) info;
        if (!((WorldInfoBridge) info).bridge$isValid()) {
            return;
        }

        this.impl$logicType = (SpongeDimensionType) properties.getDimensionType();
        this.impl$difficulty = properties.getDifficulty();
        this.impl$serializationBehavior = properties.getSerializationBehavior();
        this.impl$isEnabled = properties.isEnabled();
        this.impl$loadOnStartup = properties.doesLoadOnStartup();
        this.impl$keepSpawnLoaded = properties.doesKeepSpawnLoaded();
        this.impl$generateSpawnOnLoad = properties.doesGenerateSpawnOnLoad();
        this.impl$pvpEnabled = properties.isPVPEnabled();
        this.bonusChestEnabled = properties.doesGenerateBonusChest();
        this.impl$generatorSettings = properties.getGeneratorSettings();
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
    private void onSetGeneratorOptions(JsonElement element, CallbackInfoReturnable<WorldSettings> cir) {
        // TODO 1.14 - JsonElement -> DataContainer
    }

    @Override
    public ResourceKey bridge$getKey() {
        return this.key;
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
    public PortalAgentType bridge$getPortalAgentType() {
        return this.portalAgentType;
    }

    @Override
    public void bridge$setPortalAgentType(PortalAgentType type) {
        this.portalAgentType = type;
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
    public void bridge$setKey(ResourceKey key) {
        this.key = key;
    }

    @Override
    public void bridge$setLogicType(SpongeDimensionType dimensionType) {
        this.impl$logicType = dimensionType;
    }

    @Override
    public void bridge$setDifficulty(Difficulty difficulty) {
        this.impl$difficulty = difficulty;
    }

    @Override
    public void bridge$setSerializationBehavior(SerializationBehavior behavior) {
        this.impl$serializationBehavior = behavior;
    }

    @Override
    public void bridge$setGeneratorSettings(DataContainer generatorSettings) {
        // TODO DataContainer -> JsonElement
    }

    @Override
    public void bridge$setEnabled(boolean state) {
        this.impl$isEnabled = state;
    }

    @Override
    public void bridge$setLoadOnStartup(boolean state) {
        this.impl$loadOnStartup = state;
    }

    @Override
    public void bridge$setKeepSpawnLoaded(@Nullable Boolean state) {
        this.impl$keepSpawnLoaded = state;
    }

    @Override
    public void bridge$setGenerateSpawnOnLoad(boolean state) {
        this.impl$generateSpawnOnLoad = state;
    }

    @Override
    public void bridge$setPVPEnabled(boolean state) {
        this.impl$pvpEnabled = state;
    }

    @Override
    public void bridge$setCommandsEnabled(boolean state) {
        this.commandsAllowed = state;
    }

    @Override
    public void bridge$setGenerateBonusChest(boolean state) {
        this.bonusChestEnabled = state;
    }

    @Override
    public Boolean bridge$internalKeepSpawnLoaded() {
        return this.impl$keepSpawnLoaded;
    }

    @Override
    public void bridge$populateInfo(WorldInfo info) {
        final WorldInfoBridge infoBridge = (WorldInfoBridge) info;

        // TODO 1.14 - Add all the property setters
        infoBridge.bridge$setEnabled(this.impl$isEnabled);
        infoBridge.bridge$setLogicType(this.impl$logicType);
        infoBridge.bridge$setLoadOnStartup(this.impl$loadOnStartup);
        infoBridge.bridge$setGenerateSpawnOnLoad(this.impl$generateSpawnOnLoad);
        infoBridge.bridge$setKeepSpawnLoaded(this.impl$keepSpawnLoaded);
        infoBridge.bridge$setGenerateBonusChest(this.bonusChestEnabled);
        infoBridge.bridge$setSerializationBehavior(this.impl$serializationBehavior);
        infoBridge.bridge$forceSetDifficulty((net.minecraft.world.Difficulty) (Object) this.impl$difficulty);
    }
}
