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
package org.spongepowered.common.registry.builtin.sponge;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.handler.ClickHandler;
import org.spongepowered.api.item.inventory.menu.handler.KeySwapHandler;
import org.spongepowered.api.item.inventory.menu.handler.SlotClickHandler;
import org.spongepowered.common.inventory.menu.handler.SpongeClickType;

import java.util.stream.Stream;

public final class ClickTypeStreamGenerator {

    private ClickTypeStreamGenerator() {
    }

    public static Stream<ClickType<?>> stream() {
        return Stream.of(
                new SpongeClickType<SlotClickHandler>(ResourceKey.sponge("click_left")),
                new SpongeClickType<ClickHandler>(ResourceKey.sponge("click_left_outside")),
                new SpongeClickType<SlotClickHandler>(ResourceKey.sponge("click_middle")),
                new SpongeClickType<SlotClickHandler>(ResourceKey.sponge("click_right")),
                new SpongeClickType<ClickHandler>(ResourceKey.sponge("click_right_outside")),
                new SpongeClickType<SlotClickHandler>(ResourceKey.sponge("double_click")),
                new SpongeClickType<ClickHandler>(ResourceKey.sponge("drag_end")),
                new SpongeClickType<SlotClickHandler>(ResourceKey.sponge("drag_left_add")),
                new SpongeClickType<SlotClickHandler>(ResourceKey.sponge("drag_middle_add")),
                new SpongeClickType<SlotClickHandler>(ResourceKey.sponge("drag_right_add")),
                new SpongeClickType<ClickHandler>(ResourceKey.sponge("drag_start")),
                new SpongeClickType<KeySwapHandler>(ResourceKey.sponge("key_swap")),
                new SpongeClickType<SlotClickHandler>(ResourceKey.sponge("key_throw_all")),
                new SpongeClickType<SlotClickHandler>(ResourceKey.sponge("key_throw_one")),
                new SpongeClickType<SlotClickHandler>(ResourceKey.sponge("shift_click_left")),
                new SpongeClickType<SlotClickHandler>(ResourceKey.sponge("shift_click_right"))
        );
    }
}




