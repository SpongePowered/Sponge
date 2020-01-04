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
package org.spongepowered.common.registry.builtin;

import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class EntityTypeSupplier {

    private EntityTypeSupplier() {
    }

    public static void registerSuppliers(SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(EntityType.class, "area_effect_cloud", () -> (EntityType) net.minecraft.entity.EntityType.AREA_EFFECT_CLOUD)
            .registerSupplier(EntityType.class, "armor_stand", () -> (EntityType) net.minecraft.entity.EntityType.ARMOR_STAND)
            .registerSupplier(EntityType.class, "arrow", () -> (EntityType) net.minecraft.entity.EntityType.ARROW)
            .registerSupplier(EntityType.class, "bat", () -> (EntityType) net.minecraft.entity.EntityType.BAT)
            .registerSupplier(EntityType.class, "blaze", () -> (EntityType) net.minecraft.entity.EntityType.BLAZE)
            .registerSupplier(EntityType.class, "boat", () -> (EntityType) net.minecraft.entity.EntityType.BOAT)
            .registerSupplier(EntityType.class, "cat", () -> (EntityType) net.minecraft.entity.EntityType.CAT)
            .registerSupplier(EntityType.class, "cave_spider", () -> (EntityType) net.minecraft.entity.EntityType.CAVE_SPIDER)
            .registerSupplier(EntityType.class, "chicken", () -> (EntityType) net.minecraft.entity.EntityType.CHICKEN)
            .registerSupplier(EntityType.class, "cod", () -> (EntityType) net.minecraft.entity.EntityType.COD)
            .registerSupplier(EntityType.class, "cow", () -> (EntityType) net.minecraft.entity.EntityType.COW)
            .registerSupplier(EntityType.class, "creeper", () -> (EntityType) net.minecraft.entity.EntityType.CREEPER)
            .registerSupplier(EntityType.class, "donkey", () -> (EntityType) net.minecraft.entity.EntityType.DONKEY)
            .registerSupplier(EntityType.class, "dolphin", () -> (EntityType) net.minecraft.entity.EntityType.DOLPHIN)
            .registerSupplier(EntityType.class, "dragon_fireball", () -> (EntityType) net.minecraft.entity.EntityType.DRAGON_FIREBALL)
            .registerSupplier(EntityType.class, "drowned", () -> (EntityType) net.minecraft.entity.EntityType.DROWNED)
            .registerSupplier(EntityType.class, "elder_guardian", () -> (EntityType) net.minecraft.entity.EntityType.ELDER_GUARDIAN)
            .registerSupplier(EntityType.class, "end_crystal", () -> (EntityType) net.minecraft.entity.EntityType.END_CRYSTAL)
            .registerSupplier(EntityType.class, "ender_dragon", () -> (EntityType) net.minecraft.entity.EntityType.ENDER_DRAGON)
            .registerSupplier(EntityType.class, "enderman", () -> (EntityType) net.minecraft.entity.EntityType.ENDERMAN)
            .registerSupplier(EntityType.class, "endermite", () -> (EntityType) net.minecraft.entity.EntityType.ENDERMITE)
            .registerSupplier(EntityType.class, "evoker_fangs", () -> (EntityType) net.minecraft.entity.EntityType.EVOKER_FANGS)
            .registerSupplier(EntityType.class, "evoker", () -> (EntityType) net.minecraft.entity.EntityType.EVOKER)
            .registerSupplier(EntityType.class, "experience_orb", () -> (EntityType) net.minecraft.entity.EntityType.EXPERIENCE_ORB)
            .registerSupplier(EntityType.class, "eye_of_ender", () -> (EntityType) net.minecraft.entity.EntityType.EYE_OF_ENDER)
            .registerSupplier(EntityType.class, "falling_block", () -> (EntityType) net.minecraft.entity.EntityType.FALLING_BLOCK)
            .registerSupplier(EntityType.class, "firework_rocket", () -> (EntityType) net.minecraft.entity.EntityType.FIREWORK_ROCKET)
            .registerSupplier(EntityType.class, "fox", () -> (EntityType) net.minecraft.entity.EntityType.FOX)
            .registerSupplier(EntityType.class, "ghast", () -> (EntityType) net.minecraft.entity.EntityType.GHAST)
            .registerSupplier(EntityType.class, "giant", () -> (EntityType) net.minecraft.entity.EntityType.GIANT)
            .registerSupplier(EntityType.class, "guardian", () -> (EntityType) net.minecraft.entity.EntityType.GUARDIAN)
            .registerSupplier(EntityType.class, "horse", () -> (EntityType) net.minecraft.entity.EntityType.HORSE)
            .registerSupplier(EntityType.class, "husk", () -> (EntityType) net.minecraft.entity.EntityType.HUSK)
            .registerSupplier(EntityType.class, "illusioner", () -> (EntityType) net.minecraft.entity.EntityType.ILLUSIONER)
            .registerSupplier(EntityType.class, "item", () -> (EntityType) net.minecraft.entity.EntityType.ITEM)
            .registerSupplier(EntityType.class, "item_frame", () -> (EntityType) net.minecraft.entity.EntityType.ITEM_FRAME)
            .registerSupplier(EntityType.class, "fireball", () -> (EntityType) net.minecraft.entity.EntityType.FIREBALL)
            .registerSupplier(EntityType.class, "leash_knot", () -> (EntityType) net.minecraft.entity.EntityType.LEASH_KNOT)
            .registerSupplier(EntityType.class, "llama", () -> (EntityType) net.minecraft.entity.EntityType.LLAMA)
            .registerSupplier(EntityType.class, "llama_spit", () -> (EntityType) net.minecraft.entity.EntityType.LLAMA_SPIT)
            .registerSupplier(EntityType.class, "magma_cube", () -> (EntityType) net.minecraft.entity.EntityType.MAGMA_CUBE)
            .registerSupplier(EntityType.class, "minecart", () -> (EntityType) net.minecraft.entity.EntityType.MINECART)
            .registerSupplier(EntityType.class, "chest_minecart", () -> (EntityType) net.minecraft.entity.EntityType.CHEST_MINECART)
            .registerSupplier(EntityType.class, "command_block_minecart", () -> (EntityType) net.minecraft.entity.EntityType.COMMAND_BLOCK_MINECART)
            .registerSupplier(EntityType.class, "furnace_minecart", () -> (EntityType) net.minecraft.entity.EntityType.FURNACE_MINECART)
            .registerSupplier(EntityType.class, "hopper_minecart", () -> (EntityType) net.minecraft.entity.EntityType.HOPPER_MINECART)
            .registerSupplier(EntityType.class, "spawner_minecart", () -> (EntityType) net.minecraft.entity.EntityType.SPAWNER_MINECART)
            .registerSupplier(EntityType.class, "tnt_minecart", () -> (EntityType) net.minecraft.entity.EntityType.TNT_MINECART)
            .registerSupplier(EntityType.class, "mule", () -> (EntityType) net.minecraft.entity.EntityType.MULE)
            .registerSupplier(EntityType.class, "mooshroom", () -> (EntityType) net.minecraft.entity.EntityType.MOOSHROOM)
            .registerSupplier(EntityType.class, "ocelot", () -> (EntityType) net.minecraft.entity.EntityType.OCELOT)
            .registerSupplier(EntityType.class, "painting", () -> (EntityType) net.minecraft.entity.EntityType.PAINTING)
            .registerSupplier(EntityType.class, "panda", () -> (EntityType) net.minecraft.entity.EntityType.PANDA)
            .registerSupplier(EntityType.class, "parrot", () -> (EntityType) net.minecraft.entity.EntityType.PARROT)
            .registerSupplier(EntityType.class, "pig", () -> (EntityType) net.minecraft.entity.EntityType.PIG)
            .registerSupplier(EntityType.class, "pufferfish", () -> (EntityType) net.minecraft.entity.EntityType.PUFFERFISH)
            .registerSupplier(EntityType.class, "zombie_pigman", () -> (EntityType) net.minecraft.entity.EntityType.ZOMBIE_PIGMAN)
            .registerSupplier(EntityType.class, "polar_bear", () -> (EntityType) net.minecraft.entity.EntityType.POLAR_BEAR)
            .registerSupplier(EntityType.class, "tnt", () -> (EntityType) net.minecraft.entity.EntityType.TNT)
            .registerSupplier(EntityType.class, "rabbit", () -> (EntityType) net.minecraft.entity.EntityType.RABBIT)
            .registerSupplier(EntityType.class, "salmon", () -> (EntityType) net.minecraft.entity.EntityType.SALMON)
            .registerSupplier(EntityType.class, "sheep", () -> (EntityType) net.minecraft.entity.EntityType.SHEEP)
            .registerSupplier(EntityType.class, "shulker", () -> (EntityType) net.minecraft.entity.EntityType.SHULKER)
            .registerSupplier(EntityType.class, "shulker_bullet", () -> (EntityType) net.minecraft.entity.EntityType.SHULKER_BULLET)
            .registerSupplier(EntityType.class, "silverfish", () -> (EntityType) net.minecraft.entity.EntityType.SILVERFISH)
            .registerSupplier(EntityType.class, "skeleton", () -> (EntityType) net.minecraft.entity.EntityType.SKELETON)
            .registerSupplier(EntityType.class, "skeleton_horse", () -> (EntityType) net.minecraft.entity.EntityType.SKELETON_HORSE)
            .registerSupplier(EntityType.class, "slime", () -> (EntityType) net.minecraft.entity.EntityType.SLIME)
            .registerSupplier(EntityType.class, "small_fireball", () -> (EntityType) net.minecraft.entity.EntityType.SMALL_FIREBALL)
            .registerSupplier(EntityType.class, "snow_golem", () -> (EntityType) net.minecraft.entity.EntityType.SNOW_GOLEM)
            .registerSupplier(EntityType.class, "snowball", () -> (EntityType) net.minecraft.entity.EntityType.SNOWBALL)
            .registerSupplier(EntityType.class, "spectral_arrow", () -> (EntityType) net.minecraft.entity.EntityType.SPECTRAL_ARROW)
            .registerSupplier(EntityType.class, "spider", () -> (EntityType) net.minecraft.entity.EntityType.SPIDER)
            .registerSupplier(EntityType.class, "squid", () -> (EntityType) net.minecraft.entity.EntityType.SQUID)
            .registerSupplier(EntityType.class, "stray", () -> (EntityType) net.minecraft.entity.EntityType.STRAY)
            .registerSupplier(EntityType.class, "trader_llama", () -> (EntityType) net.minecraft.entity.EntityType.TRADER_LLAMA)
            .registerSupplier(EntityType.class, "tropical_fish", () -> (EntityType) net.minecraft.entity.EntityType.TROPICAL_FISH)
            .registerSupplier(EntityType.class, "turtle", () -> (EntityType) net.minecraft.entity.EntityType.TURTLE)
            .registerSupplier(EntityType.class, "egg", () -> (EntityType) net.minecraft.entity.EntityType.EGG)
            .registerSupplier(EntityType.class, "ender_pearl", () -> (EntityType) net.minecraft.entity.EntityType.ENDER_PEARL)
            .registerSupplier(EntityType.class, "experience_bottle", () -> (EntityType) net.minecraft.entity.EntityType.EXPERIENCE_BOTTLE)
            .registerSupplier(EntityType.class, "potion", () -> (EntityType) net.minecraft.entity.EntityType.POTION)
            .registerSupplier(EntityType.class, "trident", () -> (EntityType) net.minecraft.entity.EntityType.TRIDENT)
            .registerSupplier(EntityType.class, "vex", () -> (EntityType) net.minecraft.entity.EntityType.VEX)
            .registerSupplier(EntityType.class, "villager", () -> (EntityType) net.minecraft.entity.EntityType.VILLAGER)
            .registerSupplier(EntityType.class, "iron_golem", () -> (EntityType) net.minecraft.entity.EntityType.IRON_GOLEM)
            .registerSupplier(EntityType.class, "vindicator", () -> (EntityType) net.minecraft.entity.EntityType.VINDICATOR)
            .registerSupplier(EntityType.class, "pillager", () -> (EntityType) net.minecraft.entity.EntityType.PILLAGER)
            .registerSupplier(EntityType.class, "wandering_trader", () -> (EntityType) net.minecraft.entity.EntityType.WANDERING_TRADER)
            .registerSupplier(EntityType.class, "witch", () -> (EntityType) net.minecraft.entity.EntityType.WITCH)
            .registerSupplier(EntityType.class, "wither", () -> (EntityType) net.minecraft.entity.EntityType.WITHER)
            .registerSupplier(EntityType.class, "wither_skeleton", () -> (EntityType) net.minecraft.entity.EntityType.WITHER_SKELETON)
            .registerSupplier(EntityType.class, "wither_skull", () -> (EntityType) net.minecraft.entity.EntityType.WITHER_SKULL)
            .registerSupplier(EntityType.class, "wolf", () -> (EntityType) net.minecraft.entity.EntityType.WOLF)
            .registerSupplier(EntityType.class, "zombie", () -> (EntityType) net.minecraft.entity.EntityType.ZOMBIE)
            .registerSupplier(EntityType.class, "zombie_horse", () -> (EntityType) net.minecraft.entity.EntityType.ZOMBIE_HORSE)
            .registerSupplier(EntityType.class, "zombie_villager", () -> (EntityType) net.minecraft.entity.EntityType.ZOMBIE_VILLAGER)
            .registerSupplier(EntityType.class, "phantom", () -> (EntityType) net.minecraft.entity.EntityType.PHANTOM)
            .registerSupplier(EntityType.class, "ravager", () -> (EntityType) net.minecraft.entity.EntityType.RAVAGER)
            .registerSupplier(EntityType.class, "lightning_bolt", () -> (EntityType) net.minecraft.entity.EntityType.LIGHTNING_BOLT)
            .registerSupplier(EntityType.class, "player", () -> (EntityType) net.minecraft.entity.EntityType.PLAYER)
            .registerSupplier(EntityType.class, "fishing_bobber", () -> (EntityType) net.minecraft.entity.EntityType.FISHING_BOBBER)
        ;
    }
}
