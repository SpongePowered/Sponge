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
package org.spongepowered.common.mixin.core.world.dimension;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.DimensionConfig;

import java.nio.file.Path;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.common.registry.type.world.DimensionTypeRegistryModule;
import org.spongepowered.common.registry.type.world.WorldRegistrationRegistryModule;
import org.spongepowered.common.world.SpongeDimensionType;
import org.spongepowered.common.world.server.SpongeWorldRegistration;

import javax.annotation.Nullable;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin implements DimensionTypeBridge {

    @Nullable private CatalogKey impl$key;
    @Nullable private SpongeDimensionType impl$spongeDimensionType;
    @Nullable private SpongeWorldRegistration impl$spongeWorldRegistration;
    @Nullable private Path impl$configPath;
    @Nullable private SpongeConfig<DimensionConfig> impl$config;
    @Nullable private volatile Context impl$context;

    @Inject(method = "register", at = @At("RETURN"))
    private static void impl$setupBridgeFields(String id, DimensionType dimensionType, CallbackInfoReturnable<DimensionType> cir) {
        final DimensionTypeBridge dimensionTypeBridge = (DimensionTypeBridge) dimensionType;

        final String dimName = id.toLowerCase().replace(" ", "_").replaceAll("[^A-Za-z0-9_]", "");
        // TODO This may not work out, we'll see.
        final String modId = SpongeImplHooks.getActiveModContainer().getId();

        final Path configPath = SpongeImpl.getSpongeConfigDir().resolve("worlds").resolve(modId).resolve(dimName);
        dimensionTypeBridge.bridge$getConfigPath(configPath);
        dimensionTypeBridge.bridge$setDimensionConfig(new SpongeConfig<>(SpongeConfig.Type.DIMENSION, configPath.resolve("dimension.conf"),
            SpongeImpl.ECOSYSTEM_ID, SpongeImpl.getGlobalConfigAdapter(), false));

        // Make sure the overworld generates the spawn
        if (dimensionType.getId() == 0) {
            dimensionTypeBridge.bridge$getDimensionConfig().getConfig().getWorld().setGenerateSpawnOnLoad(true);
        }

        final CatalogKey key = CatalogKey.of(modId, dimName);
        dimensionTypeBridge.bridge$setKey(key);

        final SpongeDimensionType spongeDimensionType = new SpongeDimensionType(dimensionType);
        dimensionTypeBridge.setSpongeDimensionType(spongeDimensionType);
        DimensionTypeRegistryModule.getInstance().registerAdditionalCatalog(spongeDimensionType);

        final SpongeWorldRegistration spongeWorldRegistration = new SpongeWorldRegistration(dimensionType);
        dimensionTypeBridge.bridge$setWorldRegistration(spongeWorldRegistration);
        WorldRegistrationRegistryModule.getInstance().registerAdditionalCatalog(spongeWorldRegistration);

        dimensionTypeBridge.bridge$setContext(new Context(Context.DIMENSION_KEY, modId + "." + dimName));
    }

    @Override
    public CatalogKey bridge$getKey() {
        return this.impl$key;
    }

    @Override
    public void bridge$setKey(CatalogKey key) {
        this.impl$key = key;
    }

    @Override
    public SpongeDimensionType bridge$getSpongeDimensionType() {
        return this.impl$spongeDimensionType;
    }

    @Override
    public void setSpongeDimensionType(SpongeDimensionType dimensionType) {
        this.impl$spongeDimensionType = dimensionType;
    }

    @Override
    public SpongeWorldRegistration bridge$getWorldRegistration() {
        return this.impl$spongeWorldRegistration;
    }

    @Override
    public void bridge$setWorldRegistration(SpongeWorldRegistration worldRegistration) {
        this.impl$spongeWorldRegistration = worldRegistration;
    }

    @Override
    public Path bridge$getConfigPath() {
        return this.impl$configPath;
    }

    @Override
    public void bridge$getConfigPath(Path path) {
        this.impl$configPath = path;
    }

    @Override
    public SpongeConfig<DimensionConfig> bridge$getDimensionConfig() {
        return this.impl$config;
    }

    @Override
    public void bridge$setDimensionConfig(SpongeConfig<DimensionConfig> config) {
        this.impl$config = config;
    }

    @Override
    public Context bridge$getContext() {
        return this.impl$context;
    }

    @Override
    public void bridge$setContext(Context context) {
        this.impl$context = context;
    }
}
