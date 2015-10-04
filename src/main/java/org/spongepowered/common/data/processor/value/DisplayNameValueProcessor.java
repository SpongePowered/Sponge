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
package org.spongepowered.common.data.processor.value;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IWorldNameable;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.data.IMixinCustomNameable;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class DisplayNameValueProcessor extends AbstractSpongeValueProcessor<Text, Value<Text>> {

    public DisplayNameValueProcessor() {
        super(Keys.DISPLAY_NAME);
    }

    @Override
    public Value<Text> constructValue(Text defaultValue) {
        return new SpongeValue<Text>(Keys.DISPLAY_NAME, defaultValue);
    }

    @Override
    public Optional<Text> getValueFromContainer(ValueContainer<?> container) {
        if (container instanceof Entity && ((Entity) container).hasCustomName()) {
            return Optional.of(Texts.legacy().fromUnchecked(((Entity) container).getCustomNameTag()));
        } else if (container instanceof EntityPlayer) {
            return Optional.of(Texts.legacy().fromUnchecked(((EntityPlayer) container).getCommandSenderName()));
        } else if (container instanceof ItemStack) {
            if (((ItemStack) container).getItem() == Items.written_book) {
                final NBTTagCompound mainCompound = ((ItemStack) container).getTagCompound();
                final String titleString = mainCompound.getString("title");
                return Optional.of(Texts.legacy().fromUnchecked(titleString));
            }
            final NBTTagCompound mainCompound = ((ItemStack) container).getSubCompound("display", false);
            if (mainCompound != null && mainCompound.hasKey("Name", 8)) {
                final String displayString = mainCompound.getString("Name");
                return Optional.of(Texts.legacy().fromUnchecked(displayString));
            } else {
                return Optional.empty();
            }
        } else if (container instanceof IWorldNameable && ((IWorldNameable) container).hasCustomName()) {
            return Optional.of(Texts.legacy().fromUnchecked(((IWorldNameable) container).getCommandSenderName()));
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof Entity || container instanceof IWorldNameable || container instanceof ItemStack;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Text value) {
        checkNotNull(value, "The provided arugment for the Text display name was null!");
        if (container instanceof Entity) {
            final String legacy = Texts.legacy().to(value);
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            try {
                final Optional<Value<Text>> optional = getApiValueFromContainer(container);
                ((Entity) container).setCustomNameTag(legacy);
                if (optional.isPresent()) {
                    builder.replace(optional.get().asImmutable());
                }
                return builder.success(new ImmutableSpongeValue<Text>(Keys.DISPLAY_NAME, value))
                        .result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                Sponge.getLogger().error("There was an issue trying to replace the display name of an entity!", e);
                return DataTransactionBuilder.errorResult(new ImmutableSpongeValue<Text>(Keys.DISPLAY_NAME, value));
            }
        } else if (container instanceof ItemStack) {
            final String legacy = Texts.legacy().to(value);
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            try {
                final Optional<Value<Text>> optional = getApiValueFromContainer(container);
                ((ItemStack) container).setStackDisplayName(legacy);
                if (optional.isPresent()) {
                    builder.replace(optional.get().asImmutable());
                }
                return builder.success(new ImmutableSpongeValue<Text>(Keys.DISPLAY_NAME, value))
                        .result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                Sponge.getLogger().error("There was an issue trying to replace the display name of an itemstack!", e);
                return DataTransactionBuilder.errorResult(new ImmutableSpongeValue<Text>(Keys.DISPLAY_NAME, value));
            }
        } else if (container instanceof IMixinCustomNameable) {
            final String legacy = Texts.legacy().to(value);
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            try {
                final Optional<Value<Text>> optional = getApiValueFromContainer(container);
                ((IMixinCustomNameable) container).setCustomDisplayName(legacy);
                if (optional.isPresent()) {
                    builder.replace(optional.get().asImmutable());
                }
                return builder.success(new ImmutableSpongeValue<Text>(Keys.DISPLAY_NAME, value))
                        .result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                Sponge.getLogger().error("There was an issue trying to replace the display name of an itemstack!", e);
                return DataTransactionBuilder.errorResult(new ImmutableSpongeValue<Text>(Keys.DISPLAY_NAME, value));
            }
        }
        return DataTransactionBuilder.failResult(new ImmutableSpongeValue<Text>(Keys.DISPLAY_NAME, value));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof ItemStack) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<Text> optional = getValueFromContainer(container);
            if (optional.isPresent()) {
                try {
                    ((ItemStack) container).clearCustomName();
                    return builder.replace(new ImmutableSpongeValue<Text>(Keys.DISPLAY_NAME, optional.get()))
                            .result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    Sponge.getLogger().error("There was an issue removing the displayname from an itemstack!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        } else if (container instanceof Entity) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<Text> optional = getValueFromContainer(container);
            if (optional.isPresent()) {
                try {
                    ((Entity) container).setCustomNameTag("");
                    ((Entity) container).setAlwaysRenderNameTag(false);
                    return builder.replace(new ImmutableSpongeValue<Text>(Keys.DISPLAY_NAME, optional.get()))
                            .result(DataTransactionResult.Type.SUCCESS).build();
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
}
