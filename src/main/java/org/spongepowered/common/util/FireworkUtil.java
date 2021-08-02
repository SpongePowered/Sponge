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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.accessor.world.entity.projectile.FireworkRocketEntityAccessor;
import org.spongepowered.common.item.SpongeFireworkEffectBuilder;
import org.spongepowered.common.item.SpongeItemStack;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.MappedRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FireworkUtil {

    public static @Nullable FireworkEffect getStarEffect(final ItemStack item) {
        Preconditions.checkArgument(item.getItem() == Items.FIREWORK_STAR, "Item is not a firework star!");
        final @Nullable CompoundTag tag = item.getTagElement(Constants.Entity.Firework.EXPLOSION);
        if (tag == null) {
            return null;
        }
        return FireworkUtil.fromCompound(tag);
    }

    public static FireworkEffect fromCompound(final CompoundTag compound) {
        final FireworkEffect.Builder builder = new SpongeFireworkEffectBuilder();
        if (compound.contains(Constants.Item.Fireworks.FLICKER)) {
            builder.flicker(compound.getBoolean(Constants.Item.Fireworks.FLICKER));
        }
        if (compound.contains(Constants.Item.Fireworks.TRAIL)) {
            builder.trail(compound.getBoolean(Constants.Item.Fireworks.TRAIL));
        }
        if (compound.contains(Constants.Item.Fireworks.SHAPE_TYPE)) {
            final byte type = compound.getByte(Constants.Item.Fireworks.SHAPE_TYPE);
            final MappedRegistry<FireworkShape> registry = (MappedRegistry<FireworkShape>) (Object) Sponge.game().registries().registry(RegistryTypes.FIREWORK_SHAPE);
            final @Nullable FireworkShape shape = registry.byId(type);
            if (shape != null) {
                builder.shape(shape);
            }
        }
        if (compound.contains(Constants.Item.Fireworks.COLORS)) {
            final List<Color> colors = Lists.newArrayList();
            final int[] colorsRaw = compound.getIntArray(Constants.Item.Fireworks.COLORS);
            for (final int color : colorsRaw) {
                colors.add(Color.ofRgb(color));
            }
            builder.colors(colors);
        }
        if (compound.contains(Constants.Item.Fireworks.FADE_COLORS)) {
            final List<Color> fades = Lists.newArrayList();
            final int[] fadesRaw = compound.getIntArray(Constants.Item.Fireworks.FADE_COLORS);
            for (final int fade : fadesRaw) {
                fades.add(Color.ofRgb(fade));
            }
            builder.fades(fades);
        }

        return builder.build();
    }

    public static CompoundTag toCompound(final FireworkEffect effect) {
        final MappedRegistry<FireworkShape> registry = (MappedRegistry<FireworkShape>) (Object) Sponge.game().registries().registry(RegistryTypes.FIREWORK_SHAPE);

        final CompoundTag tag = new CompoundTag();
        tag.putBoolean(Constants.Item.Fireworks.FLICKER, effect.flickers());
        tag.putBoolean(Constants.Item.Fireworks.TRAIL, effect.hasTrail());
        tag.putByte(Constants.Item.Fireworks.SHAPE_TYPE, (byte) registry.getId(effect.shape()));
        final int[] colorsArray = effect.colors().stream()
                .mapToInt(Color::rgb)
                .toArray();
        tag.putIntArray(Constants.Item.Fireworks.COLORS, colorsArray);
        final int[] fadeArray = effect.fadeColors().stream()
                .mapToInt(Color::rgb)
                .toArray();
        tag.putIntArray(Constants.Item.Fireworks.FADE_COLORS, fadeArray);
        return tag;
    }

    public static boolean setFireworkEffects(final Object object, final List<? extends FireworkEffect> effects) {
        if (effects.isEmpty()) {
            return FireworkUtil.removeFireworkEffects(object);
        }
        final ItemStack item = FireworkUtil.getItem(object);
        if (item.isEmpty()) {
            return false;
        }

        if (item.getItem() == Items.FIREWORK_STAR) {
            item.addTagElement(Constants.Entity.Firework.EXPLOSION, FireworkUtil.toCompound(effects.get(0)));
            return true;
        } else if (item.getItem() == Items.FIREWORK_ROCKET) {
            final CompoundTag fireworks = item.getOrCreateTagElement(Constants.Item.Fireworks.FIREWORKS);
            fireworks.put(Constants.Item.Fireworks.EXPLOSIONS, effects.stream()
                    .map(FireworkUtil::toCompound)
                    .collect(NBTCollectors.toTagList()));
            return true;
        }
        return false;
    }

    public static Optional<List<FireworkEffect>> getFireworkEffects(final Object object) {
        final ItemStack item = FireworkUtil.getItem(object);
        if (item.isEmpty()) {
            return Optional.empty();
        }

        final List<FireworkEffect> effects;
        if (item.getItem() == Items.FIREWORK_ROCKET) {
            final @Nullable CompoundTag fireworks = item.getTagElement(Constants.Item.Fireworks.FIREWORKS);
            if (fireworks == null || !fireworks.contains(Constants.Item.Fireworks.EXPLOSIONS)) {
                return Optional.empty();
            }

            final ListTag effectCompounds = fireworks.getList(Constants.Item.Fireworks.EXPLOSIONS, Constants.NBT.TAG_COMPOUND);
            effects = NBTStreams.toCompounds(effectCompounds)
                    .map(FireworkUtil::fromCompound)
                    .collect(Collectors.toList());
        } else {
            final @Nullable FireworkEffect effect = FireworkUtil.getStarEffect(item);
            if (effect == null) {
                return Optional.empty();
            }
            effects = ImmutableList.of(effect);
        }

        return Optional.of(effects);
    }

    public static boolean removeFireworkEffects(final Object object) {
        final ItemStack item = FireworkUtil.getItem(object);
        if (item.isEmpty()) {
            return false;
        }

        if (item.getItem() == Items.FIREWORK_STAR) {
            final @Nullable CompoundTag tag = item.getTag();
            if (tag == null) {
                return true;
            }
            tag.remove(Constants.Entity.Firework.EXPLOSION);
            return true;
        } else if (item.getItem() == Items.FIREWORK_ROCKET) {
            final CompoundTag fireworks = item.getOrCreateTagElement(Constants.Item.Fireworks.FIREWORKS);
            fireworks.remove(Constants.Item.Fireworks.EXPLOSIONS);
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
