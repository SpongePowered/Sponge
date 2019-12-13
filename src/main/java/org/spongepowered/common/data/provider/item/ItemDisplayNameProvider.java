package org.spongepowered.common.data.provider.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;

public class ItemDisplayNameProvider extends GenericMutableDataProvider<Item, Text> {

    public ItemDisplayNameProvider() {
        super(Keys.DISPLAY_NAME);
    }

    @Override
    protected Optional<Text> getFrom(Item dataHolder) {
        final ITextComponent displayName = dataHolder.getDisplayName(new ItemStack(dataHolder));
        return Optional.of(SpongeTexts.toText(displayName));
    }

    @Override
    protected boolean set(Item dataHolder, Text value) {
        return false;
    }
}
