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
package org.spongepowered.common.mixin.api.minecraft.entity.item;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.entity.player.IMixinInventoryPlayer;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.api.minecraft.entity.MixinEntity_API;

import java.util.List;

@Mixin(EntityItem.class)
public abstract class MixinEntityItem_API extends MixinEntity_API implements Item {

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

    @ModifyConstant(method = "searchForOtherItemsNearby", constant = @Constant(doubleValue = 0.5D))
    private double getSearchRadius(double originalRadius) {
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
    public Translation getTranslation() {
        return getItemData().item().get().getType().getTranslation();
    }

    @Override
    public ItemType getItemType() {
        return (ItemType) getItem().getItem();
    }

    @Inject(method = "onCollideWithPlayer", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/entity/item/EntityItem;getItem()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    public void onPlayerItemPickup(EntityPlayer entityIn, CallbackInfo ci) {
        if (!SpongeCommonEventFactory.callPlayerChangeInventoryPickupPreEvent(entityIn, (EntityItem) (Object) this, this.pickupDelay, this.getCreator().orElse(null))) {
            ci.cancel();
        }
    }

    @Redirect(method = "onCollideWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;addItemStackToInventory(Lnet/minecraft/item/ItemStack;)Z"))
    public boolean onAddItemStackToInventory(InventoryPlayer inventory, ItemStack itemStack, EntityPlayer player) {
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

    // TODO non pre event

    // Data delegated methods - Reduces potentially expensive lookups for accessing guaranteed data

    @Override
    public RepresentedItemData getItemData() {
        return new SpongeRepresentedItemData(ItemStackUtil.snapshotOf(getItem()));
    }

    @Override
    public Value<ItemStackSnapshot> item() {
        return new SpongeValue<>(Keys.REPRESENTED_ITEM, ItemStackSnapshot.NONE, ItemStackUtil.snapshotOf(getItem()));
    }

    @Override
    public void spongeApi$supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(getItemData());
    }
}
