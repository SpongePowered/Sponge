package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.Optional;

public class InvisibilityTargetValueProcessor extends AbstractSpongeValueProcessor<Entity, Boolean, Value<Boolean>> {

    public InvisibilityTargetValueProcessor() {
        super(Entity.class, Keys.INVISIBILITY_PREVENTS_TARGETING);
    }

    @Override
    protected Value<Boolean> constructValue(Boolean actualValue) {
        return new SpongeValue<Boolean>(Keys.INVISIBILITY_PREVENTS_TARGETING, false, actualValue);
    }

    @Override
    protected boolean set(Entity container, Boolean value) {
        if (!container.worldObj.isRemote) {
            if (!((IMixinEntity) container).isReallyREALLYInvisible()) {
                return false;
            }
            ((IMixinEntity) container).setUntargetable(value);
            return true;
        }
        return false;
    }

    @Override
    protected Optional<Boolean> getVal(Entity container) {
        return Optional.of(((IMixinEntity) container).isUntargetable());
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return ImmutableSpongeValue.cachedOf(Keys.INVISIBILITY_PREVENTS_TARGETING, false, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
