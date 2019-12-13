package org.spongepowered.common.data.provider.item.stack;

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.util.Constants;

public class ItemStackHideMiscellaneousProvider extends ItemStackHideFlagsValueProviderBase {

    public ItemStackHideMiscellaneousProvider() {
        super(Keys.HIDE_MISCELLANEOUS, Constants.Item.HIDE_MISCELLANEOUS_FLAG);
    }
}
