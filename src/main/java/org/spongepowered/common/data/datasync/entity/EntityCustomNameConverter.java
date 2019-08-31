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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.datasync.DataParameterConverter;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.mixin.core.entity.EntityAccessor;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

public class EntityCustomNameConverter extends DataParameterConverter<String> {

    public EntityCustomNameConverter() {
        super(EntityAccessor.accessor$getCustomNameParameter());
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(Entity entity, String currentValue, String value) {
        Text currentText = SpongeTexts.fromLegacy(currentValue);
        Text newValue = SpongeTexts.fromLegacy(value);

        return Optional.of(DataTransactionResult.builder()
            .replace(new ImmutableSpongeValue<>(Keys.DISPLAY_NAME, Text.of(), currentText))
            .success(new ImmutableSpongeValue<>(Keys.DISPLAY_NAME, Text.of(), newValue))
            .result(DataTransactionResult.Type.SUCCESS)
            .build());
    }

    @Override
    public String getValueFromEvent(String originalValue, List<ImmutableValue<?>> immutableValues) {
        for (ImmutableValue<?> value : immutableValues) {
            if (value.getKey() == Keys.DISPLAY_NAME) {
                try {
                    return SpongeTexts.toLegacy((Text) value.get());
                } catch (Exception e) {
                    return originalValue;
                }
            }
        }
        return originalValue;
    }
}
