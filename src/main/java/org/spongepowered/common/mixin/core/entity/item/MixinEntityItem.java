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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.bridge.entity.ItemEntityBridge;
import org.spongepowered.common.interfaces.entity.player.IMixinInventoryPlayer;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

@Mixin(EntityItem.class)
public abstract class MixinEntityItem extends MixinEntity implements ItemEntityBridge {

    private static final int MAGIC_PREVIOUS = -1;
    @Shadow private int pickupDelay;
    @Shadow public int age;
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
    private double impl$changeSearchRadiusFromConfig(double originalRadius) {
        if (this.world.isRemote || ((WorldBridge) this.world).isFake()) {
            return originalRadius;
        }
        if (this.cachedRadius == -1) {
            final double configRadius = ((IMixinWorldInfo) this.world.getWorldInfo()).getConfigAdapter().getConfig().getWorld().getItemMergeRadius();
            this.cachedRadius = configRadius < 0 ? 0 : configRadius;
        }
        return this.cachedRadius;
    }

    @Override
    public int bridge$getPickupDelay() {
        return this.infinitePickupDelay ? this.previousPickupDelay : this.pickupDelay;
    }

    @Override
    public void bridge$setPickupDelay(int delay, boolean infinite) {
        this.pickupDelay = delay;
        boolean previous = this.infinitePickupDelay;
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
    public void bridge$setDespawnDelay(int delay) {
        this.age = 6000 - delay;
    }

    @Override
    public void bridge$setDespawnDelay(int delay, boolean infinite) {
        this.age = 6000 - delay;
        boolean previous = this.infiniteDespawnDelay;
        this.infiniteDespawnDelay = infinite;
        if (infinite && !previous) {
            this.previousDespawnDelay = this.age;
            this.age = Constants.Entity.Item.MAGIC_NO_DESPAWN;
        } else if (!infinite) {
            this.previousDespawnDelay = MAGIC_PREVIOUS;
        }
    }

    @Override
    public void spongeImpl$readFromSpongeCompound(NBTTagCompound compound) {
        super.spongeImpl$readFromSpongeCompound(compound);

        this.infinitePickupDelay = compound.getBoolean(NbtDataUtil.INFINITE_PICKUP_DELAY);
        if (compound.hasKey(NbtDataUtil.PREVIOUS_PICKUP_DELAY, NbtDataUtil.TAG_ANY_NUMERIC)) {
            this.previousPickupDelay = compound.getInteger(NbtDataUtil.PREVIOUS_PICKUP_DELAY);
        } else {
            this.previousPickupDelay = MAGIC_PREVIOUS;
        }
        this.infiniteDespawnDelay = compound.getBoolean(NbtDataUtil.INFINITE_DESPAWN_DELAY);
        if (compound.hasKey(NbtDataUtil.PREVIOUS_DESPAWN_DELAY, NbtDataUtil.TAG_ANY_NUMERIC)) {
            this.previousDespawnDelay = compound.getInteger(NbtDataUtil.PREVIOUS_DESPAWN_DELAY);
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
    public void spongeImpl$writeToSpongeCompound(NBTTagCompound compound) {
        super.spongeImpl$writeToSpongeCompound(compound);

        compound.setBoolean(NbtDataUtil.INFINITE_PICKUP_DELAY, this.infinitePickupDelay);
        compound.setShort(NbtDataUtil.PREVIOUS_PICKUP_DELAY, (short) this.previousPickupDelay);
        compound.setBoolean(NbtDataUtil.INFINITE_DESPAWN_DELAY, this.infiniteDespawnDelay);
        compound.setShort(NbtDataUtil.PREVIOUS_DESPAWN_DELAY, (short) this.previousDespawnDelay);
    }

    @Inject(
        method = "onCollideWithPlayer",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/entity/item/EntityItem;getItem()Lnet/minecraft/item/ItemStack;"),
        cancellable = true
    )
    private void spongeImpl$ThrowPickupEvent(EntityPlayer entityIn, CallbackInfo ci) {
        if (!SpongeCommonEventFactory.callPlayerChangeInventoryPickupPreEvent(entityIn, (EntityItem) (Object) this, this.pickupDelay)) {
            ci.cancel();
        }
    }

    @Redirect(method = "onCollideWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;addItemStackToInventory(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean spongeImpl$throwPikcupEventForAddItem(InventoryPlayer inventory, ItemStack itemStack, EntityPlayer player) {
        IMixinInventoryPlayer inv = (IMixinInventoryPlayer) inventory;
        inv.setCapture(true);
        boolean added = inventory.addItemStackToInventory(itemStack);
        inv.setCapture(false);
        inv.getCapturedTransactions();
        if (!SpongeCommonEventFactory.callPlayerChangeInventoryPickupEvent(player, inv)) {
            return false;
        }
        return added;
    }

}
