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
package org.spongepowered.common.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.accessor.world.entity.projectile.FireworkRocketEntityAccessor;
import org.spongepowered.common.item.SpongeItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class FireworkUtil {

    public static boolean setFireworkEffects(final Object object, final List<? extends FireworkEffect> effects) {
        if (effects.isEmpty()) {
            return FireworkUtil.removeFireworkEffects(object);
        }
        final ItemStack item = FireworkUtil.getItem(object);
        if (item.isEmpty()) {
            return false;
        }

        if (item.getItem() == Items.FIREWORK_STAR) {
            item.set(DataComponents.FIREWORK_EXPLOSION, (FireworkExplosion) (Object) effects.get(0));
            return true;
        } else if (item.getItem() == Items.FIREWORK_ROCKET) {
            final List<FireworkExplosion> mcEffects = effects.stream().map(FireworkExplosion.class::cast).toList();
            item.update(DataComponents.FIREWORKS, new Fireworks(1, Collections.emptyList()), p -> new Fireworks(p.flightDuration(), mcEffects));
            return true;
        }
        return false;
    }

    public static Optional<List<FireworkEffect>> getFireworkEffects(final Object object) {
        final ItemStack item = FireworkUtil.getItem(object);
        if (item.isEmpty()) {
            return Optional.empty();
        }

        if (item.getItem() == Items.FIREWORK_ROCKET) {
            final Fireworks fireworks = item.get(DataComponents.FIREWORKS);
            if (fireworks == null || fireworks.explosions().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(fireworks.explosions().stream().map(FireworkEffect.class::cast).toList());
        }

        Preconditions.checkArgument(item.getItem() == Items.FIREWORK_STAR, "Item is not a firework star!");
        final FireworkExplosion fireworkExplosion = item.get(DataComponents.FIREWORK_EXPLOSION);
        if (fireworkExplosion == null) {
            return Optional.empty();
        }
        return Optional.of(List.of((FireworkEffect) (Object) fireworkExplosion));
    }

    public static boolean removeFireworkEffects(final Object object) {
        final ItemStack item = FireworkUtil.getItem(object);
        if (item.isEmpty()) {
            return false;
        }

        if (item.getItem() == Items.FIREWORK_STAR) {
            item.remove(DataComponents.FIREWORK_EXPLOSION);
            return true;
        }
        if (item.getItem() == Items.FIREWORK_ROCKET) {
            if (item.has(DataComponents.FIREWORKS)) {
                // keep flight duration
                item.update(DataComponents.FIREWORKS, null, p -> new Fireworks(p.flightDuration(), Collections.emptyList()));
            }
            return true;
        }
        return false;
    }

    public static ItemStack getItem(final FireworkRocketEntity firework) {
        ItemStack item = firework.getEntityData().get(FireworkRocketEntityAccessor.accessor$DATA_ID_FIREWORKS_ITEM());
        if (item.isEmpty()) {
            item = (ItemStack) (Object) new SpongeItemStack.BuilderImpl().itemType(ItemTypes.FIREWORK_ROCKET).build();
            firework.getEntityData().set(FireworkRocketEntityAccessor.accessor$DATA_ID_FIREWORKS_ITEM(), item);
        }
        return item;
    }

    private static ItemStack getItem(final Object object) {
        if (object instanceof ItemStack) {
            return (ItemStack) object;
        }
        if (object instanceof FireworkRocketEntity) {
            return FireworkUtil.getItem((FireworkRocketEntity) object);
        }
        return ItemStack.EMPTY;
    }

    private FireworkUtil() {
    }
}
