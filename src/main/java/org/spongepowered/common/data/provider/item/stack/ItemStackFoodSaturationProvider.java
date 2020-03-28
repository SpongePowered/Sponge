package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

import javax.annotation.Nullable;

public class ItemStackFoodSaturationProvider extends ItemStackDataProvider<Double> {

    public ItemStackFoodSaturationProvider() {
        super(Keys.FOOD_SATURATION);
    }

    @Override
    protected Optional<Double> getFrom(ItemStack dataHolder) {
        if (dataHolder.getItem().isFood()) {
            @Nullable Food food = dataHolder.getItem().getFood();
            if (food != null) {
                // Translate's Minecraft's weird internal value to the actual saturation value
                final double saturation = food.getSaturation() * food.getHealing() * 2.0;
                return Optional.of(saturation);
            }
        }
        return Optional.empty();
    }
}
