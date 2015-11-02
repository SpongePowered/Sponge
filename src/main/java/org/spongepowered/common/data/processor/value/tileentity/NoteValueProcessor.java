package org.spongepowered.common.data.processor.value.tileentity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import net.minecraft.tileentity.TileEntityNote;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.NoteUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class NoteValueProcessor extends AbstractSpongeValueProcessor<TileEntityNote, NotePitch, Value<NotePitch>> {

    public NoteValueProcessor() {
        super(TileEntityNote.class, Keys.NOTE_PITCH);
    }

    @Override
    protected Value<NotePitch> constructValue(NotePitch value) {
        return new SpongeValue<>(Keys.NOTE_PITCH, NotePitches.F_SHARP0, value);
    }

    @Override
    protected boolean set(TileEntityNote container, NotePitch value) {
        container.note = NoteUtils.getPitch(checkNotNull(value));
        container.markDirty();
        return true;
    }

    @Override
    protected Optional<NotePitch> getVal(TileEntityNote container) {
        return Optional.of(NoteUtils.getPitch(container.note));
    }

    @Override
    protected ImmutableValue<NotePitch> constructImmutableValue(NotePitch value) {
        return ImmutableSpongeValue.cachedOf(Keys.NOTE_PITCH, NotePitches.F_SHARP0, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
