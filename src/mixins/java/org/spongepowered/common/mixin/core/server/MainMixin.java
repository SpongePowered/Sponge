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
package org.spongepowered.common.mixin.core.server;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.Main;
import net.minecraft.server.ServerPropertiesProvider;
import net.minecraft.server.dedicated.ServerProperties;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.FolderName;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.server.BootstrapProperties;

import java.nio.file.Path;

@Mixin(Main.class)
public abstract class MainMixin {

    @Redirect(method = "main", at = @At(value = "NEW", target = "net/minecraft/server/ServerPropertiesProvider"))
    private static ServerPropertiesProvider impl$cacheBootstrapProperties(final DynamicRegistries p_i242100_1_, final Path p_i242100_2_) {
        final ServerPropertiesProvider provider = new ServerPropertiesProvider(p_i242100_1_, p_i242100_2_);
        final ServerProperties properties = provider.getProperties();
        BootstrapProperties.init(properties.worldGenSettings, properties.gamemode, properties.difficulty, properties.pvp, properties.hardcore,
                properties.viewDistance, p_i242100_1_);
        return provider;
    }

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/WorldSettingsImport;create(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/resources/IResourceManager;Lnet/minecraft/util/registry/DynamicRegistries$Impl;)Lnet/minecraft/util/registry/WorldSettingsImport;"))
    private static <T> WorldSettingsImport<T> impl$cacheWorldSettingsAdapter(DynamicOps<T> p_244335_0_, IResourceManager p_244335_1_,
            DynamicRegistries.Impl p_244335_2_) {
        final WorldSettingsImport<T> worldSettingsAdapter = WorldSettingsImport.create(p_244335_0_, p_244335_1_, p_244335_2_);
        BootstrapProperties.worldSettingsAdapter(worldSettingsAdapter);
        return worldSettingsAdapter;
    }

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/SaveFormat$LevelSave;getLevelPath(Lnet/minecraft/world/storage/FolderName;)Ljava/nio/file/Path;"))
    private static Path impl$configurePackRepository(final SaveFormat.LevelSave levelSave, final FolderName folderName) {
        final Path datapackDir = levelSave.getLevelPath(folderName);
        final SpongeLifecycle lifecycle = SpongeBootstrap.getLifecycle();
        lifecycle.establishGlobalRegistries();
        lifecycle.establishDataProviders();
        lifecycle.callRegisterDataEvent();
        lifecycle.callRegisterDataPackValueEvent(datapackDir);
        return datapackDir;
    }

    @Redirect(method = "main", at = @At(value = "NEW", target = "net/minecraft/world/storage/ServerWorldInfo"))
    private static ServerWorldInfo impl$setIsNewLevel(final WorldSettings settings, final DimensionGeneratorSettings generationSettings, final
            Lifecycle lifecycle) {
        BootstrapProperties.setIsNewLevel(true);
        return new ServerWorldInfo(settings, generationSettings, lifecycle);
    }
}
