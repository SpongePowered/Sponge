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
package org.spongepowered.common.mixin.core.entity;

import co.aikar.timings.Timing;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.CatalogKeyBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.entity.EntityTypeBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.EntityTrackerCategory;
import org.spongepowered.common.config.category.EntityTrackerModCategory;
import org.spongepowered.common.config.type.TrackerConfig;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements CatalogKeyBridge, TrackableBridge, EntityTypeBridge {

    private CatalogKey impl$key;
    private boolean impl$allowsBlockBulkCaptures = true;
    private boolean impl$allowsBlockEventCreation = true;
    private boolean impl$allowsEntityBulkCaptures = true;
    private boolean impl$allowsEntityEventCreation = true;
    private boolean impl$isActivationRangeInitialized = false;
    private boolean impl$hasCheckedDamageEntity = false;
    private boolean impl$overridesDamageEntity = false;
    private Timing impl$timings;

    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;register(Lnet/minecraft/util/registry/Registry;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object impl$setKeyAndInitializeTrackerState(Registry<Object> registry, String key, Object entityType) {
        final PluginContainer container = SpongeImplHooks.getActiveModContainer();
        final CatalogKeyBridge catalogKeyBridge = (CatalogKeyBridge) entityType;
        catalogKeyBridge.bridge$setKey(container.createCatalogKey(key));

        final TrackableBridge trackableBridge = (TrackableBridge) entityType;

        final SpongeConfig<TrackerConfig> trackerConfigAdapter = SpongeImpl.getTrackerConfigAdapter();
        final EntityTrackerCategory entityTracker = trackerConfigAdapter.getConfig().getEntityTracker();

        EntityTrackerModCategory modCapturing = entityTracker.getModMappings().get(container.getId());

        if (modCapturing == null) {
            modCapturing = new EntityTrackerModCategory();
            entityTracker.getModMappings().put(container.getId(), modCapturing);
        }

        if (!modCapturing.isEnabled()) {
            trackableBridge.bridge$setAllowsBlockBulkCaptures(false);
            trackableBridge.bridge$setAllowsBlockEventCreation(false);
            trackableBridge.bridge$setAllowsEntityBulkCaptures(false);
            trackableBridge.bridge$setAllowsEntityEventCreation(false);
            modCapturing.getBlockBulkCaptureMap().computeIfAbsent(key, k -> false);
            modCapturing.getEntityBulkCaptureMap().computeIfAbsent(key, k -> false);
            modCapturing.getBlockEventCreationMap().computeIfAbsent(key, k -> false);
            modCapturing.getEntityEventCreationMap().computeIfAbsent(key, k -> false);
        } else {
            trackableBridge.bridge$setAllowsBlockBulkCaptures(modCapturing.getBlockBulkCaptureMap().computeIfAbsent(key, k -> true));
            trackableBridge.bridge$setAllowsBlockEventCreation(modCapturing.getBlockEventCreationMap().computeIfAbsent(key, k -> true));
            trackableBridge.bridge$setAllowsEntityBulkCaptures(modCapturing.getEntityBulkCaptureMap().computeIfAbsent(key, k -> true));
            trackableBridge.bridge$setAllowsEntityEventCreation(modCapturing.getEntityEventCreationMap().computeIfAbsent(key, k -> true));
        }

        if (entityTracker.autoPopulateData()) {
            trackerConfigAdapter.save();
        }

        return entityType;
    }

    @Override
    public CatalogKey bridge$getKey() {
        return this.impl$key;
    }

    @Override
    public void bridge$setKey(CatalogKey key) {
        this.impl$key = key;
    }

    @Override
    public boolean bridge$allowsBlockBulkCaptures() {
        return this.impl$allowsBlockBulkCaptures;
    }

    @Override
    public void bridge$setAllowsBlockBulkCaptures(boolean allowsBlockBulkCaptures) {
        this.impl$allowsBlockBulkCaptures = allowsBlockBulkCaptures;
    }

    @Override
    public boolean bridge$allowsBlockEventCreation() {
        return this.impl$allowsBlockEventCreation;
    }

    @Override
    public void bridge$setAllowsBlockEventCreation(boolean allowsBlockEventCreation) {
        this.impl$allowsBlockEventCreation = allowsBlockEventCreation;
    }

    @Override
    public boolean bridge$allowsEntityBulkCaptures() {
        return this.impl$allowsEntityBulkCaptures;
    }

    @Override
    public void bridge$setAllowsEntityBulkCaptures(boolean allowsEntityBulkCaptures) {
        this.impl$allowsEntityBulkCaptures = allowsEntityBulkCaptures;
    }

    @Override
    public boolean bridge$allowsEntityEventCreation() {
        return this.impl$allowsEntityEventCreation;
    }

    @Override
    public void bridge$setAllowsEntityEventCreation(boolean allowsEntityEventCreation) {
        this.impl$allowsEntityEventCreation = allowsEntityEventCreation;
    }

    @Override
    public boolean bridge$isActivationRangeInitialized() {
        return this.impl$isActivationRangeInitialized;
    }

    @Override
    public void bridge$setActivationRangeInitialized(boolean activationRangeInitialized) {
        this.impl$isActivationRangeInitialized = activationRangeInitialized;
    }

    @Override
    public boolean bridge$checkedDamageEntity() {
        return this.impl$hasCheckedDamageEntity;
    }

    @Override
    public void bridge$setCheckedDamageEntity(boolean checkedDamageEntity) {
        this.impl$hasCheckedDamageEntity = checkedDamageEntity;
    }

    @Override
    public boolean bridge$overridesDamageEntity() {
        return this.impl$overridesDamageEntity;
    }

    @Override
    public void bridge$setOverridesDamageEntity(boolean damagesEntity) {
        this.impl$overridesDamageEntity = damagesEntity;
    }

    @Override
    public Timing bridge$getTimings() {
        if (this.impl$timings == null) {
            this.impl$timings = SpongeTimings.getEntityTiming((EntityType) (Object) this);
        }
        return this.impl$timings;
    }
}
