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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.data.nbt.CustomDataNbtUtil;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

@Mixin(net.minecraft.tileentity.TileEntity.class)
abstract class MixinTileEntity implements TileEntityBridge, DataCompoundHolder, TimingBridge {

    // uses different name to not clash with SpongeForge
    private final boolean isTileVanilla = getClass().getName().startsWith("net.minecraft.");
    @Nullable private Timing timing;
    // caches owner and notifier to avoid constant lookups in chunk
    @Nullable private User spongeOwner;
    @Nullable private User spongeNotifier;
    private boolean isTicking = false;
    private WeakReference<ChunkBridge> activeChunk = new WeakReference<>(null);
    // Used by tracker config
    private boolean allowsBlockBulkCapture = true;
    private boolean allowsEntityBulkCapture = true;
    private boolean allowsBlockEventCreation = true;
    private boolean allowsEntityEventCreation = true;
    private boolean isCaptured = false;

    @Shadow protected boolean tileEntityInvalid;
    @Shadow protected net.minecraft.world.World world;
    @Shadow private int blockMetadata;
    @Shadow protected BlockPos pos;

    @Shadow public abstract BlockPos getPos();
    @Shadow public abstract Block getBlockType();
    @Shadow public abstract NBTTagCompound writeToNBT(NBTTagCompound compound);
    @Shadow public abstract void shadow$markDirty();

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void impl$RefreshTrackerStates(final CallbackInfo ci) {
        this.refreshTrackerStates();
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
        if (!((CustomDataHolderBridge) this).getCustomManipulators().isEmpty()) {
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
        return this.isTileVanilla;
    }

    @Override
    public Timing bridge$getTimingsHandler() {
        if (this.timing == null) {
            this.timing = SpongeTimings.getTileEntityTiming((TileEntity) this);
        }
        return this.timing;
    }

    @Override
    public void setSpongeOwner(@Nullable final User owner) {
        this.spongeOwner = owner;
    }

    @Nullable
    @Override
    public User getSpongeOwner() {
        return this.spongeOwner;
    }

    @Override
    public void setSpongeNotifier(@Nullable final User notifier) {
        this.spongeNotifier = notifier;
    }

    @Nullable
    @Override
    public User getSpongeNotifier() {
        return this.spongeNotifier;
    }

    @Override
    @Nullable
    public ChunkBridge getActiveChunk() {
        return this.activeChunk.get();
    }

    @Override
    public void setActiveChunk(@Nullable final ChunkBridge chunk) {
        if (chunk == null && this.world != null && !this.world.isRemote && this.tileEntityInvalid) {
            if (this.isTicking) {
                // If a TE is currently ticking and has been invalidated, delay clearing active chunk until finished
                // This is done to avoid issues during unwind when calling bridge$getActiveChunk
                // Note: This occurs with TE's such as pistons that invalidate during movement
                return;
            }
        }
        this.activeChunk = new WeakReference<>(chunk);
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
        return this.isTicking;
    }

    @Override
    public void setIsTicking(final boolean ticking) {
        this.isTicking = ticking;
    }

    @Override
    public boolean allowsBlockBulkCapture() {
        return this.allowsBlockBulkCapture;
    }

    @Override
    public boolean allowsEntityBulkCapture() {
        return this.allowsEntityBulkCapture;
    }

    @Override
    public boolean allowsBlockEventCreation() {
        return this.allowsBlockEventCreation;
    }

    @Override
    public boolean allowsEntityEventCreation() {
        return this.allowsEntityEventCreation;
    }

    @Override
    public boolean isCaptured() {
        return this.isCaptured;
    }

    @Override
    public void setCaptured(final boolean captured) {
        this.isCaptured = captured;
    }

    @Override
    public void refreshTrackerStates() {
        if (((TileEntity) this).getType() != null) {
            this.allowsBlockBulkCapture = ((SpongeTileEntityType) ((TileEntity) this).getType()).allowsBlockBulkCapture;
            this.allowsEntityBulkCapture = ((SpongeTileEntityType) ((TileEntity) this).getType()).allowsEntityBulkCapture;
            this.allowsBlockEventCreation = ((SpongeTileEntityType) ((TileEntity) this).getType()).allowsBlockEventCreation;
            this.allowsEntityEventCreation = ((SpongeTileEntityType) ((TileEntity) this).getType()).allowsEntityEventCreation;
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
