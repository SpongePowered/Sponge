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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Optional;

public class PotionEntityItemProvider extends GenericMutableDataProvider<PotionEntity, ItemStackSnapshot> {

    public PotionEntityItemProvider() {
        super(Keys.ITEM_STACK_SNAPSHOT);
    }

    @Override
    protected Optional<ItemStackSnapshot> getFrom(PotionEntity dataHolder) {
        return Optional.of(ItemStackUtil.snapshotOf(dataHolder.getItem()));
    }

    @Override
    protected boolean set(PotionEntity dataHolder, ItemStackSnapshot value) {
        final ItemStack itemStack = ItemStackUtil.fromSnapshotToNative(value);
        if (itemStack.getItem() != Items.SPLASH_POTION && itemStack.getItem() != Items.LINGERING_POTION) {
            // Minecraft will throw a hissy fit if we do allow any other type of potion
            // so, we have to return false bec≈ause the item stack is invalid.
            return false;
        }
        dataHolder.setItem(itemStack);
        return true;
    }
}
