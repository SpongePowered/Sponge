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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.api.Game;
import org.spongepowered.api.datapack.DataPackSerializable;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.common.datapack.SpongeDataPackManager;
import org.spongepowered.common.datapack.SpongeDataPackType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RegisterDataPackValueEventImpl extends AbstractLifecycleEvent implements RegisterDataPackValueEvent {

    private final Map<SpongeDataPackType, List<DataPackSerializable>> serializables;

    public RegisterDataPackValueEventImpl(final Cause cause, final Game game) {
        super(cause, game);
        this.serializables = new Object2ObjectOpenHashMap<>();
        this.serializables.computeIfAbsent((SpongeDataPackType) DataPackTypes.RECIPE, k -> new ArrayList<>());
        this.serializables.computeIfAbsent((SpongeDataPackType) DataPackTypes.ADVANCEMENT, k -> new ArrayList<>());
    }

    @Override
    public RegisterDataPackValueEvent register(final DataPackSerializable serializable) {
        Objects.requireNonNull(serializable, "serializable");
        this.serializables.computeIfAbsent((SpongeDataPackType) serializable.type(), k -> new ArrayList<>()).add(serializable);
        return this;
    }

    public Map<SpongeDataPackType, List<DataPackSerializable>> getSerializables() {
        return serializables;
    }
}
