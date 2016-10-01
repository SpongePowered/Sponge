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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IWorldNameable;
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
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.text.SpongeTexts;

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
    public boolean supports(DataHolder holder) {
        return holder instanceof Entity || holder instanceof ItemStack || holder instanceof IWorldNameable;
    }

    @Override
    public Optional<DisplayNameData> from(DataHolder holder) {
        if (holder instanceof Entity) {
            @Nullable Text displayName = ((IMixinEntity) holder).getDisplayNameText();
            if (displayName != null) {
                return Optional.of(new SpongeDisplayNameData(displayName));
            } else {
                return Optional.empty();
            }
        } else if (holder instanceof ItemStack) {
            ItemStack stack = (ItemStack) holder;
            if (!stack.hasDisplayName()) {
                return Optional.empty();
            }

            if (stack.getItem() == Items.WRITTEN_BOOK) {
                final NBTTagCompound compound = stack.getTagCompound();
                if (compound == null) {
                    return Optional.empty(); // The book wasn't initialized.
                }

                return Optional.of(new SpongeDisplayNameData(SpongeTexts.fromLegacy(compound.getString(NbtDataUtil.ITEM_BOOK_TITLE))));
            }

            final NBTTagCompound compound = ((ItemStack) holder).getSubCompound(NbtDataUtil.ITEM_DISPLAY, false);
            if (compound != null && compound.hasKey(NbtDataUtil.ITEM_DISPLAY_NAME, NbtDataUtil.TAG_STRING)) {
                return Optional.of(new SpongeDisplayNameData(SpongeTexts.fromLegacy(compound.getString(NbtDataUtil.ITEM_DISPLAY_NAME))));
            } else {
                return Optional.empty();
            }
        } else if (holder instanceof IWorldNameable) {
            if (((IWorldNameable) holder).hasCustomName()) {
                final String customName = ((IWorldNameable) holder).getName();
                final DisplayNameData data = new SpongeDisplayNameData(SpongeTexts.fromLegacy(customName));
                return Optional.of(data);
            } else {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<DisplayNameData> fill(DataContainer container, DisplayNameData displayNameData) {
        final String json = DataUtil.getData(container, Keys.DISPLAY_NAME, String.class);
        return Optional.of(displayNameData.set(Keys.DISPLAY_NAME, TextSerializers.JSON.deserialize(json)));
    }

    @Override
    public DataTransactionResult set(DataHolder holder, DisplayNameData manipulator, MergeFunction function) {
        if (holder instanceof IMixinEntity && !(holder instanceof Player)) {
            final Optional<DisplayNameData> old = from(holder);
            final DisplayNameData merged = checkNotNull(function).merge(old.orElse(null), manipulator);
            final Text newValue = merged.displayName().get();
            final ImmutableValue<Text> immutableValue = merged.displayName().asImmutable();
            try {
                ((IMixinEntity) holder).setDisplayName(newValue);
                if (old.isPresent()) {
                    return DataTransactionResult.successReplaceResult(old.get().displayName().asImmutable(), immutableValue);
                } else {
                    return DataTransactionResult.successResult(immutableValue);
                }
            } catch (Exception e) {
                SpongeImpl.getLogger().debug("An exception occurred when setting data: ", e);
                return DataTransactionResult.errorResult(immutableValue);
            }
        }
        if (holder instanceof ItemStack) {
            final Optional<DisplayNameData> prevValue = from(holder);
            final DisplayNameData merged = checkNotNull(function).merge(prevValue.orElse(null), manipulator);
            final Text newValue = merged.displayName().get();
            final ImmutableValue<Text> immutableValue = merged.displayName().asImmutable();
            ItemStack stack = (ItemStack) holder;
            if (stack.getItem() == Items.WRITTEN_BOOK) {
                NbtDataUtil.getOrCreateCompound(stack).setString(NbtDataUtil.ITEM_BOOK_TITLE, SpongeTexts.toLegacy(newValue));
            } else {
                stack.setStackDisplayName(SpongeTexts.toLegacy(newValue));
            }
            if (prevValue.isPresent()) {
                return DataTransactionResult.successReplaceResult(prevValue.get().displayName().asImmutable(), immutableValue);
            } else {
                return DataTransactionResult.successResult(immutableValue);
            }
        }
        return DataTransactionResult.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableDisplayNameData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableDisplayNameData immutable) {
        if (key == this.key) {
            return Optional.of(new ImmutableSpongeDisplayNameData((Text) value));
        }

        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder holder) {
        if (holder instanceof Entity) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<DisplayNameData> optional = this.from(holder);
            if (optional.isPresent()) {
                try {
                    ((IMixinEntity) holder).setDisplayName(null);
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    SpongeImpl.getLogger().error("There was an issue resetting the display name from an Entity!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        } else if (holder instanceof ItemStack) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<DisplayNameData> optional = this.from(holder);
            if (optional.isPresent()) {
                try {
                    ((ItemStack) holder).clearCustomName();
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    SpongeImpl.getLogger().error("There was an issue removing the display name from an ItemStack!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }

        return DataTransactionResult.failNoData();
    }

    @Override
    public boolean supports(EntityType type) {
        return Entity.class.isAssignableFrom(type.getEntityClass());
    }

}
