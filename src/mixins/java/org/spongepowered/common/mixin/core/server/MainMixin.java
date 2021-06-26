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
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.server.Main;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.datapack.SpongeDataPackManager;
import org.spongepowered.common.server.BootstrapProperties;

import java.nio.file.Path;

@Mixin(Main.class)
public abstract class MainMixin {

    @Redirect(method = "main", at = @At(value = "NEW", target = "net/minecraft/server/dedicated/DedicatedServerSettings"))
    private static DedicatedServerSettings impl$cacheBootstrapProperties(final RegistryAccess p_i242100_1_, final Path p_i242100_2_) {
        final DedicatedServerSettings provider = new DedicatedServerSettings(p_i242100_1_, p_i242100_2_);
        final DedicatedServerProperties properties = provider.getProperties();
        BootstrapProperties.init(properties.worldGenSettings, properties.gamemode, properties.difficulty, properties.pvp, properties.hardcore,
                true, properties.viewDistance, p_i242100_1_);
        return provider;
    }

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryReadOps;create(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/RegistryAccess$RegistryHolder;)Lnet/minecraft/resources/RegistryReadOps;"))
    private static <T> RegistryReadOps<T> impl$cacheWorldSettingsAdapter(DynamicOps<T> p_244335_0_, ResourceManager p_244335_1_,
            RegistryAccess.RegistryHolder p_244335_2_) {
        final RegistryReadOps<T> worldSettingsAdapter = RegistryReadOps.create(p_244335_0_, p_244335_1_, p_244335_2_);
        BootstrapProperties.worldSettingsAdapter(worldSettingsAdapter);
        SpongeDataPackManager.INSTANCE.serializeDelayedDataPack(DataPackTypes.WORLD);
        return worldSettingsAdapter;
    }

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getLevelPath(Lnet/minecraft/world/level/storage/LevelResource;)Ljava/nio/file/Path;"))
    private static Path impl$configurePackRepository(final LevelStorageSource.LevelStorageAccess levelSave, final LevelResource folderName) {
        final Path datapackDir = levelSave.getLevelPath(folderName);
        final SpongeLifecycle lifecycle = SpongeBootstrap.lifecycle();
        lifecycle.establishGlobalRegistries();
        lifecycle.establishDataProviders();
        lifecycle.callRegisterDataEvent();
        lifecycle.callRegisterDataPackValueEvent(datapackDir);
        return datapackDir;
    }

    @Redirect(method = "main", at = @At(value = "NEW", target = "net/minecraft/world/level/storage/PrimaryLevelData"))
    private static PrimaryLevelData impl$setIsNewLevel(final LevelSettings settings, final WorldGenSettings generationSettings, final
            Lifecycle lifecycle) {
        BootstrapProperties.setIsNewLevel(true);
        return new PrimaryLevelData(settings, generationSettings, lifecycle);
    }
}
