package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class ItemStackDisplayNameProvider extends GenericMutableDataProvider<ItemStack, Text> {

    public ItemStackDisplayNameProvider() {
        super(Keys.DISPLAY_NAME);
    }

    @Override
    protected Optional<Text> getFrom(ItemStack dataHolder) {
        if (dataHolder.getItem() == Items.WRITTEN_BOOK) {
            @Nullable final CompoundNBT tag = dataHolder.getTag();
            if (tag != null) {
                final String title = tag.getString(Constants.Item.Book.ITEM_BOOK_TITLE);
                return Optional.of(SpongeTexts.fromLegacy(title));
            }
        }

        /*
        @Nullable final CompoundNBT display = dataHolder.getChildTag(Constants.Item.ITEM_DISPLAY);
        if (display == null || !display.contains(Constants.Item.ITEM_DISPLAY_NAME, Constants.NBT.TAG_STRING)) {
            return Optional.empty();
        }
        */

        return Optional.of(SpongeTexts.toText(dataHolder.getDisplayName()));
    }

    @Override
    protected boolean set(ItemStack dataHolder, Text value) {
        if (dataHolder.getItem() == Items.WRITTEN_BOOK) {
            final String legacy = SpongeTexts.toLegacy(value);
            dataHolder.setTagInfo(Constants.Item.Book.ITEM_BOOK_TITLE, new StringNBT(legacy));
        } else {
            dataHolder.setDisplayName(SpongeTexts.toComponent(value));
        }
        return true;
    }

    @Override
    protected boolean removeFrom(ItemStack dataHolder) {
        @Nullable final CompoundNBT display = dataHolder.getChildTag(Constants.Item.ITEM_DISPLAY);
        if (display != null) {
            display.remove(Constants.Item.ITEM_DISPLAY_NAME);
        }
        return true;
    }
}
