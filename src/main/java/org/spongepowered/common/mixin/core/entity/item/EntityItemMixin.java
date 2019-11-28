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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.entity.EntityItemBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.mixin.core.entity.EntityMixin;
import org.spongepowered.common.util.Constants;

@Mixin(ItemEntity.class)
public abstract class EntityItemMixin extends EntityMixin implements EntityItemBridge {

    private static final int MAGIC_PREVIOUS = -1;
    @Shadow private int pickupDelay;
    @Shadow private int age;
    @Shadow public abstract ItemStack getItem();
    /**
     * A simple cached value of the merge radius for this item.
     * Since the value is configurable, the first time searching for
     * other items, this value is cached.
     */
    private double cachedRadius = -1;

    private int previousPickupDelay = MAGIC_PREVIOUS;
    private boolean infinitePickupDelay;
    private int previousDespawnDelay = MAGIC_PREVIOUS;
    private boolean infiniteDespawnDelay;

    public float dropChance = 1.0f;

    @Override
    public boolean bridge$infinitePickupDelay() {
        return this.infinitePickupDelay;
    }

    @ModifyConstant(method = "searchForOtherItemsNearby", constant = @Constant(doubleValue = 0.5D))
    private double impl$changeSearchRadiusFromConfig(final double originalRadius) {
        if (this.world.field_72995_K || ((WorldBridge) this.world).bridge$isFake()) {
            return originalRadius;
        }
        if (this.cachedRadius == -1) {
            final double configRadius = ((WorldInfoBridge) this.world.func_72912_H()).bridge$getConfigAdapter().getConfig().getWorld().getItemMergeRadius();
            this.cachedRadius = configRadius < 0 ? 0 : configRadius;
        }
        return this.cachedRadius;
    }

    @Override
    public int bridge$getPickupDelay() {
        return this.infinitePickupDelay ? this.previousPickupDelay : this.pickupDelay;
    }

    @Override
    public void bridge$setPickupDelay(final int delay, final boolean infinite) {
        this.pickupDelay = delay;
        final boolean previous = this.infinitePickupDelay;
        this.infinitePickupDelay = infinite;
        if (infinite && !previous) {
            this.previousPickupDelay = this.pickupDelay;
            this.pickupDelay = Constants.Entity.Item.MAGIC_NO_PICKUP;
        } else if (!infinite) {
            this.previousPickupDelay = MAGIC_PREVIOUS;
        }
    }

    @Override
    public boolean bridge$infiniteDespawnDelay() {
        return this.infiniteDespawnDelay;
    }

    @Override
    public int bridge$getDespawnDelay() {
        return 6000 - (this.infiniteDespawnDelay ? this.previousDespawnDelay : this.age);
    }

    @Override
    public void bridge$setDespawnDelay(final int delay) {
        this.age = 6000 - delay;
    }

    @Override
    public void bridge$setDespawnDelay(final int delay, final boolean infinite) {
        this.age = 6000 - delay;
        final boolean previous = this.infiniteDespawnDelay;
        this.infiniteDespawnDelay = infinite;
        if (infinite && !previous) {
            this.previousDespawnDelay = this.age;
            this.age = Constants.Entity.Item.MAGIC_NO_DESPAWN;
        } else if (!infinite) {
            this.previousDespawnDelay = MAGIC_PREVIOUS;
        }
    }

    @Override
    public void spongeImpl$readFromSpongeCompound(final CompoundNBT compound) {
        super.spongeImpl$readFromSpongeCompound(compound);

        this.infinitePickupDelay = compound.func_74767_n(Constants.Sponge.Entity.Item.INFINITE_PICKUP_DELAY);
        if (compound.func_150297_b(Constants.Sponge.Entity.Item.PREVIOUS_PICKUP_DELAY, Constants.NBT.TAG_ANY_NUMERIC)) {
            this.previousPickupDelay = compound.func_74762_e(Constants.Sponge.Entity.Item.PREVIOUS_PICKUP_DELAY);
        } else {
            this.previousPickupDelay = MAGIC_PREVIOUS;
        }
        this.infiniteDespawnDelay = compound.func_74767_n(Constants.Sponge.Entity.Item.INFINITE_DESPAWN_DELAY);
        if (compound.func_150297_b(Constants.Sponge.Entity.Item.PREVIOUS_DESPAWN_DELAY, Constants.NBT.TAG_ANY_NUMERIC)) {
            this.previousDespawnDelay = compound.func_74762_e(Constants.Sponge.Entity.Item.PREVIOUS_DESPAWN_DELAY);
        } else {
            this.previousDespawnDelay = MAGIC_PREVIOUS;
        }

        if (this.infinitePickupDelay) {
            if (this.previousPickupDelay != this.pickupDelay) {
                this.previousPickupDelay = this.pickupDelay;
            }

            this.pickupDelay = Constants.Entity.Item.MAGIC_NO_PICKUP;
        } else if (this.pickupDelay == Constants.Entity.Item.MAGIC_NO_PICKUP && this.previousPickupDelay != MAGIC_PREVIOUS) {
            this.pickupDelay = this.previousPickupDelay;
            this.previousPickupDelay = MAGIC_PREVIOUS;
        }

        if (this.infiniteDespawnDelay) {
            if (this.previousDespawnDelay != this.age) {
                this.previousDespawnDelay = this.age;
            }

            this.age = Constants.Entity.Item.MAGIC_NO_DESPAWN;
        } else if (this.age == Constants.Entity.Item.MAGIC_NO_DESPAWN && this.previousDespawnDelay != MAGIC_PREVIOUS) {
            this.age = this.previousDespawnDelay;
            this.previousDespawnDelay = MAGIC_PREVIOUS;
        }
    }

    @Override
    public void spongeImpl$writeToSpongeCompound(final CompoundNBT compound) {
        super.spongeImpl$writeToSpongeCompound(compound);

        compound.func_74757_a(Constants.Sponge.Entity.Item.INFINITE_PICKUP_DELAY, this.infinitePickupDelay);
        compound.func_74777_a(Constants.Sponge.Entity.Item.PREVIOUS_PICKUP_DELAY, (short) this.previousPickupDelay);
        compound.func_74757_a(Constants.Sponge.Entity.Item.INFINITE_DESPAWN_DELAY, this.infiniteDespawnDelay);
        compound.func_74777_a(Constants.Sponge.Entity.Item.PREVIOUS_DESPAWN_DELAY, (short) this.previousDespawnDelay);
    }

    @Inject(
        method = "onCollideWithPlayer",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/entity/item/EntityItem;getItem()Lnet/minecraft/item/ItemStack;"),
        cancellable = true
    )
    private void spongeImpl$ThrowPickupEvent(final PlayerEntity entityIn, final CallbackInfo ci) {
        if (!SpongeCommonEventFactory.callPlayerChangeInventoryPickupPreEvent(entityIn, (ItemEntity) (Object) this, this.pickupDelay)) {
            ci.cancel();
        }
    }
    
    @Inject(
        method = "onUpdate",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;setDead()V"),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;handleWaterMovement()Z"),
            to = @At("TAIL")
        )
    )
    private void impl$fireExpireEntityEventTargetItem(final CallbackInfo ci) {
        if (!SpongeImplHooks.isMainThread() || this.getItem().func_190926_b()) {
            // In the rare case the first if block is actually at the end of the method instruction list, we don't want to 
            // erroneously be calling this twice.
            return;
        }
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            final ExpireEntityEvent.TargetItem event = SpongeEventFactory.createExpireEntityEventTargetItem(frame.getCurrentCause(), (Item) this);
            SpongeImpl.postEvent(event);
        }
    }

    @Redirect(method = "onCollideWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;addItemStackToInventory(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean spongeImpl$throwPikcupEventForAddItem(final PlayerInventory inventory, final ItemStack itemStack, final PlayerEntity player) {
        final TrackedInventoryBridge inv = (TrackedInventoryBridge) inventory;
        inv.bridge$setCaptureInventory(true);
        final boolean added = inventory.func_70441_a(itemStack);
        inv.bridge$setCaptureInventory(false);
        inv.bridge$getCapturedSlotTransactions();
        if (!SpongeCommonEventFactory.callPlayerChangeInventoryPickupEvent(player, inv)) {
            return false;
        }
        return added;
    }

}
