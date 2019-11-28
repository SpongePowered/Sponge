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
import org.spongepowered.common.bridge.block.BlockEventDataBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseContext;

import javax.annotation.Nullable;

/**
 * A specialized phase for forge event listeners during pre tick, may need to do the same
 * if SpongeAPI adds pre tick events.
 */
abstract class ListenerPhaseState<L extends ListenerPhaseContext<L>> extends PluginPhaseState<L> {

    @Override
    public void unwind(final L phaseContext) {

    }

    @Override
    public boolean isNotReEntrant() {
        return false;
    }

    @Override
    public boolean isEvent() {
        return true;
    }

    @Override
    public boolean tracksBlockSpecificDrops(final L context) {
        return true;
    }


    @Override
    public void appendNotifierToBlockEvent(final L context, final PhaseContext<?> currentContext,
                                           final WorldServerBridge mixinWorldServer, final BlockPos pos, final BlockEventDataBridge blockEvent) {

    }

    @Override
    public void associateNeighborStateNotifier(final L unwindingContext, @Nullable final BlockPos sourcePos, final Block block, final BlockPos notifyPos,
        final WorldServer minecraftWorld, final PlayerTracker.Type notifier) {
        unwindingContext.getCapturedPlayer().ifPresent(player ->
            ((ChunkBridge) minecraftWorld.func_175726_f(notifyPos))
                .bridge$addTrackedBlockPosition(block, notifyPos, player, PlayerTracker.Type.NOTIFIER)
        );
    }

    @Override
    public void capturePlayerUsingStackToBreakBlock(@Nullable final ItemStack stack, final EntityPlayerMP playerMP, final L context) {
        context.getCapturedPlayerSupplier().addPlayer(playerMP);
    }


}
