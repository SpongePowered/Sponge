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
package org.spongepowered.common.data.processor.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.item.SpongeFireworkEffectBuilder;
import org.spongepowered.common.item.SpongeFireworkShape;
import org.spongepowered.common.item.inventory.SpongeItemStackBuilder;
import org.spongepowered.common.mixin.core.entity.item.EntityFireworkRocketAccessor;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class FireworkUtils {

    public static final BiMap<Byte, SpongeFireworkShape> shapeMapping = ImmutableBiMap.<Byte, SpongeFireworkShape>builder()
            .put((byte) 0, new SpongeFireworkShape("minecraft:ball", "Ball"))
            .put((byte) 1, new SpongeFireworkShape("minecraft:large_ball", "Large Ball"))
            .put((byte) 2, new SpongeFireworkShape("minecraft:star", "Star"))
            .put((byte) 3, new SpongeFireworkShape("minecraft:creeper", "Creeper"))
            .put((byte) 4, new SpongeFireworkShape("minecraft:burst", "Burst"))
            .build();

    public static ItemStack getItem(final EntityFireworkRocket firework) {
        ItemStack item = firework.getDataManager().get(EntityFireworkRocketAccessor.accessor$getFireworkItemParameter());
        if (item.isEmpty()) {
            item = (ItemStack) new SpongeItemStackBuilder().itemType(ItemTypes.FIREWORKS).build();
            firework.getDataManager().set(EntityFireworkRocketAccessor.accessor$getFireworkItemParameter(), item);
        }
        return item;
    }

    @Nullable
    public static FireworkEffect getChargeEffect(final ItemStack item) {
        Preconditions.checkArgument(item.getItem() == Items.FIREWORK_CHARGE, "Item is not a firework!"); // FIREWORK_CHARGE
        final NBTTagCompound tag = item.getTagCompound();
        if (tag == null) {
            return null;
        }
        final NBTTagCompound firework = tag.getCompoundTag(Constants.Entity.Firework.EXPLOSION);
        if(firework.isEmpty()) {
            return null;
        }

        return fromNbt(firework);
    }

    public static FireworkShape getShape(byte id) {
        if(id > 4) id = 0;
        return shapeMapping.get(id);
    }

    public static byte getShapeId(final FireworkShape shape) {
        return shapeMapping.inverse().get(shape);
    }

    public static FireworkEffect fromNbt(final NBTTagCompound effectNbt) {
        final FireworkEffect.Builder builder = new SpongeFireworkEffectBuilder();
        if (effectNbt.hasKey(Constants.Item.Fireworks.FLICKER)) {
            builder.flicker(effectNbt.getBoolean(Constants.Item.Fireworks.FLICKER));
        }
        if (effectNbt.hasKey(Constants.Item.Fireworks.TRAIL)) {
            builder.trail(effectNbt.getBoolean(Constants.Item.Fireworks.TRAIL));
        }
        if (effectNbt.hasKey(Constants.Item.Fireworks.SHAPE_TYPE)) {
            final byte type = effectNbt.getByte(Constants.Item.Fireworks.SHAPE_TYPE);
            builder.shape(getShape(type));
        }
        if (effectNbt.hasKey(Constants.Item.Fireworks.COLORS)) {
            final List<Color> colors = Lists.newArrayList();
            final int[] colorsRaw = effectNbt.getIntArray(Constants.Item.Fireworks.COLORS);
            for(final int color : colorsRaw) {
                colors.add(Color.ofRgb(color));
            }
            builder.colors(colors);
        }
        if (effectNbt.hasKey(Constants.Item.Fireworks.FADE_COLORS)) {
            final List<Color> fades = Lists.newArrayList();
            final int[] fadesRaw = effectNbt.getIntArray(Constants.Item.Fireworks.FADE_COLORS);
            for(final int fade : fadesRaw) {
                fades.add(Color.ofRgb(fade));
            }
            builder.fades(fades);
        }

        return builder.build();
    }

    public static NBTTagCompound toNbt(final FireworkEffect effect) {
        final NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(Constants.Item.Fireworks.FLICKER, effect.flickers());
        tag.setBoolean(Constants.Item.Fireworks.TRAIL, effect.hasTrail());
        tag.setByte(Constants.Item.Fireworks.SHAPE_TYPE, getShapeId(effect.getShape()));
        final int[] colorsArray = new int[effect.getColors().size()];
        final List<Color> colors = effect.getColors();
        for (int i = 0; i < colors.size(); i++) {
            colorsArray[i] = colors.get(i).getRgb();
        }
        tag.setIntArray(Constants.Item.Fireworks.COLORS, colorsArray);
        final int[] fadeArray = new int[effect.getFadeColors().size()];
        final List<Color> fades = effect.getFadeColors();
        for (int i = 0; i < fades.size(); i++) {
            fadeArray[i] = fades.get(i).getRgb();
        }
        tag.setIntArray(Constants.Item.Fireworks.FADE_COLORS, fadeArray);

        return tag;
    }

    public static boolean setFireworkEffects(final Object object, final List<? extends FireworkEffect> effects) {
        ItemStack item = ItemStack.EMPTY;
        if(object instanceof ItemStack) {
            item = (ItemStack) object;
        }
        if(object instanceof EntityFireworkRocket) {
            item = getItem((EntityFireworkRocket) object);
        }
        if(item.isEmpty()) return false;

        if(item.getItem() == Items.FIREWORK_CHARGE) {
            final NBTTagCompound tag = item.getTagCompound();
            if (tag == null) {
                return true;
            }
            if(!effects.isEmpty()) {
                tag.setTag(Constants.Entity.Firework.EXPLOSION, toNbt(effects.get(0)));
            } else {
                tag.removeTag(Constants.Entity.Firework.EXPLOSION);
            }
            return true;
        } else if(item.getItem() == Items.FIREWORKS) {
            final NBTTagList nbtEffects = new NBTTagList();
            effects.stream().map(FireworkUtils::toNbt).forEach(nbtEffects::appendTag);

            final NBTTagCompound fireworks = item.getOrCreateSubCompound(Constants.Item.Fireworks.FIREWORKS);
            fireworks.setTag(Constants.Item.Fireworks.EXPLOSIONS, nbtEffects);
            return true;
        }
        return false;
    }

    public static Optional<List<FireworkEffect>> getFireworkEffects(final Object object) {
        ItemStack item = ItemStack.EMPTY;
        if(object instanceof ItemStack) {
            item = (ItemStack) object;
        }
        if(object instanceof EntityFireworkRocket) {
            item = FireworkUtils.getItem((EntityFireworkRocket) object);
        }
        if(item.isEmpty()) return Optional.empty();

        final List<FireworkEffect> effects;
        if(item.getItem() == Items.FIREWORKS) {
            final NBTTagCompound fireworks = item.getSubCompound(Constants.Item.Fireworks.FIREWORKS);
            if(fireworks == null || !fireworks.hasKey(Constants.Item.Fireworks.EXPLOSIONS)) return Optional.empty();

            final NBTTagList effectsNbt = fireworks.getTagList(Constants.Item.Fireworks.EXPLOSIONS, Constants.NBT.TAG_COMPOUND);
            effects = Lists.newArrayList();
            for(int i = 0; i < effectsNbt.tagCount(); i++) {
                final NBTTagCompound effectNbt = effectsNbt.getCompoundTagAt(i);
                effects.add(fromNbt(effectNbt));
            }
        } else {
            final FireworkEffect effect = FireworkUtils.getChargeEffect(item);
            if(effect == null) return Optional.empty();
            effects = ImmutableList.of(effect);
        }

        return Optional.of(effects);
    }

    public static boolean removeFireworkEffects(final Object object) {
        ItemStack item = ItemStack.EMPTY;
        if(object instanceof ItemStack) {
            item = (ItemStack) object;
        }
        if(object instanceof EntityFireworkRocket) {
            item = FireworkUtils.getItem((EntityFireworkRocket) object);
        }
        if(item.isEmpty()) return false;

        if(item.getItem() == Items.FIREWORK_CHARGE) {
            final NBTTagCompound tag = item.getTagCompound();
            if (tag == null) {
                return true;
            }
            tag.removeTag(Constants.Entity.Firework.EXPLOSION);
            return true;
        } else if(item.getItem() == Items.FIREWORKS) {
            final NBTTagCompound fireworks = item.getOrCreateSubCompound(Constants.Item.Fireworks.FIREWORKS);
            fireworks.removeTag(Constants.Item.Fireworks.EXPLOSIONS);
            return true;
        }

        return false;
    }

}
