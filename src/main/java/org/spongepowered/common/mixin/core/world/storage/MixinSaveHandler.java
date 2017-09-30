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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
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
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.IMixinSaveHandler;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.world.WorldManager;
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
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(SaveHandler.class)
public abstract class MixinSaveHandler implements IMixinSaveHandler {

    private static final String COMPRESSED_READ_FILE = "Lnet/minecraft/nbt/CompressedStreamTools;readCompressed(Ljava/io/InputStream;)"
                                                       + "Lnet/minecraft/nbt/NBTTagCompound;";
    private static final String COMPRESSED_WRITE_FILE = "Lnet/minecraft/nbt/CompressedStreamTools;writeCompressed"
                                                        + "(Lnet/minecraft/nbt/NBTTagCompound;Ljava/io/OutputStream;)V";
    private static final String READ_PLAYER_DATA = "readPlayerData(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/nbt/NBTTagCompound;";
    private static final String NBT_COMPOUND_SET = "Lnet/minecraft/nbt/NBTTagCompound;setTag(Ljava/lang/String;"
                                                   + "Lnet/minecraft/nbt/NBTBase;)V";
    @Shadow @Final private File worldDirectory;
    @Shadow @Final private long initializationTime;

    @ModifyArg(method = "checkSessionLock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MinecraftException;<init>(Ljava/lang/String;)V"
            , ordinal = 0, remap = false))
    public String modifyMinecraftExceptionOutputIfNotInitializationTime(String message) {
        return "The save folder for world " + this.worldDirectory + " is being accessed from another location, aborting";
    }

    @ModifyArg(method = "checkSessionLock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MinecraftException;<init>(Ljava/lang/String;)V"
            , ordinal = 1, remap = false))
    public String modifyMinecraftExceptionOutputIfIOException(String message) {
        return "Failed to check session lock for world " + this.worldDirectory + ", aborting";
    }

    @Inject(method = "saveWorldInfoWithPlayer", at = @At(value = "INVOKE", target = NBT_COMPOUND_SET, shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void onSaveWorldInfoWithPlayerAfterTagSet(WorldInfo worldInformation, NBTTagCompound tagCompound, CallbackInfo ci,
            NBTTagCompound nbttagcompound1, NBTTagCompound nbttagcompound2) {
        saveDimensionAndOtherData((SaveHandler) (Object) this, worldInformation, nbttagcompound2);
    }

    @Inject(method = "saveWorldInfoWithPlayer", at = @At("RETURN"))
    public void onSaveWorldInfoWithPlayerEnd(WorldInfo worldInformation, NBTTagCompound tagCompound, CallbackInfo ci) {
        saveSpongeDatData(worldInformation);
    }

    @Override
    public void loadSpongeDatData(WorldInfo info) throws IOException {
        final File spongeFile = new File(this.worldDirectory, "level_sponge.dat");
        final File spongeOldFile = new File(this.worldDirectory, "level_sponge.dat_old");

        if (spongeFile.exists() || spongeOldFile.exists()) {
            final NBTTagCompound compound = CompressedStreamTools.readCompressed(new FileInputStream(spongeFile.exists() ? spongeFile :
                    spongeOldFile));
            ((IMixinWorldInfo) info).setSpongeRootLevelNBT(compound);
            if (compound.hasKey(NbtDataUtil.SPONGE_DATA)) {
                final NBTTagCompound spongeCompound = compound.getCompoundTag(NbtDataUtil.SPONGE_DATA);
                DataUtil.spongeDataFixer.process(FixTypes.LEVEL, spongeCompound);
                ((IMixinWorldInfo) info).readSpongeNbt(spongeCompound);
            }
        }
    }

    private void saveSpongeDatData(WorldInfo info) {
        try {
            final File spongeFile1 = new File(this.worldDirectory, "level_sponge.dat_new");
            final File spongeFile2 = new File(this.worldDirectory, "level_sponge.dat_old");
            final File spongeFile3 = new File(this.worldDirectory, "level_sponge.dat");
            CompressedStreamTools.writeCompressed(((IMixinWorldInfo) info).getSpongeRootLevelNbt(), new FileOutputStream(spongeFile1));

            if (spongeFile2.exists()) {
                spongeFile2.delete();
            }

            spongeFile3.renameTo(spongeFile2);

            if (spongeFile3.exists()) {
                spongeFile3.delete();
            }

            spongeFile1.renameTo(spongeFile3);

            if (spongeFile1.exists()) {
                spongeFile1.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void loadDimensionAndOtherData(SaveHandler handler, WorldInfo info, NBTTagCompound compound) {
        // Preserve dimension data from Sponge
        final NBTTagCompound customWorldDataCompound = compound.getCompoundTag("Forge");
        if (customWorldDataCompound.hasKey("DimensionData")) {
            WorldManager.loadDimensionDataMap(customWorldDataCompound.getCompoundTag("DimensionData"));
        }
    }

    private void saveDimensionAndOtherData(SaveHandler handler, WorldInfo info, NBTTagCompound compound) {
        // Only save dimension data to root world
        if (this.worldDirectory.getParentFile() == null
                || (SpongeImpl.getGame().getPlatform().getType().isClient() && this.worldDirectory.getParentFile().equals(
                        SpongeImpl.getGame().getSavesDirectory()))) {
            final NBTTagCompound customWorldDataCompound = new NBTTagCompound();
            final NBTTagCompound customDimensionDataCompound = WorldManager.saveDimensionDataMap();
            customWorldDataCompound.setTag("DimensionData", customDimensionDataCompound);
            // Share data back to Sponge
            compound.setTag("Forge", customWorldDataCompound);
        }
    }

    // player join stuff
    @Nullable private Path file;

    /**
     * Redirects the {@link File#exists()} checking that if the file exists, grab
     * the file for later usage to read the file attributes for pre-existing data.
     *
     * @param localfile The local file
     * @return True if the file exists
     */
    @Redirect(method = READ_PLAYER_DATA, at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z", remap = false))
    private boolean grabfile(File localfile) {
        final boolean isFile = localfile.isFile();
        this.file = isFile ? localfile.toPath() : null;
        return isFile;
    }

    /**
     * Redirects the reader such that since the player file existed already, we can safely assume
     * we can grab the file attributes and check if the first join time exists in the sponge compound,
     * if it does not, then we add it to the sponge data part of the compound.
     *
     * @param inputStream The input stream to direct to compressed stream tools
     * @return The compound that may be modified
     * @throws IOException
     */
    @Redirect(method = READ_PLAYER_DATA, at = @At(value = "INVOKE", target = COMPRESSED_READ_FILE))
    private NBTTagCompound spongeReadPlayerData(InputStream inputStream) throws IOException {
        Instant creation = this.file == null ? Instant.now() : Files.readAttributes(this.file, BasicFileAttributes.class).creationTime().toInstant();
        NBTTagCompound compound = CompressedStreamTools.readCompressed(inputStream);
        Instant lastPlayed = Instant.now();
        // first try to migrate bukkit join data stuff
        if (compound.hasKey(NbtDataUtil.BUKKIT, NbtDataUtil.TAG_COMPOUND)) {
            final NBTTagCompound bukkitCompound = compound.getCompoundTag(NbtDataUtil.BUKKIT);
            creation = Instant.ofEpochMilli(bukkitCompound.getLong(NbtDataUtil.BUKKIT_FIRST_PLAYED));
            lastPlayed = Instant.ofEpochMilli(bukkitCompound.getLong(NbtDataUtil.BUKKIT_LAST_PLAYED));
        }
        UUID playerId = null;
        if (compound.hasUniqueId(NbtDataUtil.UUID)) {
            playerId = compound.getUniqueId(NbtDataUtil.UUID);
        }
        if (playerId != null) {
            Optional<Instant> savedFirst = SpongePlayerDataHandler.getFirstJoined(playerId);
            if (savedFirst.isPresent()) {
                creation = savedFirst.get();
            }
            Optional<Instant> savedJoined = SpongePlayerDataHandler.getLastPlayed(playerId);
            if (savedJoined.isPresent()) {
                lastPlayed = savedJoined.get();
            }
            SpongePlayerDataHandler.setPlayerInfo(playerId, creation, lastPlayed);
        }
        this.file = null;
        return compound;
    }

    @Inject(method = "writePlayerData", at = @At(value = "INVOKE", target = COMPRESSED_WRITE_FILE, shift = At.Shift.AFTER))
    private void onSpongeWrite(EntityPlayer player, CallbackInfo callbackInfo) {
        SpongePlayerDataHandler.savePlayer(player.getUniqueID());
    }

    // SF overrides getWorldDirectory for mod compatibility.
    // In order to avoid conflicts, we simply use another method to guarantee
    // the sponge world directory is returned for the corresponding save handler.
    // MixinAnvilSaveHandler#getChunkLoader is one example where we must use this method.
    @Override
    public File getSpongeWorldDirectory() {
        return this.worldDirectory;
    }
}
