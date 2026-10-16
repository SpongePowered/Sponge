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
package org.spongepowered.common.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.type.*;
import org.spongepowered.api.data.value.CollectionValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.WeightedCollectionValue;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

public final class DataTest {
    private ServerLocation location;

    @BeforeEach
    public void prepare() {
        this.location = Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get().location(Vector3d.ZERO);
    }

    @Test
    public void testCreatorAndNotifier() {
        final UUID playerId = UUID.randomUUID();
        DataTest.checkOfferData(this.location, Keys.CREATOR, playerId);
        DataTest.checkOfferData(this.location, Keys.NOTIFIER, playerId);
    }

    @Test
    public void testBiomeTemperature() {
        DataTest.checkGetData(this.location, Keys.BIOME_TEMPERATURE, this.location.biome().temperature());
    }

    @Disabled
    @Test
    public void testPotionEffects() {
        final List<PotionEffect> effects = List.of(
            PotionEffect.builder().potionType(PotionEffectTypes.REGENERATION).amplifier(1).ambient(false).duration(Ticks.of(400)).build(),
            PotionEffect.builder().potionType(PotionEffectTypes.RESISTANCE).amplifier(0).ambient(false).duration(Ticks.of(6000)).build(),
            PotionEffect.builder().potionType(PotionEffectTypes.FIRE_RESISTANCE).amplifier(0).ambient(false).duration(Ticks.of(6000)).build(),
            PotionEffect.builder().potionType(PotionEffectTypes.ABSORPTION).amplifier(3).ambient(false).duration(Ticks.of(2400)).build());

        DataTest.checkOfferCollectionIgnoringOrder(this.location.createEntity(EntityTypes.SHEEP.get()), Keys.POTION_EFFECTS, effects);
        DataTest.checkOfferCollectionIgnoringOrder(this.location.createEntity(EntityTypes.AREA_EFFECT_CLOUD.get()), Keys.POTION_EFFECTS, effects);
        DataTest.checkOfferCollectionIgnoringOrder(ItemStack.of(ItemTypes.POTION), Keys.CUSTOM_POTION_EFFECTS, effects);
        DataTest.checkOfferCollectionIgnoringOrder(ItemStack.of(ItemTypes.SPLASH_POTION), Keys.CUSTOM_POTION_EFFECTS, effects);
    }

    @Disabled
    @Test
    public void testBanner() {
        final List<BannerPatternLayer> pattern = Arrays.asList(BannerPatternLayer.of(BannerPatternShapes.BASE, DyeColors.BLACK), BannerPatternLayer.of(BannerPatternShapes.RHOMBUS, DyeColors.ORANGE));

        final ItemStack shieldStack = ItemStack.of(ItemTypes.SHIELD);
        DataTest.checkGetData(shieldStack, Keys.BANNER_PATTERN_LAYERS, Collections.emptyList());
        DataTest.checkOfferData(shieldStack, Keys.BANNER_PATTERN_LAYERS,pattern);

        final ItemStack bannerStack = ItemStack.of(ItemTypes.RED_BANNER);
        DataTest.checkGetData(bannerStack, Keys.BANNER_PATTERN_LAYERS,  Collections.emptyList());
        DataTest.checkOfferData(bannerStack, Keys.BANNER_PATTERN_LAYERS, pattern);

        this.location.setBlock(BlockTypes.RED_BANNER.get().defaultState());
        final BlockEntity bannerEntity = this.location.blockEntity().get();
        DataTest.checkOfferData(bannerEntity, Keys.BANNER_PATTERN_LAYERS, pattern);
        DataTest.checkGetData(bannerEntity, Keys.DYE_COLOR, DyeColors.RED.get());

        DataTest.checkOfferData(shieldStack, Keys.HIDE_MISCELLANEOUS, true);
    }

    public static <T> void checkOfferData(final DataHolder.Mutable holder, final Key<? extends Value<T>> key, final T value) {
        DataTest.checkOfferSuccessful(holder, key, value);
        DataTest.checkGetData(holder, key, value);
    }

    public static <E, C extends Collection<E>> void checkOfferCollection(final DataHolder.Mutable holder, final Key<? extends CollectionValue<E, C>> key, final C value) {
        DataTest.checkOfferSuccessful(holder, key, value);
        DataTest.checkGetCollection(holder, key, value);
    }

    public static <E, C extends Collection<E>> void checkOfferCollectionIgnoringOrder(final DataHolder.Mutable holder, final Key<? extends CollectionValue<E, C>> key, final C value) {
        DataTest.checkOfferSuccessful(holder, key, value);
        DataTest.checkGetCollectionIgnoringOrder(holder, key, value);
    }

    private static <T> void checkOfferSuccessful(final DataHolder.Mutable holder, final Key<? extends Value<T>> key, final T value) {
        Assertions.assertTrue(holder.offer(key, value).isSuccessful(), () -> "Failed offer on " + DataTest.holderName(holder) + " for " + key.key().asString() + " with " + value + ".");
    }

    public static <T> void checkWithData(final DataHolder.Immutable<?> holder, final Key<? extends Value<T>> key, final T value) {
        final DataHolder.Immutable<?> newHolder = holder.with(key, value).get();
        DataTest.checkGetData(newHolder, key, value);
    }

    public static <T> void checkGetWeightedData(final DataHolder holder, final Key<WeightedCollectionValue<T>> key, final List<T> expected) {
        DataTest.assertCollectionEquals(expected, holder.get(key).map(table -> table.get(new Random(0))).orElse(null), DataTest.checkMessage(holder, key));
    }

    public static <T> void checkGetData(final DataHolder holder, final Key<? extends Value<T>> key, final T expected) {
        Assertions.assertEquals(expected, holder.get(key).orElse(null), DataTest.checkMessage(holder, key));
    }

    public static <E, C extends Collection<E>> void checkGetCollection(final DataHolder holder, final Key<? extends CollectionValue<E, C>> key, final C expected) {
        DataTest.assertCollectionEquals(expected, holder.get(key).orElse(null), DataTest.checkMessage(holder, key));
    }

    public static <E, C extends Collection<E>> void checkGetCollectionIgnoringOrder(final DataHolder holder, final Key<? extends CollectionValue<E, C>> key, final C expected) {
        DataTest.assertCollectionEqualsIgnoringOrder(expected, holder.get(key).orElse(null), DataTest.checkMessage(holder, key));
    }

    private static void assertCollectionEquals(final Collection<?> expected, final Collection<?> actual, final Supplier<String> message) {
        Assertions.assertArrayEquals(expected.toArray(), actual.toArray(), message);
    }

    private static <T> void assertCollectionEqualsIgnoringOrder(final Collection<T> expected, final Collection<T> actual, final Supplier<String> message) {
        Assertions.assertEquals(expected.size(), actual.size(), () -> message + " Size differs.");
        final List<T> expectedList = new ArrayList<>(expected);
        final List<T> actualList = new ArrayList<>(actual);
        expectedList.removeIf(actualList::remove);
        Assertions.assertTrue(expectedList.isEmpty(), () -> message + " Expected elements not found: " + expectedList);
        Assertions.assertTrue(actualList.isEmpty(), () -> message + " Found unexpected elements: " + actualList);
    }

    private static Supplier<String> checkMessage(final DataHolder holder, final Key<?> key) {
        return () -> "Value differs on " + DataTest.holderName(holder) + " for " + key.key().asString() + ".";
    }

    private static String holderName(final DataHolder holder) {
        String value = "";
        if (holder instanceof BlockState) {
            value = RegistryTypes.BLOCK_TYPE.keyFor(Sponge.game(), ((BlockState) holder).type()).value();
        } else if (holder instanceof ItemStack) {
            value = RegistryTypes.ITEM_TYPE.keyFor(Sponge.game(), ((ItemStack) holder).type()).value();
        }
        return String.format("%s[%s]", holder.getClass().getSimpleName(), value);
    }
}
