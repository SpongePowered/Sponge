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
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
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
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.bridge.world.storage.SaveHandlerBridge;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Mixin(SaveHandler.class)
public abstract class SaveHandlerMixin implements SaveHandlerBridge, IPlayerFileData {

    @Shadow public abstract File shadow$getWorldDirectory();

    @Nullable private Exception impl$capturedException;
    @Nullable private Path impl$file;

    @ModifyArg(method = "checkSessionLock",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MinecraftException;<init>(Ljava/lang/String;)V", ordinal = 0, remap = false))
    private String modifyMinecraftExceptionOutputIfNotInitializationTime(String message) {
        return "The save folder for world " + this.shadow$getWorldDirectory() + " is being accessed from another location, aborting";
    }

    @ModifyArg(method = "checkSessionLock",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MinecraftException;<init>(Ljava/lang/String;)V", ordinal = 1, remap = false))
    private String modifyMinecraftExceptionOutputIfIOException(String message) {
        return "Failed to check session lock for world " + this.shadow$getWorldDirectory() + ", aborting";
    }

    @Inject(method = "saveWorldInfoWithPlayer", at = @At("RETURN"))
    private void impl$saveSpongeLevelData(WorldInfo info, CompoundNBT compound, CallbackInfo ci) {
        if (!((WorldInfoBridge) info).bridge$isValid()) {
            return;
        }

        try {
            final CompoundNBT spongeLevelCompound = new CompoundNBT();
            ((WorldInfoBridge) info).bridge$writeSpongeLevelData(spongeLevelCompound);

            @Nullable final DimensionType dimensionType = ((WorldInfoBridge) info).bridge$getDimensionType();
            final String dimensionIdString = dimensionType == null ? "unknown" : String.valueOf(dimensionType.getId());

            // If the returned compound is empty then we should warn the user.
            if (spongeLevelCompound.isEmpty()) {
                new PrettyPrinter().add("Sponge Level NBT for world %s is empty!", info.getWorldName()).centre().hr()
                        .add("When trying to save Sponge data for the world %s, an empty NBT compound was provided. The old Sponge data file was "
                                        + "left intact.", info.getWorldName())
                        .add()
                        .add("The following information may be useful in debugging:")
                        .add()
                        .add("UUID: ", ((WorldInfoBridge) info).bridge$getUniqueId())
                        .add("Dimension ID: ", dimensionIdString)
                        .add("Is Mod Created: ", ((WorldInfoBridge) info).bridge$isModCreated())
                        .add("Valid flag: ", ((WorldInfoBridge) info).bridge$isValid())
                        .add()
                        .add("Stack trace:")
                        .add(new Exception())
                        .print(System.err);
                return;
            }

            final File newDataFile = new File(this.shadow$getWorldDirectory(), Constants.Sponge.World.LEVEL_SPONGE_DAT_NEW);
            final File oldDataFile = new File(this.shadow$getWorldDirectory(), Constants.Sponge.World.LEVEL_SPONGE_DAT_OLD);
            final File dataFile = new File(this.shadow$getWorldDirectory(), Constants.Sponge.World.LEVEL_SPONGE_DAT);
            try (final FileOutputStream stream = new FileOutputStream(newDataFile)) {
                CompressedStreamTools.writeCompressed(spongeLevelCompound, stream);
            }

            // Before we continue, is the file zero length?
            if (newDataFile.length() == 0) {
                // Then we just delete the file and tell the user that we didn't save properly.
                new PrettyPrinter().add("Zero length level_sponge.dat file was created for %s!", info.getWorldName()).centre().hr()
                        .add("When saving the data file for the world %s, a zero length file was written. Sponge has discarded this file.",
                                info.getWorldName())
                        .add()
                        .add("The following information may be useful in debugging:")
                        .add()
                        .add("UUID: ", ((WorldInfoBridge) info).bridge$getUniqueId())
                        .add("Dimension ID: ", dimensionIdString)
                        .add("Is Mod Created: ", ((WorldInfoBridge) info).bridge$isModCreated())
                        .add("Valid flag: ", ((WorldInfoBridge) info).bridge$isValid())
                        .add()
                        .add("Stack trace:")
                        .add(new Exception())
                        .print(System.err);
                newDataFile.delete();
                return;
            }

            if (dataFile.exists()) {
                if (oldDataFile.exists()) {
                    oldDataFile.delete();
                }

                dataFile.renameTo(oldDataFile);
                dataFile.delete();
            }

            newDataFile.renameTo(dataFile);

            if (newDataFile.exists()) {
                newDataFile.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Inject(method = "loadWorldInfo", at = @At("RETURN"))
    private void loadSpongeLevelData(CallbackInfoReturnable<WorldInfo> cir) {
        final WorldInfo info = cir.getReturnValue();

        final File spongeFile = new File(this.shadow$getWorldDirectory(), Constants.Sponge.World.LEVEL_SPONGE_DAT);
        final File spongeOldFile = new File(this.shadow$getWorldDirectory(), Constants.Sponge.World.LEVEL_SPONGE_DAT_OLD);

        boolean exceptionRaised = false;
        if (spongeFile.exists()) {
            if (this.impl$loadSpongeDatFile(info, spongeFile, true)) {
                return;
            }

            exceptionRaised = true;
        }

        if (spongeOldFile.exists()) {
            if (this.impl$loadSpongeDatFile(info, spongeOldFile, false)) {
                if (exceptionRaised) {
                    // Tell the user we successfully loaded a backup
                    SpongeImpl.getLogger().warn("Successfully loaded backup data file {} for world '{}'.", spongeFile.getName(), info.getWorldName());

                    // Delete the "current" file so we don't accidentally make it the backup file.
                    spongeFile.delete();
                }
                return;
            }

            exceptionRaised = true;
        }

        if (exceptionRaised) {
            throw new RuntimeException("Unable to load sponge data for world [" + info.getWorldName() + "]");
        }
    }

    /**
     * Redirects the {@link File#exists()} checking that if the file exists, grab
     * the file for later usage to read the file attributes for pre-existing data.
     *
     * @param localFile The local file
     * @return True if the file exists
     */
    @Redirect(method = "readPlayerData", at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z", remap = false))
    private boolean impl$grabFileToField(File localFile) {
        final boolean isFile = localFile.isFile();
        this.impl$file = isFile ? localFile.toPath() : null;
        return isFile;
    }

    /**
     * Redirects the reader such that since the player file existed already, we can safely assume
     * we can grab the file attributes and check if the first join time exists in the sponge compound,
     * if it does not, then we add it to the sponge data part of the compound.
     *
     * @param inputStream The input stream to direct to compressed stream tools
     * @return The compound that may be modified
     * @throws IOException for reasons
     */
    @Redirect(method = "readPlayerData", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompressedStreamTools;readCompressed(Ljava/io/InputStream;)Lnet/minecraft/nbt/CompoundNBT;"))
    private CompoundNBT impl$readLegacyDataAndOrSpongeData(InputStream inputStream) throws IOException {
        Instant creation = this.impl$file == null ? Instant.now() : Files.readAttributes(this.impl$file, BasicFileAttributes.class).creationTime().toInstant();
        final CompoundNBT compound = CompressedStreamTools.readCompressed(inputStream);
        Instant lastPlayed = Instant.now();
        // first try to migrate bukkit join data stuff
        if (compound.contains(Constants.Bukkit.BUKKIT, Constants.NBT.TAG_COMPOUND)) {
            final CompoundNBT bukkitCompound = compound.getCompound(Constants.Bukkit.BUKKIT);
            creation = Instant.ofEpochMilli(bukkitCompound.getLong(Constants.Bukkit.BUKKIT_FIRST_PLAYED));
            lastPlayed = Instant.ofEpochMilli(bukkitCompound.getLong(Constants.Bukkit.BUKKIT_LAST_PLAYED));
        }
        // migrate canary join data
        if (compound.contains(Constants.Canary.ROOT, Constants.NBT.TAG_COMPOUND)) {
            final CompoundNBT canaryCompound = compound.getCompound(Constants.Canary.ROOT);
            creation = Instant.ofEpochMilli(canaryCompound.getLong(Constants.Canary.FIRST_JOINED));
            lastPlayed = Instant.ofEpochMilli(canaryCompound.getLong(Constants.Canary.LAST_JOINED));
        }
        @Nullable UUID playerId = null;
        if (compound.hasUniqueId(Constants.UUID)) {
            playerId = compound.getUniqueId(Constants.UUID);
        }
        if (playerId != null) {
            final Optional<Instant> savedFirst = SpongePlayerDataHandler.getFirstJoined(playerId);
            if (savedFirst.isPresent()) {
                creation = savedFirst.get();
            }
            final Optional<Instant> savedJoined = SpongePlayerDataHandler.getLastPlayed(playerId);
            if (savedJoined.isPresent()) {
                lastPlayed = savedJoined.get();
            }
            SpongePlayerDataHandler.setPlayerInfo(playerId, creation, lastPlayed);
        }
        this.impl$file = null;
        return compound;
    }

    @Inject(method = "writePlayerData",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompressedStreamTools;writeCompressed(Lnet/minecraft/nbt/CompoundNBT;Ljava/io/OutputStream;)V",
            shift = At.Shift.AFTER))
    private void impl$saveSpongePlayerData(PlayerEntity player, CallbackInfo callbackInfo) {
        SpongePlayerDataHandler.savePlayer(player.getUniqueID());
    }

    @Inject(
        method = "writePlayerData",
        at = @At(value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V",
            remap = false),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void impl$trackExceptionForLogging(PlayerEntity player, CallbackInfo ci, Exception exception) {
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
    private void impl$useStoredException(Logger logger, String message, Object param) {
        logger.warn(message, param, this.impl$capturedException);
        this.impl$capturedException = null;
    }

    // SF overrides getWorldDirectory for mod compatibility.
    // In order to avoid conflicts, we simply use another method to guarantee
    // the sponge world directory is returned for the corresponding save handler.
    // AnvilSaveHandlerMixin#getChunkLoader is one example where we must use this method.
    @Override
    public File bridge$getWorldDirectory() {
        return this.shadow$getWorldDirectory();
    }

    private boolean impl$loadSpongeDatFile(WorldInfo info, File file, boolean isCurrent) {
        final CompoundNBT compound;
        try (final FileInputStream stream = new FileInputStream(file)) {
            compound = CompressedStreamTools.readCompressed(stream);
        } catch (Exception ex) {
            PrettyPrinter errorPrinter = new PrettyPrinter()
                    .add("Unable to load level data from world [%s] for file [%s]!", info.getWorldName(), file.getName())
                    .centre()
                    .hr();
            // We can't read it - but let's copy the file so we can ask for it to inspect what it looks like later.
            Path corrupted = file.toPath().getParent().resolve(file.getName() + ".corrupted-" +
                    DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replaceAll(":", "") + ".dat");
            try {
                Files.copy(file.toPath(), corrupted);
                errorPrinter.add("We have backed up the corrupted file to %s. Please keep hold of this, it may be useful to Sponge developers.",
                        corrupted.getFileName());
            } catch (IOException e) {
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
