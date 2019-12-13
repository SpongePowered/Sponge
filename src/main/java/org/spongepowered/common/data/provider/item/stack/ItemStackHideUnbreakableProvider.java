package org.spongepowered.common.data.provider.item.stack;

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.util.Constants;

public class ItemStackHideUnbreakableProvider extends ItemStackHideFlagsValueProviderBase {

    public ItemStackHideUnbreakableProvider() {
        super(Keys.HIDE_UNBREAKABLE, Constants.Item.HIDE_UNBREAKABLE_FLAG);
    }
}
