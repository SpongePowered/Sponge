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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.accessor.world.entity.MobAccessor;
import org.spongepowered.common.data.datasync.DataParameterConverter;
import java.util.Optional;
import net.minecraft.world.entity.Entity;

public final class MobEntityAIFlagsConverter extends DataParameterConverter<Byte> {

    public MobEntityAIFlagsConverter() {
        super(MobAccessor.accessor$DATA_MOB_FLAGS_ID());
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(final Entity entity, final Byte currentValue, final Byte value) {
        final boolean oldHasAi = (currentValue & 1) == 0;
        final boolean newHasAi = (value & 1) == 0;
        return Optional.of(DataTransactionResult.builder()
                .replace(Value.immutableOf(Keys.IS_AI_ENABLED, oldHasAi))
                .success(Value.immutableOf(Keys.IS_AI_ENABLED, newHasAi))
                .result(DataTransactionResult.Type.SUCCESS)
                .build());
    }

    @Override
    public Byte getValueFromEvent(final Byte originalValue, final DataTransactionResult result) {
        return result.successfulValue(Keys.IS_AI_ENABLED)
                .map(v -> v.get() ? (byte) (originalValue & -2) : (byte) (originalValue | 1))
                .orElse(originalValue);
    }
}
