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

import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IWorldNameable;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.mutable.SpongeDisplayNameData;
import org.spongepowered.common.data.util.DataUtil;

@SuppressWarnings("deprecation")
public class DisplayNameDataProcessor implements DataProcessor<DisplayNameData, ImmutableDisplayNameData> {

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
                return Optional.absent();
            }
        } else if (dataHolder instanceof IWorldNameable) {
            if (((IWorldNameable) dataHolder).hasCustomName()) {
                final String customName = ((IWorldNameable) dataHolder).getCommandSenderName();
                final DisplayNameData data = new SpongeDisplayNameData(Texts.legacy().fromUnchecked(customName));
                return Optional.of(data);
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<DisplayNameData> fill(DataHolder dataHolder, DisplayNameData manipulator) {
        if (dataHolder instanceof Entity) {
            if (((Entity) dataHolder).hasCustomName()) {
                manipulator.set(manipulator.displayName().set(
                        Texts.legacy().fromUnchecked(((Entity) dataHolder).getCustomNameTag())))
                        .set(Keys.SHOWS_DISPLAY_NAME, ((Entity) dataHolder).getAlwaysRenderNameTag());
                return Optional.of(manipulator);
            } else {
                manipulator.set(Keys.DISPLAY_NAME, Texts.of())
                        .set(Keys.SHOWS_DISPLAY_NAME, true);
                return Optional.of(manipulator);
            }
        } else if (dataHolder instanceof ItemStack) {
            if (((ItemStack) dataHolder).getItem() == Items.written_book) {
                final NBTTagCompound mainCompound = ((ItemStack) dataHolder).getTagCompound();
                final String titleString = mainCompound.getString("title");
                final Text titleText = Texts.legacy().fromUnchecked(titleString);
                manipulator.set(Keys.DISPLAY_NAME, titleText)
                        .set(Keys.SHOWS_DISPLAY_NAME, true);
                return Optional.of(manipulator);
            }
            final NBTTagCompound mainCompound = ((ItemStack) dataHolder).getSubCompound("display", false);
            if (mainCompound != null && mainCompound.hasKey("Name", 8)) {
                final String displayString = mainCompound.getString("Name");
                final Text displayText = Texts.legacy().fromUnchecked(displayString);
                manipulator.set(Keys.DISPLAY_NAME, displayText)
                        .set(Keys.SHOWS_DISPLAY_NAME, true);
                return Optional.of(manipulator);

            } else {
                return Optional.of(manipulator);
            }
        } else if (dataHolder instanceof IWorldNameable) {
            if (((IWorldNameable) dataHolder).hasCustomName()) {
                final String customName = ((IWorldNameable) dataHolder).getCommandSenderName();
                return Optional.of(manipulator.set(Keys.DISPLAY_NAME, Texts.legacy().fromUnchecked(customName)));
            } else {
                return Optional.of(manipulator.set(Keys.DISPLAY_NAME, Texts.of()));
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<DisplayNameData> fill(DataHolder dataHolder, DisplayNameData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final DisplayNameData data = from(dataHolder).orNull();
            final DisplayNameData newData = checkNotNull(overlap.merge(checkNotNull(manipulator), data));
            final Text display = newData.displayName().get();
            final boolean displays = newData.customNameVisible().get();
            return Optional.of(manipulator.set(Keys.DISPLAY_NAME, display).set(Keys.SHOWS_DISPLAY_NAME, displays));
        }
        return Optional.absent();
    }

    @Override
    public Optional<DisplayNameData> fill(DataContainer container, DisplayNameData displayNameData) {
        final String json = DataUtil.getData(container, Keys.DISPLAY_NAME, String.class);
        final Text displayName = Texts.json().fromUnchecked(json);
        final boolean shows = DataUtil.getData(container, Keys.SHOWS_DISPLAY_NAME);
        return Optional.of(displayNameData.set(Keys.DISPLAY_NAME, displayName).set(Keys.SHOWS_DISPLAY_NAME, shows));
    }

    @Override
    public Optional<ImmutableDisplayNameData> fillImmutable(DataHolder dataHolder, ImmutableDisplayNameData immutable) {
        final Optional<DisplayNameData> optional = from(dataHolder);
        if (optional.isPresent()) {
            return Optional.of(optional.get().asImmutable());
        }
        return Optional.absent();
    }

    @Override
    public Optional<ImmutableDisplayNameData> fillImmutable(DataHolder dataHolder, ImmutableDisplayNameData immutable, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final DisplayNameData data = from(dataHolder).orNull();
            final DisplayNameData newData = checkNotNull(overlap.merge(checkNotNull(immutable.asMutable()), data));
            return Optional.of(newData.asImmutable());
        }
        return Optional.absent();
    }

    @Override
    public Optional<ImmutableDisplayNameData> fillImmutable(DataContainer container, ImmutableDisplayNameData immutableManipulator) {
        final String json = DataUtil.getData(container, Keys.DISPLAY_NAME, String.class);
        final Text displayName = Texts.json().fromUnchecked(json);
        final boolean shows = DataUtil.getData(container, Keys.SHOWS_DISPLAY_NAME);
        final ImmutableDisplayNameData data = new ImmutableSpongeDisplayNameData(displayName, shows);
        return Optional.of(data);
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, DisplayNameData manipulator) {

        return DataTransactionBuilder.failResult(manipulator.getValues());
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
        return Optional.absent();
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
    public DisplayNameData create() {
        return new SpongeDisplayNameData();
    }

    @Override
    public ImmutableDisplayNameData createImmutable() {
        return new ImmutableSpongeDisplayNameData(Texts.of(), false);
    }

    @Override
    public Optional<DisplayNameData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            if (((Entity) dataHolder).hasCustomName()) {
                return from(dataHolder);
            } else {
                return Optional.of(create());
            }
        } else if (dataHolder instanceof ItemStack) {
            if (!((ItemStack) dataHolder).hasDisplayName()) {
                return Optional.of(create());
            } else {
                return from(dataHolder);
            }
        } else if (dataHolder instanceof IWorldNameable) {
            return from(dataHolder);
        }
        return Optional.absent();
    }

    @Override
    public Optional<DisplayNameData> build(DataView container) throws InvalidDataException {
        final String json = DataUtil.getData(container, Keys.DISPLAY_NAME, String.class);
        final Text displayName = Texts.json().fromUnchecked(json);
        final boolean shows = DataUtil.getData(container, Keys.SHOWS_DISPLAY_NAME);
        final DisplayNameData data = new SpongeDisplayNameData(displayName, shows);
        return Optional.of(data);
    }
}
