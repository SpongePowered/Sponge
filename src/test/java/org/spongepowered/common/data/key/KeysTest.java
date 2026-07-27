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
package org.spongepowered.common.data.key;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.ResourceKeyed;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ShieldDamageReduction;
import org.spongepowered.api.data.type.ShieldItemDamageFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.tag.DamageTypeTags;
import org.spongepowered.api.util.Ticks;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class KeysTest {

    private static Stream<Arguments> testSingleKeys() {
        return Stream.of(
            Arguments.of(Keys.WEAPON_DAMAGE_PER_ATTACK, 5, null),
            Arguments.of(Keys.DISABLE_SHIELD_TICKS, Ticks.of(10), null),
            Arguments.of(Keys.SHIELD_DEPLOY_TICKS, Ticks.of(15), null),
            Arguments.of(Keys.DISABLE_SHIELD_TICKS_SCALE, 2.5, null),
            Arguments.of(Keys.SHIELD_DAMAGE_REDUCTIONS, List.of(ShieldDamageReduction.of(ShieldDamageReduction.MultiplyAdd.builder()
                .horizontalBlockingAngle(45)
                .constantReduction(2)
                .fractionalReduction(0.5)
                .damageTypes(Set.of(DamageTypes.ARROW.get(), DamageTypes.PLAYER_ATTACK.get()))
                .build())), null),
            Arguments.of(Keys.SHIELD_ITEM_DAMAGE_FUNCTION, ShieldItemDamageFunction.of(ShieldItemDamageFunction.MultiplyAdd.builder()
                .constantDamage(5)
                .fractionalDamage(2)
                .minAttackDamage(2.5)
                .build()), null),
            Arguments.of(Keys.SHIELD_BLOCK_SOUND, SoundTypes.ENTITY_SHULKER_HURT.get(), null),
            Arguments.of(Keys.SHIELD_DISABLE_SOUND, SoundTypes.ENTITY_ENDER_DRAGON_DEATH.get(), null),
            Arguments.of(Keys.ALLOWED_ENTITIES, Set.of(EntityTypes.PLAYER.get(), EntityTypes.ZOMBIE.get()), ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE)),
            Arguments.of(Keys.CAMERA_OVERLAY, ResourceKey.minecraft("textures/misc/pumpkinblur.png"), ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE)),
            Arguments.of(Keys.CAN_BE_SHEARED, true, ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE)),
            Arguments.of(Keys.DAMAGE_ON_HURT, false, ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE)),
            Arguments.of(Keys.EQUIP_ON_INTERACT, true, ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE)),
            Arguments.of(Keys.EQUIP_SOUND, SoundTypes.ITEM_ARMOR_EQUIP_DIAMOND.get(), ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE)),
            Arguments.of(Keys.EQUIPMENT_TYPE, EquipmentTypes.HEAD.get(), null),
            Arguments.of(Keys.IS_DISPENSABLE, false, ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE)),
            Arguments.of(Keys.IS_SWAPPABLE, false, ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE)),
            Arguments.of(Keys.SHEARING_SOUND, SoundTypes.ENTITY_SHEEP_SHEAR.get(), ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE))
        );
    }

    @MethodSource
    @ParameterizedTest
    void testSingleKeys(Key<?> k, Object value, @Nullable ItemStack baseStack) {
        testSingleKeyUnchecked(k, value, baseStack);
    }

    @Test
    void testBypassDamageTag() {
        testSingleKey(Keys.BYPASS_DAMAGE_TAG, DamageTypeTags.BYPASSES_ARMOR, ResourceKeyed::key, null);
    }

    @Test
    void testAllWeaponKeys() {
        final var stack = ItemStack.builder()
            .itemType(ItemTypes.DIAMOND_SWORD)
            .add(Keys.WEAPON_DAMAGE_PER_ATTACK, 5)
            .add(Keys.DISABLE_SHIELD_TICKS, Ticks.of(5))
            .build();

        Assertions.assertEquals(5, stack.require(Keys.WEAPON_DAMAGE_PER_ATTACK));
        Assertions.assertEquals(Ticks.of(5), stack.require(Keys.DISABLE_SHIELD_TICKS));

        stack.remove(Keys.DISABLE_SHIELD_TICKS);
        Assertions.assertEquals(5, stack.require(Keys.WEAPON_DAMAGE_PER_ATTACK));

        stack.remove(Keys.WEAPON_DAMAGE_PER_ATTACK);

        Assertions.assertNull(stack.getOrNull(Keys.WEAPON_DAMAGE_PER_ATTACK));
        Assertions.assertNull(stack.getOrNull(Keys.DISABLE_SHIELD_TICKS));
    }

    @SuppressWarnings("unchecked")
    private static <T, V extends Value<T>> void testSingleKeyUnchecked(Key<?> k, Object value, @Nullable ItemStack baseStack) {
        if (!GenericTypeReflector.isSuperType(k.elementType(), value.getClass())) {
            throw new IllegalArgumentException("Invalid value type for key " + k.key() + ": " + value.getClass().getName());
        }

        testSingleKey((Key<V>) k, (T) value, a -> a, baseStack);
    }

    private static <T, V extends Value<T>> void testSingleKey(Key<V> k, T value, Function<T, Object> equalityExtractor, @Nullable ItemStack baseStack) {
        final ItemStack stack = Objects.requireNonNullElseGet(baseStack, () -> ItemStack.of(ItemTypes.DIAMOND_SWORD));
        stack.offer(k, value);

        Assertions.assertEquals(equalityExtractor.apply(value), equalityExtractor.apply(stack.require(k)), () -> "retrieved value is not equal " +
            "to the original for " + k.key().asString());
    }

}
