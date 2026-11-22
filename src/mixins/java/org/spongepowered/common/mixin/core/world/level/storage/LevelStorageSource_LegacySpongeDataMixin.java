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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.util.Constants;

import java.nio.file.Path;

/**
 * Worlds written by earlier versions keep the Sponge compound at the level.dat root, while the
 * level data parser only receives the "Data" compound. Move it into "Data" as the raw tag is read,
 * so the stored world unique id survives the first start after the update.
 */
@Mixin(LevelStorageSource.class)
public abstract class LevelStorageSource_LegacySpongeDataMixin {

    @Inject(method = "readLevelDataTagRaw", at = @At("RETURN"))
    private static void impl$migrateLegacySpongeData(final Path path, final CallbackInfoReturnable<CompoundTag> cir) {
        final CompoundTag root = cir.getReturnValue();
        if (root == null || !root.contains(Constants.Sponge.Data.V2.SPONGE_DATA)) {
            return;
        }
        final CompoundTag data = root.getCompound("Data");
        if (data.contains(Constants.Sponge.Data.V2.SPONGE_DATA)) {
            root.remove(Constants.Sponge.Data.V2.SPONGE_DATA);
            return;
        }
        data.put(Constants.Sponge.Data.V2.SPONGE_DATA, root.get(Constants.Sponge.Data.V2.SPONGE_DATA));
        root.remove(Constants.Sponge.Data.V2.SPONGE_DATA);
    }
}
