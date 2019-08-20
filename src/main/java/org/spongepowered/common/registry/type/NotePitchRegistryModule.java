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
package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeNotePitch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class NotePitchRegistryModule implements CatalogRegistryModule<NotePitch> {

    // Dammit Mojang
    private static final Map<Byte, SpongeNotePitch> pitchMappings = ImmutableMap.<Byte, SpongeNotePitch>builder()
            .put((byte) 0, new SpongeNotePitch((byte) 0, "F_SHARP0"))
            .put((byte) 1, new SpongeNotePitch((byte) 1, "G0"))
            .put((byte) 2, new SpongeNotePitch((byte) 2, "G_SHARP0"))
            .put((byte) 3, new SpongeNotePitch((byte) 3, "A1"))
            .put((byte) 4, new SpongeNotePitch((byte) 4, "A_SHARP1"))
            .put((byte) 5, new SpongeNotePitch((byte) 5, "B1"))
            .put((byte) 6, new SpongeNotePitch((byte) 6, "C1"))
            .put((byte) 7, new SpongeNotePitch((byte) 7, "C_SHARP1"))
            .put((byte) 8, new SpongeNotePitch((byte) 8, "D1"))
            .put((byte) 9, new SpongeNotePitch((byte) 9, "D_SHARP1"))
            .put((byte) 10, new SpongeNotePitch((byte) 10, "E1"))
            .put((byte) 11, new SpongeNotePitch((byte) 11, "F1"))
            .put((byte) 12, new SpongeNotePitch((byte) 12, "F_SHARP1"))
            .put((byte) 13, new SpongeNotePitch((byte) 13, "G1"))
            .put((byte) 14, new SpongeNotePitch((byte) 14, "G_SHARP1"))
            .put((byte) 15, new SpongeNotePitch((byte) 15, "A2"))
            .put((byte) 16, new SpongeNotePitch((byte) 16, "A_SHARP2"))
            .put((byte) 17, new SpongeNotePitch((byte) 17, "B2"))
            .put((byte) 18, new SpongeNotePitch((byte) 18, "C2"))
            .put((byte) 19, new SpongeNotePitch((byte) 19, "C_SHARP2"))
            .put((byte) 20, new SpongeNotePitch((byte) 20, "D2"))
            .put((byte) 21, new SpongeNotePitch((byte) 21, "D_SHARP2"))
            .put((byte) 22, new SpongeNotePitch((byte) 22, "E2"))
            .put((byte) 23, new SpongeNotePitch((byte) 23, "F2"))
            .put((byte) 24, new SpongeNotePitch((byte) 24, "F_SHARP2"))
            .build();

    @RegisterCatalog(NotePitches.class)
    private final Map<String, NotePitch> notePitchMap = new HashMap<>();

    public static NotePitch getPitch(byte note) {
        note = (byte) (note % 25);
        return pitchMappings.get(note);
    }

    @Override
    public Optional<NotePitch> getById(final String id) {
        return Optional.ofNullable(this.notePitchMap.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<NotePitch> getAll() {
        return ImmutableList.copyOf(this.notePitchMap.values());
    }

    @Override
    public void registerDefaults() {
        for (final SpongeNotePitch pitch : pitchMappings.values()) {
            notePitchMap.put(pitch.getId().toLowerCase(Locale.ENGLISH), pitch);
        }
    }
}
