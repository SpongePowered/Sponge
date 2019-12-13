package org.spongepowered.common.data.provider.item.stack;

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.util.Constants;

public class ItemStackHideEnchantmentsProvider extends ItemStackHideFlagsValueProviderBase {

    public ItemStackHideEnchantmentsProvider() {
        super(Keys.HIDE_ENCHANTMENTS, Constants.Item.HIDE_ENCHANTMENTS_FLAG);
    }
}
