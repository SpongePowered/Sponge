package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ToolType;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

public class ItemStackToolTypeProvider extends ItemStackDataProvider<ToolType> {

    public ItemStackToolTypeProvider() {
        super(Keys.TOOL_TYPE);
    }

    @Override
    protected Optional<ToolType> getFrom(ItemStack dataHolder) {
        Item item = dataHolder.getItem();
        if (item instanceof TieredItem) {
            IItemTier tier = ((TieredItem) item).getTier();
            if (tier instanceof ToolType) {
                return Optional.of((ToolType) tier);
            }
        }
        return Optional.empty();
    }

    @Override
    protected boolean supports(Item item) {
        return item instanceof TieredItem;
    }
}
