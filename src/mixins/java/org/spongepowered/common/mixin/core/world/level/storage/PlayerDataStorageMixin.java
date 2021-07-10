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

import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;

@Mixin(PlayerDataStorage.class)
public abstract class PlayerDataStorageMixin {

    // @formatter:off
    @Shadow @Final private File playerDir;
    // @formatter:on

    @Nullable private Exception impl$capturedException;

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;load(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void impl$readSpongePlayerData(final Player playerEntity, final CompoundTag compound) throws IOException {
        playerEntity.load(compound);
        final Path file = new File(this.playerDir, playerEntity.getStringUUID() + ".dat").toPath();
        final Instant creationTime = Files.exists(file) ? Files.readAttributes(file, BasicFileAttributes.class).creationTime().toInstant() : null;
        ((SpongeServer) SpongeCommon.server()).getPlayerDataManager().readPlayerData(compound, null, creationTime);
    }

    @Inject(method = "save",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/io/File;)V",
            shift = At.Shift.AFTER))
    private void impl$saveSpongePlayerData(final Player player, final CallbackInfo callbackInfo) {
        ((SpongeServer) Sponge.server()).getPlayerDataManager().saveSpongePlayerData(player.getUUID());
    }

    @Inject(
        method = "save",
        at = @At(value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V",
            remap = false),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void impl$trackExceptionForLogging(final Player player, final CallbackInfo ci, final Exception exception) {
        this.impl$capturedException = exception;
    }

    @Redirect(
        method = "save",
        at = @At(
            value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V",
            remap = false
        )
    )
    private void impl$useStoredException(final Logger logger, final String message, final Object param) {
        logger.warn(message, param, this.impl$capturedException);
        this.impl$capturedException = null;
    }

}
