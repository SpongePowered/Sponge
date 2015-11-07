package org.spongepowered.common.data.processor.value.tileentity;

import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public final class TileEntityLockTokenValueProcessor extends AbstractSpongeValueProcessor<TileEntityLockable, String, Value<String>> {

    protected TileEntityLockTokenValueProcessor() {
        super(TileEntityLockable.class, Keys.LOCK_TOKEN);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof TileEntityLockable) {
            set((TileEntityLockable) container, "");
            return DataTransactionBuilder.successNoData();
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected Value<String> constructValue(String actualValue) {
        return new SpongeValue<String>(Keys.LOCK_TOKEN, "", actualValue);
    }

    @Override
    protected boolean set(TileEntityLockable container, String value) {
        container.setLockCode(value.length() == 0 ? LockCode.EMPTY_CODE : new LockCode(value));
        return true;
    }

    @Override
    protected Optional<String> getVal(TileEntityLockable container) {
        return Optional.of(container.getLockCode().getLock());
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(String value) {
        return new ImmutableSpongeValue<String>(Keys.LOCK_TOKEN, "", value);
    }

}
