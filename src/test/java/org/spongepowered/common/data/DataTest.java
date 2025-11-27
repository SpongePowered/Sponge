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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.type.*;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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

    @Disabled
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

        DataTest.checkGetWeightedData(ItemStack.of(ItemTypes.ENCHANTED_GOLDEN_APPLE), Keys.APPLICABLE_POTION_EFFECTS, effects);
        DataTest.checkOfferData(this.location.createEntity(EntityTypes.SHEEP.get()), Keys.POTION_EFFECTS, effects);
        DataTest.checkOfferData(this.location.createEntity(EntityTypes.AREA_EFFECT_CLOUD.get()), Keys.POTION_EFFECTS, effects);
        DataTest.checkOfferData(ItemStack.of(ItemTypes.POTION), Keys.POTION_EFFECTS, effects);
        DataTest.checkOfferData(ItemStack.of(ItemTypes.SPLASH_POTION), Keys.POTION_EFFECTS, effects);
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
        DataTest.checkOfferData(bannerEntity, Keys.DYE_COLOR, DyeColors.PINK.get());

        DataTest.checkOfferData(shieldStack, Keys.HIDE_MISCELLANEOUS, true);
    }

    public static <T> void checkOfferData(final DataHolder.Mutable holder, final Key<? extends Value<T>> key, final T value) {
        final DataTransactionResult result = holder.offer(key, value);
        Assertions.assertTrue(result.isSuccessful(), () -> "Failed offer on " + DataTest.holderName(holder) + " for " + key.key().asString() + " with " + value + ".");
        DataTest.checkGetData(holder, key, value);
    }

    public static <T> void checkWithData(final DataHolder.Immutable<?> holder, final Key<? extends Value<T>> key, final T value) {
        final DataHolder.Immutable<?> newHolder = holder.with(key, value).get();
        DataTest.checkGetData(newHolder, key, value);
    }

    public static <T> void checkGetWeightedData(final DataHolder holder, final Key<WeightedCollectionValue<T>> key, final List<T> expected) {
        DataTest.assertDataEquals(expected, holder.get(key).map(table -> table.get(new Random(0))).orElse(null), holder, key);
    }

    public static <T> void checkGetData(final DataHolder holder, final Key<? extends Value<T>> key, final T expected) {
        DataTest.assertDataEquals(expected, holder.get(key).orElse(null), holder, key);
    }

    private static void assertDataEquals(final Object expected, final Object actual, final DataHolder holder, final Key<?> key) {
        Assertions.assertEquals(expected, actual, () -> "Value differs on " + DataTest.holderName(holder) + " for " + key.key().asString() + ".");
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
