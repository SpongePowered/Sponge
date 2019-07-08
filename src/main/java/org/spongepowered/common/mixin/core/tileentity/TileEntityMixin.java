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
package org.spongepowered.common.mixin.core.tileentity;

import co.aikar.timings.Timing;
import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.data.nbt.CustomDataNbtUtil;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;

import javax.annotation.Nullable;

@Mixin(net.minecraft.tileentity.TileEntity.class)
abstract class TileEntityMixin implements TileEntityBridge, DataCompoundHolder, TimingBridge, TrackableBridge {

    private final boolean impl$isTileVanilla = getClass().getName().startsWith("net.minecraft.");
    @Nullable private Timing impl$timing;
    private boolean impl$isTicking = false;
    // Used by tracker config
    private boolean impl$allowsBlockBulkCapture = true;
    private boolean impl$allowsEntityBulkCapture = true;
    private boolean impl$allowsBlockEventCreation = true;
    private boolean impl$allowsEntityEventCreation = true;
    private boolean impl$isCaptured = false;

    @Shadow protected net.minecraft.world.World world;
    @Shadow private int blockMetadata;
    @Shadow protected BlockPos pos;

    @Shadow public abstract BlockPos getPos();
    @Shadow public abstract Block getBlockType();
    @Shadow public abstract NBTTagCompound writeToNBT(NBTTagCompound compound);
    @Shadow public abstract void shadow$markDirty();

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void impl$RefreshTrackerStates(final CallbackInfo ci) {
        this.bridge$refreshTrackerStates();
    }

    @Override
    public void bridge$markDirty() {
        this.shadow$markDirty();
    }


    @SuppressWarnings({"rawtypes"})
    @Inject(method = "register(Ljava/lang/String;Ljava/lang/Class;)V", at = @At(value = "RETURN"))
    private static void impl$registerTileEntityClassWithSpongeRegistry(final String name, final Class clazz, final CallbackInfo callbackInfo) {
        if (clazz != null) {
            TileEntityTypeRegistryModule.getInstance().doTileEntityRegistration(clazz, name);
        }
    }

    @Inject(method = "invalidate", at = @At("RETURN"))
    private void impl$RemoveActiveChunkOnInvalidate(final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) this).bridge$setActiveChunk(null);
    }

    /**
     * Hooks into vanilla's writeToNBT to call {@link #bridge$writeToSpongeCompound}.
     * <p>
     * <p> This makes it easier for other entity mixins to override writeToNBT without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla writes to (unused because we write to SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;", at = @At("HEAD"))
    private void impl$WriteSpongeDataToCompound(final NBTTagCompound compound, final CallbackInfoReturnable<NBTTagCompound> ci) {
        if (!((CustomDataHolderBridge) this).bridge$getCustomManipulators().isEmpty()) {
            this.bridge$writeToSpongeCompound(this.data$getSpongeCompound());
        }
    }

    /**
     * Hooks into vanilla's readFromNBT to call {@link #bridge$readFromSpongeCompound}.
     * <p>
     * <p> This makes it easier for other entity mixins to override readSpongeNBT without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla reads from (unused because we read from SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/tileentity/TileEntity;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void impl$ReadSpongeDataFromCompound(final NBTTagCompound compound, final CallbackInfo ci) {
        if (this.data$hasRootCompound()) {
            this.bridge$readFromSpongeCompound(this.data$getSpongeCompound());
        }
    }

    /**
     * Read extra data (SpongeData) from the tile entity's NBT tag.
     *
     * @param compound The SpongeData compound to read from
     */
    protected void bridge$readFromSpongeCompound(final NBTTagCompound compound) {
        CustomDataNbtUtil.readCustomData(compound, (TileEntity) this);
    }

    /**
     * Write extra data (SpongeData) to the tile entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    protected void bridge$writeToSpongeCompound(final NBTTagCompound compound) {
        CustomDataNbtUtil.writeCustomData(compound, (TileEntity) this);
    }


    @Override
    public boolean isVanilla() {
        return this.impl$isTileVanilla;
    }

    @Override
    public Timing bridge$getTimingsHandler() {
        if (this.impl$timing == null) {
            this.impl$timing = SpongeTimings.getTileEntityTiming((TileEntity) this);
        }
        return this.impl$timing;
    }


    @Override
    public boolean shouldTick() {
        final ChunkBridge chunk = ((ActiveChunkReferantBridge) this).bridge$getActiveChunk();
        // Don't tick if chunk is queued for unload or is in progress of being scheduled for unload
        // See https://github.com/SpongePowered/SpongeVanilla/issues/344
        if (chunk == null) {
            return false;
        }
        if (!chunk.isActive()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isTicking() {
        return this.impl$isTicking;
    }

    @Override
    public void setIsTicking(final boolean ticking) {
        this.impl$isTicking = ticking;
    }

    @Override
    public boolean bridge$allowsBlockBulkCapture() {
        return this.impl$allowsBlockBulkCapture;
    }

    @Override
    public boolean bridge$allowsEntityBulkCapture() {
        return this.impl$allowsEntityBulkCapture;
    }

    @Override
    public boolean bridge$allowsBlockEventCreation() {
        return this.impl$allowsBlockEventCreation;
    }

    @Override
    public boolean bridge$allowsEntityEventCreation() {
        return this.impl$allowsEntityEventCreation;
    }

    @Override
    public boolean isCaptured() {
        return this.impl$isCaptured;
    }

    @Override
    public void setCaptured(final boolean captured) {
        this.impl$isCaptured = captured;
    }

    @Override
    public void bridge$refreshTrackerStates() {
        if (((TileEntity) this).getType() != null) {
            this.impl$allowsBlockBulkCapture = ((SpongeTileEntityType) ((TileEntity) this).getType()).allowsBlockBulkCapture;
            this.impl$allowsEntityBulkCapture = ((SpongeTileEntityType) ((TileEntity) this).getType()).allowsEntityBulkCapture;
            this.impl$allowsBlockEventCreation = ((SpongeTileEntityType) ((TileEntity) this).getType()).allowsBlockEventCreation;
            this.impl$allowsEntityEventCreation = ((SpongeTileEntityType) ((TileEntity) this).getType()).allowsEntityEventCreation;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tileType", ((TileEntity) this).getType().getId())
            .add("world", this.world)
            .add("pos", this.pos)
            .add("blockMetadata", this.blockMetadata)
            .toString();
    }

    protected MoreObjects.ToStringHelper getPrettyPrinterStringHelper() {
        return MoreObjects.toStringHelper(this)
            .add("type", ((TileEntity) this).getType().getId())
            .add("world", this.world.getWorldInfo().getWorldName())
            .add("pos", this.pos);
    }

    @Override
    public String getPrettyPrinterString() {
        return getPrettyPrinterStringHelper().toString();
    }
}
