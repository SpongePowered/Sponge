package org.spongepowered.common.data.manipulator.immutable.tileentity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableNoteData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.NoteData;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeNoteData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeNoteData extends AbstractImmutableSingleData<NotePitch, ImmutableNoteData, NoteData> implements ImmutableNoteData {

    private final ImmutableValue<NotePitch> cachedValue = ImmutableSpongeValue.cachedOf(Keys.NOTE_PITCH, NotePitches.F_SHARP0, this.value);

    public ImmutableSpongeNoteData() {
        this(NotePitches.F_SHARP0);
    }

    public ImmutableSpongeNoteData(NotePitch note) {
        super(ImmutableNoteData.class, note, Keys.NOTE_PITCH);
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return note();
    }

    @Override
    public ImmutableValue<NotePitch> note() {
        return this.cachedValue;
    }

    @Override
    public NoteData asMutable() {
        return new SpongeNoteData(this.value);
    }

    @Override
    public int compareTo(ImmutableNoteData o) {
        return ComparisonChain.start()
                .compare(note().get().getId(), o.note().get().getId())
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.NOTE_PITCH, getValue());
    }
}
