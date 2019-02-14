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
package org.spongepowered.common.data.datasync.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.datasync.DataParameterConverter;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

public class EntityCustomNameConverter extends DataParameterConverter<Optional<ITextComponent>> {

    public EntityCustomNameConverter() {
        super(Entity.CUSTOM_NAME);
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(Optional<ITextComponent> currentValue, Optional<ITextComponent> value) {
        if (!currentValue.isPresent() && !value.isPresent()) {
            return Optional.empty();
        }
        Optional<Text> currentText = currentValue.map(SpongeTexts::toText);
        Optional<Text> newValue = value.map(SpongeTexts::toText);

        DataTransactionResult.Builder builder = DataTransactionResult.builder();
        currentText.map(v -> builder.replace(new SpongeImmutableValue<>(Keys.DISPLAY_NAME, v)));
        newValue.map(v -> builder.success(new SpongeImmutableValue<>(Keys.DISPLAY_NAME, v)));

        return Optional.of(builder.result(DataTransactionResult.Type.SUCCESS).build());
    }

    @Override
    public Optional<ITextComponent> getValueFromEvent(Optional<ITextComponent> originalValue, List<Value.Immutable<?>> immutableValues) {
        for (Value.Immutable<?> value : immutableValues) {
            if (value.getKey() == Keys.DISPLAY_NAME) {
                try {
                    return Optional.of(SpongeTexts.toComponent((Text) value.get()));
                } catch (Exception e) {
                    return originalValue;
                }
            }
        }
        return originalValue;
    }
}
