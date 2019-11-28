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

import org.spongepowered.api.service.context.Context;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.DimensionTypeBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.DimensionConfig;
import org.spongepowered.common.registry.type.world.DimensionTypeRegistryModule;
import org.spongepowered.common.world.WorldManager;

import java.nio.file.Path;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin implements DimensionTypeBridge {

    private String impl$sanitizedId;
    private Path impl$configPath;
    private SpongeConfig<DimensionConfig> impl$config;
    private volatile Context impl$context;
    private boolean impl$generateSpawnOnLoad;
    private boolean impl$loadSpawn;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpBridgeFields(
        final String enumName, final int ordinal, final int idIn, final String nameIn, final String suffixIn, final Class <? extends Dimension > clazzIn,
            final CallbackInfo ci) {
        final String dimName = enumName.toLowerCase().replace(" ", "_").replaceAll("[^A-Za-z0-9_]", "");
        final String modId = SpongeImplHooks.getModIdFromClass(clazzIn);
        this.impl$configPath = SpongeImpl.getSpongeConfigDir().resolve("worlds").resolve(modId).resolve(dimName);
        this.impl$config = new SpongeConfig<>(SpongeConfig.Type.DIMENSION, this.impl$configPath.resolve("dimension.conf"), SpongeImpl.ECOSYSTEM_ID,
            SpongeImpl.getGlobalConfigAdapter(), false);
        this.impl$generateSpawnOnLoad = idIn == 0;
        this.impl$loadSpawn = this.impl$generateSpawnOnLoad;
        this.impl$config.getConfig().getWorld().setGenerateSpawnOnLoad(this.impl$generateSpawnOnLoad);
        this.impl$sanitizedId = modId + ":" + dimName;
        final String contextId = this.impl$sanitizedId.replace(":", ".");
        this.impl$context = new Context(Context.DIMENSION_KEY, contextId);
        if (!WorldManager.isDimensionRegistered(idIn)) {
            DimensionTypeRegistryModule.getInstance().registerAdditionalCatalog((org.spongepowered.api.world.DimensionType) this);
        }
    }

    @Override
    public boolean bridge$shouldGenerateSpawnOnLoad() {
        return this.impl$generateSpawnOnLoad;
    }

    @Override
    public boolean bridge$shouldLoadSpawn() {
        return this.impl$loadSpawn;
    }

    @Override
    public void setShouldLoadSpawn(final boolean keepSpawnLoaded) {
        this.impl$loadSpawn = keepSpawnLoaded;
    }

    @Override
    public String bridge$getSanitizedId() {
        return this.impl$sanitizedId;
    }

    @Override
    public Path bridge$getConfigPath() {
        return this.impl$configPath;
    }

    @Override
    public SpongeConfig<DimensionConfig> bridge$getDimensionConfig() {
        return this.impl$config;
    }

    @Override
    public Context bridge$getContext() {
        return this.impl$context;
    }

    /**
     * @author Zidane - March 30th, 2016
     * @reason This method generally checks dimension type ids (-1 | 0 | 1) in Vanilla. I change this assumption to dimension
     * instance ids. Since the WorldManager tracks dimension instance ids by dimension type ids and Vanilla keeps
     * their ids 1:1, this is a safe change that ensures a mixup can't happen.
     */
    @Overwrite
    public static DimensionType getById(final int dimensionTypeId) {
        return WorldManager.getDimensionTypeByTypeId(dimensionTypeId).orElseThrow(() -> new IllegalArgumentException("Invalid dimension id " + dimensionTypeId));
    }
}
