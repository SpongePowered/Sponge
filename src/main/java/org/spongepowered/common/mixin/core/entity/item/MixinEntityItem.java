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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.entity.item.IMixinEntityItem;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

import java.util.List;

@Mixin(EntityItem.class)
public abstract class MixinEntityItem extends MixinEntity implements Item, IMixinEntityItem {

    private static final int MAGIC_PREVIOUS = -1;
    @Shadow private int delayBeforeCanPickup;
    @Shadow private int age;
    @Shadow public abstract ItemStack getEntityItem();
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
    public boolean infinitePickupDelay() {
        return this.infinitePickupDelay;
    }

    @Inject(method = "onUpdate()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;setDead()V"))
    public void onEntityItemUpdate(CallbackInfo ci) {
        this.destructCause = Cause.of(NamedCause.of("ExpiredItem", this));
    }

    @ModifyConstant(method = "searchForOtherItemsNearby", constant = @Constant(doubleValue = 0.5D))
    private double getSearchRadius(double originalRadius) {
        if (this.worldObj.isRemote) {
            return originalRadius;
        }
        if (this.cachedRadius == -1) {
            final double configRadius = ((IMixinWorldServer) this.worldObj).getActiveConfig().getConfig().getWorld().getItemMergeRadius();
            this.cachedRadius = configRadius < 0 ? 0 : configRadius;
        }
        return this.cachedRadius;
    }

    @Override
    public int getPickupDelay() {
        return this.infinitePickupDelay ? this.previousPickupDelay : this.delayBeforeCanPickup;
    }

    @Override
    public void setPickupDelay(int delay, boolean infinite) {
        this.delayBeforeCanPickup = delay;
        boolean previous = this.infinitePickupDelay;
        this.infinitePickupDelay = infinite;
        if (infinite && !previous) {
            this.previousPickupDelay = this.delayBeforeCanPickup;
            this.delayBeforeCanPickup = DataConstants.Entity.Item.MAGIC_NO_PICKUP;
        } else if (!infinite) {
            this.previousPickupDelay = MAGIC_PREVIOUS;
        }
    }

    @Override
    public boolean infiniteDespawnDelay() {
        return this.infiniteDespawnDelay;
    }

    @Override
    public int getDespawnDelay() {
        return this.infiniteDespawnDelay ? this.previousDespawnDelay : this.age;
    }

    @Override
    public void setDespawnDelay(int delay, boolean infinite) {
        this.age = delay;
        boolean previous = this.infiniteDespawnDelay;
        this.infiniteDespawnDelay = infinite;
        if (infinite && !previous) {
            this.previousDespawnDelay = this.age;
            this.age = DataConstants.Entity.Item.MAGIC_NO_DESPAWN;
        } else if (!infinite) {
            this.previousDespawnDelay = MAGIC_PREVIOUS;
        }
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);

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
            if (this.previousPickupDelay != this.delayBeforeCanPickup) {
                this.previousPickupDelay = this.delayBeforeCanPickup;
            }

            this.delayBeforeCanPickup = DataConstants.Entity.Item.MAGIC_NO_PICKUP;
        } else if (this.delayBeforeCanPickup == DataConstants.Entity.Item.MAGIC_NO_PICKUP && this.previousPickupDelay != MAGIC_PREVIOUS) {
            this.delayBeforeCanPickup = this.previousPickupDelay;
            this.previousPickupDelay = MAGIC_PREVIOUS;
        }

        if (this.infiniteDespawnDelay) {
            if (this.previousDespawnDelay != this.age) {
                this.previousDespawnDelay = this.age;
            }

            this.age = DataConstants.Entity.Item.MAGIC_NO_DESPAWN;
        } else if (this.age == DataConstants.Entity.Item.MAGIC_NO_DESPAWN && this.previousDespawnDelay != MAGIC_PREVIOUS) {
            this.age = this.previousDespawnDelay;
            this.previousDespawnDelay = MAGIC_PREVIOUS;
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);

        compound.setBoolean(NbtDataUtil.INFINITE_PICKUP_DELAY, this.infinitePickupDelay);
        compound.setShort(NbtDataUtil.PREVIOUS_PICKUP_DELAY, (short) this.previousPickupDelay);
        compound.setBoolean(NbtDataUtil.INFINITE_DESPAWN_DELAY, this.infiniteDespawnDelay);
        compound.setShort(NbtDataUtil.PREVIOUS_DESPAWN_DELAY, (short) this.previousDespawnDelay);
    }

    @Override
    public Translation getTranslation() {
        return getItemData().item().get().getType().getTranslation();
    }

    @Override
    public ItemType getItemType() {
        return (ItemType) getEntityItem().getItem();
    }

    @Inject(method = "combineItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;setDead()V"))
    public void onCombineItems(EntityItem other, CallbackInfoReturnable<Boolean> cir) {
        this.destructCause = Cause.of(NamedCause.of("CombinedItem", other));
    }

    @Inject(method = "onCollideWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;getEntityItem()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    public void onPlayerItemPickup(EntityPlayer entityIn, CallbackInfo ci) {
        if (!SpongeCommonEventFactory.callPlayerChangeInventoryPickupEvent(entityIn, (EntityItem)(Object) this, this.delayBeforeCanPickup, ((Entity)(Object) this).getCreator().orElse(null))) {
            ci.cancel();
        }
    }

    // Data delegated methods - Reduces potentially expensive lookups for accessing guaranteed data

    @Override
    public RepresentedItemData getItemData() {
        return new SpongeRepresentedItemData(ItemStackUtil.createSnapshot(getEntityItem()));
    }

    @Override
    public Value<ItemStackSnapshot> item() {
        return new SpongeValue<>(Keys.REPRESENTED_ITEM, ItemStackSnapshot.NONE, ItemStackUtil.createSnapshot(getEntityItem()));
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getItemData());
    }
}
