package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.provider.GenericMutableBoundedDataProvider;

import java.util.Optional;

public class ItemStackDurabilityProvider extends GenericMutableBoundedDataProvider<ItemStack, Integer> {

    public ItemStackDurabilityProvider() {
        super(Keys.ITEM_DURABILITY);
    }

    @Override
    protected boolean supports(ItemStack dataHolder) {
        return dataHolder.getItem().isDamageable();
    }

    @Override
    protected BoundedValue<Integer> constructValue(ItemStack dataHolder, Integer element) {
        final int maxDamage = dataHolder.getItem().getMaxDamage();
        return BoundedValue.immutableOf(this.getKey(), element, 0, maxDamage);
    }

    @Override
    protected Optional<Integer> getFrom(ItemStack dataHolder) {
        final int maxDamage = dataHolder.getItem().getMaxDamage();
        return Optional.of(maxDamage - dataHolder.getDamage());
    }

    @Override
    protected boolean set(ItemStack dataHolder, Integer value) {
        final int maxDamage = dataHolder.getItem().getMaxDamage();
        final int damage = maxDamage - value;
        if (damage > maxDamage || damage < 0) {
            return false;
        }
        dataHolder.setDamage(damage);
        return true;
    }
}
