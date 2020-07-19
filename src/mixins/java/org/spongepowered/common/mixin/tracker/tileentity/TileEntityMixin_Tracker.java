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
package org.spongepowered.common.mixin.tracker.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.tileentity.TrackableTileEntityBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;

@Mixin(TileEntity.class)
public class TileEntityMixin_Tracker implements TrackableBridge, TrackableTileEntityBridge {

    // @formatter:off
    @Shadow @Final private TileEntityType<?> type;
    // @formatter:on

    // Used by tracker config
    private boolean tracker$allowsBlockBulkCapture = true;
    private boolean tracker$allowsEntityBulkCapture = true;
    private boolean tracker$allowsBlockEventCreation = true;
    private boolean tracker$allowsEntityEventCreation = true;
    private boolean tracker$isCaptured = false;

    @Override
    public boolean bridge$isCaptured() {
        return this.tracker$isCaptured;
    }

    @Override
    public void bridge$setCaptured(final boolean captured) {
        this.tracker$isCaptured = captured;
    }

    @Override
    public boolean bridge$isWorldTracked() {
        return false;
    }

    @Override
    public void bridge$setWorldTracked(final boolean tracked) {

    }

    @Override
    public boolean bridge$shouldTick() {
//        final ChunkBridge chunk = ((ActiveChunkReferantBridge) this).bridge$getActiveChunk();
//        // Don't tick if chunk is queued for unload or is in progress of being scheduled for unload
//        // See https://github.com/SpongePowered/SpongeVanilla/issues/344
//        if (chunk == null) {
//            return false;
//        }
//        return chunk.bridge$isActive();
        return true;
    }


    @Override
    public void bridge$setAllowsBlockBulkCaptures(final boolean allowsBlockBulkCaptures) {

    }

    @Override
    public void bridge$setAllowsBlockEventCreation(final boolean allowsBlockEventCreation) {

    }
    @Override
    public boolean bridge$allowsBlockBulkCaptures() {
        return this.tracker$allowsBlockBulkCapture;
    }

    @Override
    public boolean bridge$allowsBlockEventCreation() {
        return this.tracker$allowsBlockEventCreation;
    }

    @Override
    public boolean bridge$allowsEntityBulkCaptures() {
        return this.tracker$allowsEntityBulkCapture;
    }


    @Override
    public void bridge$setAllowsEntityBulkCaptures(final boolean allowsEntityBulkCaptures) {

    }

    @Override
    public boolean bridge$allowsEntityEventCreation() {
        return this.tracker$allowsEntityEventCreation;
    }

    @Override
    public void bridge$setAllowsEntityEventCreation(final boolean allowsEntityEventCreation) {

    }

    @Override
    public void bridge$refreshTrackerStates() {
        if (this.type instanceof TrackableBridge) {
            this.tracker$allowsBlockBulkCapture = ((TrackableBridge) this.type).bridge$allowsBlockBulkCaptures();
            this.tracker$allowsEntityBulkCapture = ((TrackableBridge) this.type).bridge$allowsEntityBulkCaptures();
            this.tracker$allowsBlockEventCreation = ((TrackableBridge) this.type).bridge$allowsBlockEventCreation();
            this.tracker$allowsEntityEventCreation = ((TrackableBridge) this.type).bridge$allowsEntityEventCreation();
        }
    }
}
