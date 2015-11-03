package org.spongepowered.common.data.manipulator.mutable.tileentity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
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
    public int compareTo(NoteData o) {
        return ComparisonChain.start()
                .compare(note().get().getId(), o.note().get().getId())
                .result();
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
        return new MemoryDataContainer().set(Keys.NOTE_PITCH, getValue());
    }
}
