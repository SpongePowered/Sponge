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
                        generatorFor(new EmeraldForItems(Items.WHEAT, new PriceInfo(18, 22))),
                        generatorFor(new EmeraldForItems(Items.POTATO, new PriceInfo(15, 19))),
                        generatorFor(new EmeraldForItems(Items.CARROT, new PriceInfo(15, 19))),
                        generatorFor(new ListItemForEmeralds(Items.BREAD, new PriceInfo(-4, -2)))
                ))
                        .setMutators(Careers.FARMER, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Item.getItemFromBlock(Blocks.PUMPKIN), new PriceInfo(8, 13))),
                                generatorFor(new ListItemForEmeralds(Items.PUMPKIN_PIE, new PriceInfo(-3, -2)))
                        ))
                        .setMutators(Careers.FARMER, 3, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Item.getItemFromBlock(Blocks.MELON_BLOCK), new PriceInfo(7, 12))),
                                generatorFor(new ListItemForEmeralds(Items.APPLE, new PriceInfo(-5, -7)))
                        ))
                        .setMutators(Careers.FARMER, 4, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.COOKIE, new PriceInfo(-6, -10))),
                                generatorFor(new ListItemForEmeralds(Items.CAKE, new PriceInfo(1, 1)))
                        ));
            }
            { // Fisherman
                instance.setMutators(Careers.FISHERMAN, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.STRING, new PriceInfo(15, 20))),
                        generatorFor(new EmeraldForItems(Items.COAL, new PriceInfo(16, 24))),
                        generatorFor(new ItemAndEmeraldToItem(Items.FISH, new PriceInfo(6, 6), Items.COOKED_FISH, new PriceInfo(6, 6)))
                ))
                        .setMutators(Careers.FISHERMAN, 2, ImmutableList.of(
                                generatorFor(new ListEnchantedItemForEmeralds(Items.FISHING_ROD, new PriceInfo(7, 8)))
                        ));
            }
            { // Shepherd
                instance.setMutators(Careers.SHEPHERD, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Item.getItemFromBlock(Blocks.WOOL), new PriceInfo(16, 22))),
                        generatorFor(new ListItemForEmeralds(Items.SHEARS, new PriceInfo(3, 4)))
                ))
                        .setMutators(Careers.SHEPHERD, 2, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 0), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 1), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 2), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 3), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 4), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 5), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 6), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 7), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 8), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 9), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 10), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 11), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 12), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 13), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 14), new PriceInfo(1, 2))),
                                generatorFor(new ListItemForEmeralds(new ItemStack(Blocks.WOOL, 1, 15), new PriceInfo(1, 2)))
                        ));
            }
            { // Fletcher
                instance.setMutators(Careers.FLETCHER, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.STRING, new PriceInfo(15, 20))),
                        generatorFor(new ListItemForEmeralds(Items.ARROW, new PriceInfo(-12, -8)))
                ))
                        .setMutators(Careers.FLETCHER, 2, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.BOW, new PriceInfo(2, 3))),
                                generatorFor(new ItemAndEmeraldToItem(Item.getItemFromBlock(Blocks.GRAVEL), new PriceInfo(10, 10), Items.FLINT,
                                        new PriceInfo(6, 10)))
                        ));
            }
        }
        { // Librarian
            { // Librarian
                instance.setMutators(Careers.LIBRARIAN, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.PAPER, new PriceInfo(24, 36))),
                        generatorFor(new ListEnchantedBookForEmeralds())
                ))
                        .setMutators(Careers.LIBRARIAN, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.BOOK, new PriceInfo(8, 10))),
                                generatorFor(new ListItemForEmeralds(Items.COMPASS, new PriceInfo(10, 12))),
                                generatorFor(new ListItemForEmeralds(Item.getItemFromBlock(Blocks.BOOKSHELF), new PriceInfo(3, 4)))
                        ))
                        .setMutators(Careers.LIBRARIAN, 3, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.WRITTEN_BOOK, new PriceInfo(2, 2))),
                                generatorFor(new ListItemForEmeralds(Items.CLOCK, new PriceInfo(10, 12))),
                                generatorFor(new ListItemForEmeralds(Item.getItemFromBlock(Blocks.GLASS), new PriceInfo(-5, -3)))
                        ))
                        .setMutators(Careers.LIBRARIAN, 4, ImmutableList.of(
                                generatorFor(new ListEnchantedBookForEmeralds())
                        ))
                        .setMutators(Careers.LIBRARIAN, 5, ImmutableList.of(
                                generatorFor(new ListEnchantedBookForEmeralds())
                        ))
                        .setMutators(Careers.LIBRARIAN, 6, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.NAME_TAG, new PriceInfo(20, 22)))
                        ));
            }
        }
        { // Priest
            { // Cleric
                instance.setMutators(Careers.CLERIC, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.ROTTEN_FLESH, new PriceInfo(36, 40))),
                        generatorFor(new EmeraldForItems(Items.GOLD_INGOT, new PriceInfo(8, 10)))
                ))
                        .setMutators(Careers.CLERIC, 2, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.REDSTONE, new PriceInfo(-4, -1))),
                                generatorFor(
                                        new ListItemForEmeralds(new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), new PriceInfo(-2, -1)))
                        ))
                        .setMutators(Careers.CLERIC, 3, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.ENDER_EYE, new PriceInfo(7, 11))),
                                generatorFor(new ListItemForEmeralds(Item.getItemFromBlock(Blocks.GLOWSTONE), new PriceInfo(-3, -1)))
                        ))
                        .setMutators(Careers.CLERIC, 4, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.EXPERIENCE_BOTTLE, new PriceInfo(3, 11)))
                        ));
            }
        }
        { // Blacksmith
            { // Armorer
                instance.setMutators(Careers.ARMORER, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.COAL, new PriceInfo(16, 24))),
                        generatorFor(new ListItemForEmeralds(Items.IRON_HELMET, new PriceInfo(4, 6)))
                ))
                        .setMutators(Careers.ARMORER, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.IRON_INGOT, new PriceInfo(7, 9))),
                                generatorFor(new ListItemForEmeralds(Items.IRON_CHESTPLATE, new PriceInfo(10, 14)))
                        ))
                        .setMutators(Careers.ARMORER, 3, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.DIAMOND, new PriceInfo(3, 4))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.DIAMOND_CHESTPLATE, new PriceInfo(16, 19)))
                        ))
                        .setMutators(Careers.ARMORER, 4, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.CHAINMAIL_BOOTS, new PriceInfo(5, 7))),
                                generatorFor(new ListItemForEmeralds(Items.CHAINMAIL_LEGGINGS, new PriceInfo(9, 11))),
                                generatorFor(new ListItemForEmeralds(Items.CHAINMAIL_HELMET, new PriceInfo(5, 7))),
                                generatorFor(new ListItemForEmeralds(Items.CHAINMAIL_CHESTPLATE, new PriceInfo(11, 15)))
                        ));
            }
            { // Weapon Smith
                instance.setMutators(Careers.WEAPON_SMITH, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.COAL, new PriceInfo(16, 24))),
                        generatorFor(new ListItemForEmeralds(Items.IRON_AXE, new PriceInfo(6, 8)))
                ))
                        .setMutators(Careers.WEAPON_SMITH, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.IRON_INGOT, new PriceInfo(7, 9))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.IRON_SWORD, new PriceInfo(9, 10)))
                        ))
                        .setMutators(Careers.WEAPON_SMITH, 3, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.DIAMOND, new PriceInfo(3, 4))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.DIAMOND_SWORD, new PriceInfo(12, 15))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.DIAMOND_AXE, new PriceInfo(9, 12)))
                        ));
            }
            { // Tool Smith
                instance.setMutators(Careers.TOOL_SMITH, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.COAL, new PriceInfo(16, 24))),
                        generatorFor(new ListEnchantedItemForEmeralds(Items.IRON_SHOVEL, new PriceInfo(5, 7)))
                ))
                        .setMutators(Careers.TOOL_SMITH, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.IRON_INGOT, new PriceInfo(7, 9))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.IRON_PICKAXE, new PriceInfo(9, 11)))
                        ))
                        .setMutators(Careers.TOOL_SMITH, 3, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.DIAMOND, new PriceInfo(3, 4))),
                                generatorFor(new ListEnchantedItemForEmeralds(Items.DIAMOND_PICKAXE, new PriceInfo(12, 15)))
                        ));
            }
        }
        { // Butcher
            { // Butcher
                instance.setMutators(Careers.BUTCHER, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.PORKCHOP, new PriceInfo(14, 18))),
                        generatorFor(new EmeraldForItems(Items.CHICKEN, new PriceInfo(14, 18)))
                ))
                        .setMutators(Careers.BUTCHER, 2, ImmutableList.of(
                                generatorFor(new EmeraldForItems(Items.COAL, new PriceInfo(16, 24))),
                                generatorFor(new ListItemForEmeralds(Items.COOKED_PORKCHOP, new PriceInfo(-7, -5))),
                                generatorFor(new ListItemForEmeralds(Items.COOKED_CHICKEN, new PriceInfo(-8, -6)))
                        ));
            }
            { // Leather Worker
                instance.setMutators(Careers.LEATHERWORKER, 1, ImmutableList.of(
                        generatorFor(new EmeraldForItems(Items.LEATHER, new PriceInfo(9, 12))),
                        generatorFor(new ListItemForEmeralds(Items.LEATHER_LEGGINGS, new PriceInfo(2, 4)))
                ))
                        .setMutators(Careers.LEATHERWORKER, 2, ImmutableList.of(
                                generatorFor(new ListEnchantedItemForEmeralds(Items.LEATHER_CHESTPLATE, new PriceInfo(7, 12)))
                        ))
                        .setMutators(Careers.LEATHERWORKER, 3, ImmutableList.of(
                                generatorFor(new ListItemForEmeralds(Items.SADDLE, new PriceInfo(8, 10)))
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
