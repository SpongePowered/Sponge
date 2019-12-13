package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class ItemStackShieldBannerBaseColorProvider extends GenericMutableDataProvider<ItemStack, DyeColor> {

    public ItemStackShieldBannerBaseColorProvider() {
        super(Keys.BANNER_BASE_COLOR);
    }

    @Override
    protected boolean supports(ItemStack dataHolder) {
        return dataHolder.getItem() == Items.SHIELD;
    }

    @Override
    protected Optional<DyeColor> getFrom(ItemStack dataHolder) {
        final CompoundNBT tag = dataHolder.getChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        if (tag == null || tag.contains(Constants.TileEntity.Banner.BANNER_PATTERNS, Constants.NBT.TAG_LIST)) {
            return Optional.of(DyeColors.WHITE);
        }
        final int id = tag.getInt(Constants.TileEntity.Banner.BANNER_BASE);
        return Optional.of((DyeColor) (Object) net.minecraft.item.DyeColor.byId(id));
    }

    @Override
    protected boolean set(ItemStack dataHolder, DyeColor value) {
        final CompoundNBT tag = dataHolder.getOrCreateChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        tag.putInt(Constants.TileEntity.Banner.BANNER_BASE, ((net.minecraft.item.DyeColor) (Object) value).getId());
        return true;
    }
}
