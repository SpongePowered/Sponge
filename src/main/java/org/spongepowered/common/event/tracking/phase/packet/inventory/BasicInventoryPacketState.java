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
package org.spongepowered.common.event.tracking.phase.packet.inventory;

import net.minecraft.network.protocol.Packet;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketState;
import org.spongepowered.common.util.Constants;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class BasicInventoryPacketState extends PacketState<InventoryPacketContext> {


    /**
     * Flags we care about
     */
    final int stateId;

    /**
     * Mask for flags we care about, the caremask if you will
     */
    final int stateMask;

    /**
     * Don't care about anything
     */
    public BasicInventoryPacketState() {
        this(0, Constants.Networking.MASK_NONE);
    }

    /**
     * We care a lot
     *
     * @param stateId state
     */
    public BasicInventoryPacketState(final int stateId) {
        this(stateId, Constants.Networking.MASK_ALL);
    }

    /**
     * We care about some things
     *
     * @param stateId flags we care about
     * @param stateMask caring mask
     */
    public BasicInventoryPacketState(final int stateId, final int stateMask) {
        this.stateId = stateId & stateMask;
        this.stateMask = stateMask;
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, InventoryPacketContext> getFrameModifier() {
        return super.getFrameModifier().andThen((frame, context) -> {
            final net.minecraft.server.level.ServerPlayer player = context.getPacketPlayer();
            final net.minecraft.world.inventory.AbstractContainerMenu openContainer = player.containerMenu;
            frame.pushCause(openContainer);
            frame.pushCause(player);
        });
    }

    @Override
    public Supplier<SpawnType> getSpawnTypeForTransaction(
        final InventoryPacketContext context, final net.minecraft.world.entity.Entity entityToSpawn
    ) {
        return SpawnTypes.DROPPED_ITEM;
    }

    // Checks the proper ShouldFire flag for the event to be fired.
    // This should be the most specific known event that will be fired.
    // It's fine for this to be a supertype of the actual event - that will
    // just result in unnecessarily firing events.
    protected boolean shouldFire() {
       return ShouldFire.CLICK_CONTAINER_EVENT;
    }

    @Override
    public boolean matches(final int packetState) {
        return this.stateMask != Constants.Networking.MASK_NONE && ((packetState & this.stateMask & this.stateId) == (packetState & this.stateMask));
    }

    @Override
    public void populateContext(final net.minecraft.server.level.ServerPlayer playerMP, final Packet<?> packet, final InventoryPacketContext context) {

    }

    @Override
    public InventoryPacketContext createNewContext(final PhaseTracker tracker) {
        return new InventoryPacketContext(this, tracker);
    }

}
