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
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;

import javax.annotation.Nullable;

@Mixin(net.minecraft.tileentity.TileEntity.class)
abstract class TileEntityMixin implements TileEntityBridge, DataCompoundHolder, TimingBridge, TrackableBridge {

    //@formatter:off
    @Shadow @Final private TileEntityType<?> type;
    @Shadow @Nullable private BlockState cachedBlockState;
    @Shadow protected net.minecraft.world.World world;
    @Shadow protected BlockPos pos;

    @Shadow public abstract BlockPos getPos();
    //@formatter:on

    @Nullable private Timing impl$timing;
    private boolean impl$isTicking = false;
    // Used by tracker config
    private boolean impl$allowsBlockBulkCapture = true;
    private boolean impl$allowsEntityBulkCapture = true;
    private boolean impl$allowsBlockEventCreation = true;
    private boolean impl$allowsEntityEventCreation = true;
    private boolean impl$isCaptured = false;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void impl$RefreshTrackerStates(final CallbackInfo ci) {
        this.bridge$refreshTrackerStates();
    }



    /**
     * Read extra data (SpongeData) from the tile entity's NBT tag.
     *
     * @param compound The SpongeData compound to read from
     */
    protected void bridge$readFromSpongeCompound(final CompoundNBT compound) {
        throw new UnsupportedOperationException("Implement me");
    }

    /**
     * Write extra data (SpongeData) to the tile entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    protected void bridge$writeToSpongeCompound(final CompoundNBT compound) {
        throw new UnsupportedOperationException("Implement me");
    }


    @Override
    public Timing bridge$getTimingsHandler() {
        if (this.impl$timing == null) {
            this.impl$timing = SpongeTimings.getTileEntityTiming((BlockEntity) this);
        }
        return this.impl$timing;
    }


    @Override
    public boolean bridge$shouldTick() {
        final ChunkBridge chunk = ((ActiveChunkReferantBridge) this).bridge$getActiveChunk();
        // Don't tick if chunk is queued for unload or is in progress of being scheduled for unload
        // See https://github.com/SpongePowered/SpongeVanilla/issues/344
        if (chunk == null) {
            return false;
        }
        if (!chunk.bridge$isActive()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean bridge$allowsBlockBulkCaptures() {
        return this.impl$allowsBlockBulkCapture;
    }

    @Override
    public boolean bridge$allowsBlockEventCreation() {
        return this.impl$allowsBlockEventCreation;
    }

    @Override
    public boolean bridge$allowsEntityBulkCaptures() {
        return this.impl$allowsEntityBulkCapture;
    }


    @Override
    public boolean bridge$allowsEntityEventCreation() {
        return this.impl$allowsEntityEventCreation;
    }

    @Override
    public boolean bridge$isCaptured() {
        return this.impl$isCaptured;
    }

    @Override
    public void bridge$setCaptured(final boolean captured) {
        this.impl$isCaptured = captured;
    }

    @Override
    public void bridge$refreshTrackerStates() {
        if (((BlockEntity) this).getType() != null) {
            this.impl$allowsBlockBulkCapture = ((TrackableBridge) this.type).bridge$allowsBlockBulkCaptures();
            this.impl$allowsEntityBulkCapture = ((TrackableBridge) this.type).bridge$allowsEntityBulkCaptures();
            this.impl$allowsBlockEventCreation = ((TrackableBridge) this.type).bridge$allowsBlockEventCreation();
            this.impl$allowsEntityEventCreation = ((TrackableBridge) this.type).bridge$allowsEntityEventCreation();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                // Double check some mods are registering their tile entities and doing some "interesting"
                // things with doing a to string on a tile entity not actually functionally valid in the game "yet".
            .add("tileType", ((BlockEntityType) this.type).getKey())
            .add("world", this.world)
            .add("pos", this.pos)
            .add("blockMetadata", this.cachedBlockState)
            .toString();
    }

    protected MoreObjects.ToStringHelper getPrettyPrinterStringHelper() {
        return MoreObjects.toStringHelper(this)
            .add("type", ((BlockEntityType) this.type).getKey())
            .add("world", this.world.getWorldInfo().getWorldName())
            .add("pos", this.pos);
    }

    @Override
    public String bridge$getPrettyPrinterString() {
        return this.getPrettyPrinterStringHelper().toString();
    }
}
