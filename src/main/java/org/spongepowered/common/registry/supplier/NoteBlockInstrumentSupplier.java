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
package org.spongepowered.common.registry.supplier;

import net.minecraft.state.properties.NoteBlockInstrument;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class NoteBlockInstrumentSupplier {

    private NoteBlockInstrumentSupplier() {
    }

    public static void registerSuppliers(SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(InstrumentType.class, "harp", () -> (InstrumentType) (Object) NoteBlockInstrument.HARP)
            .registerSupplier(InstrumentType.class, "basedrum", () -> (InstrumentType) (Object) NoteBlockInstrument.BASEDRUM)
            .registerSupplier(InstrumentType.class, "snare", () -> (InstrumentType) (Object) NoteBlockInstrument.SNARE)
            .registerSupplier(InstrumentType.class, "hat", () -> (InstrumentType) (Object) NoteBlockInstrument.HAT)
            .registerSupplier(InstrumentType.class, "bass", () -> (InstrumentType) (Object) NoteBlockInstrument.BASS)
            .registerSupplier(InstrumentType.class, "flute", () -> (InstrumentType) (Object) NoteBlockInstrument.FLUTE)
            .registerSupplier(InstrumentType.class, "bell", () -> (InstrumentType) (Object) NoteBlockInstrument.BELL)
            .registerSupplier(InstrumentType.class, "guitar", () -> (InstrumentType) (Object) NoteBlockInstrument.GUITAR)
            .registerSupplier(InstrumentType.class, "chime", () -> (InstrumentType) (Object) NoteBlockInstrument.CHIME)
            .registerSupplier(InstrumentType.class, "xylophone", () -> (InstrumentType) (Object) NoteBlockInstrument.XYLOPHONE)
            .registerSupplier(InstrumentType.class, "iron_xylophone", () -> (InstrumentType) (Object) NoteBlockInstrument.IRON_XYLOPHONE)
            .registerSupplier(InstrumentType.class, "cow_bell", () -> (InstrumentType) (Object) NoteBlockInstrument.COW_BELL)
            .registerSupplier(InstrumentType.class, "didgeridoo", () -> (InstrumentType) (Object) NoteBlockInstrument.DIDGERIDOO)
            .registerSupplier(InstrumentType.class, "bit", () -> (InstrumentType) (Object) NoteBlockInstrument.BIT)
            .registerSupplier(InstrumentType.class, "banjo", () -> (InstrumentType) (Object) NoteBlockInstrument.BANJO)
            .registerSupplier(InstrumentType.class, "pling", () -> (InstrumentType) (Object) NoteBlockInstrument.PLING)
        ;
    }
}
