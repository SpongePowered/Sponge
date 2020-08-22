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
package org.spongepowered.common.event.tracking.phase.packet.player;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.PrettyPrinter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public final class InteractEntityPacketState extends BasicPacketState {

    @Override
    public boolean ignoresItemPreMerging() {
        return true;
    }
    @Override
    public boolean isPacketIgnored(IPacket<?> packetIn, ServerPlayerEntity packetPlayer) {
        final CUseEntityPacket useEntityPacket = (CUseEntityPacket) packetIn;
        // There are cases where a player is interacting with an entity that doesn't exist on the server.
        @Nullable net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(packetPlayer.world);
        return entity == null;
    }

    @Override
    public void populateContext(ServerPlayerEntity playerMP, IPacket<?> packet, BasicPacketContext context) {
        final CUseEntityPacket useEntityPacket = (CUseEntityPacket) packet;
        net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(playerMP.world);
        if (entity != null) {
            final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getHeldItem(useEntityPacket.getHand()));
            if (stack != null) {
                context.itemUsed(stack);
            }
            final HandType handType = (HandType) (Object) useEntityPacket.getHand();
            context.handUsed(handType);
        }

    }

    @Override
    public boolean doesCaptureEntityDrops(BasicPacketContext context) {
        return true;
    }

    @Override
    public void unwind(BasicPacketContext phaseContext) {

        final ServerPlayerEntity player = phaseContext.getPacketPlayer();
        final CUseEntityPacket useEntityPacket = phaseContext.getPacket();
        final net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(player.world);
        if (entity == null) {
            // Something happened?
            return;
        }
        final World spongeWorld = (World) player.world;
        if (entity instanceof CreatorTrackedBridge) {
            ((CreatorTrackedBridge) entity).tracked$setCreatorReference((User) player);
        } else {
            ((Entity) entity).offer(Keys.NOTIFIER, player.getUniqueID());
        }

        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(phaseContext);
        phaseContext.getCapturedEntitySupplier()
            .acceptAndClearIfNotEmpty(entities -> {
                final PrettyPrinter printer = new PrettyPrinter(80);
                printer.add("Processing Interact Entity").centre().hr();
                printer.add("The entities captured are:");
                for (Entity capturedEntity : entities) {
                    printer.add("  Entity: %s", capturedEntity);
                }
                printer.trace(System.err);
            });
        try (CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            phaseContext.getCapturedItemsSupplier().acceptAndClearIfNotEmpty(entities -> {
                final List<Entity> items = entities.stream().map(entity1 -> (Entity) entity1).collect(Collectors.toList());
                SpongeCommonEventFactory.callSpawnEntity(items, phaseContext);
            });
        }

        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(phaseContext);
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

}
