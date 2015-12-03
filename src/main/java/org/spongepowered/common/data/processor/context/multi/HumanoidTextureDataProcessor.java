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
package org.spongepowered.common.data.processor.context.multi;

import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.context.DataContext;
import org.spongepowered.api.data.context.DataContextual;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHumanoidTextureData;
import org.spongepowered.api.data.manipulator.mutable.entity.HumanoidTextureData;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHumanoidTextureData;
import org.spongepowered.common.data.processor.context.common.AbstractContextAwareMultiDataSingleTargetProcessor;
import org.spongepowered.common.entity.context.SpongeEntityContext;
import org.spongepowered.common.entity.context.store.HumanoidContextStore;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.interfaces.entity.IMixinEntityContext;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public final class HumanoidTextureDataProcessor extends AbstractContextAwareMultiDataSingleTargetProcessor<Entity, SpongeEntityContext,
        HumanoidTextureData, ImmutableHumanoidTextureData> {

    public HumanoidTextureDataProcessor() {
        super(Entity.class, SpongeEntityContext.class);
    }

    @Override
    protected boolean doesDataExist(Entity container, ContextViewer viewer, SpongeEntityContext context) {
        HumanoidContextStore store = (HumanoidContextStore) ((IMixinEntityContext) container).getContextStore();
        return store.getTextureProperty(viewer) != null || store.getTextureId(viewer) != null;
    }

    @Override
    protected boolean set(Entity container, ContextViewer viewer, SpongeEntityContext context, Map<Key<?>, Object> keyValues) {
        @Nullable ProfileProperty property = (ProfileProperty) keyValues.get(Keys.HUMANOID_TEXTURES_PROPERTY);
        @Nullable UUID uniqueId = (UUID) keyValues.get(Keys.HUMANOID_TEXTURES_UNIQUE_ID);

        ((HumanoidContextStore) ((IMixinEntityContext) container).getContextStore()).setTexturePropertyAndId(viewer, property, uniqueId);

        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(Entity container, ContextViewer viewer, SpongeEntityContext context) {
        HumanoidContextStore store = (HumanoidContextStore) ((IMixinEntityContext) container).getContextStore();

        Map<Key<?>, Object> result = Maps.newHashMap();
        result.put(Keys.HUMANOID_TEXTURES_PROPERTY, store.getTextureProperty(viewer));
        result.put(Keys.HUMANOID_TEXTURES_UNIQUE_ID, store.getTextureId(viewer));

        return result;
    }

    @Override
    protected boolean doesDataExist(Entity dataHolder) {
        HumanoidContextStore store = (HumanoidContextStore) ((IMixinEntityContext) dataHolder).getContextStore();
        return store.getTextureProperty() != null || store.getTextureId() != null;
    }

    @Override
    protected boolean set(Entity dataHolder, Map<Key<?>, Object> keyValues) {
        @Nullable ProfileProperty property = (ProfileProperty) keyValues.get(Keys.HUMANOID_TEXTURES_PROPERTY);
        @Nullable UUID uniqueId = (UUID) keyValues.get(Keys.HUMANOID_TEXTURES_UNIQUE_ID);

        ((HumanoidContextStore) ((IMixinEntityContext) dataHolder).getContextStore()).setTexturePropertyAndId(property, uniqueId);

        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(Entity dataHolder) {
        HumanoidContextStore store = (HumanoidContextStore) ((IMixinEntityContext) dataHolder).getContextStore();

        Map<Key<?>, Object> result = Maps.newHashMap();
        result.put(Keys.HUMANOID_TEXTURES_PROPERTY, store.getTextureProperty());
        result.put(Keys.HUMANOID_TEXTURES_UNIQUE_ID, store.getTextureId());

        return result;
    }

    @Override
    protected HumanoidTextureData createManipulator() {
        return new SpongeHumanoidTextureData();
    }

    @Override
    public boolean supports(DataContextual contextual, ContextViewer viewer, DataContext context) {
        return contextual instanceof EntityPlayer || contextual instanceof EntityHuman;
    }

    @Override
    public DataTransactionResult remove(DataContextual contextual, ContextViewer viewer, DataContext context) {
        if (this.supports(contextual, viewer, context)) {
            ((HumanoidContextStore) ((IMixinEntityContext) contextual).getContextStore()).setTexturePropertyAndId(viewer, null, null);

            return DataTransactionResult.successNoData();
        }

        return DataTransactionResult.failNoData();
    }

    @Override
    public Optional<HumanoidTextureData> fill(DataContainer container, HumanoidTextureData data) {
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (this.supports(dataHolder)) {
            HumanoidContextStore store = (HumanoidContextStore) ((IMixinEntityContext) dataHolder).getContextStore();
            ((HumanoidContextStore) ((IMixinEntityContext) dataHolder).getContextStore()).setTexturePropertyAndId(null, null);

            return DataTransactionResult.successNoData();
        }

        return DataTransactionResult.failNoData();
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityPlayer || dataHolder instanceof EntityHuman;
    }

    @Override
    public boolean supports(EntityType type) {
        return type.getEntityClass().isAssignableFrom(EntityPlayer.class) || type.getEntityClass().isAssignableFrom(EntityHuman.class);
    }

}
