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
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.effect.particle.SpongeParticleType;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.NotePitchRegistryModule;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency({ ParticleOptionRegistryModule.class, NotePitchRegistryModule.class, BlockTypeRegistryModule.class,
        ItemTypeRegistryModule.class})
public final class ParticleRegistryModule implements CatalogRegistryModule<ParticleType> {

    @RegisterCatalog(ParticleTypes.class)
    private final Map<String, SpongeParticleType> particleMappings = Maps.newHashMap();
    private final Map<String, ParticleType> particleByName = Maps.newHashMap();

    @Override
    public Optional<ParticleType> getById(String id) {
        return Optional.ofNullable(this.particleByName.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<ParticleType> getAll() {
        return ImmutableList.copyOf(this.particleByName.values());
    }

    @Override
    public void registerDefaults() {
        this.addParticleType("ambient_mob_spell", "ambientMobSpell", EnumParticleTypes.SPELL_MOB_AMBIENT, false, ImmutableMap.of(
                ParticleOptions.COLOR, Color.BLACK));
        this.addParticleType("angry_villager", "angryVillager", EnumParticleTypes.VILLAGER_ANGRY, false);
        this.addParticleType("barrier", "barrier", EnumParticleTypes.BARRIER, false);
        this.addParticleType("block_crack", "blockCrack", EnumParticleTypes.BLOCK_CRACK, true, ImmutableMap.of(
                ParticleOptions.BLOCK_STATE, Blocks.STONE.getDefaultState(),
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.STONE))));
        this.addParticleType("block_dust", "blockDust", EnumParticleTypes.BLOCK_DUST, true, ImmutableMap.of(
                ParticleOptions.BLOCK_STATE, Blocks.STONE.getDefaultState(),
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.STONE))));
        this.addParticleType("cloud", "cloud", EnumParticleTypes.CLOUD, true);
        this.addParticleType("critical_hit", "criticalHit", EnumParticleTypes.CRIT, true);
        this.addParticleType("damage_indicator", "damageIndicator", EnumParticleTypes.DAMAGE_INDICATOR, true);
        this.addParticleType("dragon_breath", "dragonBreath", EnumParticleTypes.DRAGON_BREATH, true);
        this.addParticleType("drip_lava", "dripLava", EnumParticleTypes.DRIP_LAVA, false);
        this.addParticleType("drip_water", "dripWater", EnumParticleTypes.DRIP_WATER, false);
        this.addParticleType("enchanting_glyphs", "enchantingGlyphs", EnumParticleTypes.ENCHANTMENT_TABLE, true);
        this.addParticleType("end_rod", "endRod", EnumParticleTypes.END_ROD, true);
        this.addParticleType("explosion", "explosion", EnumParticleTypes.EXPLOSION_NORMAL, true);
        this.addParticleType("fireworks_spark", "fireworksSpark", EnumParticleTypes.FIREWORKS_SPARK, true);
        this.addParticleType("flame", "flame", EnumParticleTypes.FLAME, true);
        this.addParticleType("footstep", "footstep", EnumParticleTypes.FOOTSTEP, false);
        this.addParticleType("guardian_appearance", "guardianAppearance", EnumParticleTypes.MOB_APPEARANCE, false);
        this.addParticleType("happy_villager", "happyVillager", EnumParticleTypes.VILLAGER_HAPPY, true);
        this.addParticleType("heart", "heart", EnumParticleTypes.HEART, false);
        this.addParticleType("huge_explosion", "hugeExplosion", EnumParticleTypes.EXPLOSION_HUGE, false);
        this.addParticleType("instant_spell", "instantSpell", EnumParticleTypes.SPELL_INSTANT, false);
        this.addParticleType("item_crack", "itemCrack", EnumParticleTypes.ITEM_CRACK, true, ImmutableMap.of(
                ParticleOptions.ITEM_STACK_SNAPSHOT, new SpongeItemStackSnapshot((ItemStack) new net.minecraft.item.ItemStack(Blocks.STONE))));
        this.addParticleType("large_explosion", "largeExplosion", EnumParticleTypes.EXPLOSION_LARGE, false, ImmutableMap.of(
                ParticleOptions.SCALE, 1.0));
        this.addParticleType("large_smoke", "largeSmoke", EnumParticleTypes.SMOKE_LARGE, true);
        this.addParticleType("lava", "lava", EnumParticleTypes.LAVA, false);
        this.addParticleType("magic_critical_hit", "magicCriticalHit", EnumParticleTypes.CRIT_MAGIC, true);
        this.addParticleType("mob_spell", "mobSpell", EnumParticleTypes.END_ROD, false, ImmutableMap.of(
                ParticleOptions.COLOR, Color.BLACK));
        this.addParticleType("note", "note", EnumParticleTypes.NOTE, false, ImmutableMap.of(
                ParticleOptions.NOTE, NotePitches.F_SHARP0));
        this.addParticleType("portal", "portal", EnumParticleTypes.PORTAL, true);
        this.addParticleType("redstone_dust", "redstoneDust", EnumParticleTypes.REDSTONE, false, ImmutableMap.of(
                ParticleOptions.COLOR, Color.RED));
        this.addParticleType("slime", "slime", EnumParticleTypes.SLIME, false);
        this.addParticleType("smoke", "smoke", EnumParticleTypes.SMOKE_NORMAL, true);
        this.addParticleType("snowball", "snowball", EnumParticleTypes.SNOWBALL, false);
        this.addParticleType("snow_shovel", "snowShovel", EnumParticleTypes.SNOW_SHOVEL, true);
        // TODO: Has vertical velocity and x and z velocity are * 0.1 on the client when x and y are 0 on the server
        this.addParticleType("spell", "spell", EnumParticleTypes.SPELL, false);
        this.addParticleType("suspended", "suspended", EnumParticleTypes.SUSPENDED, false);
        this.addParticleType("suspended_depth", "suspendedDepth", EnumParticleTypes.SUSPENDED_DEPTH, false);
        this.addParticleType("sweep_attack", "sweepAttack", EnumParticleTypes.SWEEP_ATTACK, false, ImmutableMap.of(
                ParticleOptions.SCALE, 1.0));
        this.addParticleType("town_aura", "townAura", EnumParticleTypes.TOWN_AURA, true);
        this.addParticleType("water_bubble", "waterBubble", EnumParticleTypes.WATER_BUBBLE, true);
        this.addParticleType("water_drop", "waterDrop", EnumParticleTypes.WATER_DROP, false);
        this.addParticleType("water_splash", "waterSplash", EnumParticleTypes.WATER_SPLASH, true);
        this.addParticleType("water_wake", "waterWake", EnumParticleTypes.WATER_WAKE, true);
        this.addParticleType("witch_spell", "witchSpell", EnumParticleTypes.SPELL_WITCH, false);
        // Is not exposed in the api, since it doesn't do anything
        this.addParticleType("item_take", "itemTake", EnumParticleTypes.ITEM_TAKE, false);
    }

    private void addParticleType(String mapping, String id, EnumParticleTypes internalType, boolean velocity) {
        this.addParticleType(mapping, id, internalType, velocity, Collections.emptyMap());
    }

    private void addParticleType(String mapping, String id, EnumParticleTypes internalType, boolean velocity,
            Map<ParticleOption<?>, Object> extraOptions) {
        ImmutableMap.Builder<ParticleOption<?>, Object> options = ImmutableMap.builder();
        options.put(ParticleOptions.OFFSET, Vector3d.ZERO);
        options.put(ParticleOptions.COUNT, 1);
        if (velocity) {
            options.put(ParticleOptions.VELOCITY, Vector3d.ZERO);
        }
        options.putAll(extraOptions);
        SpongeParticleType particleType = new SpongeParticleType(id, internalType, options.build());
        this.particleMappings.put(mapping, particleType);
        this.particleByName.put(particleType.getId().toLowerCase(Locale.ENGLISH), particleType);
    }
}
