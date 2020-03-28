package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

public class ItemStackContainerProvider extends ItemStackDataProvider<ItemType> {

    public ItemStackContainerProvider() {
        super(Keys.CONTAINER_ITEM);
    }

    @Override
    protected Optional<ItemType> getFrom(ItemStack dataHolder) {
        Item item = dataHolder.getItem();
        return Optional.ofNullable((ItemType) item.getContainerItem());
    }
}
