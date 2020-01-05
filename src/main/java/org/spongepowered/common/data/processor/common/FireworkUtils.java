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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.item.SpongeFireworkEffectBuilder;
import org.spongepowered.common.item.SpongeItemStackBuilder;
import org.spongepowered.common.mixin.accessor.entity.item.FireworkRocketEntityAccessor;
import org.spongepowered.common.registry.MappedRegistry;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class FireworkUtils {

    public static ItemStack getItem(final FireworkRocketEntity firework) {
        ItemStack item = firework.getDataManager().get(FireworkRocketEntityAccessor.accessor$getFireworkItem());
        if (item.isEmpty()) {
            item = (ItemStack) new SpongeItemStackBuilder().itemType(ItemTypes.FIREWORKS).build();
            firework.getDataManager().set(FireworkRocketEntityAccessor.accessor$getFireworkItem(), item);
        }
        return item;
    }

    @Nullable
    public static FireworkEffect getChargeEffect(final ItemStack item) {
        Preconditions.checkArgument(item.getItem() == Items.FIREWORK_CHARGE, "Item is not a firework!"); // FIREWORK_CHARGE
        final CompoundNBT tag = item.getTag();
        if (tag == null) {
            return null;
        }
        final CompoundNBT firework = tag.getCompound(Constants.Entity.Firework.EXPLOSION);
        if(firework.isEmpty()) {
            return null;
        }

        return fromCompound(firework);
    }

    public static FireworkEffect fromCompound(final CompoundNBT compound) {
        final FireworkEffect.Builder builder = new SpongeFireworkEffectBuilder();
        if (compound.contains(Constants.Item.Fireworks.FLICKER)) {
            builder.flicker(compound.getBoolean(Constants.Item.Fireworks.FLICKER));
        }
        if (compound.contains(Constants.Item.Fireworks.TRAIL)) {
            builder.trail(compound.getBoolean(Constants.Item.Fireworks.TRAIL));
        }
        if (compound.contains(Constants.Item.Fireworks.SHAPE_TYPE)) {
            final byte type = compound.getByte(Constants.Item.Fireworks.SHAPE_TYPE);
            final MappedRegistry<FireworkShape, Byte> registry = SpongeImpl.getRegistry().getCatalogRegistry().getRegistry(FireworkShape.class);
            builder.shape(registry.getReverseMapping(type));
        }
        if (compound.contains(Constants.Item.Fireworks.COLORS)) {
            final List<Color> colors = Lists.newArrayList();
            final int[] colorsRaw = compound.getIntArray(Constants.Item.Fireworks.COLORS);
            for(final int color : colorsRaw) {
                colors.add(Color.ofRgb(color));
            }
            builder.colors(colors);
        }
        if (compound.contains(Constants.Item.Fireworks.FADE_COLORS)) {
            final List<Color> fades = Lists.newArrayList();
            final int[] fadesRaw = compound.getIntArray(Constants.Item.Fireworks.FADE_COLORS);
            for(final int fade : fadesRaw) {
                fades.add(Color.ofRgb(fade));
            }
            builder.fades(fades);
        }

        return builder.build();
    }

    public static CompoundNBT toCompound(final FireworkEffect effect) {
        final MappedRegistry<FireworkShape, Byte> registry = SpongeImpl.getRegistry().getCatalogRegistry().getRegistry(FireworkShape.class);

        final CompoundNBT tag = new CompoundNBT();
        tag.putBoolean(Constants.Item.Fireworks.FLICKER, effect.flickers());
        tag.putBoolean(Constants.Item.Fireworks.TRAIL, effect.hasTrail());
        tag.putByte(Constants.Item.Fireworks.SHAPE_TYPE, registry.getMapping(effect.getShape()));
        final int[] colorsArray = new int[effect.getColors().size()];
        final List<Color> colors = effect.getColors();
        for (int i = 0; i < colors.size(); i++) {
            colorsArray[i] = colors.get(i).getRgb();
        }
        tag.putIntArray(Constants.Item.Fireworks.COLORS, colorsArray);
        final int[] fadeArray = new int[effect.getFadeColors().size()];
        final List<Color> fades = effect.getFadeColors();
        for (int i = 0; i < fades.size(); i++) {
            fadeArray[i] = fades.get(i).getRgb();
        }
        tag.putIntArray(Constants.Item.Fireworks.FADE_COLORS, fadeArray);

        return tag;
    }

    public static boolean setFireworkEffects(final Object object, final List<? extends FireworkEffect> effects) {
        ItemStack item = ItemStack.EMPTY;
        if(object instanceof ItemStack) {
            item = (ItemStack) object;
        }
        if(object instanceof FireworkRocketEntity) {
            item = getItem((FireworkRocketEntity) object);
        }
        if(item.isEmpty()) return false;

        if(item.getItem() == Items.FIREWORK_CHARGE) {
            final CompoundNBT tag = item.getTag();
            if (tag == null) {
                return true;
            }
            if(!effects.isEmpty()) {
                tag.put(Constants.Entity.Firework.EXPLOSION, toCompound(effects.get(0)));
            } else {
                tag.remove(Constants.Entity.Firework.EXPLOSION);
            }
            return true;
        } else if(item.getItem() == Items.FIREWORKS) {
            final ListNBT nbtEffects = new ListNBT();
            effects.stream().map(FireworkUtils::toCompound).forEach(nbtEffects::add);

            final CompoundNBT fireworks = item.getOrCreateChildTag(Constants.Item.Fireworks.FIREWORKS);
            fireworks.put(Constants.Item.Fireworks.EXPLOSIONS, nbtEffects);
            return true;
        }
        return false;
    }

    public static Optional<List<FireworkEffect>> getFireworkEffects(final Object object) {
        ItemStack item = ItemStack.EMPTY;
        if(object instanceof ItemStack) {
            item = (ItemStack) object;
        }
        if(object instanceof FireworkRocketEntity) {
            item = FireworkUtils.getItem((FireworkRocketEntity) object);
        }
        if(item.isEmpty()) return Optional.empty();

        final List<FireworkEffect> effects;
        if(item.getItem() == Items.FIREWORKS) {
            final CompoundNBT fireworks = item.getChildTag(Constants.Item.Fireworks.FIREWORKS);
            if(fireworks == null || !fireworks.contains(Constants.Item.Fireworks.EXPLOSIONS)) return Optional.empty();

            final ListNBT effectsNbt = fireworks.getList(Constants.Item.Fireworks.EXPLOSIONS, Constants.NBT.TAG_COMPOUND);
            effects = Lists.newArrayList();
            for(int i = 0; i < effectsNbt.size(); i++) {
                final CompoundNBT effectNbt = effectsNbt.getCompound(i);
                effects.add(fromCompound(effectNbt));
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
        if(object instanceof FireworkRocketEntity) {
            item = FireworkUtils.getItem((FireworkRocketEntity) object);
        }
        if(item.isEmpty()) return false;

        if(item.getItem() == Items.FIREWORK_CHARGE) {
            final CompoundNBT tag = item.getTag();
            if (tag == null) {
                return true;
            }
            tag.remove(Constants.Entity.Firework.EXPLOSION);
            return true;
        } else if(item.getItem() == Items.FIREWORK_STAR) {
            final CompoundNBT fireworks = item.getOrCreateChildTag(Constants.Item.Fireworks.FIREWORKS);
            fireworks.remove(Constants.Item.Fireworks.EXPLOSIONS);
            return true;
        }

        return false;
    }

}
