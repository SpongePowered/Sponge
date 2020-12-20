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
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.storage.IServerWorldInfoBridge;
import org.spongepowered.common.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Mixin(SaveFormat.LevelSave.class)
public abstract class SaveFormat_LevelSaveMixin {

    // @formatter:off
    @Shadow @Final private Path levelPath;
    // @formatter:on

    @ModifyArg(method = "checkLock",
        at = @At(value = "INVOKE", target = "Ljava/lang/IllegalStateException;<init>(Ljava/lang/String;)V", ordinal = 0, remap = false))
    private String modifyMinecraftExceptionOutputIfNotInitializationTime(final String message) {
        return "The save folder for world " + this.levelPath + " is being accessed from another location, aborting";
    }

    @Inject(method = "saveDataTag(Lnet/minecraft/util/registry/DynamicRegistries;Lnet/minecraft/world/storage/IServerConfiguration;Lnet/minecraft/nbt/CompoundNBT;)V", at = @At("RETURN"))
    private void impl$saveSpongeLevelData(final DynamicRegistries registries, final IServerConfiguration info, final CompoundNBT compound, final CallbackInfo ci) {
        if (!Sponge.isServerAvailable() || !((IServerWorldInfoBridge) info).bridge$isValid()) {
            return;
        }

        final String levelName = info.getLevelName();
        try {
            final CompoundNBT spongeLevelCompound = new CompoundNBT();
            ((IServerWorldInfoBridge) info).bridge$writeSpongeLevelData(spongeLevelCompound);

            // If the returned compound is empty then we should warn the user.
            if (spongeLevelCompound.isEmpty()) {
                new PrettyPrinter().add("Sponge Level NBT for world %s is empty!", levelName).centre().hr()
                        .add("When trying to save Sponge data for the world %s, an empty NBT compound was provided. The old Sponge data file was "
                                        + "left intact.", levelName)
                        .add()
                        .add("The following information may be useful in debugging:")
                        .add()
                        .add("World: %s", ((ResourceKeyBridge) info).bridge$getKey())
                        .add("Valid flag: ", ((IServerWorldInfoBridge) info).bridge$isValid())
                        .add()
                        .add("Stack trace:")
                        .add(new Exception())
                        .print(System.err);
                return;
            }

            final Path spongeLevelFile = this.levelPath.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT);
            final Path newSpongeLevelFile = spongeLevelFile.resolveSibling(Constants.Sponge.World.LEVEL_SPONGE_DAT_NEW);
            final Path oldSpongeLevelFile = spongeLevelFile.resolveSibling(Constants.Sponge.World.LEVEL_SPONGE_DAT_OLD);

            try (final OutputStream stream = Files.newOutputStream(newSpongeLevelFile)) {
                CompressedStreamTools.writeCompressed(spongeLevelCompound, stream);
            }

            // Before we continue, is the file zero length?
            if (newSpongeLevelFile.toFile().length() == 0) {
                // Then we just delete the file and tell the user that we didn't save properly.
                new PrettyPrinter().add("Zero length level_sponge.dat file was created for %s!", levelName).centre().hr()
                        .add("When saving the data file for the world %s, a zero length file was written. Sponge has discarded this file.", levelName)
                        .add()
                        .add("The following information may be useful in debugging:")
                        .add()
                        .add("World: %s", ((ResourceKeyBridge) info).bridge$getKey())
                        .add("Valid flag: ", ((IServerWorldInfoBridge) info).bridge$isValid())
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


    @Inject(method = "getDataTag", at = @At("RETURN"))
    private void impl$loadSpongeLevelDataBeforeVanilla(final CallbackInfoReturnable<IServerConfiguration> cir) {
        if (!Sponge.isServerAvailable()) {
            return;
        }

        final ServerWorldInfo info = (ServerWorldInfo) cir.getReturnValue();

        final Path spongeLevelFile = this.levelPath.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT);
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
                    SpongeCommon.getLogger().warn("Successfully loaded backup data file {} for world '{}'.",
                            oldSpongeLevelFile.getFileName().toString(), info.getLevelName());

                    // Delete the "current" file so we don't accidentally make it the backup file.
                    try {
                        Files.deleteIfExists(oldSpongeLevelFile);
                    } catch (final IOException e) {
                        // This server has some disk issues, bring it down to prevent more damage..
                        throw new RuntimeException(String.format("Failed to delete the old Sponge level file in world '%s'!", info.getLevelName()), e);
                    }
                }
                return;
            }

            exceptionRaised = true;
        }

        if (exceptionRaised) {
            throw new RuntimeException("Unable to load sponge level data for world '" + info.getLevelName() + "'!");
        }
    }

    private boolean impl$loadSpongeLevelData(final IServerWorldInfo info, final Path levelFile, final boolean isCurrent) {
        final CompoundNBT compound;
        try (final InputStream stream = Files.newInputStream(levelFile)) {
            compound = CompressedStreamTools.readCompressed(stream);
        } catch (final Exception ex) {
            final PrettyPrinter errorPrinter = new PrettyPrinter()
                    .add("Unable to load level data from world '%s' for file '%s'!", info.getLevelName(), levelFile.getFileName().toString())
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

        ((IServerWorldInfoBridge) info).bridge$readSpongeLevelData(compound);
        return true;
    }


}
