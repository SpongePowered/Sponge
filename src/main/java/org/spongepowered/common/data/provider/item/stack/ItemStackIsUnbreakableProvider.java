package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.OptBool;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class ItemStackIsUnbreakableProvider extends GenericMutableDataProvider<ItemStack, Boolean> {

    public ItemStackIsUnbreakableProvider() {
        super(Keys.IS_UNBREAKABLE);
    }

    @Override
    protected Optional<Boolean> getFrom(ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getTag();
        if (tag == null || !tag.contains(Constants.Item.ITEM_UNBREAKABLE, Constants.NBT.TAG_BYTE)) {
            return OptBool.FALSE;
        }
        return OptBool.of(tag.getBoolean(Constants.Item.ITEM_UNBREAKABLE));
    }

    @Override
    protected boolean set(ItemStack dataHolder, Boolean value) {
        if (!value && !dataHolder.hasTag()) {
            return true;
        }
        final CompoundNBT tag = dataHolder.getOrCreateTag();
        if (value) {
            tag.putBoolean(Constants.Item.ITEM_UNBREAKABLE, true);
        } else {
            tag.remove(Constants.Item.ITEM_UNBREAKABLE);
        }
        return true;
    }

    @Override
    protected boolean removeFrom(ItemStack dataHolder) {
        return this.set(dataHolder, false);
    }
}
