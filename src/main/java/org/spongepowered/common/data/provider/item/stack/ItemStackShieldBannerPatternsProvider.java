package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerPattern;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.data.util.NbtCollectors;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class ItemStackShieldBannerPatternsProvider extends GenericMutableDataProvider<ItemStack, List<PatternLayer>> {

    private static final Map<String, BannerPatternShape> SHAPE_BY_HASHNAME = new HashMap<>();

    static {
        for (final BannerPattern pattern : BannerPattern.values()) {
            SHAPE_BY_HASHNAME.put(pattern.getHashname(), (BannerPatternShape) (Object) pattern);
        }
    }

    public ItemStackShieldBannerPatternsProvider() {
        super(Keys.BANNER_PATTERNS);
    }

    @Override
    protected boolean supports(ItemStack dataHolder) {
        return dataHolder.getItem() == Items.SHIELD;
    }

    @Override
    protected Optional<List<PatternLayer>> getFrom(ItemStack dataHolder) {
        final CompoundNBT tag = dataHolder.getTag();
        if (tag == null || tag.contains(Constants.TileEntity.Banner.BANNER_PATTERNS, Constants.NBT.TAG_LIST)) {
            return Optional.of(new ArrayList<>());
        }
        final ListNBT layersList = tag.getList(Constants.TileEntity.Banner.BANNER_PATTERNS, Constants.NBT.TAG_COMPOUND);
        return Optional.of(layersList.stream()
                .map(layer -> layerFromNbt((CompoundNBT) layer))
                .collect(Collectors.toList()));
    }

    @Override
    protected boolean set(ItemStack dataHolder, List<PatternLayer> value) {
        final ListNBT layersTag = value.stream()
                .map(ItemStackShieldBannerPatternsProvider::layerToNbt)
                .collect(NbtCollectors.toList());

        final CompoundNBT blockEntity = dataHolder.getOrCreateChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        blockEntity.put(Constants.TileEntity.Banner.BANNER_PATTERNS, layersTag);
        return true;
    }

    private static PatternLayer layerFromNbt(CompoundNBT layerCompound) {
        final BannerPatternShape shape = SHAPE_BY_HASHNAME.get(
                layerCompound.getString(Constants.TileEntity.Banner.BANNER_PATTERN_ID));
        final DyeColor dyeColor = DyeColor.byId(
                layerCompound.getInt(Constants.TileEntity.Banner.BANNER_PATTERN_COLOR));
        return PatternLayer.of(shape, (org.spongepowered.api.data.type.DyeColor) (Object) dyeColor);
    }

    private static CompoundNBT layerToNbt(PatternLayer layer) {
        final CompoundNBT layerCompound = new CompoundNBT();
        layerCompound.putString(Constants.TileEntity.Banner.BANNER_PATTERN_ID,
                ((BannerPattern) (Object) layer.getShape()).getHashname());
        layerCompound.putInt(Constants.TileEntity.Banner.BANNER_PATTERN_COLOR,
                ((DyeColor) (Object) layer.getColor()).getId());
        return layerCompound;
    }
}
