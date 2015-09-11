package org.spongepowered.common.data.processor.value.item;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;

public class ItemLoreValueProcessor extends AbstractSpongeValueProcessor<List<Text>, ListValue<Text>> {

    public ItemLoreValueProcessor() {
        super(Keys.ITEM_LORE);
    }

    @Override
    protected ListValue<Text> constructValue(List<Text> defaultValue) {
        return new SpongeListValue<Text>(Keys.ITEM_LORE, defaultValue);
    }

    @Override
    public Optional<List<Text>> getValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            final ItemStack itemStack = (ItemStack) container;
            final NBTTagCompound subCompound = itemStack.getSubCompound(NbtDataUtil.DISPLAY, false);
            if (subCompound == null) {
                return Optional.absent();
            }
            if (!subCompound.hasKey(NbtDataUtil.LORE, NbtDataUtil.TAG_LIST)) {
                return Optional.absent();
            }
            return Optional.of(NbtDataUtil.getLoreFromNBT(subCompound));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof ItemStack;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, List<Text> value) {
        final ImmutableListValue<Text> lore = new ImmutableSpongeListValue<Text>(Keys.ITEM_LORE, ImmutableList.copyOf(value));
        if (this.supports(container)) {
            final Optional<List<Text>> oldData = getValueFromContainer(container);
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            if (oldData.isPresent()) {
                final ImmutableListValue<Text> oldLore =
                        new ImmutableSpongeListValue<Text>(Keys.ITEM_LORE, ImmutableList.copyOf(oldData.get()));
                builder.replace(oldLore);
            }
            NbtDataUtil.setLoreToNBT((ItemStack) container, lore.get());
            builder.success(lore);
            return builder.result(DataTransactionResult.Type.SUCCESS).build();

        }
        return DataTransactionBuilder.failResult(lore);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<List<Text>> oldData = getValueFromContainer(container);
            if (oldData.isPresent()) {
                final ImmutableListValue<Text> lore = new ImmutableSpongeListValue<Text>(Keys.ITEM_LORE, ImmutableList.copyOf(oldData.get()));
                builder.replace(lore);
            }
            NbtDataUtil.removeLoreFromNBT((ItemStack) container);
            return builder.result(DataTransactionResult.Type.SUCCESS).build();

        }
        return DataTransactionBuilder.failNoData();
    }
}
