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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinChunk;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

final class PostWorldTickListenerState extends ListenerPhaseState {

    PostWorldTickListenerState() {
    }

    @Override
    public void unwind(ListenerPhaseContext phaseContext) {
        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(this, phaseContext);
        // This could be happening regardless whether block bulk captures are done or not.
        // Would depend on whether entity captures are done.
        phaseContext.getBlockItemDropSupplier()
            .acceptAndClearIfNotEmpty(map -> {
                map.asMap().entrySet().forEach(entry -> {
                    final BlockPos key = entry.getKey();
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                        final List<Entity> items = entry.getValue().stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                        SpongeCommonEventFactory.callDropItemDestruct(items, phaseContext);

                        frame.popCause();
                    }

                });

            });
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
}
