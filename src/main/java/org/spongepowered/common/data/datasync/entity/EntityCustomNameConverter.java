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

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.accessor.world.entity.EntityAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.datasync.DataParameterConverter;

import java.util.Optional;

public final class EntityCustomNameConverter extends DataParameterConverter<Optional<Component>> {
    public EntityCustomNameConverter() {
        super(EntityAccessor.accessor$DATA_CUSTOM_NAME());
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(final Entity entity, final Optional<Component> oldValue, final Optional<Component> newValue) {
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        oldValue.ifPresent(v -> builder.replace(Value.immutableOf(Keys.CUSTOM_NAME, SpongeAdventure.asAdventure(v))));
        newValue.ifPresent(v -> builder.success(Value.immutableOf(Keys.CUSTOM_NAME, SpongeAdventure.asAdventure(v))));
        return Optional.of(builder.result(DataTransactionResult.Type.SUCCESS).build());
    }

    @Override
    public Optional<Component> getValueFromEvent(final Optional<Component> oldValue, final DataTransactionResult result) {
        final Optional<net.kyori.adventure.text.Component> component = result.successfulValue(Keys.CUSTOM_NAME).map(Value::get);
        if (component.isPresent()) {
            try {
                return Optional.of(SpongeAdventure.asVanilla(component.get()));
            } catch (Exception e) {
                return oldValue;
            }
        }
        return oldValue;
    }
}
