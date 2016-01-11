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
package org.spongepowered.common.data.processor.data.tileentity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeSignData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("deprecation")
public class SignDataProcessor extends AbstractSpongeDataProcessor<SignData, ImmutableSignData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof TileEntitySign || (dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem().equals(Items.sign));
    }

    @Override
    public Optional<SignData> from(DataHolder dataHolder) {
        if (dataHolder instanceof TileEntitySign) {
            final SignData signData = new SpongeSignData();
            final IChatComponent[] rawLines = ((TileEntitySign) dataHolder).signText;
            final List<Text> signLines = Lists.newArrayListWithExpectedSize(4);
            for (int i = 0; i < rawLines.length; i++) {
                signLines.add(i, rawLines[i] == null ? Text.EMPTY : SpongeTexts.toText(rawLines[i]));
            }
            return Optional.of(signData.set(Keys.SIGN_LINES, signLines));
        } else if (dataHolder instanceof ItemStack) {
            if (!((ItemStack) dataHolder).hasTagCompound()) {
                return Optional.empty();
            } else {
                final NBTTagCompound mainCompound = ((ItemStack) dataHolder).getTagCompound();
                if (!mainCompound.hasKey(NbtDataUtil.BLOCK_ENTITY_TAG, NbtDataUtil.TAG_COMPOUND) || !mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG).hasKey(NbtDataUtil.BLOCK_ENTITY_ID)) {
                    return Optional.empty();
                }
                final NBTTagCompound tileCompound = mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
                final String id = tileCompound.getString(NbtDataUtil.BLOCK_ENTITY_ID);
                if (!id.equalsIgnoreCase(NbtDataUtil.SIGN)) {
                    return Optional.empty();
                }
                final List<Text> texts = Lists.newArrayListWithCapacity(4);
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text1")));
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text2")));
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text3")));
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text4")));
                return Optional.of(new SpongeSignData(texts));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<SignData> fill(DataHolder dataHolder, SignData manipulator, MergeFunction overlap) {
        if (dataHolder instanceof TileEntitySign) {
            final SignData signData = new SpongeSignData();
            final IChatComponent[] rawLines = ((TileEntitySign) dataHolder).signText;
            final List<Text> signLines = Lists.newArrayListWithCapacity(4);
            for (int i = 0; i < rawLines.length; i++) {
                signLines.add(i, SpongeTexts.toText(rawLines[i]));
            }
            signData.set(Keys.SIGN_LINES, signLines);
            return Optional.of(overlap.merge(manipulator, signData));
        } else if (dataHolder instanceof ItemStack) {
            if (!((ItemStack) dataHolder).hasTagCompound()) {
                return Optional.of(manipulator);
            } else {
                final NBTTagCompound mainCompound = ((ItemStack) dataHolder).getTagCompound();
                if (!mainCompound.hasKey(NbtDataUtil.BLOCK_ENTITY_TAG, NbtDataUtil.TAG_COMPOUND) || !mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG).hasKey(NbtDataUtil.BLOCK_ENTITY_ID)) {
                    return Optional.empty();
                }
                final NBTTagCompound tileCompound = mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
                final String id = tileCompound.getString(NbtDataUtil.BLOCK_ENTITY_ID);
                if (!id.equalsIgnoreCase(NbtDataUtil.SIGN)) {
                    return Optional.of(manipulator);
                }
                final List<Text> texts = Lists.newArrayListWithCapacity(4);
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text1")));
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text2")));
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text3")));
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text4")));
                return Optional.of(overlap.merge(manipulator, new SpongeSignData(texts)));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<SignData> fill(DataContainer container, SignData signData) {
        if (!container.contains(Keys.SIGN_LINES.getQuery())) {
            return Optional.empty();
        }
        checkNotNull(signData);
        final List<String> lines = container.getStringList(Keys.SIGN_LINES.getQuery()).get();
        final List<Text> textLines = Lists.newArrayListWithCapacity(4);
        try {
            for (int i = 0; i < 4; i++) {
                textLines.set(i, TextSerializers.JSON.deserialize(lines.get(i)));
            }
        } catch (Exception e) {
            throw new InvalidDataException("Could not deserialize text json lines", e);
        }
        return Optional.of(signData.set(Keys.SIGN_LINES, textLines));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, SignData manipulator, MergeFunction function) {
        if (dataHolder instanceof TileEntitySign) {
            final Optional<SignData> oldData = dataHolder.get(SignData.class);
            if (oldData.isPresent()) {
                DataTransactionResult.Builder builder = DataTransactionResult.builder();
                builder.replace(oldData.get().getValues());
                final List<Text> texts = manipulator.get(Keys.SIGN_LINES).get();
                for (int i = 0; i < 4; i++) {
                    ((TileEntitySign) dataHolder).signText[i] = SpongeTexts.toComponent(texts.get(i));
                }
                ((TileEntitySign) dataHolder).markDirty();
                ((TileEntitySign) dataHolder).getWorld().markBlockForUpdate(((TileEntitySign) dataHolder).getPos());
                builder.success(manipulator.getValues()).result(DataTransactionResult.Type.SUCCESS);
                return builder.build();
            }
        } if (dataHolder instanceof ItemStack) {
            if (!((ItemStack) dataHolder).getItem().equals(Items.sign)) {
                return DataTransactionResult.failResult(manipulator.getValues());
            }
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<SignData> oldData = from(dataHolder);
            if (oldData.isPresent()) {
                builder.replace(oldData.get().getValues());

            }
            try {
                final SignData newData = function.merge(oldData.orElse(null), manipulator);
                final NBTTagCompound mainCompound = NbtDataUtil.getOrCreateCompound(((ItemStack) dataHolder));
                final NBTTagCompound tileCompound = NbtDataUtil.getOrCreateSubCompound(mainCompound, NbtDataUtil.BLOCK_ENTITY_TAG);
                tileCompound.setString(NbtDataUtil.BLOCK_ENTITY_ID, NbtDataUtil.SIGN);
                final List<Text> newText = newData.lines().get();
                tileCompound.setString("Text1", TextSerializers.JSON.serialize(newText.get(1)));
                tileCompound.setString("Text2", TextSerializers.JSON.serialize(newText.get(2)));
                tileCompound.setString("Text3", TextSerializers.JSON.serialize(newText.get(3)));
                tileCompound.setString("Text4", TextSerializers.JSON.serialize(newText.get(4)));
                builder.success(manipulator.getValues());
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                return builder.reject(manipulator.getValues()).result(DataTransactionResult.Type.ERROR).build();
            }
        }
        return DataTransactionResult.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableSignData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableSignData immutable) {
        if (!key.equals(Keys.SIGN_LINES)) {
            return Optional.empty();
        }
        // TODO
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            if (!((ItemStack) dataHolder).getItem().equals(Items.sign)) {
                return DataTransactionResult.failNoData();
            } else {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                final Optional<SignData> oldData = from(dataHolder);
                if (oldData.isPresent()) {
                    builder.replace(oldData.get().getValues());
                }
                if (!((ItemStack) dataHolder).hasTagCompound()) {
                    return builder.result(DataTransactionResult.Type.SUCCESS).build();
                }
                try {
                    final Optional<NBTTagCompound> mainCompound = NbtDataUtil.getItemCompound(((ItemStack) dataHolder));
                    if (mainCompound.isPresent()) {
                        mainCompound.get().removeTag(NbtDataUtil.BLOCK_ENTITY_TAG);
                    }
                    return builder.result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public Optional<SignData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof TileEntitySign) {
            return from(dataHolder);
        } else if (dataHolder instanceof ItemStack) {
            final ItemStack itemStack = ((ItemStack) dataHolder);
            if (!itemStack.getItem().equals(Items.sign)) {
                return Optional.empty();
            }
            if (itemStack.hasTagCompound()) {
                final NBTTagCompound mainCompound = ((ItemStack) dataHolder).getTagCompound();
                if (!mainCompound.hasKey(NbtDataUtil.BLOCK_ENTITY_TAG, NbtDataUtil.TAG_COMPOUND)) {
                    return Optional.of(new SpongeSignData());
                }
                final NBTTagCompound tileCompound = mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
                final String id = tileCompound.getString(NbtDataUtil.BLOCK_ENTITY_ID);
                if (!id.equalsIgnoreCase(NbtDataUtil.SIGN)) {
                    return Optional.empty();
                }
                final List<Text> texts = Lists.newArrayListWithCapacity(4);
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text1")));
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text2")));
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text3")));
                texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text4")));
                return Optional.of(new SpongeSignData(texts));
            }
        }
        return Optional.empty();
    }
}
