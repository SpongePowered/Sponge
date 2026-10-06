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
package org.spongepowered.common.mixin.core.server.level;

import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.Ticket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.server.level.TicketAccessor;
import org.spongepowered.common.accessor.world.level.TicketStorageAccessor;
import org.spongepowered.common.bridge.world.DistanceManagerBridge;
import org.spongepowered.common.bridge.world.server.TicketBridge;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin implements DistanceManagerBridge {

    // @formatter:off
    @Shadow @Final TicketStorage ticketStorage;
    // @formatter:on

    @Override
    public boolean bridge$checkTicketValid(final Ticket ticket) {
        // Only report the ticket is valid if it's associated with this manager.
        final var nativeTicket = (net.minecraft.server.level.Ticket) ticket;
        final long chunkPos = ((TicketBridge) ticket).bridge$chunkPosition();
        return !nativeTicket.isTimedOut() && this.ticketStorage.getTickets(chunkPos).contains(nativeTicket);
    }

    @Override
    public Ticks bridge$timeLeft(final Ticket ticket) {
        if (this.bridge$checkTicketValid(ticket)) {
            final var mcTicket = (net.minecraft.server.level.Ticket) ticket;
            if (mcTicket.getType().timeout() == Constants.ChunkTicket.INFINITE_TIMEOUT) {
                return Ticks.infinite();
            }
            final long left = ((TicketAccessor) ticket).accessor$ticksLeft();
            return new SpongeTicks(Math.max(0, left));
        }
        return Ticks.zero();
    }

    @Override
    public boolean bridge$renewTicket(final Ticket ticket) {
        if (this.bridge$checkTicketValid(ticket)) {
            final var nativeTicket = (net.minecraft.server.level.Ticket) ticket;
            nativeTicket.resetTicksLeft();
            this.ticketStorage.setDirty();
            return true;
        }
        return false;
    }

    @Override
    public Ticket bridge$registerTicket(final org.spongepowered.api.world.server.TicketType ticketType, final Vector3i pos, final int radius) {
        final TicketType type = (TicketType) (Object) ticketType;
        final int level = Math.max(0, Constants.ChunkTicket.FULL_CHUNK_MAX_TICKET_LEVEL - Math.max(0, radius));
        final net.minecraft.server.level.Ticket ticketToRequest = new net.minecraft.server.level.Ticket(type, level);
        this.ticketStorage.addTicket(VecHelper.toChunkPos(pos).toLong(), ticketToRequest);
        return (Ticket) ((TicketBridge) ticketToRequest).bridge$retrieveAppropriateTicket();
    }

    @Override
    public boolean bridge$releaseTicket(final Ticket ticket) {
        final var nativeTicket = (net.minecraft.server.level.Ticket) ticket;
        if (!nativeTicket.isTimedOut()) {
            final long chunkPos = ((TicketBridge) ticket).bridge$chunkPosition();
            return this.ticketStorage.removeTicket(chunkPos, nativeTicket);
        }
        return false;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Collection<Ticket> bridge$tickets(final Vector3i pos) {
        return (Collection) List.copyOf(this.ticketStorage.getTickets(VecHelper.toChunkPos(pos).toLong()));
    }

    @Override
    public Collection<Ticket> bridge$tickets(final org.spongepowered.api.world.server.TicketType type) {
        return ((TicketStorageAccessor) this.ticketStorage).accessor$tickets().values()
            .stream().flatMap(Collection::stream)
            .map(Ticket.class::cast).filter(ticket -> ticket.type().equals(type))
            .collect(Collectors.toList());
    }

    @Override
    public Collection<Ticket> bridge$tickets() {
        return ((TicketStorageAccessor) this.ticketStorage).accessor$tickets().values()
            .stream().flatMap(Collection::stream)
            .map(Ticket.class::cast)
            .collect(Collectors.toList());
    }
}
