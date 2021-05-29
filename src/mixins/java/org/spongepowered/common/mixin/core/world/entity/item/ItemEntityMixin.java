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
package org.spongepowered.common.mixin.core.world.entity.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ExpireEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.world.entity.item.ItemEntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.data.provider.entity.ItemData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;
import org.spongepowered.common.util.Constants;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends EntityMixin implements ItemEntityBridge {

    private static final int MAGIC_PREVIOUS = -1;

    // @formatter:off
    @Shadow private int pickupDelay;
    @Shadow private int age;
    @Shadow public abstract ItemStack shadow$getItem();
    // @formatter:on

    /**
     * A simple cached value of the merge radius for this item.
     * Since the value is configurable, the first time searching for
     * other items, this value is cached.
     */
    private double impl$cachedRadius = -1;

    private int impl$previousPickupDelay = ItemEntityMixin.MAGIC_PREVIOUS;
    private boolean impl$infinitePickupDelay;
    private int impl$previousDespawnDelay = ItemEntityMixin.MAGIC_PREVIOUS;
    private boolean impl$infiniteDespawnDelay;

    @Override
    public boolean bridge$infinitePickupDelay() {
        return this.impl$infinitePickupDelay;
    }

    @ModifyConstant(method = "mergeWithNeighbours", constant = @Constant(doubleValue = Constants.Entity.Item.DEFAULT_ITEM_MERGE_RADIUS))
    private double impl$changeSearchRadiusFromConfig(final double originalRadius) {
        if (this.level.isClientSide || ((WorldBridge) this.level).bridge$isFake()) {
            return originalRadius;
        }
        if (this.impl$cachedRadius == -1) {
            final double configRadius = ((PrimaryLevelDataBridge) this.level.getLevelData()).bridge$configAdapter().get().world.itemMergeRadius;
            this.impl$cachedRadius = configRadius < 0 ? 0 : configRadius;
        }
        return this.impl$cachedRadius;
    }

    @Override
    public int bridge$getPickupDelay() {
        return this.impl$infinitePickupDelay ? this.impl$previousPickupDelay : this.pickupDelay;
    }

    @Override
    public void bridge$setPickupDelay(final int delay, final boolean infinite) {
        this.pickupDelay = delay;
        final boolean previous = this.impl$infinitePickupDelay;
        this.impl$infinitePickupDelay = infinite;
        if (infinite && !previous) {
            this.impl$previousPickupDelay = this.pickupDelay;
            this.pickupDelay = Constants.Entity.Item.MAGIC_NO_PICKUP;
        } else if (!infinite) {
            this.impl$previousPickupDelay = ItemEntityMixin.MAGIC_PREVIOUS;
        }
        if (infinite) {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.INFINITE_PICKUP_DELAY, true);
            ((SpongeDataHolderBridge) this).bridge$offer(ItemData.PREVIOUS_PICKUP_DELAY, this.impl$previousPickupDelay);
        } else {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.INFINITE_PICKUP_DELAY);
            ((SpongeDataHolderBridge) this).bridge$remove(ItemData.PREVIOUS_PICKUP_DELAY);
        }
    }

    @Override
    public void bridge$setPrevPickupDelay(int delay) {
        this.impl$previousPickupDelay = delay;
    }

    @Override
    public void bridge$setPrevDespawnDelay(int delay) {
        this.impl$previousDespawnDelay = delay;
    }

    @Override
    public boolean bridge$infiniteDespawnDelay() {
        return this.impl$infiniteDespawnDelay;
    }

    @Override
    public int bridge$getDespawnDelay() {
        return SpongeGameConfigs.getForWorld(this.level).get().entity.item.despawnRate - (this.impl$infiniteDespawnDelay ? this.impl$previousDespawnDelay : this.age);
    }

    @Override
    public void bridge$setDespawnDelay(final int delay, final boolean infinite) {
        this.age = SpongeGameConfigs.getForWorld(this.level).get().entity.item.despawnRate - delay;
        final boolean previous = this.impl$infiniteDespawnDelay;
        this.impl$infiniteDespawnDelay = infinite;
        if (infinite && !previous) {
            this.impl$previousDespawnDelay = this.age;
            this.age = Constants.Entity.Item.MAGIC_NO_DESPAWN;
        } else if (!infinite) {
            this.impl$previousDespawnDelay = ItemEntityMixin.MAGIC_PREVIOUS;
        }
        if (infinite) {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.INFINITE_DESPAWN_DELAY, true);
            ((SpongeDataHolderBridge) this).bridge$offer(ItemData.PREVIOUS_DESPAWN_DELAY, this.impl$previousPickupDelay);
        } else {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.INFINITE_DESPAWN_DELAY);
            ((SpongeDataHolderBridge) this).bridge$remove(ItemData.PREVIOUS_DESPAWN_DELAY);
        }
    }

    @Inject(
        method = "tick()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;remove()V"),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;updateInWaterStateAndDoFluidPushing()Z"),
            to = @At("TAIL")
        )
    )
    private void impl$fireExpireEntityEventTargetItem(final CallbackInfo ci) {
        if (!PhaseTracker.SERVER.onSidedThread() || this.shadow$getItem().isEmpty()) {
            // In the rare case the first if block is actually at the end of the method instruction list, we don't want to
            // erroneously be calling this twice.
            return;
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            final ExpireEntityEvent event = SpongeEventFactory.createExpireEntityEvent(frame.currentCause(), (Item) this);
            SpongeCommon.postEvent(event);
        }
    }

    @ModifyConstant(method = "isMergable", constant = @Constant(intValue = 6000))
    private int impl$isMergableUseDespawnRateFromConfig(final int originalValue) {
        return SpongeGameConfigs.getForWorld(this.level).get().entity.item.despawnRate;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 6000))
    private int impl$tickUseDespawnRateFromConfig(final int originalValue) {
        return SpongeGameConfigs.getForWorld(this.level).get().entity.item.despawnRate;
    }

    @Inject(method = "tryToMerge", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;)V"))
    private void impl$merge(final ItemEntity param0, final CallbackInfo ci) {
        final Cause currentCause = Sponge.server().causeStackManager().currentCause();
        if (Sponge.eventManager().post(SpongeEventFactory.createItemMergeWithItemEvent(currentCause, (Item) this, (Item) param0))) {
            ci.cancel();
        }
    }

}
