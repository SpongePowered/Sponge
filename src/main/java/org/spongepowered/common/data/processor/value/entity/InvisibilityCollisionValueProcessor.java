package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class InvisibilityCollisionValueProcessor extends AbstractSpongeValueProcessor<Entity, Boolean, Value<Boolean>> {

    public InvisibilityCollisionValueProcessor() {
        super(Entity.class, Keys.INVISIBILITY_IGNORES_COLLISION);
    }

    @Override
    protected Value<Boolean> constructValue(Boolean actualValue) {
        return new SpongeValue<Boolean>(Keys.INVISIBILITY_IGNORES_COLLISION, false, actualValue);
    }

    @Override
    protected boolean set(Entity container, Boolean value) {
        if (!container.worldObj.isRemote) {
            if (!((IMixinEntity) container).isReallyREALLYInvisible()) {
                return false;
            }
            ((IMixinEntity) container).setIgnoresCollision(value);
            return true;
        }
        return false;
    }

    @Override
    protected Optional<Boolean> getVal(Entity container) {
        return Optional.of(((IMixinEntity) container).ignoresCollision());
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return ImmutableSpongeValue.cachedOf(Keys.INVISIBILITY_IGNORES_COLLISION, false, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
