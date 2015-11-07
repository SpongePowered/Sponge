package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.item.EntityMinecart;
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

public class OffsetValueProcessor extends AbstractSpongeValueProcessor<EntityMinecart, Integer, Value<Integer>> {

    public OffsetValueProcessor() {
        super(EntityMinecart.class, Keys.OFFSET);
    }

    @Override
    protected Value<Integer> constructValue(Integer value) {
        return new SpongeValue<>(Keys.OFFSET, 6, value);
    }

    @Override
    protected boolean set(EntityMinecart container, Integer value) {
        if(!container.hasDisplayTile()) return false;
        container.setDisplayTileOffset(value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(EntityMinecart container) {
        return Optional.of(container.getDisplayTileOffset());
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(Integer value) {
        return new ImmutableSpongeValue<Integer>(Keys.OFFSET, 6, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
