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
package org.spongepowered.common.mixin.core.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelVersion;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.util.Constants;

@Mixin(LevelStorageSource.class)
public abstract class LevelStorageSourceMixin {

    private static Dynamic<Tag> impl$spongeLevelData;

    @Redirect(
            method = "*",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/nbt/NbtIo;readCompressed(Ljava/io/File;)Lnet/minecraft/nbt/CompoundTag;"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/level/storage/LevelStorageSource;readWorldGenSettings(Lcom/mojang/serialization/Dynamic;Lcom/mojang/datafixers/DataFixer;I)Lcom/mojang/datafixers/util/Pair;"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    private static CompoundTag impl$createSpongeLevelData(final CompoundTag compoundNBT, final String path, final DynamicOps<Tag> ops,
            final DataPackConfig p_237270_1s) {
        LevelStorageSourceMixin.impl$spongeLevelData = new Dynamic<>(ops, compoundNBT.getCompound(Constants.Sponge.Data.V2.SPONGE_DATA));
        return compoundNBT.getCompound(path);
    }

    @Redirect(
            method = "*",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/level/LevelSettings;parse(Lcom/mojang/serialization/Dynamic;Lnet/minecraft/world/level/DataPackConfig;)Lnet/minecraft/world/level/LevelSettings;"
                    ),
                    to = @At(
                            value = "RETURN"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/PrimaryLevelData;parse(Lcom/mojang/serialization/Dynamic;Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/storage/LevelVersion;Lnet/minecraft/world/level/levelgen/WorldGenSettings;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/world/level/storage/PrimaryLevelData;"
            )
    )
    private static PrimaryLevelData impl$readSpongeLevelData(final Dynamic<Tag> p_237369_0_, final DataFixer p_237369_1_, final int p_237369_2_,
            final CompoundTag p_237369_3_, final LevelSettings p_237369_4_, final LevelVersion p_237369_5_,
            final WorldGenSettings p_237369_6_, final Lifecycle p_237369_7_)
    {
        final PrimaryLevelData levelData = PrimaryLevelData.parse(p_237369_0_, p_237369_1_, p_237369_2_, p_237369_3_, p_237369_4_, p_237369_5_,
                p_237369_6_, p_237369_7_);

        ((PrimaryLevelDataBridge) levelData).bridge$readSpongeLevelData(LevelStorageSourceMixin.impl$spongeLevelData);

        LevelStorageSourceMixin.impl$spongeLevelData = null;
        return levelData;
    }
}
