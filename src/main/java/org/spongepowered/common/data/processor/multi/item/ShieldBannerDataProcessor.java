package org.spongepowered.common.data.processor.multi.item;

import com.google.common.collect.ImmutableMap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityBanner;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBannerData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BannerData;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.value.mutable.PatternListValue;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBannerData;
import org.spongepowered.common.data.processor.common.AbstractItemDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.mixin.core.data.types.MixinEnumDyeColor;
import org.spongepowered.common.util.NonNullArrayList;

import java.util.Map;
import java.util.Optional;

public class ShieldBannerDataProcessor extends AbstractItemDataProcessor<BannerData, ImmutableBannerData> {

    public ShieldBannerDataProcessor() {
        super(input -> input.getItem() instanceof ItemShield);
    }

    @Override
    public boolean doesDataExist(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean set(ItemStack itemStack, Map<Key<?>, Object> keyValues) {
        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound tagCompound = itemStack.getTagCompound();
        final NBTTagCompound blockEntity = new NBTTagCompound();
        final DyeColor baseColor = (DyeColor) keyValues.get(Keys.BANNER_BASE_COLOR);
        final PatternListValue patternLayers = (PatternListValue) keyValues.get(Keys.BANNER_PATTERNS);
        if (!patternLayers.isEmpty()) {
            final NBTTagList patterns = new NBTTagList();

            for (PatternLayer layer : patternLayers) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setString(NbtDataUtil.BANNER_PATTERN_ID, ((TileEntityBanner.EnumBannerPattern) (Object) layer.getShape()).getPatternID());
                compound.setInteger(NbtDataUtil.BANNER_PATTERN_COLOR, ((EnumDyeColor) (Object) layer.getColor()).getDyeDamage());
                patterns.appendTag(compound);
            }
            blockEntity.setTag(NbtDataUtil.BANNER_PATTERNS, patterns);
        }
        blockEntity.setInteger(NbtDataUtil.BANNER_BASE, ((EnumDyeColor) (Object) baseColor).getDyeDamage());
        tagCompound.setTag(NbtDataUtil.BLOCK_ENTITY_TAG, blockEntity);
        return true;
    }

    @Override
    public Map<Key<?>, ?> getValues(ItemStack itemStack) {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey(NbtDataUtil.ITEM_UNBREAKABLE)) {
            return ImmutableMap.of(Keys.ITEM_DURABILITY, itemStack.getMaxDamage() - itemStack.getItemDamage(),
                    Keys.UNBREAKABLE, itemStack.getTagCompound().getBoolean(NbtDataUtil.ITEM_UNBREAKABLE));
        }
        return ImmutableMap.of(Keys.ITEM_DURABILITY, itemStack.getMaxDamage() - itemStack.getItemDamage(), Keys.UNBREAKABLE, false);
    }

    @Override
    public BannerData createManipulator() {
        return new SpongeBannerData();
    }

    @Override
    public Optional<BannerData> fill(DataContainer container, BannerData durabilityData) {
        final Optional<Integer> durability = container.getInt(Keys.ITEM_DURABILITY.getQuery());
        final Optional<Boolean> unbreakable = container.getBoolean(Keys.UNBREAKABLE.getQuery());
        if (durability.isPresent() && unbreakable.isPresent()) {
            durabilityData.set(Keys.ITEM_DURABILITY, durability.get());
            durabilityData.set(Keys.UNBREAKABLE, unbreakable.get());
            return Optional.of(durabilityData);
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
