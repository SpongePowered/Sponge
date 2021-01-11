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

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.storage.ServerWorldInfoBridge;
import org.spongepowered.common.util.Constants;

import java.nio.file.Path;

@Mixin(SaveFormat.LevelSave.class)
public abstract class SaveFormat_LevelSaveMixin {

    // @formatter:off
    @Shadow @Final private Path levelPath;
    // @formatter:on

    @ModifyArg(method = "checkLock",
        at = @At(value = "INVOKE", target = "Ljava/lang/IllegalStateException;<init>(Ljava/lang/String;)V", ordinal = 0, remap = false))
    private String modifyMinecraftExceptionOutputIfNotInitializationTime(final String message) {
        return "Lock for world " + this.levelPath + " is no longer valid!";
    }

    @Redirect(
            method = "saveDataTag(Lnet/minecraft/util/registry/DynamicRegistries;Lnet/minecraft/world/storage/IServerConfiguration;Lnet/minecraft/"
                    + "nbt/CompoundNBT;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundNBT;put(Ljava/lang/String;Lnet/minecraft/nbt/INBT;)Lnet/minecraft/nbt/INBT;"
            )
    )
    private INBT impl$saveSpongeLevelData(final CompoundNBT root, final String path, final INBT data, final DynamicRegistries p_237288_1_,
            final IServerConfiguration levelData) {
        final CompoundNBT spongeLevelData = new CompoundNBT();
        root.put(Constants.Sponge.SPONGE_DATA, spongeLevelData);
        spongeLevelData.putUUID(Constants.Sponge.World.UNIQUE_ID, ((ServerWorldInfoBridge) levelData).bridge$uniqueId());

        return root.put(path, data);
    }
}
