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

import net.minecraft.server.level.TicketType;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.common.bridge.world.server.TicketTypeBridge;
import org.spongepowered.common.util.SpongeTicks;

import java.util.Comparator;
import java.util.function.Function;

// For use with our own ticket types.
public final class SpongeTicketType<T> extends TicketType<T> implements org.spongepowered.api.world.server.TicketType<T> {

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public SpongeTicketType(final String param0, final Comparator<T> param1, final long param2) {
        super(param0, param1, param2);
        // base class was mixed in to, so we know we can do this.
        ((TicketTypeBridge<T, T>) (Object) this).bridge$setTypeConverter(Function.identity());
    }

    @Override
    public Ticks lifetime() {
        return new SpongeTicks(this.timeout());
    }

}
