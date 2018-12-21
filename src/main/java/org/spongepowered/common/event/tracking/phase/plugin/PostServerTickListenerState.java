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
package org.spongepowered.common.event.tracking.phase.plugin;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinChunk;

import javax.annotation.Nullable;

final class PostServerTickListenerState extends ListenerPhaseState {

    PostServerTickListenerState() {
    }

    @Override
    public void unwind(ListenerPhaseContext phaseContext) {

        final Object listener = phaseContext.getSource(Object.class)
            .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a ServerTickEvent listener!", phaseContext));

        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(phaseContext.getCapturedBlockSupplier(), this, phaseContext);
    }

    @Override
    public void associateNeighborBlockNotifier(ListenerPhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                                               WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        context.getCapturedPlayer().ifPresent(player ->
                ((IMixinChunk) minecraftWorld.getChunk(notifyPos))
                        .addTrackedBlockPosition(block, notifyPos, player, PlayerTracker.Type.NOTIFIER)
        );
    }

    @Override
    public void capturePlayerUsingStackToBreakBlock(@Nullable ItemStack stack, EntityPlayerMP playerMP, ListenerPhaseContext context) {
        context.getCapturedPlayerSupplier().addPlayer(playerMP);
    }


    @Override
    public boolean doesDenyChunkRequests() {
        return true;
    }


    @Override
    public boolean doesBulkBlockCapture(ListenerPhaseContext context) {
        return false;
    }

    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
    }

    @Override
    public boolean doesCaptureEntityDrops(ListenerPhaseContext context) {
        return false;
    }


}
