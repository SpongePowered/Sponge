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
package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class EntityDisplayNameValueProcessor extends AbstractSpongeValueProcessor<Entity, Text, Value<Text>> {

    public EntityDisplayNameValueProcessor() {
        super(Entity.class, Keys.DISPLAY_NAME);
    }

    @Override
    protected Value<Text> constructValue(Text defaultValue) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean set(Entity container, Text value) {
        final String legacy = Texts.legacy().to(value);
        container.setCustomNameTag(legacy);
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Optional<Text> getVal(Entity container) {
        if (container instanceof EntityPlayer) {
            return Optional.of(Texts.legacy().fromUnchecked(container.getCommandSenderName()));
        }
        return Optional.of(Texts.legacy().fromUnchecked(container.getCustomNameTag()));
    }

    @Override
    protected ImmutableValue<Text> constructImmutableValue(Text value) {
        return new ImmutableSpongeValue<>(Keys.DISPLAY_NAME, Texts.of(), value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        final DataTransactionBuilder builder = DataTransactionBuilder.builder();
        final Optional<Text> optional = getValueFromContainer(container);
        if (optional.isPresent()) {
            try {
                ((Entity) container).setCustomNameTag("");
                ((Entity) container).setAlwaysRenderNameTag(false);
                return builder.replace(new ImmutableSpongeValue<>(Keys.DISPLAY_NAME, optional.get()))
                    .result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                Sponge.getLogger().error("There was an issue resetting the custom name on an entity!", e);
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
        } else {
            return builder.result(DataTransactionResult.Type.SUCCESS).build();
        }
    }
}
