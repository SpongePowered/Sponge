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

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.gen.WorldGenerationSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.bridge.world.storage.IServerWorldInfoBridge;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldCategory;
import org.spongepowered.common.config.inheritable.WorldConfig;

import javax.annotation.Nullable;

@Mixin(WorldSettings.class)
public abstract class WorldSettingsMixin implements ResourceKeyBridge, WorldSettingsBridge {

    // @formatter:off
    @Mutable@ Final @Shadow private boolean allowCommands;
    // @formatter:on

    private ResourceKey impl$key;
    // Sponge Start - Vanilla registry catalogs cannot be default set here, causes chain classloading
    private DimensionType impl$dimensionType;
    // Sponge End
    private WorldGenerationSettings impl$worldGenerationSettings;
    private SerializationBehavior impl$serializationBehavior = SerializationBehavior.AUTOMATIC;
    private boolean impl$isEnabled = true;
    private boolean impl$loadOnStartup = true;
    private boolean impl$keepSpawnLoaded = false;
    private boolean impl$generateSpawnOnLoad = false;
    private boolean impl$pvpEnabled = true;
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
    public DimensionType bridge$getDimensionType() {
        return this.impl$dimensionType;
    }

    @Override
    public WorldGenerationSettings bridge$getWorldGenerationSettings() {
        return this.impl$worldGenerationSettings;
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
    public void bridge$setDimensionType(final DimensionType dimensionType) {
        this.impl$dimensionType = dimensionType;
    }

    @Override
    public void bridge$setWorldGenerationSettings(final WorldGenerationSettings worldGenerationSettings) {
        this.impl$worldGenerationSettings = worldGenerationSettings;
    }

    @Override
    public void bridge$setSerializationBehavior(final SerializationBehavior behavior) {
        this.impl$serializationBehavior = behavior;
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
        this.allowCommands = state;
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
    public void bridge$populateInfo(final IServerWorldInfoBridge infoBridge) {
        if (this.impl$configAdapter != null) {
            infoBridge.bridge$setConfigAdapter(this.impl$configAdapter);
            this.impl$configAdapter = null;
        }

        infoBridge.bridge$setDimensionType(this.impl$dimensionType, false);

        if (this.impl$configExists) {
            this.impl$configExists = false;
            return;
        }

        final WorldCategory category = infoBridge.bridge$getConfigAdapter().get().world;
        category.enabled = this.impl$isEnabled;
        category.loadOnStartup = this.impl$loadOnStartup;
        category.generateSpawnOnLoad = this.impl$generateSpawnOnLoad;
        category.keepSpawnLoaded = this.impl$keepSpawnLoaded;
        category.serializationBehavior = this.impl$serializationBehavior;
        category.pvp = this.impl$pvpEnabled;
    }
}
