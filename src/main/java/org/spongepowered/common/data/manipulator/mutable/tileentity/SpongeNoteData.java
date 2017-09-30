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
package org.spongepowered.common.data.manipulator.mutable.tileentity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableNoteData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.NoteData;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeNoteData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeNoteData extends AbstractSingleData<NotePitch, NoteData, ImmutableNoteData> implements NoteData {

    public SpongeNoteData() {
        this(NotePitches.F_SHARP0);
    }

    public SpongeNoteData(NotePitch note) {
        super(NoteData.class, note, Keys.NOTE_PITCH);
    }

    @Override
    protected Value<?> getValueGetter() {
        return note();
    }

    @Override
    public ImmutableNoteData asImmutable() {
        return new ImmutableSpongeNoteData(this.getValue());
    }

    @Override
    public Value<NotePitch> note() {
        return new SpongeValue<>(Keys.NOTE_PITCH, NotePitches.F_SHARP0, getValue());
    }

    @Override
    public NoteData copy() {
        return new SpongeNoteData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.NOTE_PITCH.getQuery(), getValue().getId());
    }
}
