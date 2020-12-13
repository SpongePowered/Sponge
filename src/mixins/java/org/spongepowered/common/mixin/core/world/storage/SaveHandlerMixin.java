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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.util.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Mixin(SaveHandler.class)
public abstract class SaveHandlerMixin implements IPlayerFileData {

    @Shadow public abstract File shadow$getWorldDirectory();

    @Nullable private Exception impl$capturedException;
    @Nullable private Path impl$file;

    @ModifyArg(method = "checkSessionLock",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/SessionLockException;<init>(Ljava/lang/String;)V", ordinal = 0, remap = false))
    private String modifyMinecraftExceptionOutputIfNotInitializationTime(final String message) {
        return "The save folder for world " + this.shadow$getWorldDirectory() + " is being accessed from another location, aborting";
    }

    @ModifyArg(method = "checkSessionLock",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/SessionLockException;<init>(Ljava/lang/String;)V", ordinal = 1, remap = false))
    private String modifyMinecraftExceptionOutputIfIOException(final String message) {
        return "Failed to check session lock for world " + this.shadow$getWorldDirectory() + ", aborting";
    }

    @Inject(method = "saveWorldInfoWithPlayer", at = @At("RETURN"))
    private void impl$saveSpongeLevelData(final WorldInfo info, final CompoundNBT compound, final CallbackInfo ci) {
        if (!Sponge.isServerAvailable() || !((WorldInfoBridge) info).bridge$isValid()) {
            return;
        }

        try {
            final CompoundNBT spongeLevelCompound = new CompoundNBT();
            ((WorldInfoBridge) info).bridge$writeSpongeLevelData(spongeLevelCompound);

            // If the returned compound is empty then we should warn the user.
            if (spongeLevelCompound.isEmpty()) {
                new PrettyPrinter().add("Sponge Level NBT for world %s is empty!", info.getWorldName()).centre().hr()
                        .add("When trying to save Sponge data for the world %s, an empty NBT compound was provided. The old Sponge data file was "
                                        + "left intact.", info.getWorldName())
                        .add()
                        .add("The following information may be useful in debugging:")
                        .add()
                        .add("World: %s", ((ResourceKeyBridge) info).bridge$getKey())
                        .add("Valid flag: ", ((WorldInfoBridge) info).bridge$isValid())
                        .add()
                        .add("Stack trace:")
                        .add(new Exception())
                        .print(System.err);
                return;
            }

            final Path spongeLevelFile = this.shadow$getWorldDirectory().toPath().resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT);
            final Path newSpongeLevelFile = spongeLevelFile.resolveSibling(Constants.Sponge.World.LEVEL_SPONGE_DAT_NEW);
            final Path oldSpongeLevelFile = spongeLevelFile.resolveSibling(Constants.Sponge.World.LEVEL_SPONGE_DAT_OLD);

            try (final OutputStream stream = Files.newOutputStream(newSpongeLevelFile)) {
                CompressedStreamTools.writeCompressed(spongeLevelCompound, stream);
            }

            // Before we continue, is the file zero length?
            if (newSpongeLevelFile.toFile().length() == 0) {
                // Then we just delete the file and tell the user that we didn't save properly.
                new PrettyPrinter().add("Zero length level_sponge.dat file was created for %s!", info.getWorldName()).centre().hr()
                        .add("When saving the data file for the world %s, a zero length file was written. Sponge has discarded this file.",
                                info.getWorldName())
                        .add()
                        .add("The following information may be useful in debugging:")
                        .add()
                        .add("World: %s", ((ResourceKeyBridge) info).bridge$getKey())
                        .add("Is Mod Created: ", ((WorldInfoBridge) info).bridge$isModCreated())
                        .add("Valid flag: ", ((WorldInfoBridge) info).bridge$isValid())
                        .add()
                        .add("Stack trace:")
                        .add(new Exception())
                        .print(System.err);
                Files.deleteIfExists(newSpongeLevelFile);
                return;
            }

            if (Files.exists(spongeLevelFile)) {
                Files.copy(spongeLevelFile, oldSpongeLevelFile, StandardCopyOption.REPLACE_EXISTING);
            }

            Files.move(newSpongeLevelFile, spongeLevelFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Inject(method = "loadWorldInfo", at = @At("RETURN"))
    private void impl$loadSpongeLevelDataBeforeVanilla(final CallbackInfoReturnable<WorldInfo> cir) {
        if (!Sponge.isServerAvailable()) {
            return;
        }

        final WorldInfo info = cir.getReturnValue();

        final Path spongeLevelFile = this.shadow$getWorldDirectory().toPath().resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT);
        final Path oldSpongeLevelFile = spongeLevelFile.resolveSibling(Constants.Sponge.World.LEVEL_SPONGE_DAT_OLD);

        boolean exceptionRaised = false;
        if (Files.exists(spongeLevelFile)) {
            if (this.impl$loadSpongeLevelData(info, spongeLevelFile, true)) {
                return;
            }

            exceptionRaised = true;
        }

        if (Files.exists(oldSpongeLevelFile)) {
            if (this.impl$loadSpongeLevelData(info, oldSpongeLevelFile, false)) {
                if (exceptionRaised) {
                    // Tell the user we successfully loaded a backup
                    SpongeCommon.getLogger().warn("Successfully loaded backup data file {} for world '{}'.", oldSpongeLevelFile.getFileName().toString(),
                            info.getWorldName());

                    // Delete the "current" file so we don't accidentally make it the backup file.
                    try {
                        Files.deleteIfExists(oldSpongeLevelFile);
                    } catch (final IOException e) {
                        // This server has some disk issues, bring it down to prevent more damage..
                        throw new RuntimeException(String.format("Failed to delete the old Sponge level file in world '%s'!", info.getWorldName()),
                                e);
                    }
                }
                return;
            }

            exceptionRaised = true;
        }

        if (exceptionRaised) {
            throw new RuntimeException("Unable to load sponge level data for world '" + info.getWorldName() + "'!");
        }
    }

    /**
     * Redirects the {@link File#exists()} checking that if the file exists, grab
     * the file for later usage to read the file attributes for pre-existing data.
     */
    @Redirect(method = "readPlayerData", at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z", remap = false))
    private boolean impl$grabFileToField(final File localFile) {
        final boolean isFile = localFile.isFile();
        this.impl$file = isFile ? localFile.toPath() : null;
        return isFile;
    }

    @Redirect(method = "readPlayerData", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;read(Lnet/minecraft/nbt/CompoundNBT;)V"))
    private void impl$readSpongePlayerData(final PlayerEntity playerEntity, final CompoundNBT compound) throws IOException {
        playerEntity.read(compound);
        ((SpongeServer) SpongeCommon.getServer()).getPlayerDataManager().readPlayerData(compound, null, this.impl$file == null ? null :
                Files.readAttributes(this.impl$file, BasicFileAttributes.class).creationTime().toInstant());
        this.impl$file = null;
    }

    @Inject(method = "writePlayerData",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompressedStreamTools;writeCompressed(Lnet/minecraft/nbt/CompoundNBT;Ljava/io/OutputStream;)V",
            shift = At.Shift.AFTER))
    private void impl$saveSpongePlayerData(final PlayerEntity player, final CallbackInfo callbackInfo) {
        ((SpongeServer) Sponge.getServer()).getPlayerDataManager().savePlayer(player.getUniqueID());
    }

    @Inject(
        method = "writePlayerData",
        at = @At(value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V",
            remap = false),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void impl$trackExceptionForLogging(final PlayerEntity player, final CallbackInfo ci, final Exception exception) {
        this.impl$capturedException = exception;
    }

    @Redirect(
        method = "writePlayerData",
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

    private boolean impl$loadSpongeLevelData(final WorldInfo info, final Path levelFile, final boolean isCurrent) {
        final CompoundNBT compound;
        try (final InputStream stream = Files.newInputStream(levelFile)) {
            compound = CompressedStreamTools.readCompressed(stream);
        } catch (final Exception ex) {
            final PrettyPrinter errorPrinter = new PrettyPrinter()
                    .add("Unable to load level data from world '%s' for file '%s'!", info.getWorldName(), levelFile.getFileName().toString())
                    .centre()
                    .hr();
            // We can't read it - but let's copy the file so we can ask for it to inspect what it looks like later.
            final Path corrupted = levelFile.getParent().resolve(levelFile.getFileName().toString() + ".corrupted-" +
                    DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replaceAll(":", "") + ".dat");
            try {
                Files.copy(levelFile, corrupted);
                errorPrinter.add("We have backed up the corrupted file to %s. Please keep hold of this, it may be useful to Sponge developers.",
                        corrupted.getFileName());
            } catch (final IOException e) {
                errorPrinter.add("We were unable to copy the corrupted file.");
            }

            if (isCurrent) {
                errorPrinter.add("We will try to load the backup file (if it exists)");
            }

            errorPrinter
                    .hr()
                    .add("Exception:")
                    .add(ex)
                    .print(System.err);
            return false;
        }

        ((WorldInfoBridge) info).bridge$readSpongeLevelData(compound);
        return true;
    }
}
