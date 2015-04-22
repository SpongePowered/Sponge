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
package org.spongepowered.mod.data.manipulators.tiles;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import org.spongepowered.api.data.AbstractDataManipulator;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.tileentities.NoteData;
import org.spongepowered.api.data.types.NotePitch;
import org.spongepowered.api.data.types.NotePitches;

public class SpongeNoteData extends AbstractDataManipulator<NoteData> implements NoteData {

    private NotePitch pitch = NotePitches.F_SHARP0;

    @Override
    public NotePitch getValue() {
        return this.pitch;
    }

    @Override
    public void setValue(NotePitch value) {
        this.pitch = checkNotNull(value);
    }

    @Override
    public Optional<NoteData> fill(DataHolder dataHolder) {
        return null;
    }

    @Override
    public Optional<NoteData> fill(DataHolder dataHolder, DataPriority overlap) {
        return null;
    }

    @Override
    public Optional<NoteData> from(DataContainer container) {
        return null;
    }

    @Override
    public int compareTo(NoteData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("Pitch"), this.pitch.getId());
        return container;
    }

    @Override
    public NotePitch getNote() {
        return getValue();
    }

    @Override
    public void setNote(NotePitch note) {
        setValue(note);
    }

}
