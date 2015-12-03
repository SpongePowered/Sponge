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
package org.spongepowered.common.data.processor.context.value;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.context.DataContextual;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.common.data.processor.context.common.AbstractContextAwareValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.entity.context.SpongeEntityContext;
import org.spongepowered.common.entity.context.store.HumanoidContextStore;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.interfaces.entity.IMixinEntityContext;

import java.util.Optional;
import java.util.UUID;

public final class HumanoidTextureIdValueProcessor extends AbstractContextAwareValueProcessor<Entity, SpongeEntityContext, Optional<UUID>, OptionalValue<UUID>> {

    public HumanoidTextureIdValueProcessor() {
        super(Entity.class, SpongeEntityContext.class, Keys.HUMANOID_TEXTURES_UNIQUE_ID);
    }

    @Override
    protected Optional<Optional<UUID>> getVal(Entity entity, ContextViewer viewer, SpongeEntityContext context) {
        return Optional.of(Optional.ofNullable(((HumanoidContextStore) ((IMixinEntityContext) entity).getContextStore()).getTextureId(viewer)));
    }

    @Override
    protected boolean set(Entity entity, ContextViewer viewer, SpongeEntityContext context, Optional<UUID> value) {
        ((HumanoidContextStore) ((IMixinEntityContext) entity).getContextStore()).setTextureId(viewer, value.orElse(null));
        return true;
    }

    @Override
    protected OptionalValue<UUID> constructValue(Optional<UUID> actualValue) {
        return new SpongeOptionalValue<>(this.key, actualValue);
    }

    @Override
    protected boolean set(Entity container, Optional<UUID> value) {
        ((HumanoidContextStore) ((IMixinEntityContext) container).getContextStore()).setTextureId(value.orElse(null));
        return true;
    }

    @Override
    protected Optional<Optional<UUID>> getVal(Entity container) {
        return Optional.of(Optional.ofNullable(((HumanoidContextStore) ((IMixinEntityContext) container).getContextStore()).getTextureId()));
    }

    @Override
    protected ImmutableValue<Optional<UUID>> constructImmutableValue(Optional<UUID> value) {
        return new ImmutableSpongeOptionalValue<>(this.key, value);
    }

    @Override
    public DataTransactionResult removeFrom(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container) {
        if (this.supports(contextual, viewer, container)) {
            ((HumanoidContextStore) ((IMixinEntityContext) contextual).getContextStore()).setTextureId(viewer, null);

            return DataTransactionResult.successNoData();
        }

        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
            ((HumanoidContextStore) ((IMixinEntityContext) container).getContextStore()).setTextureId(null);

            return DataTransactionResult.successNoData();
        }

        return DataTransactionResult.failNoData();
    }

    @Override
    public boolean supports(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container) {
        return contextual instanceof EntityPlayer || contextual instanceof EntityHuman;
    }

    @Override
    protected boolean supports(Entity container) {
        return container instanceof EntityPlayer || container instanceof EntityHuman;
    }

}