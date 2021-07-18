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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.entity.player.SpongeUserData;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;

public final class IdentifiableData {

    private IdentifiableData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Identifiable.class)
                    .create(Keys.FIRST_DATE_JOINED)
                        .get(h -> ((SpongeServer) Sponge.server()).getPlayerDataManager().getFirstJoined(h.uniqueId()).orElse(null))
                        .set((h, v) -> {
                            final UUID id = h.uniqueId();
                            final Instant played = ((SpongeServer) Sponge.server()).getPlayerDataManager().getFirstJoined(id).orElse(v);
                            ((SpongeServer) Sponge.server()).getPlayerDataManager().setPlayerInfo(id, played, v);
                        })
                        .supports(h -> h instanceof Player || h instanceof SpongeUserData)
                    .create(Keys.LAST_DATE_PLAYED)
                        .get(h -> ((SpongeServer) Sponge.server()).getPlayerDataManager().getLastPlayed(h.uniqueId()).orElse(null))
                        .set((h, v) -> {
                            final UUID id = h.uniqueId();
                            final Instant played = ((SpongeServer) Sponge.server()).getPlayerDataManager().getFirstJoined(id).orElse(v);
                            ((SpongeServer) Sponge.server()).getPlayerDataManager().setPlayerInfo(id, played, v);
                        })
                        .supports(h -> h instanceof Player || h instanceof SpongeUserData);
    }
    // @formatter:on
}
