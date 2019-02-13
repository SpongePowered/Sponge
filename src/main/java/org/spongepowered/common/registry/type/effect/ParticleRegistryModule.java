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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.init.Blocks;
import net.minecraft.init.Particles;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.effect.particle.SpongeParticleType;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.NotePitchRegistryModule;
import org.spongepowered.common.registry.type.item.FireworkShapeRegistryModule;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

@RegisterCatalog(ParticleTypes.class)
@RegistrationDependency({ ParticleOptionRegistryModule.class, NotePitchRegistryModule.class, BlockTypeRegistryModule.class,
        ItemTypeRegistryModule.class, PotionEffectTypeRegistryModule.class, FireworkShapeRegistryModule.class })
public final class ParticleRegistryModule extends AbstractCatalogRegistryModule<ParticleType> implements CatalogRegistryModule<ParticleType> {

    public static ParticleRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private final Map<String, ParticleType> particleByName = Maps.newHashMap();


    @Override
    public void registerDefaults() {
        this.addParticleType("ambient_mob_spell", Particles.AMBIENT_ENTITY_EFFECT, false, ImmutableMap.of(
                ParticleOptions.COLOR, Color.BLACK));
        this.addParticleType("angry_villager", Particles.ANGRY_VILLAGER, false);
        this.addParticleType("barrier", Particles.BARRIER, false);
        this.addParticleType("block_crack", Particles.BLOCK, true, ImmutableMap.of(
                ParticleOptions.BLOCK_STATE, Blocks.STONE.getDefaultState(),
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.STONE))));
        this.addParticleType("block_dust", Particles.DUST, true, ImmutableMap.of(
                ParticleOptions.BLOCK_STATE, Blocks.STONE.getDefaultState(),
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.STONE))));
        this.addEffectType("break_block", null, ImmutableMap.of(
                ParticleOptions.BLOCK_STATE, Blocks.STONE.getDefaultState(),
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.STONE))));
        this.addParticleType("cloud", Particles.CLOUD, true);
        this.addParticleType("critical_hit", Particles.CRIT, true);
        this.addParticleType("damage_indicator", Particles.DAMAGE_INDICATOR, true);
        this.addParticleType("dragon_breath", Particles.DRAGON_BREATH, true);
        this.addEffectType("dragon_breath_attack", null, ImmutableMap.of());
        this.addParticleType("drip_lava", Particles.DRIPPING_LAVA, false);
        this.addParticleType("drip_water", Particles.DRIPPING_WATER, false);
        this.addParticleType("enchanting_glyphs", Particles.ENCHANT, true);
        this.addParticleType("end_rod", Particles.END_ROD, true);
        this.addEffectType("ender_teleport", null, ImmutableMap.of());
        this.addParticleType("explosion", Particles.EXPLOSION, true);
        this.addParticleType("falling_dust", Particles.FALLING_DUST, false, ImmutableMap.of(
                ParticleOptions.BLOCK_STATE, Blocks.STONE.getDefaultState(),
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.STONE))));
        this.addEffectType("fertilizer", null, ImmutableMap.of(
                ParticleOptions.QUANTITY, 15));
        this.addParticleType("fireworks_spark", Particles.FIREWORK, true);
        this.addEffectType("fireworks", null, ImmutableMap.of(
                ParticleOptions.FIREWORK_EFFECTS, ImmutableList.of(
                        FireworkEffect.builder().color(Color.BLACK).shape(FireworkShapes.BALL).build())));
        this.addEffectType("fire_smoke", null, ImmutableMap.of(
                ParticleOptions.DIRECTION, Direction.UP));
        this.addParticleType("flame", Particles.FLAME, true);
        this.addParticleType("footstep", Particles.FOOTSTEP, false);
        this.addParticleType("guardian_appearance", Particles.ELDER_GUARDIAN, false);
        this.addParticleType("happy_villager", Particles.HAPPY_VILLAGER, true);
        this.addParticleType("heart", Particles.HEART, false);
        this.addParticleType("huge_explosion", Particles.EXPLOSION_HUGE, false);
        this.addParticleType("instant_spell", Particles.INSTANT_EFFECT, true, ImmutableMap.of(
                ParticleOptions.SLOW_HORIZONTAL_VELOCITY, false));
        this.addParticleType("item_crack", Particles.ITEM_CRACK, true, ImmutableMap.of(
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.STONE))));
        this.addParticleType("large_explosion", Particles.EXPLOSION_LARGE, false, ImmutableMap.of(
                ParticleOptions.SCALE, 1.0));
        this.addParticleType("large_smoke", Particles.SMOKE_LARGE, true);
        this.addParticleType("lava", Particles.LAVA, false);
        this.addParticleType("magic_critical_hit", Particles.CRIT_MAGIC, true);
        this.addEffectType("mobspawner_flames", null, ImmutableMap.of());
        this.addParticleType("mob_spell", Particles.SPELL_MOB, false, ImmutableMap.of(
                ParticleOptions.COLOR, Color.BLACK));
        this.addParticleType("note", Particles.NOTE, false, ImmutableMap.of(
                ParticleOptions.NOTE, NotePitches.F_SHARP0));
        this.addParticleType("portal", Particles.PORTAL, true);
        this.addParticleType("redstone_dust", Particles.REDSTONE, false, ImmutableMap.of(
                ParticleOptions.COLOR, Color.RED));
        this.addParticleType("slime", Particles.SLIME, false);
        this.addParticleType("smoke", Particles.SMOKE_NORMAL, true);
        this.addParticleType("snowball", Particles.SNOWBALL, false);
        this.addParticleType("snow_shovel", Particles.SNOW_SHOVEL, true);
        this.addParticleType("spell", Particles.SPELL, true, ImmutableMap.of(
                ParticleOptions.SLOW_HORIZONTAL_VELOCITY, false));
        this.addEffectType("splash_potion", null, ImmutableMap.of(
                ParticleOptions.POTION_EFFECT_TYPE, PotionEffectTypes.ABSORPTION));
        this.addParticleType("suspended", Particles.SUSPENDED, false);
        this.addParticleType("suspended_depth", Particles.SUSPENDED_DEPTH, false);
        this.addParticleType("sweep_attack", Particles.SWEEP_ATTACK, false, ImmutableMap.of(
                ParticleOptions.SCALE, 1.0));
        this.addParticleType("town_aura", Particles.TOWN_AURA, true);
        this.addParticleType("water_bubble", Particles.WATER_BUBBLE, true);
        this.addParticleType("water_drop", Particles.WATER_DROP, false);
        this.addParticleType("water_splash", Particles.WATER_SPLASH, true);
        this.addParticleType("water_wake", Particles.WATER_WAKE, true);
        this.addParticleType("witch_spell", Particles.WITCH, true, ImmutableMap.of(
                ParticleOptions.SLOW_HORIZONTAL_VELOCITY, false));
        // Is not exposed in the api, since it doesn't do anything
        this.addParticleType("item_take", Particles.ITEM_TAKE, false);
    }

    private void addParticleType(String id, net.minecraft.particles.ParticleType<?> internalType, boolean velocity) {
        this.addParticleType(id, internalType, velocity, Collections.emptyMap());
    }

    private void addParticleType(String id, net.minecraft.particles.ParticleType<?> internalType, boolean velocity,
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

    private void addEffectType(String id, @Nullable net.minecraft.particles.ParticleType<?> internalType, Map<ParticleOption<?>, Object> options) {
        SpongeParticleType particleType = new SpongeParticleType(CatalogKey.minecraft(id), internalType, options);
        this.map.put(CatalogKey.minecraft(id), particleType);
        this.particleByName.put(particleType.getKey().toString().toLowerCase(Locale.ENGLISH), particleType);
    }

    ParticleRegistryModule() {
    }

    private static class Holder {
        static final ParticleRegistryModule INSTANCE = new ParticleRegistryModule();
    }
}
