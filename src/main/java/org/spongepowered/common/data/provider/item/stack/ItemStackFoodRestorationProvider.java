package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

import javax.annotation.Nullable;

public class ItemStackFoodRestorationProvider extends ItemStackDataProvider<Integer> {

    public ItemStackFoodRestorationProvider() {
        super(Keys.FOOD_RESTORATION);
    }

    @Override
    protected Optional<Integer> getFrom(ItemStack dataHolder) {
        if (dataHolder.getItem().isFood()) {
            @Nullable Food food = dataHolder.getItem().getFood();
            if (food != null) {
                return Optional.of(food.getHealing());
            }
        }
        return Optional.empty();
    }

    @Override
    protected boolean supports(Item item) {
        return item.isFood();
    }
}
