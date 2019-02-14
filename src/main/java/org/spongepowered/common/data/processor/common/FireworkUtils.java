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
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.item.SpongeFireworkEffectBuilder;
import org.spongepowered.common.item.SpongeFireworkShape;
import org.spongepowered.common.item.inventory.SpongeItemStackBuilder;

import java.util.List;
import java.util.Optional;

public class FireworkUtils {

    public static final BiMap<Byte, SpongeFireworkShape> shapeMapping = ImmutableBiMap.<Byte, SpongeFireworkShape>builder()
            .put((byte) 0, new SpongeFireworkShape((byte) 0, "BALL"))
            .put((byte) 1, new SpongeFireworkShape((byte) 1, "LARGE_BALL"))
            .put((byte) 2, new SpongeFireworkShape((byte) 2, "STAR"))
            .put((byte) 3, new SpongeFireworkShape((byte) 3, "CREEPER"))
            .put((byte) 4, new SpongeFireworkShape((byte) 4, "BURST"))
            .build();

    public static ItemStack getItem(EntityFireworkRocket firework) {
        ItemStack item = firework.getDataManager().get(EntityFireworkRocket.FIREWORK_ITEM);
        if (item.isEmpty()) {
            item = (ItemStack) (Object) new SpongeItemStackBuilder().itemType(ItemTypes.FIREWORK_ROCKET).build();
            firework.getDataManager().set(EntityFireworkRocket.FIREWORK_ITEM, item);
        }
        return item;
    }

    public static FireworkEffect getChargeEffect(ItemStack item) {
        Preconditions.checkArgument(item.getItem() == Items.FIREWORK_STAR, "Item is not a firework!"); // FIREWORK_CHARGE
        NBTTagCompound firework = NbtDataUtil.getOrCreateCompound(item).getCompound("Explosion");
        if(firework == null) return null;

        return fromNbt(firework);
    }

    public static FireworkShape getShape(byte id) {
        if(id > 4) id = 0;
        return shapeMapping.get(id);
    }

    public static byte getShapeId(FireworkShape shape) {
        return shapeMapping.inverse().get(shape);
    }

    public static FireworkEffect fromNbt(NBTTagCompound effectNbt) {
        FireworkEffect.Builder builder = new SpongeFireworkEffectBuilder();
        if(effectNbt.contains("Flicker")) {
            builder.flicker(effectNbt.getBoolean("Flicker"));
        }
        if(effectNbt.contains("Trail")) {
            builder.trail(effectNbt.getBoolean("Trail"));
        }
        if(effectNbt.contains("Type")) {
            byte type = effectNbt.getByte("Type");
            builder.shape(getShape(type));
        }
        if(effectNbt.contains("Colors")) {
            List<Color> colors = Lists.newArrayList();
            int[] colorsRaw = effectNbt.getIntArray("Colors");
            for(int color : colorsRaw) {
                colors.add(Color.ofRgb(color));
            }
            builder.colors(colors);
        }
        if(effectNbt.contains("FadeColors")) {
            List<Color> fades = Lists.newArrayList();
            int[] fadesRaw = effectNbt.getIntArray("FadeColors");
            for(int fade : fadesRaw) {
                fades.add(Color.ofRgb(fade));
            }
            builder.fades(fades);
        }

        return builder.build();
    }

    public static NBTTagCompound toNbt(FireworkEffect effect) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.putBoolean("Flicker", effect.flickers());
        tag.putBoolean("Trail", effect.hasTrail());
        tag.putByte("Type", getShapeId(effect.getShape()));
        int[] colorsArray = new int[effect.getColors().size()];
        List<Color> colors = effect.getColors();
        for (int i = 0; i < colors.size(); i++) {
            colorsArray[i] = colors.get(i).getRgb();
        }
        tag.putIntArray("Colors", colorsArray);
        int[] fadeArray = new int[effect.getFadeColors().size()];
        List<Color> fades = effect.getFadeColors();
        for (int i = 0; i < fades.size(); i++) {
            fadeArray[i] = fades.get(i).getRgb();
        }
        tag.putIntArray("FadeColors", fadeArray);

        return tag;
    }

    public static boolean setFireworkEffects(Object object, List<FireworkEffect> effects) {
        ItemStack item = ItemStack.EMPTY;
        if(object instanceof ItemStack) {
            item = (ItemStack) object;
        }
        if(object instanceof EntityFireworkRocket) {
            item = getItem((EntityFireworkRocket) object);
        }
        if(item.isEmpty()) return false;

        if(item.getItem() == Items.FIREWORK_STAR) {
            if(effects.size() != 0) {
                NbtDataUtil.getOrCreateCompound(item).put("Explosion", toNbt(effects.get(0)));
            } else {
                NbtDataUtil.getOrCreateCompound(item).remove("Explosion");
            }
            return true;
        } else if(item.getItem() == Items.FIREWORK_ROCKET) {
            NBTTagList nbtEffects = new NBTTagList();
            effects.stream().map(FireworkUtils::toNbt).forEach(nbtEffects::add);

            NBTTagCompound fireworks = item.getOrCreateChildTag("Fireworks");
            fireworks.put("Explosions", nbtEffects);
            return true;
        }
        return false;
    }

    public static Optional<List<FireworkEffect>> getFireworkEffects(Object object) {
        ItemStack item = ItemStack.EMPTY;
        if(object instanceof ItemStack) {
            item = (ItemStack) object;
        }
        if(object instanceof EntityFireworkRocket) {
            item = FireworkUtils.getItem((EntityFireworkRocket) object);
        }
        if(item.isEmpty()) return Optional.empty();

        List<FireworkEffect> effects;
        if(item.getItem() == Items.FIREWORK_ROCKET) {
            NBTTagCompound fireworks = item.getChildTag("Fireworks");
            if(fireworks == null || !fireworks.contains("Explosions")) return Optional.empty();

            NBTTagList effectsNbt = fireworks.getList("Explosions", NbtDataUtil.TAG_COMPOUND);
            effects = Lists.newArrayList();
            for(int i = 0; i < effectsNbt.size(); i++) {
                NBTTagCompound effectNbt = effectsNbt.getCompound(i);
                effects.add(fromNbt(effectNbt));
            }
        } else {
            FireworkEffect effect = FireworkUtils.getChargeEffect(item);
            if(effect == null) return Optional.empty();
            effects = ImmutableList.of(effect);
        }

        return Optional.of(effects);
    }

    public static boolean removeFireworkEffects(Object object) {
        ItemStack item = ItemStack.EMPTY;
        if(object instanceof ItemStack) {
            item = (ItemStack) object;
        }
        if(object instanceof EntityFireworkRocket) {
            item = FireworkUtils.getItem((EntityFireworkRocket) object);
        }
        if(item.isEmpty()) return false;

        if(item.getItem() == Items.FIREWORK_STAR) {
            NbtDataUtil.getOrCreateCompound(item).remove("Explosion");
            return true;
        } else if(item.getItem() == Items.FIREWORK_ROCKET) {
            NBTTagCompound fireworks = item.getChildTag("Fireworks");
            fireworks.remove("Explosions");
            return true;
        }

        return false;
    }

}
