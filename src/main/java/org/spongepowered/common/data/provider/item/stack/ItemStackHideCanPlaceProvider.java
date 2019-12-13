package org.spongepowered.common.data.provider.item.stack;

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.util.Constants;

public class ItemStackHideCanPlaceProvider extends ItemStackHideFlagsValueProviderBase {

    public ItemStackHideCanPlaceProvider() {
        super(Keys.HIDE_CAN_PLACE, Constants.Item.HIDE_CAN_PLACE_FLAG);
    }
}
