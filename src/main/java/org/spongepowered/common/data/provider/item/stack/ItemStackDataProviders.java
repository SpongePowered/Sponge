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

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.provider.util.BreakablePlaceableUtils;
import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.data.provider.DataProviderRegistryBuilder;
import org.spongepowered.common.util.Constants;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItemStackDataProviders extends DataProviderRegistryBuilder {

    public ItemStackDataProviders(DataProviderRegistry registry) {
        super(registry);
    }

    protected <E> void register(Key<? extends Value<E>> key, Function<ItemStack, E> getter) {
        this.register(ItemStack.class, key, getter);
    }

    protected <E> void register(Supplier<? extends Key<? extends Value<E>>> key, Function<ItemStack, E> getter) {
        this.register(ItemStack.class, key, getter);
    }

    protected <E> void register(Key<? extends Value<E>> key, Function<ItemStack, E> getter, BiConsumer<ItemStack, E> setter) {
        this.register(ItemStack.class, key, getter, setter);
    }

    protected <E> void register(Supplier<? extends Key<? extends Value<E>>> key, Function<ItemStack, E> getter, BiConsumer<ItemStack, E> setter) {
        this.register(ItemStack.class, key, getter, setter);
    }

    protected <E> void register(Key<? extends Value<E>> key, E defaultValue, Function<ItemStack, E> getter, BiConsumer<ItemStack, E> setter) {
        this.register(ItemStack.class, key, defaultValue, getter, setter);
    }

    protected <E> void register(Supplier<? extends Key<? extends Value<E>>> key, E defaultValue, Function<ItemStack, E> getter, BiConsumer<ItemStack, E> setter) {
        this.register(ItemStack.class, key, defaultValue, getter, setter);
    }

    @Override
    public void register() {
        register(new ItemStackDisplayNameProvider());
        register(new ItemStackDurabilityProvider());
        register(new ItemStackIsUnbreakableProvider());
        register(new ItemStackLockTokenProvider());
        register(new ItemStackLoreProvider());
        register(new ItemStackAppliedEnchantmentsProvider());
        register(new ItemStackStoredEnchantmentsProvider());

        register(Keys.PLACEABLE_BLOCK_TYPES, ImmutableSet.of(),
                (accessor) -> BreakablePlaceableUtils.get(accessor, Constants.Item.ITEM_PLACEABLE_BLOCKS),
                (accessor, value) -> BreakablePlaceableUtils.set(accessor, Constants.Item.ITEM_PLACEABLE_BLOCKS, value));

        register(Keys.BREAKABLE_BLOCK_TYPES, ImmutableSet.of(),
                (accessor) -> BreakablePlaceableUtils.get(accessor, Constants.Item.ITEM_BREAKABLE_BLOCKS),
                (accessor, value) -> BreakablePlaceableUtils.set(accessor, Constants.Item.ITEM_BREAKABLE_BLOCKS, value));

        register(new ItemStackHideFlagsValueProvider(Keys.HIDE_ATTRIBUTES, Constants.Item.HIDE_ATTRIBUTES_FLAG));
        register(new ItemStackHideFlagsValueProvider(Keys.HIDE_CAN_DESTROY, Constants.Item.HIDE_CAN_DESTROY_FLAG));
        register(new ItemStackHideFlagsValueProvider(Keys.HIDE_CAN_PLACE, Constants.Item.HIDE_CAN_PLACE_FLAG));
        register(new ItemStackHideFlagsValueProvider(Keys.HIDE_ENCHANTMENTS, Constants.Item.HIDE_ENCHANTMENTS_FLAG));
        register(new ItemStackHideFlagsValueProvider(Keys.HIDE_MISCELLANEOUS, Constants.Item.HIDE_MISCELLANEOUS_FLAG));
        register(new ItemStackHideFlagsValueProvider(Keys.HIDE_UNBREAKABLE, Constants.Item.HIDE_UNBREAKABLE_FLAG));

        register(new ItemStackBookAuthorProvider());
        register(new ItemStackBookGenerationProvider());
        register(new ItemStackBookPagesProvider());
        register(new ItemStackPlainBookPagesProvider());

        register(new ItemStackFireworkEffectsProvider());
        register(new ItemStackFireworkFlightModifierProvider());

        register(new ItemStackPotionColorProvider());
        register(new ItemStackPotionEffectsProvider());
        register(new ItemStackPotionTypeProvider());

        register(new ItemStackShieldBannerBaseColorProvider());
        register(new ItemStackShieldBannerPatternsProvider());

        register(new ItemStackSignLinesProvider());

        // Properties
        register(new ItemStackFuelBurnTimeProvider());
        register(new ItemStackEfficiencyProvider());
        register(new ItemStackFoodRestorationProvider());
        register(new ItemStackHarvestingProvider());
        register(new ItemStackMusicDiscProvider());
        register(new ItemStackUseLimitProvider());
        register(new ItemStackToolTypeProvider());
        register(new ItemStackArmorTypeProvider());
        register(new ItemStackEquipmentTypeProvider());
        register(new ItemStackDamageAbsorbtionProvider());

    }
}
