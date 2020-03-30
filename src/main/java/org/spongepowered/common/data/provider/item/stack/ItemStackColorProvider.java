package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

public class ItemStackColorProvider extends ItemStackDataProvider<Color> {

    public ItemStackColorProvider() {
        super(Keys.COLOR);
    }

    @Override
    protected Optional<Color> getFrom(ItemStack dataHolder) {
        int color = ((IDyeableArmorItem) dataHolder.getItem()).getColor(dataHolder);
        return color == -1 ? Optional.empty() : Optional.of(Color.ofRgb(color));
    }

    @Override
    protected boolean set(ItemStack dataHolder, Color value) {
        IDyeableArmorItem item = (IDyeableArmorItem) dataHolder.getItem();
        if (value == null) {
            item.removeColor(dataHolder);
        } else {
            item.setColor(dataHolder, toMojangColor(value));
        }
        return true;
    }

    @Override
    protected boolean supports(Item item) {
        return item instanceof IDyeableArmorItem;
    }

    private static int toMojangColor(final Color color) {
        return (((color.getRed() << 8) + color.getGreen()) << 8) + color.getBlue();
    }
}
