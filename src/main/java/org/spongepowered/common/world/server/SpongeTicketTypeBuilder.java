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
package org.spongepowered.common.world.server;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.TicketType;
import org.spongepowered.common.util.Constants;

import java.util.Objects;

public final class SpongeTicketTypeBuilder implements TicketType.Builder {

    private @MonotonicNonNull Ticks lifetime;
    private int flags;

    @Override
    public TicketType.Builder reset() {
        this.lifetime = null;
        this.flags = 0;
        return this;
    }

    private void setFlag(int flag, boolean value) {
        if (value) {
            this.flags |= flag;
        } else {
            this.flags &= ~flag;
        }
    }

    @Override
    public TicketType.Builder persists(boolean persists) {
        this.setFlag(net.minecraft.server.level.TicketType.FLAG_PERSIST, persists);
        return this;
    }

    @Override
    public TicketType.Builder loadsChunks(boolean loadsChunks) {
        this.setFlag(net.minecraft.server.level.TicketType.FLAG_LOADING, loadsChunks);
        return this;
    }

    @Override
    public TicketType.Builder simulatesChunks(boolean simulatesChunks) {
        this.setFlag(net.minecraft.server.level.TicketType.FLAG_SIMULATION, simulatesChunks);
        return this;
    }

    @Override
    public TicketType.Builder keepsWorldActive(boolean keepsWorldActive) {
        this.setFlag(net.minecraft.server.level.TicketType.FLAG_KEEP_DIMENSION_ACTIVE, keepsWorldActive);
        return this;
    }

    @Override
    public TicketType.Builder canExpireIfUnloaded(boolean canExpireIfUnloaded) {
        this.setFlag(net.minecraft.server.level.TicketType.FLAG_CAN_EXPIRE_IF_UNLOADED, canExpireIfUnloaded);
        return this;
    }

    @Override
    public TicketType.Builder lifetime(final Ticks lifetime) {
        Objects.requireNonNull(lifetime, "Lifetime cannot be null");
        if (!lifetime.isInfinite() && lifetime.ticks() <= 0) {
            throw new IllegalArgumentException("The lifetime is required to be a positive integer");
        }
        this.lifetime = lifetime;
        return this;
    }

    @Override
    public TicketType build() {
        Objects.requireNonNull(this.lifetime, "Lifetime cannot be null");
        final var timeout = this.lifetime.isInfinite() ? Constants.ChunkTicket.INFINITE_TIMEOUT : this.lifetime.ticks();
        return (TicketType) (Object) new net.minecraft.server.level.TicketType(timeout, this.flags);
    }
}
