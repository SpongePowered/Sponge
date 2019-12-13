package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.OptBool;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public abstract class ItemStackHideFlagsValueProviderBase extends GenericMutableDataProvider<ItemStack, Boolean> {

    private final int flag;

    ItemStackHideFlagsValueProviderBase(Key<? extends Value<Boolean>> key, int flag) {
        super(key);
        this.flag = flag;
    }

    @Override
    protected Optional<Boolean> getFrom(ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getTag();
        if (tag != null && tag.contains(Constants.Item.ITEM_HIDE_FLAGS, Constants.NBT.TAG_INT)) {
            int flag = tag.getInt(Constants.Item.ITEM_HIDE_FLAGS);
            if ((flag & this.flag) != 0) {
                return OptBool.TRUE;
            }
        }
        return OptBool.FALSE;
    }

    @Override
    protected boolean set(ItemStack dataHolder, Boolean value) {
        final CompoundNBT tag = dataHolder.getOrCreateTag();
        if (tag.contains(Constants.Item.ITEM_HIDE_FLAGS, Constants.NBT.TAG_INT)) {
            final int flag = tag.getInt(Constants.Item.ITEM_HIDE_FLAGS);
            if (value) {
                tag.putInt(Constants.Item.ITEM_HIDE_FLAGS, flag | this.flag);
            } else {
                final int flags = flag & ~this.flag;
                if (flags == 0) {
                    tag.remove(Constants.Item.ITEM_HIDE_FLAGS);
                } else {
                    tag.putInt(Constants.Item.ITEM_HIDE_FLAGS, flags);
                }
            }
        } else if (value) {
            tag.putInt(Constants.Item.ITEM_HIDE_FLAGS, this.flag);
        }
        return true;
    }
}
