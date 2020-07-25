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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.entity.EntityTypeBridge;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;
import org.spongepowered.common.util.Constants;

import java.util.Objects;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements ResourceKeyBridge, TrackableBridge, EntityTypeBridge {

    private ResourceKey impl$key;
    private boolean impl$allowsBlockBulkCaptures = true;
    private boolean impl$allowsBlockEventCreation = true;
    private boolean impl$allowsEntityBulkCaptures = true;
    private boolean impl$allowsEntityEventCreation = true;
    private boolean impl$isActivationRangeInitialized = false;
    private boolean impl$hasCheckedDamageEntity = false;
    private boolean impl$overridesDamageEntity = false;
    private Timing impl$timings;

    /**
     * @author gabizou - January 10th, 2020 - 1.14.3
     * @reason Because the original method uses field instance checks in a big if statement, and
     * Forge moves the original method into a new method and replaces it with a {@link java.util.function.IntSupplier},
     * we have to basically inject at the head and say "fuck it" to check for our human cases.
     * @param cir The return value for the player tracking range, or do nothing
     */
    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "RedundantCast", "rawtypes"})
    @Inject(method = "getTrackingRange", at = @At("HEAD"), cancellable = true)
    private void impl$getHumanTrackingRange(final CallbackInfoReturnable<Integer> cir) {
        if (((EntityType) (Object) this) == EntityTypes.HUMAN.get()) {
            cir.setReturnValue(Constants.Entity.Player.TRACKING_RANGE);
        }
    }

    @Override
    public ResourceKey bridge$getKey() {
        return this.impl$key;
    }

    @Override
    public void bridge$setKey(final ResourceKey key) {
        this.impl$key = key;
    }

    @Override
    public boolean bridge$allowsBlockBulkCaptures() {
        return this.impl$allowsBlockBulkCaptures;
    }

    @Override
    public void bridge$setAllowsBlockBulkCaptures(final boolean allowsBlockBulkCaptures) {
        this.impl$allowsBlockBulkCaptures = allowsBlockBulkCaptures;
    }

    @Override
    public boolean bridge$allowsBlockEventCreation() {
        return this.impl$allowsBlockEventCreation;
    }

    @Override
    public void bridge$setAllowsBlockEventCreation(final boolean allowsBlockEventCreation) {
        this.impl$allowsBlockEventCreation = allowsBlockEventCreation;
    }

    @Override
    public boolean bridge$allowsEntityBulkCaptures() {
        return this.impl$allowsEntityBulkCaptures;
    }

    @Override
    public void bridge$setAllowsEntityBulkCaptures(final boolean allowsEntityBulkCaptures) {
        this.impl$allowsEntityBulkCaptures = allowsEntityBulkCaptures;
    }

    @Override
    public boolean bridge$allowsEntityEventCreation() {
        return this.impl$allowsEntityEventCreation;
    }

    @Override
    public void bridge$setAllowsEntityEventCreation(final boolean allowsEntityEventCreation) {
        this.impl$allowsEntityEventCreation = allowsEntityEventCreation;
    }

    @Override
    public boolean bridge$isActivationRangeInitialized() {
        return this.impl$isActivationRangeInitialized;
    }

    @Override
    public void bridge$setActivationRangeInitialized(final boolean activationRangeInitialized) {
        this.impl$isActivationRangeInitialized = activationRangeInitialized;
    }

    @Override
    public boolean bridge$checkedDamageEntity() {
        return this.impl$hasCheckedDamageEntity;
    }

    @Override
    public void bridge$setCheckedDamageEntity(final boolean checkedDamageEntity) {
        this.impl$hasCheckedDamageEntity = checkedDamageEntity;
    }

    @Override
    public boolean bridge$overridesDamageEntity() {
        return this.impl$overridesDamageEntity;
    }

    @Override
    public void bridge$setOverridesDamageEntity(final boolean damagesEntity) {
        this.impl$overridesDamageEntity = damagesEntity;
    }

    @Override
    public Timing bridge$getTimings() {
        if (this.impl$timings == null) {
            this.impl$timings = SpongeTimings.getEntityTiming((EntityType) (Object) this);
        }
        return this.impl$timings;
    }

    @Redirect(method = "register",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/registry/Registry;register(Lnet/minecraft/util/registry/Registry;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object impl$setKey(final Registry<Object> registry, final String resourcePath, final Object entityType) {
        ((ResourceKeyBridge) entityType).bridge$setKey(ResourceKey.resolve(resourcePath));
        return Registry.register(registry, resourcePath, entityType);
    }

}
