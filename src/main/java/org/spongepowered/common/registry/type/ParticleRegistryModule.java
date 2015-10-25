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
package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.common.effect.particle.SpongeParticleType;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.AdditionalRegistration;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class ParticleRegistryModule implements CatalogRegistryModule<ParticleType> {

    private final Map<String, SpongeParticleType> particleMappings = Maps.newHashMap();
    private final Map<String, ParticleType> particleByName = Maps.newHashMap();

    @Override
    public Optional<ParticleType> getById(String id) {
        return Optional.ofNullable(this.particleByName.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<ParticleType> getAll() {
        return ImmutableList.copyOf(this.particleByName.values());
    }

    @Override
    public void registerDefaults() {
        this.addParticleType("explosion_normal", new SpongeParticleType(EnumParticleTypes.EXPLOSION_NORMAL, true));
        this.addParticleType("explosion_large", new SpongeParticleType.Resizable(EnumParticleTypes.EXPLOSION_LARGE, 1f));
        this.addParticleType("explosion_huge", new SpongeParticleType(EnumParticleTypes.EXPLOSION_HUGE, false));
        this.addParticleType("fireworks_spark", new SpongeParticleType(EnumParticleTypes.FIREWORKS_SPARK, true));
        this.addParticleType("water_bubble", new SpongeParticleType(EnumParticleTypes.WATER_BUBBLE, true));
        this.addParticleType("water_splash", new SpongeParticleType(EnumParticleTypes.WATER_SPLASH, true));
        this.addParticleType("water_wake", new SpongeParticleType(EnumParticleTypes.WATER_WAKE, true));
        this.addParticleType("suspended", new SpongeParticleType(EnumParticleTypes.SUSPENDED, false));
        this.addParticleType("suspended_depth", new SpongeParticleType(EnumParticleTypes.SUSPENDED_DEPTH, false));
        this.addParticleType("crit", new SpongeParticleType(EnumParticleTypes.CRIT, true));
        this.addParticleType("crit_magic", new SpongeParticleType(EnumParticleTypes.CRIT_MAGIC, true));
        this.addParticleType("smoke_normal", new SpongeParticleType(EnumParticleTypes.SMOKE_NORMAL, true));
        this.addParticleType("smoke_large", new SpongeParticleType(EnumParticleTypes.SMOKE_LARGE, true));
        this.addParticleType("spell", new SpongeParticleType(EnumParticleTypes.SPELL, false));
        this.addParticleType("spell_instant", new SpongeParticleType(EnumParticleTypes.SPELL_INSTANT, false));
        this.addParticleType("spell_mob", new SpongeParticleType.Colorable(EnumParticleTypes.SPELL_MOB, Color.BLACK));
        this.addParticleType("spell_mob_ambient", new SpongeParticleType.Colorable(EnumParticleTypes.SPELL_MOB_AMBIENT, Color.BLACK));
        this.addParticleType("spell_witch", new SpongeParticleType(EnumParticleTypes.SPELL_WITCH, false));
        this.addParticleType("drip_water", new SpongeParticleType(EnumParticleTypes.DRIP_WATER, false));
        this.addParticleType("drip_lava", new SpongeParticleType(EnumParticleTypes.DRIP_LAVA, false));
        this.addParticleType("villager_angry", new SpongeParticleType(EnumParticleTypes.VILLAGER_ANGRY, false));
        this.addParticleType("villager_happy", new SpongeParticleType(EnumParticleTypes.VILLAGER_HAPPY, true));
        this.addParticleType("town_aura", new SpongeParticleType(EnumParticleTypes.TOWN_AURA, true));
        this.addParticleType("note", new SpongeParticleType.Note(EnumParticleTypes.NOTE, 0f));
        this.addParticleType("portal", new SpongeParticleType(EnumParticleTypes.PORTAL, true));
        this.addParticleType("enchantment_table", new SpongeParticleType(EnumParticleTypes.ENCHANTMENT_TABLE, true));
        this.addParticleType("flame", new SpongeParticleType(EnumParticleTypes.FLAME, true));
        this.addParticleType("lava", new SpongeParticleType(EnumParticleTypes.LAVA, false));
        this.addParticleType("footstep", new SpongeParticleType(EnumParticleTypes.FOOTSTEP, false));
        this.addParticleType("cloud", new SpongeParticleType(EnumParticleTypes.CLOUD, true));
        this.addParticleType("redstone", new SpongeParticleType.Colorable(EnumParticleTypes.REDSTONE, Color.RED));
        this.addParticleType("snowball", new SpongeParticleType(EnumParticleTypes.SNOWBALL, false));
        this.addParticleType("snow_shovel", new SpongeParticleType(EnumParticleTypes.SNOW_SHOVEL, true));
        this.addParticleType("slime", new SpongeParticleType(EnumParticleTypes.SLIME, false));
        this.addParticleType("heart", new SpongeParticleType(EnumParticleTypes.HEART, false));
        this.addParticleType("barrier", new SpongeParticleType(EnumParticleTypes.BARRIER, false));
        this.addParticleType("item_crack",
                             new SpongeParticleType.Material(EnumParticleTypes.ITEM_CRACK, new net.minecraft.item.ItemStack(Blocks.air), true));
        this.addParticleType("block_crack",
                             new SpongeParticleType.Material(EnumParticleTypes.BLOCK_CRACK, new net.minecraft.item.ItemStack(Blocks.air), true));
        this.addParticleType("block_dust",
                             new SpongeParticleType.Material(EnumParticleTypes.BLOCK_DUST, new net.minecraft.item.ItemStack(Blocks.air), true));
        this.addParticleType("water_drop", new SpongeParticleType(EnumParticleTypes.WATER_DROP, false));
        // Is this particle available to be spawned? It's not registered on the
        // client though
        this.addParticleType("item_take", new SpongeParticleType(EnumParticleTypes.ITEM_TAKE, false));
        this.addParticleType("mob_appearance", new SpongeParticleType(EnumParticleTypes.MOB_APPEARANCE, false));
    }

    private void addParticleType(String mapping, SpongeParticleType particle) {
        this.particleMappings.put(mapping, particle);
        this.particleByName.put(particle.getName(), particle);
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (EnumParticleTypes particleTypes : EnumParticleTypes.values()) {
            if (!this.particleByName.containsKey(particleTypes.getParticleName())) {
                addParticleType(particleTypes.getParticleName().toLowerCase(), new SpongeParticleType(particleTypes, false));
            }
        }

    }
}
