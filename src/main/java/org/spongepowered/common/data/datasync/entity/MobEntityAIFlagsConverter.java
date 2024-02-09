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

import net.minecraft.world.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.accessor.world.entity.MobAccessor;
import org.spongepowered.common.data.datasync.DataParameterConverter;

import java.util.Optional;

public final class MobEntityAIFlagsConverter extends DataParameterConverter<Byte> {

    public MobEntityAIFlagsConverter() {
        super(MobAccessor.accessor$DATA_MOB_FLAGS_ID());
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(final Entity entity, final Byte currentValue, final Byte value) {
        final boolean noAi = this.getFlag(currentValue, MobEntityAIFlagsConverter.NO_AI_MASK);
        final boolean leftHanded = this.getFlag(currentValue, MobEntityAIFlagsConverter.LEFT_HANDED_MASK);
        final boolean aggressive = this.getFlag(currentValue, MobEntityAIFlagsConverter.AGGRESSIVE_MASK);

        final boolean newNoAi = this.getFlag(value, MobEntityAIFlagsConverter.NO_AI_MASK);
        final boolean newLeftHanded = this.getFlag(value, MobEntityAIFlagsConverter.LEFT_HANDED_MASK);
        final boolean newAggressive = this.getFlag(value, MobEntityAIFlagsConverter.AGGRESSIVE_MASK);

        final DataTransactionResult.Builder builder = DataTransactionResult.builder();

        boolean changed = false;
        if (noAi != newNoAi) {
            builder.replace(Value.immutableOf(Keys.IS_AI_ENABLED, !noAi));
            builder.success(Value.immutableOf(Keys.IS_AI_ENABLED, !newNoAi));
            changed = true;
        }

        builder.result(DataTransactionResult.Type.SUCCESS);
        return changed ? Optional.of(builder.build()) : Optional.empty();
    }

    @Override
    public Byte getValueFromEvent(final Byte originalValue, final DataTransactionResult result) {
        if (result.successfulData().isEmpty()) {
            // Short circuit when there are no changes.
            return originalValue;
        }

        final boolean noAi = result.successfulValue(Keys.IS_AI_ENABLED)
                .map(v -> !v.get())
                .orElseGet(() -> this.getFlag(originalValue, MobEntityAIFlagsConverter.NO_AI_MASK));
        final boolean leftHanded = this.getFlag(originalValue, MobEntityAIFlagsConverter.LEFT_HANDED_MASK);
        final boolean aggressive = this.getFlag(originalValue, MobEntityAIFlagsConverter.AGGRESSIVE_MASK);

        byte newValue = (byte) (noAi ? MobEntityAIFlagsConverter.NO_AI_MASK : 0);
        newValue |= (byte) (leftHanded ? MobEntityAIFlagsConverter.LEFT_HANDED_MASK : 0);
        newValue |= (byte) (aggressive ? MobEntityAIFlagsConverter.AGGRESSIVE_MASK : 0);

        return newValue;
    }

    private boolean getFlag(final byte value, final int mask) {
        return (value & mask) != 0;
    }

    public static final int NO_AI_MASK          = 0b00000001; // 0x01
    public static final int LEFT_HANDED_MASK    = 0b00000010; // 0x02
    public static final int AGGRESSIVE_MASK     = 0b00000100; // 0x04
}
