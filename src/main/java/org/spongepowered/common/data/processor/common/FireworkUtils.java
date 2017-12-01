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
            item = (ItemStack) new SpongeItemStackBuilder().itemType(ItemTypes.FIREWORKS).build();
            firework.getDataManager().set(EntityFireworkRocket.FIREWORK_ITEM, item);
        }
        return item;
    }

    public static FireworkEffect getChargeEffect(ItemStack item) {
        Preconditions.checkArgument(item.getItem() == Items.FIREWORK_CHARGE, "Item is not a firework!"); // FIREWORK_CHARGE
        NBTTagCompound firework = NbtDataUtil.getOrCreateCompound(item).getCompoundTag("Explosion");
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
        if(effectNbt.hasKey("Flicker")) {
            builder.flicker(effectNbt.getBoolean("Flicker"));
        }
        if(effectNbt.hasKey("Trail")) {
            builder.trail(effectNbt.getBoolean("Trail"));
        }
        if(effectNbt.hasKey("Type")) {
            byte type = effectNbt.getByte("Type");
            builder.shape(getShape(type));
        }
        if(effectNbt.hasKey("Colors")) {
            List<Color> colors = Lists.newArrayList();
            int[] colorsRaw = effectNbt.getIntArray("Colors");
            for(int color : colorsRaw) {
                colors.add(Color.of(color));
            }
            builder.colors(colors);
        }
        if(effectNbt.hasKey("FadeColors")) {
            List<Color> fades = Lists.newArrayList();
            int[] fadesRaw = effectNbt.getIntArray("FadeColors");
            for(int fade : fadesRaw) {
                fades.add(Color.of(fade));
            }
            builder.fades(fades);
        }

        return builder.build();
    }

    public static NBTTagCompound toNbt(FireworkEffect effect) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("Flicker", effect.flickers());
        tag.setBoolean("Trail", effect.hasTrail());
        tag.setByte("Type", getShapeId(effect.getShape()));
        int[] colorsArray = new int[effect.getColors().size()];
        List<Color> colors = effect.getColors();
        for (int i = 0; i < colors.size(); i++) {
            colorsArray[i] = colors.get(i).getRgb();
        }
        tag.setIntArray("Colors", colorsArray);
        int[] fadeArray = new int[effect.getFadeColors().size()];
        List<Color> fades = effect.getFadeColors();
        for (int i = 0; i < fades.size(); i++) {
            fadeArray[i] = fades.get(i).getRgb();
        }
        tag.setIntArray("FadeColors", fadeArray);

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

        if(item.getItem() == Items.FIREWORK_CHARGE) {
            if(effects.size() != 0) {
                NbtDataUtil.getOrCreateCompound(item).setTag("Explosion", toNbt(effects.get(0)));
            } else {
                NbtDataUtil.getOrCreateCompound(item).removeTag("Explosion");
            }
            return true;
        } else if(item.getItem() == Items.FIREWORKS) {
            NBTTagList nbtEffects = new NBTTagList();
            effects.stream().map(FireworkUtils::toNbt).forEach(nbtEffects::appendTag);

            NBTTagCompound fireworks = item.getOrCreateSubCompound("Fireworks");
            fireworks.setTag("Explosions", nbtEffects);
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
        if(item.getItem() == Items.FIREWORKS) {
            NBTTagCompound fireworks = item.getSubCompound("Fireworks");
            if(fireworks == null || !fireworks.hasKey("Explosions")) return Optional.empty();

            NBTTagList effectsNbt = fireworks.getTagList("Explosions", NbtDataUtil.TAG_COMPOUND);
            effects = Lists.newArrayList();
            for(int i = 0; i < effectsNbt.tagCount(); i++) {
                NBTTagCompound effectNbt = effectsNbt.getCompoundTagAt(i);
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

        if(item.getItem() == Items.FIREWORK_CHARGE) {
            NbtDataUtil.getOrCreateCompound(item).removeTag("Explosion");
            return true;
        } else if(item.getItem() == Items.FIREWORKS) {
            NBTTagCompound fireworks = item.getOrCreateSubCompound("Fireworks");
            fireworks.removeTag("Explosions");
            return true;
        }

        return false;
    }

}
