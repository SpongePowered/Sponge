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
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.storage.SaveHandlerBridge;
import org.spongepowered.common.data.util.DataUtil;
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

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(SaveHandler.class)
public abstract class SaveHandlerMixin implements SaveHandlerBridge {

    @Shadow @Final private File worldDirectory;

    @Nullable private Exception impl$capturedException;
    // player join stuff
    @Nullable private Path impl$file;

    @ModifyArg(method = "checkSessionLock",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MinecraftException;<init>(Ljava/lang/String;)V", ordinal = 0, remap = false))
    private String modifyMinecraftExceptionOutputIfNotInitializationTime(final String message) {
        return "The save folder for world " + this.worldDirectory + " is being accessed from another location, aborting";
    }

    @ModifyArg(method = "checkSessionLock",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MinecraftException;<init>(Ljava/lang/String;)V", ordinal = 1, remap = false))
    private String modifyMinecraftExceptionOutputIfIOException(final String message) {
        return "Failed to check session lock for world " + this.worldDirectory + ", aborting";
    }

    @Inject(method = "saveWorldInfoWithPlayer", at = @At("RETURN"))
    private void impl$saveLevelSpongeDataFile(final WorldInfo worldInformation, final CompoundNBT tagCompound, final CallbackInfo ci) {
        try {
            // If the returned NBT is empty, then we should warn the user.
            CompoundNBT spongeRootLevelNBT = ((WorldInfoBridge) worldInformation).bridge$getSpongeRootLevelNbt();
            if (spongeRootLevelNBT.func_82582_d()) {
                Integer dimensionId = ((WorldInfoBridge) worldInformation).bridge$getDimensionId();
                String dimensionIdString = dimensionId == null ? "unknown" : String.valueOf(dimensionId);

                // We should warn the user about the NBT being empty, but not saving it.
                new PrettyPrinter().add("Sponge Root Level NBT for world %s is empty!", worldInformation.func_76065_j()).centre().hr()
                        .add("When trying to save Sponge data for the world %s, an empty NBT compound was provided. The old Sponge data file was "
                                        + "left intact.",
                                worldInformation.func_76065_j())
                        .add()
                        .add("The following information may be useful in debugging:")
                        .add()
                        .add("UUID: ", ((WorldInfoBridge) worldInformation).bridge$getAssignedId())
                        .add("Dimension ID: ", dimensionIdString)
                        .add("Is Modded: ", ((WorldInfoBridge) worldInformation).bridge$getIsMod())
                        .add("Valid flag: ", ((WorldInfoBridge) worldInformation).bridge$isValid())
                        .add()
                        .add("Stack trace:")
                        .add(new Exception())
                        .print(System.err);
                return;
            }

            final File newDataFile = new File(this.worldDirectory, Constants.Sponge.World.LEVEL_SPONGE_DAT_NEW);
            final File oldDataFile = new File(this.worldDirectory, Constants.Sponge.World.LEVEL_SPONGE_DAT_OLD);
            final File dataFile = new File(this.worldDirectory, Constants.Sponge.World.LEVEL_SPONGE_DAT);
            try (final FileOutputStream stream = new FileOutputStream(newDataFile)) {
                CompressedStreamTools.func_74799_a(spongeRootLevelNBT, stream);
            }

            // Before we continue, is the file zero length?
            if (newDataFile.length() == 0) {
                Integer dimensionId = ((WorldInfoBridge) worldInformation).bridge$getDimensionId();
                String dimensionIdString = dimensionId == null ? "unknown" : String.valueOf(dimensionId);
                // Then we just delete the file and tell the user that we didn't save properly.
                new PrettyPrinter().add("Zero length level_sponge.dat file was created for %s!", worldInformation.func_76065_j()).centre().hr()
                        .add("When saving the data file for the world %s, a zero length file was written. Sponge has discarded this file.",
                                worldInformation.func_76065_j())
                        .add()
                        .add("The following information may be useful in debugging:")
                        .add()
                        .add("UUID: ", ((WorldInfoBridge) worldInformation).bridge$getAssignedId())
                        .add("Dimension ID: ", dimensionIdString)
                        .add("Is Modded: ", ((WorldInfoBridge) worldInformation).bridge$getIsMod())
                        .add("Valid flag: ", ((WorldInfoBridge) worldInformation).bridge$isValid())
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

    @Override
    public void bridge$loadSpongeDatData(final WorldInfo info) {
        final File spongeFile = new File(this.worldDirectory, Constants.Sponge.World.LEVEL_SPONGE_DAT);
        final File spongeOldFile = new File(this.worldDirectory, Constants.Sponge.World.LEVEL_SPONGE_DAT_OLD);

        boolean exceptionRaised = false;
        if (spongeFile.exists()) {
            if (impl$loadSpongeDatFile(info, spongeFile, true)) {
                return;
            }

            exceptionRaised = true;
        }

        if (spongeOldFile.exists()) {
            if (impl$loadSpongeDatFile(info, spongeOldFile, false)) {
                if (exceptionRaised) {
                    // Tell the user we successfully loaded a backup
                    SpongeImpl.getLogger().warn("Successfully loaded backup data file {} for world {}.", spongeFile.getName(), info.func_76065_j());

                    // Delete the "current" file so we don't accidentally make it the backup file.
                    spongeFile.delete();
                }
                return;
            }

            exceptionRaised = true;
        }

        if (exceptionRaised) {
            throw new RuntimeException("Unable to load sponge data for world [" + info.func_76065_j() + "]");
        }
    }

    /**
     * Redirects the {@link File#exists()} checking that if the file exists, grab
     * the file for later usage to read the file attributes for pre-existing data.
     *
     * @param localfile The local file
     * @return True if the file exists
     */
    @Redirect(method = "readPlayerData(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/nbt/NBTTagCompound;",
        at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z", remap = false))
    private boolean impl$grabFileToField(final File localfile) {
        final boolean isFile = localfile.isFile();
        this.impl$file = isFile ? localfile.toPath() : null;
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
    @Redirect(method = "readPlayerData(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/nbt/NBTTagCompound;", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/nbt/CompressedStreamTools;readCompressed(Ljava/io/InputStream;)"
            + "Lnet/minecraft/nbt/NBTTagCompound;"))
    private CompoundNBT impl$readLegacyDataAndOrSpongeData(final InputStream inputStream) throws IOException {
        Instant creation = this.impl$file == null ? Instant.now() : Files.readAttributes(this.impl$file, BasicFileAttributes.class).creationTime().toInstant();
        final CompoundNBT compound = CompressedStreamTools.func_74796_a(inputStream);
        Instant lastPlayed = Instant.now();
        // first try to migrate bukkit join data stuff
        if (compound.func_150297_b(Constants.Bukkit.BUKKIT, Constants.NBT.TAG_COMPOUND)) {
            final CompoundNBT bukkitCompound = compound.func_74775_l(Constants.Bukkit.BUKKIT);
            creation = Instant.ofEpochMilli(bukkitCompound.func_74763_f(Constants.Bukkit.BUKKIT_FIRST_PLAYED));
            lastPlayed = Instant.ofEpochMilli(bukkitCompound.func_74763_f(Constants.Bukkit.BUKKIT_LAST_PLAYED));
        }
        // migrate canary join data
        if (compound.func_150297_b(Constants.Canary.ROOT, Constants.NBT.TAG_COMPOUND)) {
            final CompoundNBT canaryCompound = compound.func_74775_l(Constants.Canary.ROOT);
            creation = Instant.ofEpochMilli(canaryCompound.func_74763_f(Constants.Canary.FIRST_JOINED));
            lastPlayed = Instant.ofEpochMilli(canaryCompound.func_74763_f(Constants.Canary.LAST_JOINED));
        }
        UUID playerId = null;
        if (compound.func_186855_b(Constants.UUID)) {
            playerId = compound.func_186857_a(Constants.UUID);
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
            target = "Lnet/minecraft/nbt/CompressedStreamTools;writeCompressed(Lnet/minecraft/nbt/NBTTagCompound;Ljava/io/OutputStream;)V",
            shift = At.Shift.AFTER))
    private void impl$saveSpongePlayerData(final PlayerEntity player, final CallbackInfo callbackInfo) {
        SpongePlayerDataHandler.savePlayer(player.func_110124_au());
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

    // SF overrides getWorldDirectory for mod compatibility.
    // In order to avoid conflicts, we simply use another method to guarantee
    // the sponge world directory is returned for the corresponding save handler.
    // AnvilSaveHandlerMixin#getChunkLoader is one example where we must use this method.
    @Override
    public File bridge$getSpongeWorldDirectory() {
        return this.worldDirectory;
    }

    private boolean impl$loadSpongeDatFile(final WorldInfo info, final File file, boolean isCurrent) {
        final CompoundNBT compound;
        try (final FileInputStream stream = new FileInputStream(file)) {
            compound = CompressedStreamTools.func_74796_a(stream);
        } catch (Exception ex) {
            PrettyPrinter errorPrinter = new PrettyPrinter()
                    .add("Unable to load level data from world [%s] for file [%s]!", info.func_76065_j(), file.getName())
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
        ((WorldInfoBridge) info).bridge$setSpongeRootLevelNBT(compound);
        if (compound.func_74764_b(Constants.Sponge.SPONGE_DATA)) {
            final CompoundNBT spongeCompound = compound.func_74775_l(Constants.Sponge.SPONGE_DATA);
            DataUtil.spongeDataFixer.func_188257_a(FixTypes.LEVEL, spongeCompound);
            ((WorldInfoBridge) info).bridge$readSpongeNbt(spongeCompound);
        }

        return true;
    }

}
