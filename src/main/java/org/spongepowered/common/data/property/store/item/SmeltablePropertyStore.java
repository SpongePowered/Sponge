package org.spongepowered.common.data.property.store.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import org.spongepowered.api.data.property.item.SmeltableProperty;
import org.spongepowered.common.data.property.store.common.AbstractItemStackPropertyStore;

import java.util.Optional;

public class SmeltablePropertyStore extends AbstractItemStackPropertyStore<SmeltableProperty> {

    @Override protected Optional<SmeltableProperty> getFor(ItemStack itemStack) {
        if(FurnaceRecipes.instance().getSmeltingResult(itemStack) != null) {
            return Optional.of(new SmeltableProperty(true));
        }
        return Optional.of(new SmeltableProperty(false));
    }
}
