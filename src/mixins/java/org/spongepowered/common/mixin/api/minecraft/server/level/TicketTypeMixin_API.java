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
package org.spongepowered.common.mixin.api.minecraft.server.level;

import net.minecraft.server.level.TicketType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;

@Mixin(TicketType.class)
public abstract class TicketTypeMixin_API implements org.spongepowered.api.world.server.TicketType {

    // @formatter:off
    @Shadow @Final private long timeout;
    @Shadow @Final private int flags;
    // @formatter:on

    @Override
    public boolean persists() {
        return (this.flags & TicketType.FLAG_PERSIST) != 0;
    }

    @Override
    public boolean loadsChunks() {
        return (this.flags & TicketType.FLAG_LOADING) != 0;
    }

    @Override
    public boolean simulatesChunks() {
        return (this.flags & TicketType.FLAG_SIMULATION) != 0;
    }

    @Override
    public boolean keepsWorldActive() {
        return (this.flags & TicketType.FLAG_KEEP_DIMENSION_ACTIVE) != 0;
    }

    @Override
    public boolean canExpireIfUnloaded() {
        return (this.flags & TicketType.FLAG_CAN_EXPIRE_IF_UNLOADED) != 0;
    }

    @Override
    @NonNull
    public Ticks lifetime() {
        return this.timeout == Constants.ChunkTicket.INFINITE_TIMEOUT ? Ticks.infinite() : Ticks.of(this.timeout);
    }
}
