/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.processor.data;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.INameable;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.mutable.SpongeDisplayNameData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;

// TODO Improve this processor
public class DisplayNameDataProcessor extends AbstractSingleDataProcessor<Text, Value<Text>, DisplayNameData, ImmutableDisplayNameData> {

    public DisplayNameDataProcessor() {
        super(Keys.DISPLAY_NAME);
    }

    @Override
    protected DisplayNameData createManipulator() {
        return new SpongeDisplayNameData();
    }

    @Override
    public boolean supports(final DataHolder holder) {
        return holder instanceof Entity || holder instanceof ItemStack || holder instanceof INameable;
    }

    @Override
    public Optional<DisplayNameData> from(final DataHolder holder) {
        if (holder instanceof Entity) {
            @Nullable final Text displayName = ((EntityBridge) holder).bridge$getDisplayNameText();
            if (displayName != null) {
                return Optional.of(new SpongeDisplayNameData(displayName));
            }
            return Optional.empty();
        } else if (holder instanceof ItemStack) {
            final ItemStack stack = (ItemStack) holder;
            if (!stack.hasDisplayName()) {
                return Optional.empty();
            }

            if (stack.getItem() == Items.WRITTEN_BOOK) {
                final CompoundNBT compound = stack.getTag();
                if (compound == null) {
                    return Optional.empty(); // The book wasn't initialized.
                }

                return Optional.of(new SpongeDisplayNameData(SpongeTexts.fromLegacy(compound.getString(Constants.Item.Book.ITEM_BOOK_TITLE))));
            }

            final CompoundNBT compound = ((ItemStack) holder).getChildTag(Constants.Item.ITEM_DISPLAY);
            if (compound != null && compound.contains(Constants.Item.ITEM_DISPLAY_NAME, Constants.NBT.TAG_STRING)) {
                return Optional.of(new SpongeDisplayNameData(SpongeTexts.fromLegacy(compound.getString(Constants.Item.ITEM_DISPLAY_NAME))));
            }
            return Optional.empty();
        } else if (holder instanceof INameable) {
            if (((INameable) holder).hasCustomName()) {
                final String customName = ((INameable) holder).func_70005_c_();
                final DisplayNameData data = new SpongeDisplayNameData(SpongeTexts.fromLegacy(customName));
                return Optional.of(data);
            }
            return Optional.empty();
        }

        return Optional.empty();
    }

    @Override
    public Optional<DisplayNameData> fill(final DataContainer container, final DisplayNameData displayNameData) {
        final String json = DataUtil.getData(container, Keys.DISPLAY_NAME, String.class);
        return Optional.of(displayNameData.set(Keys.DISPLAY_NAME, TextSerializers.JSON.deserialize(json)));
    }

    @Override
    public DataTransactionResult set(final DataHolder holder, final DisplayNameData manipulator, final MergeFunction function) {
        if (holder instanceof EntityBridge) {
            if (holder instanceof Player) {
                return DataTransactionResult.failResult(manipulator.getValues());
            }
        } else if (!(holder instanceof ItemStack)) {
            return DataTransactionResult.failResult(manipulator.getValues());
        }
        final Optional<DisplayNameData> old = from(holder);
        final DisplayNameData merged = checkNotNull(function).merge(old.orElse(null), manipulator);
        final Text newValue = merged.displayName().get();
        final ImmutableValue<Text> immutableValue = merged.displayName().asImmutable();

        try {
            if (holder instanceof EntityBridge) {
                ((EntityBridge) holder).bridge$setDisplayName(newValue);
            } else {
                final ItemStack stack = (ItemStack) holder;
                if (stack.getItem() == Items.WRITTEN_BOOK) {
                    stack.setTagInfo(Constants.Item.Book.ITEM_BOOK_TITLE, new StringNBT(SpongeTexts.toLegacy(newValue)));
                } else {
                    stack.func_151001_c(SpongeTexts.toLegacy(newValue));
                }
            }
        } catch (final Exception e) {
            SpongeImpl.getLogger().debug("An exception occurred when setting data: ", e);
            return DataTransactionResult.errorResult(immutableValue);
        }
        return old
            .map(displayNameData -> DataTransactionResult.successReplaceResult(displayNameData.displayName().asImmutable(), immutableValue))
            .orElseGet(() -> DataTransactionResult.successResult(immutableValue));
    }

    @Override
    public Optional<ImmutableDisplayNameData> with(final Key<? extends BaseValue<?>> key, final Object value, final ImmutableDisplayNameData immutable) {
        if (key == this.key) {
            return Optional.of(new ImmutableSpongeDisplayNameData((Text) value));
        }

        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(final DataHolder holder) {
        if (holder instanceof Entity) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<DisplayNameData> optional = this.from(holder);
            if (optional.isPresent()) {
                try {
                    ((EntityBridge) holder).bridge$setDisplayName(null);
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (final Exception e) {
                    SpongeImpl.getLogger().error("There was an issue resetting the display name from an Entity!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            }
            return builder.result(DataTransactionResult.Type.SUCCESS).build();
        }
        if (holder instanceof ItemStack) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<DisplayNameData> optional = this.from(holder);
            if (optional.isPresent()) {
                try {
                    ((ItemStack) holder).clearCustomName();
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (final Exception e) {
                    SpongeImpl.getLogger().error("There was an issue removing the display name from an ItemStack!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            }
            return builder.result(DataTransactionResult.Type.SUCCESS).build();
        }

        return DataTransactionResult.failNoData();
    }

    @Override
    public boolean supports(final EntityType type) {
        return Entity.class.isAssignableFrom(type.getEntityClass());
    }

}
