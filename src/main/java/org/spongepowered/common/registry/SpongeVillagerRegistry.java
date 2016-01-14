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
package org.spongepowered.common.registry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityVillager.EmeraldForItems;
import net.minecraft.entity.passive.EntityVillager.ItemAndEmeraldToItem;
import net.minecraft.entity.passive.EntityVillager.ListEnchantedBookForEmeralds;
import net.minecraft.entity.passive.EntityVillager.ListEnchantedItemForEmeralds;
import net.minecraft.entity.passive.EntityVillager.ListItemForEmeralds;
import net.minecraft.entity.passive.EntityVillager.PriceInfo;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Careers;
import org.spongepowered.api.item.merchant.TradeOfferListMutator;
import org.spongepowered.api.item.merchant.VillagerRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Basically, until Forge figures out their VillagerRegistry stuff, we can only hope to
 * make this useful by enforcing generic villager registrations ourselves.
 * The related Forge PR: https://github.com/MinecraftForge/MinecraftForge/pull/2337
 *
 * Note: This registry is being used by MixinVillager in common as Forge doesn't
 * currently change it.
 */
public final class SpongeVillagerRegistry implements VillagerRegistry {

    public static SpongeVillagerRegistry getInstance() {
        return Holder.INSTANCE;
    }

    private final Map<Career, Multimap<Integer, TradeOfferListMutator>> careerGeneratorMap = new HashMap<>();

    SpongeVillagerRegistry() {
    }

    @Override
    public Multimap<Integer, TradeOfferListMutator> getTradeOfferLevelMap(Career career) {
        final Multimap<Integer, TradeOfferListMutator> multimap = this.careerGeneratorMap.get(checkNotNull(career, "Career cannot be null!"));
        if (multimap == null) {
            return ImmutableMultimap.of();
        } else {
            return ImmutableMultimap.copyOf(multimap);
        }
    }

    @Override
    public VillagerRegistry addMutator(Career career, int level, TradeOfferListMutator generator) {
        checkArgument(level > 0, "Career level must be at least greater than zero!");
        checkNotNull(career, "Career cannot be null!");
        checkNotNull(generator, "Generator cannot be null!");
        Multimap<Integer, TradeOfferListMutator> multimap = this.careerGeneratorMap.get(career);
        if (multimap == null) {
            multimap = ArrayListMultimap.create(3, 3);
            this.careerGeneratorMap.put(career, multimap);
        }
        multimap.put(level, generator);
        return this;
    }

    @Override
    public VillagerRegistry addMutators(Career career, int level, TradeOfferListMutator generator, TradeOfferListMutator... generators) {
        checkArgument(level > 0, "Career level must be at least greater than zero!");
        checkNotNull(career, "Career cannot be null!");
        checkNotNull(generator, "Generator cannot be null!");
        checkNotNull(generators, "Generators cannot be null!");
        Multimap<Integer, TradeOfferListMutator> multimap = this.careerGeneratorMap.get(career);
        List<TradeOfferListMutator> list = new ArrayList<>();
        list.add(generator);
        for (TradeOfferListMutator element : generators) {
            list.add(checkNotNull(element, "TradeOfferListMutator cannot be null!"));
        }
        if (multimap == null) {
            multimap = ArrayListMultimap.create(3, list.size());
            this.careerGeneratorMap.put(career, multimap);
        }
        multimap.putAll(level, list);
        return this;
    }

    @Override
    public VillagerRegistry setMutators(Career career, int level, List<TradeOfferListMutator> generators) {
        checkArgument(level > 0, "Career level must be at least greater than zero!");
        checkNotNull(career, "Career cannot be null!");
        checkNotNull(generators, "Generators cannot be null!");
        Multimap<Integer, TradeOfferListMutator> multimap = this.careerGeneratorMap.get(career);
        if (multimap == null) {
            multimap = ArrayListMultimap.create(3, generators.size());
            this.careerGeneratorMap.put(career, multimap);
        }
        multimap.replaceValues(level, generators);
        return this;
    }

    @Override
    public VillagerRegistry setMutators(Career career, Multimap<Integer, TradeOfferListMutator> generatorMap) {
        checkNotNull(career, "Career cannot be null!");
        checkNotNull(generatorMap, "Generators cannot be null!");
        Multimap<Integer, TradeOfferListMutator> multimap = this.careerGeneratorMap.get(career);
        if (multimap != null) {
            multimap.clear();
        }
        multimap = ArrayListMultimap.create(generatorMap);
        this.careerGeneratorMap.put(career, multimap);
        return this;
    }

    static void registerVanillaTrades() {
        VillagerRegistry instance = getInstance();

        { // Farmers
            { // Farmer
                instance.setMutators(Careers.FARMER, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.wheat, new PriceInfo(18, 22))),
                        generatorFor(new EmeraldForItems(Items.potato, new PriceInfo(15, 19))),
                        generatorFor(new EmeraldForItems(Items.carrot, new PriceInfo(15, 19))),
                        generatorFor(new ListItemForEmeralds(Items.bread, new PriceInfo(-4, -2)))
                ))
                        .setMutators(Careers.FARMER, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Item.getItemFromBlock(Blocks.pumpkin), new PriceInfo(8, 13))),
                                generatorFor(new ListItemForEmeralds(Items.pumpkin_pie, new PriceInfo(-3, -2)))
                        ))
                        .setMutators(Careers.FARMER, 3, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Item.getItemFromBlock(Blocks.melon_block), new PriceInfo(7, 12))),
                                generatorFor(new ListItemForEmeralds(Items.apple, new PriceInfo(-5, -7)))
                        ))
                        .setMutators(Careers.FARMER, 4, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.cookie, new PriceInfo(-6, -10))),
                                generatorFor(new ListItemForEmeralds(Items.cake, new PriceInfo(1, 1)))
                        ));
            }
            { // Fisherman
                instance.setMutators(Careers.FISHERMAN, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.string, new PriceInfo(15, 20))),
                        generatorFor(new EmeraldForItems(Items.coal, new PriceInfo(16, 24))),
                        generatorFor(new ItemAndEmeraldToItem(Items.fish, new PriceInfo(6, 6), Items.cooked_fish, new PriceInfo(6, 6)))
                ))
                        .setMutators(Careers.FISHERMAN, 2, ImmutableList.of(
                                generatorFor(new ListEnchantedItemForEmeralds(Items.fishing_rod, new PriceInfo(7, 8)))
                        ));
            }
            { // Shepherd
                instance.setMutators(Careers.SHEPHERD, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Item.getItemFromBlock(Blocks.wool), new PriceInfo(16, 22))),
                        generatorFor(new ListItemForEmeralds(Items.shears, new PriceInfo(3, 4)))
                ))
                        .setMutators(Careers.SHEPHERD, 2, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 0), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 1), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 2), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 3), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 4), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 5), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 6), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 7), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 8), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 9), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 10), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 11), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 12), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 13), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 14), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.wool, 1, 15), new PriceInfo(1, 2)))
                        ));
            }
            { // Fletcher
                instance.setMutators(Careers.FLETCHER, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.string, new PriceInfo(15, 20))),
                        generatorFor(new ListItemForEmeralds(Items.arrow, new PriceInfo(-12, -8)))
                ))
                        .setMutators(Careers.FLETCHER, 2, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.bow, new PriceInfo(2, 3))),
                                generatorFor(new ItemAndEmeraldToItem(Item.getItemFromBlock(Blocks.gravel), new PriceInfo(10, 10), Items.flint,
                                        new PriceInfo(6, 10)))
                        ));
            }
        }
        { // Librarian
            { // Librarian
                instance.setMutators(Careers.LIBRARIAN, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.paper, new PriceInfo(24, 36))),
                        generatorFor(new ListEnchantedBookForEmeralds())
                ))
                        .setMutators(Careers.LIBRARIAN, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.book, new PriceInfo(8, 10))),
                                generatorFor(new ListItemForEmeralds(Items.compass, new PriceInfo(10, 12))),
                                generatorFor(new ListItemForEmeralds(Item.getItemFromBlock(Blocks.bookshelf), new PriceInfo(3, 4)))
                        ))
                        .setMutators(Careers.LIBRARIAN, 3, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.written_book, new PriceInfo(2, 2))),
                                generatorFor(new ListItemForEmeralds(Items.clock, new PriceInfo(10, 12))),
                                generatorFor(new ListItemForEmeralds(Item.getItemFromBlock(Blocks.glass), new PriceInfo(-5, -3)))
                        ))
                        .setMutators(Careers.LIBRARIAN, 4, ImmutableList.of(
                                generatorFor(new ListEnchantedBookForEmeralds())
                        ))
                        .setMutators(Careers.LIBRARIAN, 5, ImmutableList.of(
                                generatorFor(new ListEnchantedBookForEmeralds())
                        ))
                        .setMutators(Careers.LIBRARIAN, 6, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.name_tag, new PriceInfo(20, 22)))
                        ));
            }
        }
        { // Priest
            { // Cleric
                instance.setMutators(Careers.CLERIC, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.rotten_flesh, new PriceInfo(36, 40))),
                        generatorFor(new EmeraldForItems(Items.gold_ingot, new PriceInfo(8, 10)))
                ))
                        .setMutators(Careers.CLERIC, 2, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.redstone, new PriceInfo(-4, -1))),
                                generatorFor(
                                        new ListItemForEmeralds(new ItemStack(Items.dye, 1, EnumDyeColor.BLUE.getDyeDamage()), new PriceInfo(-2, -1)))
                        ))
                        .setMutators(Careers.CLERIC, 3, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.ender_eye, new PriceInfo(7, 11))),
                                generatorFor(new ListItemForEmeralds(Item.getItemFromBlock(Blocks.glowstone), new PriceInfo(-3, -1)))
                        ))
                        .setMutators(Careers.CLERIC, 4, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.experience_bottle, new PriceInfo(3, 11)))
                        ));
            }
        }
        { // Blacksmith
            { // Armorer
                instance.setMutators(Careers.ARMORER, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.coal, new PriceInfo(16, 24))),
                        generatorFor(new ListItemForEmeralds(Items.iron_helmet, new PriceInfo(4, 6)))
                ))
                        .setMutators(Careers.ARMORER, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.iron_ingot, new PriceInfo(7, 9))),
                                generatorFor(new ListItemForEmeralds(Items.iron_chestplate, new PriceInfo(10, 14)))
                        ))
                        .setMutators(Careers.ARMORER, 3, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.diamond, new PriceInfo(3, 4))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.diamond_chestplate, new PriceInfo(16, 19)))
                        ))
                        .setMutators(Careers.ARMORER, 4, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.chainmail_boots, new PriceInfo(5, 7))),
                                generatorFor(new ListItemForEmeralds(Items.chainmail_leggings, new PriceInfo(9, 11))),
                                generatorFor(new ListItemForEmeralds(Items.chainmail_helmet, new PriceInfo(5, 7))),
                                generatorFor(new ListItemForEmeralds(Items.chainmail_chestplate, new PriceInfo(11, 15)))
                        ));
            }
            { // Weapon Smith
                instance.setMutators(Careers.WEAPON_SMITH, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.coal, new PriceInfo(16, 24))),
                        generatorFor(new ListItemForEmeralds(Items.iron_axe, new PriceInfo(6, 8)))
                ))
                        .setMutators(Careers.WEAPON_SMITH, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.iron_ingot, new PriceInfo(7, 9))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.iron_sword, new PriceInfo(9, 10)))
                        ))
                        .setMutators(Careers.WEAPON_SMITH, 3, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.diamond, new PriceInfo(3, 4))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.diamond_sword, new PriceInfo(12, 15))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.diamond_axe, new PriceInfo(9, 12)))
                        ));
            }
            { // Tool Smith
                instance.setMutators(Careers.TOOL_SMITH, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.coal, new PriceInfo(16, 24))),
                        generatorFor(new ListEnchantedItemForEmeralds(Items.iron_shovel, new PriceInfo(5, 7)))
                ))
                        .setMutators(Careers.TOOL_SMITH, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.iron_ingot, new PriceInfo(7, 9))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.iron_pickaxe, new PriceInfo(9, 11)))
                        ))
                        .setMutators(Careers.TOOL_SMITH, 3, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.diamond, new PriceInfo(3, 4))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.diamond_pickaxe, new PriceInfo(12, 15)))
                        ));
            }
        }
        { // Butcher
            { // Butcher
                instance.setMutators(Careers.BUTCHER, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.porkchop, new PriceInfo(14, 18))),
                        generatorFor(new EmeraldForItems(Items.chicken, new PriceInfo(14, 18)))
                ))
                        .setMutators(Careers.BUTCHER, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.coal, new PriceInfo(16, 24))),
                                generatorFor(new ListItemForEmeralds(Items.cooked_porkchop, new PriceInfo(-7, -5))),
                                generatorFor(new ListItemForEmeralds(Items.cooked_chicken, new PriceInfo(-8, -6)))
                        ));
            }
            { // Leather Worker
                instance.setMutators(Careers.LEATHERWORKER, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.leather, new PriceInfo(9, 12))),
                        generatorFor(new ListItemForEmeralds(Items.leather_leggings, new PriceInfo(2, 4)))
                ))
                        .setMutators(Careers.LEATHERWORKER, 2, ImmutableList.of(
                                generatorFor(new ListEnchantedItemForEmeralds(Items.leather_chestplate, new PriceInfo(7, 12)))
                        ))
                        .setMutators(Careers.LEATHERWORKER, 3, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.saddle, new PriceInfo(8, 10)))
                        ));
            }
        }
    }

    private static TradeOfferListMutator generatorFor(EntityVillager.ITradeList iTradeList) {
        return (TradeOfferListMutator) iTradeList;
    }

    static final class Holder {
         static final SpongeVillagerRegistry INSTANCE = new SpongeVillagerRegistry();
    }
}
