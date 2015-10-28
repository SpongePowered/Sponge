package org.spongepowered.common.data.processor.value.item;

import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueBuilder;

import java.util.Optional;

public class SpawnableEntityTypeValueProcessor extends AbstractSpongeValueProcessor<ItemStack, EntityType, Value<EntityType>> {

    public SpawnableEntityTypeValueProcessor() {
        super(ItemStack.class, Keys.SPAWNABLE_ENTITY_TYPE);
    }

    @Override
    public boolean supports(ItemStack container) {
        return container.getItem().equals(Items.spawn_egg);
    }

    @Override
    public Value<EntityType> constructValue(EntityType defaultValue) {
        return new SpongeValueBuilder().createValue(Keys.SPAWNABLE_ENTITY_TYPE, defaultValue, EntityTypes.CREEPER);
    }

    @Override
    public boolean set(ItemStack container, EntityType value) {
        final String name = (String) EntityList.classToStringMapping.get(value.getEntityClass());
        final int id = (int) EntityList.stringToIDMapping.get(name);
        container.setItemDamage(id);
        return true;
    }

    @Override
    public Optional<EntityType> getVal(ItemStack container) {
        final Class entity = EntityList.getClassFromID(container.getItemDamage());
        for (EntityType type : Sponge.getSpongeRegistry().getAllOf(EntityType.class)) {
            if (type.getEntityClass().equals(entity)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    @Override
    public ImmutableValue<EntityType> constructImmutableValue(EntityType value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
            ((ItemStack) container).setItemDamage(0);
            return DataTransactionBuilder.successNoData();
        }
        return DataTransactionBuilder.failNoData();
    }
}
