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
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.entity.item.IMixinEntityItem;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

import java.util.List;

@Mixin(EntityItem.class)
public abstract class MixinEntityItem extends MixinEntity implements Item, IMixinEntityItem {

    @Shadow private int delayBeforeCanPickup;
    @Shadow private int age;

    @Shadow public abstract ItemStack getEntityItem();

    public float dropChance = 1.0f;

    @Override
    public int getPickupDelay() {
        return this.delayBeforeCanPickup;
    }

    @Override
    public int getDespawnDelay() {
        return this.age;
    }

    @Override
    public void setDespawnDelay(int delay) {
        this.age = delay;
    }

    @Override
    public Translation getTranslation() {
        return getItemData().item().get().getType().getTranslation();
    }

    @Override
    public ItemType getItemType() {
        return (ItemType) getEntityItem().getItem();
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
