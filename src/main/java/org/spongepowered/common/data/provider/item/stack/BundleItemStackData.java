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
package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Platform;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.item.component.BundleContentsAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public final class BundleItemStackData {

    private BundleItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.INVENTORY)
                        .get(h -> {
                            final @Nullable BundleContents contents = h.get(DataComponents.BUNDLE_CONTENTS);
                            if (contents == null) {
                                return null;
                            }
                            final var inventory = Inventory.builder().slots(contents.isEmpty() ? 1 : contents.size()).completeStructure()
                                .plugin(SpongeCommon.game().platform().container(Platform.Component.IMPLEMENTATION))
                                .build();
                            for (int i = 0; i < contents.size(); i++) {
                                inventory.set(i, ItemStackUtil.cloneDefensive(contents.getItemUnsafe(i)));
                            }
                            return inventory;
                        })
                        .setAnd((h, v) -> {
                            final var result = BundleContentsAccessor.invoker$checkAndCreate(v.slots().stream()
                                .map(slot -> ItemStackUtil.fromLikeToNativeCopy(slot.peek()))
                                .toList());
                            if (result.isError()) {
                                return false;
                            }
                            h.set(DataComponents.BUNDLE_CONTENTS, result.getOrThrow());
                            return true;
                        })
                        .delete(h -> h.remove(DataComponents.BUNDLE_CONTENTS))
                        .supports(h -> h.has(DataComponents.BUNDLE_CONTENTS))
                    .create(Keys.SLOT_INDEX)
                        .get(h -> BundleItem.hasSelectedItem(h) ? BundleItem.getSelectedItem(h) : null)
                    .create(Keys.SLOT_ITEM_STACK_SNAPSHOT)
                        .get(h -> {
                            if (!BundleItem.hasSelectedItem(h)) {
                                return null;
                            }
                            return ItemStackUtil.snapshotOf(BundleItem.getSelectedItemStack(h));
                        })
                        .setAnd((h, v) -> {
                            if (!BundleItem.hasSelectedItem(h)) {
                                return false;
                            }
                            final var contents = h.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
                            final List<ItemStack> itemsMutable = new ArrayList<>(contents.size());
                            contents.items().forEach(itemsMutable::add);
                            itemsMutable.set(BundleItem.getSelectedItem(h), ItemStackUtil.fromLikeToNativeCopy(v));
                            final var result = BundleContentsAccessor.invoker$checkAndCreate(itemsMutable);
                            if (result.isError()) {
                                return false;
                            }
                            h.set(DataComponents.BUNDLE_CONTENTS, result.getOrThrow());
                            return true;
                        });
    }
    // @formatter:on

}
