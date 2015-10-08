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
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.mutable.SpongeDisplayNameData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.util.DataUtil;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class DisplayNameDataProcessor extends AbstractSpongeDataProcessor<DisplayNameData, ImmutableDisplayNameData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof Entity || dataHolder instanceof ItemStack || dataHolder instanceof IWorldNameable;
    }

    @Override
    public Optional<DisplayNameData> from(DataHolder dataHolder) {
        if (dataHolder instanceof Entity && ((Entity) dataHolder).hasCustomName()) {
            final String displayName = ((Entity) dataHolder).getCustomNameTag();
            final boolean shows = ((Entity) dataHolder).getAlwaysRenderNameTag();
            final DisplayNameData data = new SpongeDisplayNameData(Texts.legacy().fromUnchecked(displayName), shows);
            return Optional.of(data);
        } else if (dataHolder instanceof ItemStack) {
            if (((ItemStack) dataHolder).getItem() == Items.written_book) {
                final NBTTagCompound mainCompound = ((ItemStack) dataHolder).getTagCompound();
                final String titleString = mainCompound.getString("title");
                final DisplayNameData data = new SpongeDisplayNameData(Texts.legacy().fromUnchecked(titleString));
                return Optional.of(data);
            }
            final NBTTagCompound mainCompound = ((ItemStack) dataHolder).getSubCompound("display", false);
            if (mainCompound != null && mainCompound.hasKey("Name", 8)) {
                final String displayString = mainCompound.getString("Name");
                final DisplayNameData data = new SpongeDisplayNameData(Texts.legacy().fromUnchecked(displayString));
                return Optional.of(data);
            } else {
                return Optional.empty();
            }
        } else if (dataHolder instanceof IWorldNameable) {
            if (((IWorldNameable) dataHolder).hasCustomName()) {
                final String customName = ((IWorldNameable) dataHolder).getCommandSenderName();
                final DisplayNameData data = new SpongeDisplayNameData(Texts.legacy().fromUnchecked(customName));
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<DisplayNameData> fill(DataHolder dataHolder, DisplayNameData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final DisplayNameData data = from(dataHolder).orElse(null);
            final DisplayNameData newData = checkNotNull(overlap.merge(checkNotNull(manipulator), data));
            final Text display = newData.displayName().get();
            final boolean displays = newData.customNameVisible().get();
            return Optional.of(manipulator.set(Keys.DISPLAY_NAME, display).set(Keys.SHOWS_DISPLAY_NAME, displays));
        }
        return Optional.empty();
    }

    @Override
    public Optional<DisplayNameData> fill(DataContainer container, DisplayNameData displayNameData) {
        final String json = DataUtil.getData(container, Keys.DISPLAY_NAME, String.class);
        final Text displayName = Texts.json().fromUnchecked(json);
        final boolean shows = DataUtil.getData(container, Keys.SHOWS_DISPLAY_NAME);
        return Optional.of(displayNameData.set(Keys.DISPLAY_NAME, displayName).set(Keys.SHOWS_DISPLAY_NAME, shows));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, DisplayNameData manipulator, MergeFunction function) {
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableDisplayNameData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableDisplayNameData immutable) {
        if (key == Keys.DISPLAY_NAME) {
            return Optional.<ImmutableDisplayNameData>of(
                    new ImmutableSpongeDisplayNameData((Text) value, immutable.customNameVisible().get()));
        } else if (key == Keys.SHOWS_DISPLAY_NAME) {
            return Optional.<ImmutableDisplayNameData>of(
                    new ImmutableSpongeDisplayNameData(immutable.displayName().get(), (Boolean) value));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<DisplayNameData> optional = from(dataHolder);
            if (optional.isPresent()) {
                try {
                    ((ItemStack) dataHolder).clearCustomName();
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    Sponge.getLogger().error("There was an issue removing the displayname from an itemstack!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        } else if (dataHolder instanceof Entity) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<DisplayNameData> optional = from(dataHolder);
            if (optional.isPresent()) {
                try {
                    ((Entity) dataHolder).setCustomNameTag("");
                    ((Entity) dataHolder).setAlwaysRenderNameTag(false);
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    Sponge.getLogger().error("There was an issue resetting the custom name on an entity!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public Optional<DisplayNameData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            if (((Entity) dataHolder).hasCustomName()) {
                return from(dataHolder);
            } else {
                return Optional.<DisplayNameData>of(new SpongeDisplayNameData());
            }
        } else if (dataHolder instanceof ItemStack) {
            if (!((ItemStack) dataHolder).hasDisplayName()) {
                return Optional.<DisplayNameData>of(new SpongeDisplayNameData());
            } else {
                return from(dataHolder);
            }
        } else if (dataHolder instanceof IWorldNameable) {
            return from(dataHolder);
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(EntityType entityType) {
        return Entity.class.isAssignableFrom(entityType.getEntityClass());
    }
}
