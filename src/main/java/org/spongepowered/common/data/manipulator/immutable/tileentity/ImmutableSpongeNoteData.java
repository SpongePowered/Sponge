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
package org.spongepowered.common.data.manipulator.immutable.tileentity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableNoteData;
import org.spongepowered.api.data.manipulator.mutable.block.NoteData;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeNoteData;
import org.spongepowered.common.data.value.SpongeImmutableValue;

public class ImmutableSpongeNoteData extends AbstractImmutableSingleData<NotePitch, ImmutableNoteData, NoteData> implements ImmutableNoteData {

    private final Value.Immutable<NotePitch> cachedValue = SpongeImmutableValue.cachedOf(Keys.NOTE_PITCH, this.value);

    public ImmutableSpongeNoteData() {
        this(NotePitches.F_SHARP0);
    }

    public ImmutableSpongeNoteData(NotePitch note) {
        super(ImmutableNoteData.class, note, Keys.NOTE_PITCH);
    }

    @Override
    protected Value.Immutable<?> getValueGetter() {
        return note();
    }

    @Override
    public Value.Immutable<NotePitch> note() {
        return this.cachedValue;
    }

    @Override
    public NoteData asMutable() {
        return new SpongeNoteData(this.value);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer().set(Keys.NOTE_PITCH, getValue());
    }
}
