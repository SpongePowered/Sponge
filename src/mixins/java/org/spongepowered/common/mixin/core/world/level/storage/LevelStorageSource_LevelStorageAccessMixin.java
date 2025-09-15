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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.world.level.storage.LevelStorageAccessBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.data.DataUtil;
import org.spongepowered.common.util.Constants;

import java.nio.file.Path;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public abstract class LevelStorageSource_LevelStorageAccessMixin implements LevelStorageAccessBridge {

    // @formatter:off
    @Shadow @Final LevelStorageSource.LevelDirectory levelDirectory;
    // @formatter:on

    private boolean impl$dedicated = false;

    @Inject(method = "getDimensionPath", at = @At("HEAD"), cancellable = true)
    public void impl$dedicatedDimensionPath(final CallbackInfoReturnable<Path> cir) {
        if (this.impl$dedicated) {
            cir.setReturnValue(this.levelDirectory.path());
        }
    }

    @ModifyArg(method = "checkLock",
        at = @At(value = "INVOKE", target = "Ljava/lang/IllegalStateException;<init>(Ljava/lang/String;)V", ordinal = 0, remap = false))
    private String modifyMinecraftExceptionOutputIfNotInitializationTime(final String message) {
        return "Lock for world " + this.levelDirectory.path() + " is no longer valid!";
    }

    @WrapOperation(
            method = "saveDataTag(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/storage/WorldData;Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;saveLevelData(Lnet/minecraft/nbt/CompoundTag;)V"
            )
    )
    private void impl$saveSpongeLevelData(final LevelStorageSource.LevelStorageAccess instance, final CompoundTag root, final Operation<Void> original,
            final RegistryAccess registry, final WorldData levelData, final @Nullable CompoundTag tag) {
        root.put(Constants.Sponge.Data.V2.SPONGE_DATA, ((PrimaryLevelDataBridge) levelData).bridge$writeSpongeLevelData());
        if (DataUtil.syncDataToTag(levelData)) {
            root.merge(((DataCompoundHolder) levelData).data$getCompound());
        }
        original.call(instance, root);
    }

    @Override
    public void bridge$setDedicated(final boolean dedicated) {
        this.impl$dedicated = dedicated;
    }
}
