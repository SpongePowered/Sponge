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
import net.minecraft.nbt.Tag;
import net.minecraft.server.WorldStem;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.datapack.SpongeDataPackManager;
import org.spongepowered.common.server.BootstrapProperties;

@Mixin(WorldStem.WorldDataSupplier.class)
public interface WorldStem_WorldDataSupplierMixin {

    @Redirect(method = "lambda$loadFromWorld$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getDataTag(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/DataPackConfig;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/world/level/storage/WorldData;"))
    public static WorldData impl$serializeDelayedDataPackOnLoadAndSetBootstrapProperties(final LevelStorageSource.LevelStorageAccess instance,
            final DynamicOps<Tag> $$0, final DataPackConfig $$1, final Lifecycle $$2) {
        SpongeDataPackManager.INSTANCE.serializeDelayedDataPack(DataPackTypes.WORLD);
        if (true) {
            // TODO registryaccess?
            throw new UnsupportedOperationException("registryAccess");
        }
        final WorldData saveData = instance.getDataTag($$0, $$1, $$2);
        BootstrapProperties.init(saveData.worldGenSettings(), saveData.getGameType(), saveData.getDifficulty(), true, saveData.isHardcore(),
                saveData.getAllowCommands(), 10, null);
        return saveData;
    }
}
