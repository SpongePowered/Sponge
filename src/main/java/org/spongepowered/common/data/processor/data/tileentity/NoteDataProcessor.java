package org.spongepowered.common.data.processor.data.tileentity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import net.minecraft.tileentity.TileEntityNote;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableNoteData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.NoteData;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeNoteData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.processor.common.NoteUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class NoteDataProcessor extends AbstractTileEntitySingleDataProcessor<TileEntityNote, NotePitch, Value<NotePitch>, NoteData, ImmutableNoteData> {

    public NoteDataProcessor() {
        super(TileEntityNote.class, Keys.NOTE_PITCH);
    }

    @Override
    protected boolean set(TileEntityNote entity, NotePitch value) {
        entity.note = NoteUtils.getPitch(checkNotNull(value));
        entity.markDirty();
        return true;
    }

    @Override
    protected Optional<NotePitch> getVal(TileEntityNote entity) {
        return Optional.of(NoteUtils.getPitch(entity.note));
    }

    @Override
    protected ImmutableValue<NotePitch> constructImmutableValue(NotePitch value) {
        return ImmutableSpongeValue.cachedOf(Keys.NOTE_PITCH, NotePitches.F_SHARP0, value);
    }

    @Override
    protected NoteData createManipulator() {
        return new SpongeNoteData();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }
}
