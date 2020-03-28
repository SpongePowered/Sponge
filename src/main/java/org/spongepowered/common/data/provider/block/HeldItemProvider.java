package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class HeldItemProvider extends GenericMutableDataProvider<BlockState, ItemType> {

    public HeldItemProvider() {
        super(Keys.HELD_ITEM);
    }

    @Override
    protected Optional<ItemType> getFrom(BlockState dataHolder) {
        Item item = dataHolder.getBlock().asItem();
        if (item instanceof BlockItem) {
            return Optional.of((ItemType) item);
        }
        return Optional.empty();
    }

    @Override
    protected boolean supports(BlockState dataHolder) {
        return dataHolder.getBlock().asItem() instanceof BlockItem;
    }
}
