package org.spongepowered.common.data.provider.item.stack;

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.util.Constants;

public class ItemStackHideAttributesProvider extends ItemStackHideFlagsValueProviderBase {

    public ItemStackHideAttributesProvider() {
        super(Keys.HIDE_ATTRIBUTES, Constants.Item.HIDE_ATTRIBUTES_FLAG);
    }
}
