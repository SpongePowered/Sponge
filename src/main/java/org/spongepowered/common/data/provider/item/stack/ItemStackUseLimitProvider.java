package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

public class ItemStackUseLimitProvider extends ItemStackDataProvider<Integer> {

    public ItemStackUseLimitProvider() {
        super(Keys.USE_LIMIT);
    }

    @Override
    protected Optional<Integer> getFrom(ItemStack dataHolder) {
        Item item = dataHolder.getItem();
        if (item.isDamageable()) {
            return Optional.of(item.getMaxDamage());
        }
        return Optional.empty();
    }
}
