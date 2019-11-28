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
package org.spongepowered.common.registry.type.effect;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.effect.particle.SpongeParticleType;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;
import org.spongepowered.common.registry.type.AbstractPrefixCheckCatalogRegistryModule;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.NotePitchRegistryModule;
import org.spongepowered.common.registry.type.item.FireworkShapeRegistryModule;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@RegistrationDependency({ ParticleOptionRegistryModule.class, NotePitchRegistryModule.class, BlockTypeRegistryModule.class,
        ItemTypeRegistryModule.class, PotionEffectTypeRegistryModule.class, FireworkShapeRegistryModule.class })
public final class ParticleTypeRegistryModule extends AbstractPrefixCheckCatalogRegistryModule<ParticleType> {

    public static ParticleTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(ParticleTypes.class)
    private final Map<String, ParticleType> particleByName = Maps.newHashMap();

    @Override
    public Optional<ParticleType> getById(String id) {
        String key = checkNotNull(id).toLowerCase(Locale.ENGLISH);
        if (!key.contains(":")) {
            key = this.defaultModIdToPrepend + ":" + key;
        }
        return Optional.ofNullable(this.particleByName.get(key));
    }

    @Override
    public Collection<ParticleType> getAll() {
        return ImmutableList.copyOf(this.particleByName.values());
    }

    @Override
    public void registerDefaults() {
        this.addParticleType("ambient_mob_spell", EnumParticleTypes.SPELL_MOB_AMBIENT, false, ImmutableMap.of(
                ParticleOptions.COLOR, Color.BLACK));
        this.addParticleType("angry_villager", EnumParticleTypes.VILLAGER_ANGRY, false);
        this.addParticleType("barrier", EnumParticleTypes.BARRIER, false);
        this.addParticleType("block_crack", EnumParticleTypes.BLOCK_CRACK, true, ImmutableMap.of(
                ParticleOptions.BLOCK_STATE, Blocks.field_150348_b.func_176223_P(),
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.field_150348_b))));
        this.addParticleType("block_dust", EnumParticleTypes.BLOCK_DUST, true, ImmutableMap.of(
                ParticleOptions.BLOCK_STATE, Blocks.field_150348_b.func_176223_P(),
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.field_150348_b))));
        this.addEffectType("break_block", null, ImmutableMap.of(
                ParticleOptions.BLOCK_STATE, Blocks.field_150348_b.func_176223_P(),
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.field_150348_b))));
        this.addParticleType("cloud", EnumParticleTypes.CLOUD, true);
        this.addParticleType("critical_hit", EnumParticleTypes.CRIT, true);
        this.addParticleType("damage_indicator", EnumParticleTypes.DAMAGE_INDICATOR, true);
        this.addParticleType("dragon_breath", EnumParticleTypes.DRAGON_BREATH, true);
        this.addEffectType("dragon_breath_attack", null, ImmutableMap.of());
        this.addParticleType("drip_lava", EnumParticleTypes.DRIP_LAVA, false);
        this.addParticleType("drip_water", EnumParticleTypes.DRIP_WATER, false);
        this.addParticleType("enchanting_glyphs", EnumParticleTypes.ENCHANTMENT_TABLE, true);
        this.addParticleType("end_rod", EnumParticleTypes.END_ROD, true);
        this.addEffectType("ender_teleport", null, ImmutableMap.of());
        this.addParticleType("explosion", EnumParticleTypes.EXPLOSION_NORMAL, true);
        this.addParticleType("falling_dust", EnumParticleTypes.FALLING_DUST, false, ImmutableMap.of(
                ParticleOptions.BLOCK_STATE, Blocks.field_150348_b.func_176223_P(),
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.field_150348_b))));
        this.addEffectType("fertilizer", null, ImmutableMap.of(
                ParticleOptions.QUANTITY, 15));
        this.addParticleType("fireworks_spark", EnumParticleTypes.FIREWORKS_SPARK, true);
        this.addEffectType("fireworks", null, ImmutableMap.of(
                ParticleOptions.FIREWORK_EFFECTS, ImmutableList.of(
                        FireworkEffect.builder().color(Color.BLACK).shape(FireworkShapes.BALL).build())));
        this.addEffectType("fire_smoke", null, ImmutableMap.of(
                ParticleOptions.DIRECTION, Direction.UP));
        this.addParticleType("flame", EnumParticleTypes.FLAME, true);
        this.addParticleType("footstep", EnumParticleTypes.FOOTSTEP, false);
        this.addParticleType("guardian_appearance", EnumParticleTypes.MOB_APPEARANCE, false);
        this.addParticleType("happy_villager", EnumParticleTypes.VILLAGER_HAPPY, true);
        this.addParticleType("heart", EnumParticleTypes.HEART, false);
        this.addParticleType("huge_explosion", EnumParticleTypes.EXPLOSION_HUGE, false);
        this.addParticleType("instant_spell", EnumParticleTypes.SPELL_INSTANT, true, ImmutableMap.of(
                ParticleOptions.SLOW_HORIZONTAL_VELOCITY, false));
        this.addParticleType("item_crack", EnumParticleTypes.ITEM_CRACK, true, ImmutableMap.of(
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.field_150348_b))));
        this.addParticleType("large_explosion", EnumParticleTypes.EXPLOSION_LARGE, false, ImmutableMap.of(
                ParticleOptions.SCALE, 1.0));
        this.addParticleType("large_smoke", EnumParticleTypes.SMOKE_LARGE, true);
        this.addParticleType("lava", EnumParticleTypes.LAVA, false);
        this.addParticleType("magic_critical_hit", EnumParticleTypes.CRIT_MAGIC, true);
        this.addEffectType("mobspawner_flames", null, ImmutableMap.of());
        this.addParticleType("mob_spell", EnumParticleTypes.SPELL_MOB, false, ImmutableMap.of(
                ParticleOptions.COLOR, Color.BLACK));
        this.addParticleType("note", EnumParticleTypes.NOTE, false, ImmutableMap.of(
                ParticleOptions.NOTE, NotePitches.F_SHARP0));
        this.addParticleType("portal", EnumParticleTypes.PORTAL, true);
        this.addParticleType("redstone_dust", EnumParticleTypes.REDSTONE, false, ImmutableMap.of(
                ParticleOptions.COLOR, Color.RED));
        this.addParticleType("slime", EnumParticleTypes.SLIME, false);
        this.addParticleType("smoke", EnumParticleTypes.SMOKE_NORMAL, true);
        this.addParticleType("snowball", EnumParticleTypes.SNOWBALL, false);
        this.addParticleType("snow_shovel", EnumParticleTypes.SNOW_SHOVEL, true);
        this.addParticleType("spell", EnumParticleTypes.SPELL, true, ImmutableMap.of(
                ParticleOptions.SLOW_HORIZONTAL_VELOCITY, false));
        this.addEffectType("splash_potion", null, ImmutableMap.of(
                ParticleOptions.POTION_EFFECT_TYPE, PotionEffectTypes.ABSORPTION));
        this.addParticleType("suspended", EnumParticleTypes.SUSPENDED, false);
        this.addParticleType("suspended_depth", EnumParticleTypes.SUSPENDED_DEPTH, false);
        this.addParticleType("sweep_attack", EnumParticleTypes.SWEEP_ATTACK, false, ImmutableMap.of(
                ParticleOptions.SCALE, 1.0));
        this.addParticleType("town_aura", EnumParticleTypes.TOWN_AURA, true);
        this.addParticleType("water_bubble", EnumParticleTypes.WATER_BUBBLE, true);
        this.addParticleType("water_drop", EnumParticleTypes.WATER_DROP, false);
        this.addParticleType("water_splash", EnumParticleTypes.WATER_SPLASH, true);
        this.addParticleType("water_wake", EnumParticleTypes.WATER_WAKE, true);
        this.addParticleType("witch_spell", EnumParticleTypes.SPELL_WITCH, true, ImmutableMap.of(
                ParticleOptions.SLOW_HORIZONTAL_VELOCITY, false));
        // Is not exposed in the api, since it doesn't do anything
        this.addParticleType("item_take", EnumParticleTypes.ITEM_TAKE, false);
    }

    private void addParticleType(String id, EnumParticleTypes internalType, boolean velocity) {
        this.addParticleType(id, internalType, velocity, Collections.emptyMap());
    }

    private void addParticleType(String id, EnumParticleTypes internalType, boolean velocity,
            Map<ParticleOption<?>, Object> extraOptions) {
        ImmutableMap.Builder<ParticleOption<?>, Object> options = ImmutableMap.builder();
        options.put(ParticleOptions.OFFSET, Vector3d.ZERO);
        options.put(ParticleOptions.QUANTITY, 1);
        if (velocity) {
            options.put(ParticleOptions.VELOCITY, Vector3d.ZERO);
        }
        options.putAll(extraOptions);
        this.addEffectType(id, internalType, options.build());
    }

    private void addEffectType(String id, @Nullable EnumParticleTypes internalType, Map<ParticleOption<?>, Object> options) {
        SpongeParticleType particleType = new SpongeParticleType("minecraft:" + id, id, internalType, options);
        this.particleByName.put(particleType.getId().toLowerCase(Locale.ENGLISH), particleType);
    }

    private ParticleTypeRegistryModule() {
        super("minecraft");
    }

    private static class Holder {
        static final ParticleTypeRegistryModule INSTANCE = new ParticleTypeRegistryModule();
    }
}
