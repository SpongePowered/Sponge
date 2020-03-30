package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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

    /**
     * Maybe try mixin injection in {@link Block.Properties#create(Material, net.minecraft.item.DyeColor)}
     * to capture DyeColor.
     */
    @Override
    protected Optional<DyeColor> getFrom(ItemStack dataHolder) {
        return ((DyeColorItemBrige)dataHolder).bridge$getDyeColor();
    }

    @Override
    protected boolean supports(Item item) {
        return item instanceof DyeColorItemBridge;
    }
}
