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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.entity.player.SpongeUserData;

import java.time.Instant;

public final class IdentifiableData {

    private IdentifiableData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Identifiable.class)
                    .create(Keys.FIRST_DATE_JOINED)
                        .get(h -> ((SpongeDataHolderBridge) h).bridge$get(Keys.FIRST_DATE_JOINED).orElse(null))
                        .set((h, v) -> ((SpongeDataHolderBridge) h).bridge$offer(Keys.FIRST_DATE_JOINED, v))
                        .supports(h -> h instanceof ServerPlayer || h instanceof SpongeUserData)
                    .create(Keys.LAST_DATE_PLAYED)
                        .get(h -> ((SpongeDataHolderBridge) h).bridge$get(Keys.LAST_DATE_PLAYED).orElse(null))
                        .set((h, v) -> ((SpongeDataHolderBridge) h).bridge$offer(Keys.LAST_DATE_PLAYED, v))
                        .supports(h -> h instanceof ServerPlayer || h instanceof SpongeUserData);

        IdentifiableData.registerSpongeDataStore(registrator, Keys.FIRST_DATE_JOINED);
        IdentifiableData.registerSpongeDataStore(registrator, Keys.LAST_DATE_PLAYED);
    }
    // @formatter:on

    private static void registerSpongeDataStore(final DataProviderRegistrator registrator, final Key<Value<Instant>> key) {
        registrator.spongeDataStore(key.key(), 1, Identifiable.class, builder -> {
            final DataQuery query = DataQuery.of(key.key().value());
            builder.key(key, (view, value) -> view.set(query, value.toEpochMilli()), view -> view.getLong(query).map(Instant::ofEpochMilli));
        }, key);
    }
}
