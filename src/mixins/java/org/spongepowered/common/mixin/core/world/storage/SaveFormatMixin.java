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
package org.spongepowered.common.mixin.core.world.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.UUIDCodec;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.ServerWorldInfo;
import net.minecraft.world.storage.VersionData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.world.storage.ServerWorldInfoBridge;
import org.spongepowered.common.util.Constants;

import java.util.UUID;

@Mixin(SaveFormat.class)
public abstract class SaveFormatMixin {

    private static Dynamic<INBT> impl$spongeLevelData;

    @Redirect(
            method = "*",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/nbt/CompressedStreamTools;readCompressed(Ljava/io/File;)Lnet/minecraft/nbt/CompoundNBT;"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/storage/SaveFormat;readWorldGenSettings(Lcom/mojang/serialization/Dynamic;Lcom/mojang/"
                                    + "datafixers/DataFixer;I)Lcom/mojang/datafixers/util/Pair;"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundNBT;getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundNBT;"
            )
    )
    private static CompoundNBT impl$createSpongeLevelData(final CompoundNBT compoundNBT, final String path, final DynamicOps<INBT> ops,
            final DatapackCodec p_237270_1s) {
        SaveFormatMixin.impl$spongeLevelData = new Dynamic<>(ops, compoundNBT.getCompound(Constants.Sponge.SPONGE_DATA));
        return compoundNBT.getCompound(path);
    }

    @Redirect(
            method = "*",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/WorldSettings;parse(Lcom/mojang/serialization/Dynamic;Lnet/minecraft/util/datafix/codec/DatapackCodec;)Lnet/minecraft/world/WorldSettings;"
                    ),
                    to = @At(
                            value = "RETURN"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/storage/ServerWorldInfo;parse(Lcom/mojang/serialization/Dynamic;Lcom/mojang/datafixers/DataFixer;"
                            + "ILnet/minecraft/nbt/CompoundNBT;Lnet/minecraft/world/WorldSettings;Lnet/minecraft/world/storage/VersionData;Lnet/"
                            + "minecraft/world/gen/settings/DimensionGeneratorSettings;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/world/"
                            + "storage/ServerWorldInfo;"
            )
    )
    private static ServerWorldInfo impl$readSpongeLevelData(final Dynamic<INBT> p_237369_0_, final DataFixer p_237369_1_, final int p_237369_2_,
            final CompoundNBT p_237369_3_, final WorldSettings p_237369_4_, final VersionData p_237369_5_,
            final DimensionGeneratorSettings p_237369_6_, final Lifecycle p_237369_7_)
    {
        final ServerWorldInfo levelData = ServerWorldInfo.parse(p_237369_0_, p_237369_1_, p_237369_2_, p_237369_3_, p_237369_4_, p_237369_5_,
                p_237369_6_, p_237369_7_);

        if (SaveFormatMixin.impl$spongeLevelData == null) {
            ((ServerWorldInfoBridge) levelData).bridge$setUniqueId(UUID.randomUUID());
        } else {
            ((ServerWorldInfoBridge) levelData).bridge$setUniqueId(SaveFormatMixin.impl$spongeLevelData.get(Constants.Sponge.World.UNIQUE_ID).read(
                    UUIDCodec.CODEC).result().orElse(UUID.randomUUID()));
        }

        SaveFormatMixin.impl$spongeLevelData = null;
        return levelData;
    }
}
