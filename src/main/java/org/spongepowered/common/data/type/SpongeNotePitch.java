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
package org.spongepowered.common.data.type;

import net.minecraft.core.MappedRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.registry.RegistryTypes;

public final class SpongeNotePitch implements NotePitch {

    @Override
    public NotePitch cycleNext() {
        final MappedRegistry<NotePitch> registry = (MappedRegistry<NotePitch>) (Object) Sponge.game().registry(RegistryTypes.NOTE_PITCH);
        final int value = registry.getId(this) + 1;
        NotePitch next = registry.byId(value);
        if (next == null) {
            next = NotePitches.A1.get();
        }
        return next;
    }
}
