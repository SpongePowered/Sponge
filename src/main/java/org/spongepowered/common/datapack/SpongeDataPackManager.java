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
package org.spongepowered.common.datapack;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.api.Game;
import org.spongepowered.api.datapack.DataPackSerializable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.common.event.lifecycle.RegisterDataPackValueEventImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class SpongeDataPackManager {

    public static SpongeDataPackManager INSTANCE;
    private final Game game;
    private final Map<SpongeDataPackType, List<DataPackSerializable>> serializables;

    @Inject
    public SpongeDataPackManager(final Game game) {
        SpongeDataPackManager.INSTANCE = this;
        this.game = game;
        this.serializables = new Object2ObjectOpenHashMap<>();
    }

    public void callRegisterDataPackValueEvent() {
        final RegisterDataPackValueEventImpl event =
                new RegisterDataPackValueEventImpl(Cause.of(EventContext.empty(), this.game), this.game);
        this.game.getEventManager().post(event);

        this.serializables.putAll(event.getSerializables());
    }

    @SuppressWarnings("unchecked")
    public void serialize(final Path dataPacksDirectory) throws IOException {
        for (final Map.Entry<SpongeDataPackType, List<DataPackSerializable>> entry : this.serializables.entrySet()) {
            final SpongeDataPackType key = entry.getKey();
            final List<DataPackSerializable> value = entry.getValue();

            final List<DataPackSerializedObject> serialized = new ArrayList<>();

            for (final DataPackSerializable serializable : value) {
                final JsonObject o = key.getObjectSerializer().serialize(serializable);
                serialized.add((DataPackSerializedObject) key.getObjectFunction().apply(serializable, o));
            }

            if (!serialized.isEmpty()) {
                key.getPackSerializer().serialize(dataPacksDirectory, serialized);
            }
        }
        this.serializables.clear();
    }
}
