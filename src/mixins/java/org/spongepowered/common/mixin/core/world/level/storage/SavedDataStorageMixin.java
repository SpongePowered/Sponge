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
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Util;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.world.level.storage.DimensionDataStorageBridge;
import org.spongepowered.common.data.DataUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Mixin(SavedDataStorage.class)
public abstract class SavedDataStorageMixin implements DimensionDataStorageBridge {

    // @formatter:off
    private @Nullable Identifier impl$dimensionKey;
    // @formatter:on

    @WrapOperation(method = "encodeUnchecked",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"
        )
    )
    public <T extends SavedData> Tag impl$writeAdditionalMapNBT(
        final CompoundTag instance, final String key, final Tag encoded,
        final Operation<Tag> original, final SavedDataType<T> type,
        final SavedData data, final RegistryOps<Tag> registry
    ) {
        if (data instanceof DataCompoundHolder dch) {
            if (DataUtil.syncDataToTag(dch)) {
                instance.merge(dch.data$getCompound());
            }
        }
        return original.call(instance, key, encoded);
    }

    @WrapOperation(method = "readSavedData", at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElse(Ljava/lang/Object;)Ljava/lang/Object;"))
    public <T> T readSpongeMapData(
        final Optional<T> instance, final T other, final Operation<T> original,
        final @Local CompoundTag rootTag
    ) {
        final var result = original.call(instance, other);
        if (result instanceof DataCompoundHolder dch) {
            dch.data$setCompound(rootTag);
            DataUtil.syncTagToData(dch);
            dch.data$setCompound(null);
        }
        return result;
    }

    @Override
    public void bridge$dimensionKey(final @Nullable Identifier dimensionKey) {
        this.impl$dimensionKey = dimensionKey;
    }

    @Override
    public @Nullable Identifier bridge$dimensionKey() {
        return this.impl$dimensionKey;
    }

    /**
     * Writes Sponge's own data the way vanilla writes {@code level.dat}: to a temporary file, which then replaces the
     * old one. We need this because a world's id, level data and players are rewritten on every save, and vanilla
     * writes saved data in place. Neo already writes all saved data this way through its own call, so there is
     * nothing to wrap there.
     */
    @WrapOperation(method = "tryWrite", require = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/nio/file/Path;)V"))
    private void impl$writeSpongeDataSafely(final CompoundTag tag, final Path path, final Operation<Void> original,
        final @Local(argsOnly = true) SavedDataType<?> type
    ) throws IOException {
        if (!ResourceKey.SPONGE_NAMESPACE.equals(type.id().getNamespace())) {
            original.call(tag, path);
            return;
        }
        final Path written = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
        original.call(tag, written);
        Util.safeReplaceFile(path, written, path.resolveSibling(path.getFileName() + "_old"));
    }
}
