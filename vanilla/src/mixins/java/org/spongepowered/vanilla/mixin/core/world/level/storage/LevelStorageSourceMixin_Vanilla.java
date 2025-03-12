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
package org.spongepowered.vanilla.mixin.core.world.level.storage;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;

@Mixin(LevelStorageSource.class)
public abstract class LevelStorageSourceMixin_Vanilla {

    private static Dynamic<Tag> impl$spongeLevelData;

    @WrapOperation(
            method = "readLevelDataTagFixed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;",
                    ordinal = 0
            )
    )
    private static CompoundTag impl$createSpongeLevelData(final CompoundTag compoundNBT, final String path, final Operation<CompoundTag> original) {
        LevelStorageSourceMixin_Vanilla.impl$spongeLevelData = new Dynamic<>(NbtOps.INSTANCE, compoundNBT);
        return original.call(compoundNBT, path);
    }

    @WrapOperation(
            method = "getLevelDataAndDimensions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/PrimaryLevelData;parse(Lcom/mojang/serialization/Dynamic;Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/storage/PrimaryLevelData$SpecialWorldProperty;Lnet/minecraft/world/level/levelgen/WorldOptions;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/world/level/storage/PrimaryLevelData;"
            )
    )
    private static PrimaryLevelData impl$readSpongeLevelData(final Dynamic<?> dynamic, final LevelSettings settings,
            final PrimaryLevelData.SpecialWorldProperty special, final WorldOptions options, final Lifecycle lifecycle, final Operation<PrimaryLevelData> original)
    {
        final PrimaryLevelData levelData = original.call(dynamic, settings, special, options, lifecycle);

        ((PrimaryLevelDataBridge) levelData).bridge$readSpongeLevelData(LevelStorageSourceMixin_Vanilla.impl$spongeLevelData);

        LevelStorageSourceMixin_Vanilla.impl$spongeLevelData = null;
        return levelData;
    }
}
