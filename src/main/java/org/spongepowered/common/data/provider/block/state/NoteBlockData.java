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
package org.spongepowered.common.data.provider.block.state;

import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class NoteBlockData {

    private NoteBlockData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.INSTRUMENT_TYPE)
                        .get(h -> (InstrumentType) (Object) h.getValue(NoteBlock.INSTRUMENT))
                        .set((h, v) -> h.setValue(NoteBlock.INSTRUMENT, (NoteBlockInstrument) (Object) v))
                        .supports(h -> h.getBlock() instanceof NoteBlock)
                    .create(Keys.NOTE_PITCH)
                        .get(h -> {
                            final SimpleRegistry<NotePitch> registry =
                                    SpongeCommon.getRegistry().getCatalogRegistry().requireRegistry(NotePitch.class);
                            return registry.byId(h.getValue(NoteBlock.NOTE));
                        })
                        .set((h, v) -> {
                            final SimpleRegistry<NotePitch> registry =
                                    SpongeCommon.getRegistry().getCatalogRegistry().requireRegistry(NotePitch.class);
                            return h.setValue(NoteBlock.NOTE, registry.getId(v));
                        })
                        .supports(h -> h.getBlock() instanceof NoteBlock);
    }
    // @formatter:on
}
