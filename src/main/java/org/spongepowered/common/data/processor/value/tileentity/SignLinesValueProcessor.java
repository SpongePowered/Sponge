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
package org.spongepowered.common.data.processor.value.tileentity;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;

@SuppressWarnings("deprecation")
public class SignLinesValueProcessor extends AbstractSpongeValueProcessor<List<Text>, ListValue<Text>> {

    public SignLinesValueProcessor() {
        super(Keys.SIGN_LINES);
    }

    @Override
    public ListValue<Text> constructValue(List<Text> defaultValue) {
        return new SpongeListValue<Text>(Keys.SIGN_LINES, defaultValue);
    }

    @Override
    public Optional<List<Text>> getValueFromContainer(ValueContainer<?> container) {
        if (container instanceof TileEntitySign) {
            final IChatComponent[] rawLines = ((TileEntitySign) container).signText;
            final List<Text> signLines = Lists.newArrayListWithCapacity(4);
            for (int i = 0; i < rawLines.length; i++) {
                signLines.add(i, SpongeTexts.toText(rawLines[i]));
            }
            return Optional.of(signLines);
        } else if (container instanceof ItemStack) {
            if (!((ItemStack) container).hasTagCompound()) {
                return Optional.absent();
            } else {
                final NBTTagCompound mainCompound = ((ItemStack) container).getTagCompound();
                if (!mainCompound.hasKey(NbtDataUtil.BLOCK_ENTITY_TAG, NbtDataUtil.TAG_COMPOUND) || !mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG).hasKey(NbtDataUtil.BLOCK_ENTITY_ID)) {
                    return Optional.absent();
                }
                final NBTTagCompound tileCompound = mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
                final String id = tileCompound.getString(NbtDataUtil.BLOCK_ENTITY_ID);
                if (!id.equalsIgnoreCase(NbtDataUtil.SIGN)) {
                    return Optional.absent();
                }
                final List<Text> texts = Lists.newArrayListWithCapacity(4);
                texts.add(Texts.legacy().fromUnchecked(tileCompound.getString("Text1")));
                texts.add(Texts.legacy().fromUnchecked(tileCompound.getString("Text2")));
                texts.add(Texts.legacy().fromUnchecked(tileCompound.getString("Text3")));
                texts.add(Texts.legacy().fromUnchecked(tileCompound.getString("Text4")));
                return Optional.of(texts);
            }
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof TileEntitySign || (container instanceof ItemStack && ((ItemStack) container).getItem().equals(Items.sign));
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, List<Text> value) {
        final ImmutableListValue<Text> immutableTexts = new ImmutableSpongeListValue<Text>(Keys.SIGN_LINES, ImmutableList.copyOf(value));
        if (container instanceof TileEntitySign) {
            final Optional<SignData> oldData = ((Sign) container).get(SignData.class);
            if (oldData.isPresent()) {
                DataTransactionBuilder builder = DataTransactionBuilder.builder();
                builder.replace(oldData.get().getValues());
                for (int i = 0; i < 4; i++) {
                    ((TileEntitySign) container).signText[i] = SpongeTexts.toComponent(value.get(i));
                }
                ((TileEntitySign) container).markDirty();
                builder.success(immutableTexts).result(DataTransactionResult.Type.SUCCESS);
                return builder.build();
            }
        } if (container instanceof ItemStack) {
            if (!((ItemStack) container).getItem().equals(Items.sign)) {
                return DataTransactionBuilder.failResult(immutableTexts);
            } else {
                final DataTransactionBuilder builder = DataTransactionBuilder.builder();
                final Optional<List<Text>> oldData = getValueFromContainer(container);
                if (oldData.isPresent()) {
                    final ImmutableListValue<Text> immutableListValue = new ImmutableSpongeListValue<Text>(Keys.SIGN_LINES, ImmutableList.copyOf(oldData.get()));
                    builder.replace(immutableListValue);
                }
                final NBTTagCompound mainCompound = NbtDataUtil.getOrCreateCompound(((ItemStack) container));
                final NBTTagCompound tileCompound = NbtDataUtil.getOrCreateSubCompound(mainCompound, NbtDataUtil.BLOCK_ENTITY_TAG);
                tileCompound.setString(NbtDataUtil.BLOCK_ENTITY_ID, NbtDataUtil.SIGN);
                tileCompound.setString("Text1", Texts.json().to(value.get(1)));
                tileCompound.setString("Text2", Texts.json().to(value.get(2)));
                tileCompound.setString("Text3", Texts.json().to(value.get(3)));
                tileCompound.setString("Text4", Texts.json().to(value.get(4)));
                builder.success(immutableTexts);
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionBuilder.failResult(immutableTexts);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof TileEntitySign) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<List<Text>> oldData = getValueFromContainer(container);
            if (oldData.isPresent()) {
                final ImmutableListValue<Text> immutableTexts = new ImmutableSpongeListValue<Text>(Keys.SIGN_LINES, ImmutableList.copyOf(oldData.get()));
                builder.replace(immutableTexts);
            }
            try {
                for (int i = 0; i < 4; i++) {
                    ((TileEntitySign) container).signText[i] = SpongeTexts.toComponent(Texts.of());
                }
                ((TileEntitySign) container).markDirty();
            } catch (Exception e) {
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
            return builder.result(DataTransactionResult.Type.SUCCESS).build();
        } else if (container instanceof ItemStack) {
            if (!((ItemStack) container).getItem().equals(Items.sign)) {
                return DataTransactionBuilder.failNoData();
            } else {
                final DataTransactionBuilder builder = DataTransactionBuilder.builder();
                final Optional<List<Text>> oldData = getValueFromContainer(container);
                if (oldData.isPresent()) {
                    final ImmutableListValue<Text> immutableTexts = new ImmutableSpongeListValue<Text>(Keys.SIGN_LINES, ImmutableList.copyOf(oldData.get()));
                    builder.replace(immutableTexts);
                }
                if (!((ItemStack) container).hasTagCompound()) {
                    return builder.result(DataTransactionResult.Type.SUCCESS).build();
                }
                try {
                    final Optional<NBTTagCompound> mainCompound = NbtDataUtil.getItemCompound(((ItemStack) container));
                    if (mainCompound.isPresent()) {
                        mainCompound.get().removeTag(NbtDataUtil.BLOCK_ENTITY_TAG);
                    }
                    return builder.result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            }
        }
        return DataTransactionBuilder.failNoData();
    }
}
