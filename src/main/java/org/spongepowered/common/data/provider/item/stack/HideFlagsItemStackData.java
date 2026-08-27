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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.Map;

public final class HideFlagsItemStackData {

    private HideFlagsItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        final var keyToComponentMapping = Map.of(
            Keys.HIDE_ATTRIBUTES, DataComponents.ATTRIBUTE_MODIFIERS,
            Keys.HIDE_ENCHANTMENTS, DataComponents.ENCHANTMENTS,
            Keys.HIDE_CAN_DESTROY, DataComponents.CAN_BREAK,
            Keys.HIDE_CAN_PLACE, DataComponents.CAN_PLACE_ON,
            Keys.HIDE_STORED_ENCHANTMENTS, DataComponents.STORED_ENCHANTMENTS,
            Keys.HIDE_UNBREAKABLE, DataComponents.UNBREAKABLE,
            Keys.HIDE_MISCELLANEOUS, DataComponents.CUSTOM_DATA,
            Keys.HIDE_POTION_EFFECTS, DataComponents.POTION_CONTENTS
        );

        final var mutableItem = registrator
                .asMutable(ItemStack.class);
        for (final var entry : keyToComponentMapping.entrySet()) {
            mutableItem
                    .create(entry.getKey())
                        .get(h -> h.has(DataComponents.TOOLTIP_DISPLAY) && !h.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).shows(entry.getValue()))
                        .set((h, v) -> h.update(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT, p -> p.withHidden(entry.getValue(), v)));
        }
        mutableItem
                    .create(Keys.HIDE_TOOLTIP)
                        .get(h -> h.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).hideTooltip())
                        .set((h, v) -> h.update(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT, p -> new TooltipDisplay(v, p.hiddenComponents())))
        ;


        // TODO missing show_in_tooltip (DYED_COLOR, more?)
    }
    // @formatter:on
//
//    @NotNull
//    private static AdventureModePredicate newAdventureModePredicate(final ItemStack h, final DataComponentType<AdventureModePredicate> old, final boolean showInTooltip) {
//        return new AdventureModePredicate(((AdventureModePredicateAccessor) h.get(old)).accessor$predicates(), showInTooltip);
//    }
//
//    @NotNull
//    private static ItemEnchantments newItemEnchantments(final ItemStack h, final boolean showInTooltip) {
//        final ItemEnchantmentsAccessor enchantments = (ItemEnchantmentsAccessor) h.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);;
//        return ItemEnchantmentsAccessor.invoker$new(enchantments.accessor$enchantments(), showInTooltip);
//    }

}
