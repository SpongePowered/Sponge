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
package org.spongepowered.common.entity;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.EntityTrackerCategory;
import org.spongepowered.common.config.category.EntityTrackerModCategory;
import org.spongepowered.common.config.type.TrackerConfig;
import org.spongepowered.common.text.translation.SpongeTranslation;

public class SpongeEntityType<I extends Entity> extends SpongeCatalogType.Translatable implements EntityType {

    public static final EntityType UNKNOWN = new UnknownEntityType();

    public final ResourceLocation key;
    public final net.minecraft.entity.EntityType<I> type;
    public final Class<I> entityClass;
    public final int networkId;

    private EnumCreatureType creatureType;
    private boolean activationRangeInitialized = false;
    // currently not used
    public int trackingRange;
    public int updateFrequency;
    public boolean sendsVelocityUpdates;
    // Used by tracker config
    public boolean allowsBlockBulkCapture = true;
    public boolean allowsEntityBulkCapture = true;
    public boolean allowsBlockEventCreation = true;
    public boolean allowsEntityEventCreation = true;

    public SpongeEntityType(final ResourceLocation key, final net.minecraft.entity.EntityType<I> type, final Class<I> klass) {
        this(key, type, klass, IRegistry.ENTITY_TYPE.getId(type));
    }

    public SpongeEntityType(final ResourceLocation key, final net.minecraft.entity.EntityType<I> type, final Class<I> klass, final int networkId) {
        super((CatalogKey) (Object) key, new SpongeTranslation(type.getTranslationKey()));
        this.key = key;
        this.type = type;
        this.entityClass = klass;
        this.networkId = networkId;

        this.initializeTrackerState();
    }

    public EnumCreatureType getEnumCreatureType() {
        return this.creatureType;
    }

    public void setEnumCreatureType(EnumCreatureType type) {
        this.creatureType = type;
    }

    public boolean isActivationRangeInitialized() {
        return this.activationRangeInitialized;
    }

    public void setActivationRangeInitialized(boolean flag) {
        this.activationRangeInitialized = flag;
    }

    public void initializeTrackerState() {
        SpongeConfig<TrackerConfig> trackerConfig = SpongeImpl.getTrackerConfig();
        EntityTrackerCategory entityTracker = trackerConfig.getConfig().getEntityTracker();
        EntityTrackerModCategory modCapturing = entityTracker.getModMappings().get(this.key.getNamespace());

        if (modCapturing == null) {
            modCapturing = new EntityTrackerModCategory();
            entityTracker.getModMappings().put(this.key.getNamespace(), modCapturing);
        }

        final String key = this.key.getPath();
        if (!modCapturing.isEnabled()) {
            this.allowsBlockBulkCapture = false;
            this.allowsEntityBulkCapture = false;
            this.allowsBlockEventCreation = false;
            this.allowsEntityEventCreation = false;
            modCapturing.getBlockBulkCaptureMap().computeIfAbsent(key, k -> this.allowsBlockBulkCapture);
            modCapturing.getEntityBulkCaptureMap().computeIfAbsent(key, k -> this.allowsEntityBulkCapture);
            modCapturing.getBlockEventCreationMap().computeIfAbsent(key, k -> this.allowsBlockEventCreation);
            modCapturing.getEntityEventCreationMap().computeIfAbsent(key, k -> this.allowsEntityEventCreation);
        } else {
            this.allowsBlockBulkCapture = modCapturing.getBlockBulkCaptureMap().computeIfAbsent(key, k -> true);
            this.allowsEntityBulkCapture = modCapturing.getEntityBulkCaptureMap().computeIfAbsent(key, k -> true);
            this.allowsBlockEventCreation = modCapturing.getBlockEventCreationMap().computeIfAbsent(key, k -> true);
            this.allowsEntityEventCreation = modCapturing.getEntityEventCreationMap().computeIfAbsent(key, k -> true);
        }

        if (entityTracker.autoPopulateData()) {
            trackerConfig.save();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends org.spongepowered.api.entity.Entity> getEntityClass() {
        return (Class<? extends org.spongepowered.api.entity.Entity>) this.entityClass;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("key", this.key)
                .add("type", this.type)
                .add("class", this.entityClass.getName());
    }

}
