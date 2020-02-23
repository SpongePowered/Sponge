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
package org.spongepowered.common.mixin.core.entity.item;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ExpireEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.entity.item.ItemEntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.mixin.core.entity.EntityMixin;
import org.spongepowered.common.util.Constants;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends EntityMixin implements ItemEntityBridge {

    private static final int MAGIC_PREVIOUS = -1;
    @Shadow private int pickupDelay;
    @Shadow private int age;
    @Shadow public abstract ItemStack shadow$getItem();
    /**
     * A simple cached value of the merge radius for this item.
     * Since the value is configurable, the first time searching for
     * other items, this value is cached.
     */
    private double impl$cachedRadius = -1;

    private int impl$previousPickupDelay = MAGIC_PREVIOUS;
    private boolean impl$infinitePickupDelay;
    private int impl$previousDespawnDelay = MAGIC_PREVIOUS;
    private boolean impl$infiniteDespawnDelay;

    public float dropChance = 1.0f;

    @Override
    public boolean bridge$infinitePickupDelay() {
        return this.impl$infinitePickupDelay;
    }

    @ModifyConstant(method = "searchForOtherItemsNearby", constant = @Constant(doubleValue = 0.5D))
    private double impl$changeSearchRadiusFromConfig(final double originalRadius) {
        if (this.world.isRemote || ((WorldBridge) this.world).bridge$isFake()) {
            return originalRadius;
        }
        if (this.impl$cachedRadius == -1) {
            final double configRadius = ((WorldInfoBridge) this.world.getWorldInfo()).bridge$getConfigAdapter().getConfig().getWorld().getItemMergeRadius();
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
            this.impl$previousPickupDelay = MAGIC_PREVIOUS;
        }
    }

    @Override
    public boolean bridge$infiniteDespawnDelay() {
        return this.impl$infiniteDespawnDelay;
    }

    @Override
    public int bridge$getDespawnDelay() {
        return 6000 - (this.impl$infiniteDespawnDelay ? this.impl$previousDespawnDelay : this.age);
    }

    @Override
    public void bridge$setDespawnDelay(final int delay, final boolean infinite) {
        this.age = 6000 - delay;
        final boolean previous = this.impl$infiniteDespawnDelay;
        this.impl$infiniteDespawnDelay = infinite;
        if (infinite && !previous) {
            this.impl$previousDespawnDelay = this.age;
            this.age = Constants.Entity.Item.MAGIC_NO_DESPAWN;
        } else if (!infinite) {
            this.impl$previousDespawnDelay = MAGIC_PREVIOUS;
        }
    }

    @Override
    public void impl$readFromSpongeCompound(final CompoundNBT compound) {
        super.impl$readFromSpongeCompound(compound);

        this.impl$infinitePickupDelay = compound.getBoolean(Constants.Sponge.Entity.Item.INFINITE_PICKUP_DELAY);
        if (compound.contains(Constants.Sponge.Entity.Item.PREVIOUS_PICKUP_DELAY, Constants.NBT.TAG_ANY_NUMERIC)) {
            this.impl$previousPickupDelay = compound.getInt(Constants.Sponge.Entity.Item.PREVIOUS_PICKUP_DELAY);
        } else {
            this.impl$previousPickupDelay = MAGIC_PREVIOUS;
        }
        this.impl$infiniteDespawnDelay = compound.getBoolean(Constants.Sponge.Entity.Item.INFINITE_DESPAWN_DELAY);
        if (compound.contains(Constants.Sponge.Entity.Item.PREVIOUS_DESPAWN_DELAY, Constants.NBT.TAG_ANY_NUMERIC)) {
            this.impl$previousDespawnDelay = compound.getInt(Constants.Sponge.Entity.Item.PREVIOUS_DESPAWN_DELAY);
        } else {
            this.impl$previousDespawnDelay = MAGIC_PREVIOUS;
        }

        if (this.impl$infinitePickupDelay) {
            if (this.impl$previousPickupDelay != this.pickupDelay) {
                this.impl$previousPickupDelay = this.pickupDelay;
            }

            this.pickupDelay = Constants.Entity.Item.MAGIC_NO_PICKUP;
        } else if (this.pickupDelay == Constants.Entity.Item.MAGIC_NO_PICKUP && this.impl$previousPickupDelay != MAGIC_PREVIOUS) {
            this.pickupDelay = this.impl$previousPickupDelay;
            this.impl$previousPickupDelay = MAGIC_PREVIOUS;
        }

        if (this.impl$infiniteDespawnDelay) {
            if (this.impl$previousDespawnDelay != this.age) {
                this.impl$previousDespawnDelay = this.age;
            }

            this.age = Constants.Entity.Item.MAGIC_NO_DESPAWN;
        } else if (this.age == Constants.Entity.Item.MAGIC_NO_DESPAWN && this.impl$previousDespawnDelay != MAGIC_PREVIOUS) {
            this.age = this.impl$previousDespawnDelay;
            this.impl$previousDespawnDelay = MAGIC_PREVIOUS;
        }
    }

    @Override
    public void impl$writeToSpongeCompound(final CompoundNBT compound) {
        super.impl$writeToSpongeCompound(compound);

        compound.putBoolean(Constants.Sponge.Entity.Item.INFINITE_PICKUP_DELAY, this.impl$infinitePickupDelay);
        compound.putShort(Constants.Sponge.Entity.Item.PREVIOUS_PICKUP_DELAY, (short) this.impl$previousPickupDelay);
        compound.putBoolean(Constants.Sponge.Entity.Item.INFINITE_DESPAWN_DELAY, this.impl$infiniteDespawnDelay);
        compound.putShort(Constants.Sponge.Entity.Item.PREVIOUS_DESPAWN_DELAY, (short) this.impl$previousDespawnDelay);
    }

    @Inject(
        method = "tick()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/ItemEntity;remove()V"),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/ItemEntity;handleWaterMovement()Z"),
            to = @At("TAIL")
        )
    )
    private void impl$fireExpireEntityEventTargetItem(final CallbackInfo ci) {
        if (!SpongeImplHooks.onServerThread() || this.shadow$getItem().isEmpty()) {
            // In the rare case the first if block is actually at the end of the method instruction list, we don't want to 
            // erroneously be calling this twice.
            return;
        }
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            final ExpireEntityEvent event = SpongeEventFactory.createExpireEntityEvent(frame.getCurrentCause(), (Item) this);
            SpongeImpl.postEvent(event);
        }
    }

}
