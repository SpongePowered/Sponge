package org.spongepowered.common.data.processor.dual.entity;

import net.minecraft.entity.EntityLiving;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableLeashData;
import org.spongepowered.api.data.manipulator.mutable.entity.LeashData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeLeashData;
import org.spongepowered.common.data.processor.dual.common.AbstractSingleTargetDualProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class LeashDualProcessor extends AbstractSingleTargetDualProcessor<EntityLiving, Entity, Value<Entity>, LeashData, ImmutableLeashData> {

    public LeashDualProcessor() {
        super(EntityLiving.class, Keys.LEASH_HOLDER);
    }

    @Override
    protected Value<Entity> constructValue(Entity actualValue) {
        return SpongeValueFactory.getInstance().createValue(Keys.LEASH_HOLDER, actualValue);
    }

    @Override
    protected boolean set(EntityLiving entity, Entity value) {
        entity.setLeashedToEntity((net.minecraft.entity.Entity) value, true);
        return true;
    }

    @Override
    protected Optional<Entity> getVal(EntityLiving entity) {
        return Optional.ofNullable((Entity) entity.getLeashedToEntity());
    }

    @Override
    protected ImmutableValue<Entity> constructImmutableValue(Entity value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected LeashData createManipulator() {
        return new SpongeLeashData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if(supports(container)) {
            EntityLiving living = (EntityLiving) container;
            living.setLeashedToEntity(null, true);
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }
}
