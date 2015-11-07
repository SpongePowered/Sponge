package org.spongepowered.common.data.processor.value;

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

public final class LockTokenValueProcessor extends AbstractSpongeValueProcessor<ILockableContainer, String, Value<String>> {

    public LockTokenValueProcessor() {
        super(ILockableContainer.class, Keys.LOCK_TOKEN);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof ILockableContainer) {
            set((ILockableContainer) container, "");
            return DataTransactionBuilder.successNoData();
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected Value<String> constructValue(String actualValue) {
        return new SpongeValue<String>(Keys.LOCK_TOKEN, "", actualValue);
    }

    @Override
    protected boolean set(ILockableContainer container, String value) {
        container.setLockCode(value.length() == 0 ? LockCode.EMPTY_CODE : new LockCode(value));
        return true;
    }

    @Override
    protected Optional<String> getVal(ILockableContainer container) {
        return Optional.of(container.getLockCode().getLock());
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(String value) {
        return new ImmutableSpongeValue<String>(Keys.LOCK_TOKEN, "", value);
    }

}
