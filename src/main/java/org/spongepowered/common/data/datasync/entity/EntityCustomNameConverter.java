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
import org.spongepowered.common.accessor.world.entity.EntityAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.datasync.DataParameterConverter;

import java.util.Optional;

public final class EntityCustomNameConverter extends DataParameterConverter<net.minecraft.network.chat.Component> {
    public EntityCustomNameConverter() {
        super(EntityAccessor.accessor$DATA_CUSTOM_NAME());
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(final Entity entity, final net.minecraft.network.chat.Component oldValue, final net.minecraft.network.chat.Component newValue) {
        return Optional.of(DataTransactionResult.builder()
                .replace(Value.immutableOf(Keys.DISPLAY_NAME, SpongeAdventure.asAdventure(oldValue)))
                .success(Value.immutableOf(Keys.DISPLAY_NAME, SpongeAdventure.asAdventure(newValue)))
                .result(DataTransactionResult.Type.SUCCESS)
                .build());
    }

    @Override
    public net.minecraft.network.chat.Component getValueFromEvent(final net.minecraft.network.chat.Component oldValue, final DataTransactionResult result) {
        return result.successfulValue(Keys.DISPLAY_NAME)
                .map(v -> {
                    try {
                        return SpongeAdventure.asVanilla(v.get());
                    } catch (final Exception ignored) {
                        return oldValue;
                    }
                })
                .orElse(oldValue);
    }
}
