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

import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.display.BillboardType;
import org.spongepowered.api.entity.display.ItemDisplayType;
import org.spongepowered.common.accessor.world.entity.DisplayAccessor;
import org.spongepowered.common.accessor.world.entity.Display_ItemDisplayAccessor;
import org.spongepowered.common.accessor.world.entity.Display_TextDisplayAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.item.util.ItemStackUtil;

public class DisplayEntityData {

    private DisplayEntityData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Display.class)
                    .create(Keys.BILLBOARD_TYPE)
                        .get(h -> (BillboardType) (Object) h.getBillboardConstraints())
                        .set((h, v) -> ((DisplayAccessor)h).invoker$setBillboardConstraints((Display.BillboardConstraints) (Object) v))
                .asMutable(Display.BlockDisplay.class)
                    .create(Keys.BLOCK_STATE)
                        .get(h -> ((BlockState) h.getBlockState()))
                        .set((h, v) -> h.setBlockState((net.minecraft.world.level.block.state.BlockState) v))
                .asMutable(Display.ItemDisplay.class)
                    .create(Keys.ITEM_STACK_SNAPSHOT)
                        .get(h -> ItemStackUtil.snapshotOf(h.getItemStack()))
                        .set((h, v) -> h.getSlot(0).set(ItemStackUtil.fromSnapshotToNative(v)))
                    .create(Keys.ITEM_DISPLAY_TYPE)
                        .get(h -> (ItemDisplayType) (Object) h.getItemTransform())
                        .set((h, v) -> ((Display_ItemDisplayAccessor) h).invoker$setItemTransform(((ItemDisplayContext) (Object) v)))
                .asMutable(Display.TextDisplay.class)
                    .create(Keys.DISPLAY_NAME)
                        .get(h -> SpongeAdventure.asAdventure(h.getText()))
                        .set((h, v) -> ((Display_TextDisplayAccessor) h).invoker$setText(SpongeAdventure.asVanilla(v)))
        ;
    }
    // @formatter:on
}
