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
package org.spongepowered.common.event.lifecycle;

import org.spongepowered.api.Game;
import org.spongepowered.api.datapack.DataPackSerializable;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RegisterDataPackValueEventImpl<T extends DataPackSerializable> extends AbstractLifecycleEvent.GenericImpl<T> implements RegisterDataPackValueEvent<T> {

    private final DataPackType<T> type;
    private final List<T> serializables;

    public RegisterDataPackValueEventImpl(final Cause cause, final Game game, final DataPackType<T> type) {
        super(cause, game, type.type());
        this.type = type;
        this.serializables = new ArrayList<>();
    }

    @Override
    public DataPackType<T> type() {
        return this.type;
    }

    @Override
    public RegisterDataPackValueEvent<T> register(final T serializable) {
        this.serializables.add(Objects.requireNonNull(serializable, "serializable"));
        return this;
    }

    public List<T> serializables() {
        return this.serializables;
    }
}
