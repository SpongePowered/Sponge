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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.effect.particle.SpongeParticleType;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
        this.addParticleType("explosion_normal", new SpongeParticleType(EnumParticleTypes.EXPLOSION_NORMAL, "explosion_normal", true));
        this.addParticleType("explosion_large", new SpongeParticleType.Resizable(EnumParticleTypes.EXPLOSION_LARGE, "explosion_large", 1f));
        this.addParticleType("explosion_huge", new SpongeParticleType(EnumParticleTypes.EXPLOSION_HUGE, "explosion_huge", false));
        this.addParticleType("fireworks_spark", new SpongeParticleType(EnumParticleTypes.FIREWORKS_SPARK, "fireworks_spark", true));
        this.addParticleType("water_bubble", new SpongeParticleType(EnumParticleTypes.WATER_BUBBLE, "water_bubble", true));
        this.addParticleType("water_splash", new SpongeParticleType(EnumParticleTypes.WATER_SPLASH, "water_splash", true));
        this.addParticleType("water_wake", new SpongeParticleType(EnumParticleTypes.WATER_WAKE, "water_wake", true));
        this.addParticleType("suspended", new SpongeParticleType(EnumParticleTypes.SUSPENDED, "suspended", false));
        this.addParticleType("suspended_depth", new SpongeParticleType(EnumParticleTypes.SUSPENDED_DEPTH, "suspended_depth", false));
        this.addParticleType("crit", new SpongeParticleType(EnumParticleTypes.CRIT, "crit", true));
        this.addParticleType("crit_magic", new SpongeParticleType(EnumParticleTypes.CRIT_MAGIC, "crit_magic", true));
        this.addParticleType("smoke_normal", new SpongeParticleType(EnumParticleTypes.SMOKE_NORMAL, "smoke_normal", true));
        this.addParticleType("smoke_large", new SpongeParticleType(EnumParticleTypes.SMOKE_LARGE, "smoke_large", true));
        this.addParticleType("spell", new SpongeParticleType(EnumParticleTypes.SPELL, "spell", false));
        this.addParticleType("spell_instant", new SpongeParticleType(EnumParticleTypes.SPELL_INSTANT, "spell_instant", false));
        this.addParticleType("spell_mob", new SpongeParticleType.Colorable(EnumParticleTypes.SPELL_MOB, "spell_mob", Color.BLACK));
        this.addParticleType("spell_mob_ambient", new SpongeParticleType.Colorable(EnumParticleTypes.SPELL_MOB_AMBIENT, "spell_mob_ambient", Color.BLACK));
        this.addParticleType("spell_witch", new SpongeParticleType(EnumParticleTypes.SPELL_WITCH, "spell_witch", false));
        this.addParticleType("drip_water", new SpongeParticleType(EnumParticleTypes.DRIP_WATER, "drip_water", false));
        this.addParticleType("drip_lava", new SpongeParticleType(EnumParticleTypes.DRIP_LAVA, "drip_lava", false));
        this.addParticleType("villager_angry", new SpongeParticleType(EnumParticleTypes.VILLAGER_ANGRY, "villager_angry", false));
        this.addParticleType("villager_happy", new SpongeParticleType(EnumParticleTypes.VILLAGER_HAPPY, "villager_happy", true));
        this.addParticleType("town_aura", new SpongeParticleType(EnumParticleTypes.TOWN_AURA, "town_aura", true));
        this.addParticleType("note", new SpongeParticleType.Note(EnumParticleTypes.NOTE, "note", NotePitches.F_SHARP0));
        this.addParticleType("portal", new SpongeParticleType(EnumParticleTypes.PORTAL, "portal", true));
        this.addParticleType("enchantment_table", new SpongeParticleType(EnumParticleTypes.ENCHANTMENT_TABLE, "enchantment_table", true));
        this.addParticleType("flame", new SpongeParticleType(EnumParticleTypes.FLAME, "flame", true));
        this.addParticleType("lava", new SpongeParticleType(EnumParticleTypes.LAVA, "lava", false));
        this.addParticleType("footstep", new SpongeParticleType(EnumParticleTypes.FOOTSTEP, "footstep", false));
        this.addParticleType("cloud", new SpongeParticleType(EnumParticleTypes.CLOUD, "cloud", true));
        this.addParticleType("redstone", new SpongeParticleType.Colorable(EnumParticleTypes.REDSTONE, "redstone", Color.RED));
        this.addParticleType("snowball", new SpongeParticleType(EnumParticleTypes.SNOWBALL, "snowball", false));
        this.addParticleType("snow_shovel", new SpongeParticleType(EnumParticleTypes.SNOW_SHOVEL, "snow_shovel", true));
        this.addParticleType("slime", new SpongeParticleType(EnumParticleTypes.SLIME, "slime", false));
        this.addParticleType("heart", new SpongeParticleType(EnumParticleTypes.HEART, "heart", false));
        this.addParticleType("barrier", new SpongeParticleType(EnumParticleTypes.BARRIER, "barrier", false));
        this.addParticleType("item_crack",
                             new SpongeParticleType.Item(EnumParticleTypes.ITEM_CRACK, "item_crack", new net.minecraft.item.ItemStack(Blocks.STONE), true));
        this.addParticleType("block_crack",
                             new SpongeParticleType.Block(EnumParticleTypes.BLOCK_CRACK, "block_crack", (BlockState) Blocks.STONE.getDefaultState(), true));
        this.addParticleType("block_dust",
                             new SpongeParticleType.Block(EnumParticleTypes.BLOCK_DUST, "block_dust", (BlockState) Blocks.STONE.getDefaultState(), true));
        this.addParticleType("water_drop", new SpongeParticleType(EnumParticleTypes.WATER_DROP, "water_drop", false));
        // Is this particle available to be spawned? It's not registered on the
        // client though
        this.addParticleType("item_take", new SpongeParticleType(EnumParticleTypes.ITEM_TAKE, "item_take", false));
        this.addParticleType("mob_appearance", new SpongeParticleType(EnumParticleTypes.MOB_APPEARANCE, "mob_appearance", false));
    }

    private void addParticleType(String mapping, SpongeParticleType particle) {
        this.particleMappings.put(mapping, particle);
        this.particleByName.put(particle.getName(), particle);
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (EnumParticleTypes particleTypes : EnumParticleTypes.values()) {
            if (!this.particleByName.containsKey(particleTypes.getParticleName())) {
                addParticleType(particleTypes.getParticleName().toLowerCase(Locale.ENGLISH), new SpongeParticleType(particleTypes, particleTypes.getParticleName().toLowerCase(Locale.ENGLISH), false));
            }
        }

    }
}
