package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

public class ItemStackDyeColorProvider extends ItemStackDataProvider<DyeColor> {

    public ItemStackDyeColorProvider() {
        super(Keys.DYE_COLOR);
    }

    @Override
    protected Optional<DyeColor> getFrom(ItemStack dataHolder) {
        // TODO manual
    }

    @Override
    protected boolean supports(Item item) {
        // TODO manual
    }
}
