package org.spongepowered.common.data.provider.item.stack;

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.util.Constants;

public class ItemStackHideCanDestroyProvider extends ItemStackHideFlagsValueProviderBase {

    public ItemStackHideCanDestroyProvider() {
        super(Keys.HIDE_CAN_DESTROY, Constants.Item.HIDE_CAN_DESTROY_FLAG);
    }
}
