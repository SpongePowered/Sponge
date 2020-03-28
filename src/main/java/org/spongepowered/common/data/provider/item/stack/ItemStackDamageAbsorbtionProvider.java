package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

public class ItemStackDamageAbsorbtionProvider extends ItemStackDataProvider<Integer> {

    public ItemStackDamageAbsorbtionProvider() {
        super(Keys.DAMAGE_ABSORBTION);
    }

    @Override
    protected Optional<Integer> getFrom(ItemStack dataHolder) {
        if (dataHolder.getItem() instanceof ArmorItem) {
            final ArmorItem armor = (ArmorItem) dataHolder.getItem();
            final int reduction = armor.getDamageReduceAmount();
            return Optional.of(reduction);
        }
        return Optional.empty();
    }
}
