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

package org.spongepowered.common.item.merchant;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.VillagerRegistry;
import org.spongepowered.api.item.merchant.generator.TraceableTradeOfferGenerator;
import org.spongepowered.api.item.merchant.generator.TradeOfferGenerator;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.entity.SpongeProfession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class SpongeVillagerRegistry implements VillagerRegistry {

    private static final SpongeVillagerRegistry instance = new SpongeVillagerRegistry();
    private static final ITradeList[] EMPTY_TRADES = new ITradeList[0];

    private SpongeVillagerRegistry() {
    }

    public static VillagerRegistry getInstance() {
        return instance;
    }

    @Override
    public Map<Integer, List<TradeOfferGenerator>> getTradeOffers(final Career career) {
        final ITradeList[][] careerTrades = getFor(career);

        // Convert
        final ImmutableMap.Builder<Integer, List<TradeOfferGenerator>> result = ImmutableMap.builder();
        for (int level = 0; level < careerTrades.length; level++) {
            final ITradeList[] levelTrades = careerTrades[level];
            if (levelTrades == null || levelTrades.length == 0) {
                continue;
            }
            final ImmutableList.Builder<TradeOfferGenerator> offers = ImmutableList.builder();
            for (final Object trade : levelTrades) {
                offers.add((TradeOfferGenerator) trade);
            }
            result.put(level + 1, offers.build());
        }
        return result.build();
    }

    @Override
    public List<TradeOfferGenerator> getTradeOffers(final Career career, final int level) throws IllegalArgumentException {
        final ITradeList[] levelTrades = getFor(career, level);

        // Convert
        final ImmutableList.Builder<TradeOfferGenerator> offers = ImmutableList.builder();
        for (final Object trade : levelTrades) {
            if (trade != null) {
                offers.add((TradeOfferGenerator) trade);
            }
        }
        return offers.build();
    }

    @Override
    public void addTradeOffers(final Career career, final int level, final TradeOfferGenerator... generators) throws IllegalArgumentException {
        validateLevel(level);
        checkNotNull(generators, "generators");

        final ITradeList[][][] professionTrades = getForProfession(career);
        final ITradeList[][] careerTrades = getFor(career, professionTrades);

        int careerType = ((SpongeCareer) career).type;
        // Change and set the generators
        professionTrades[careerType - 1] = modify(careerTrades, level, Arrays.asList(generators), false);
    }

    @Override
    public void setTradeOffers(final Career career, final int level, final List<TradeOfferGenerator> generators) throws IllegalArgumentException {
        validateLevel(level);
        checkNotNull(generators, "generators");

        final ITradeList[][][] professionTrades = getForProfession(career);
        final ITradeList[][] careerTrades = getFor(career, professionTrades);

        int careerType = ((SpongeCareer) career).type;
        // Change and set the generators
        professionTrades[careerType - 1] = modify(careerTrades, level, generators, true);
    }

    @Override
    public void setTradeOffers(final Career career, final Map<Integer, List<TradeOfferGenerator>> generatorMap) throws IllegalArgumentException {
        checkNotNull(generatorMap, "generator map");

        final ITradeList[][][] professionTrades = getFor(career.getProfession());
        getFor(career, professionTrades); // Just for validation

        // Ignore previous entries
        ITradeList[][] careerTrades = new ITradeList[generatorMap.size()][0];
        // Change the generators
        for (final Entry<Integer, List<TradeOfferGenerator>> entry : generatorMap.entrySet()) {
            final int level = validateLevel(checkNotNull(entry.getKey(), "level"));
            careerTrades = modify(careerTrades, level, checkNotNull(entry.getValue(), "generators"), true);
        }

        final int careerType = ((SpongeCareer) career).type;
        // And set them
        professionTrades[careerType - 1] = careerTrades;

    }

    @Override
    public List<TradeOffer> generateTradeOffers(final Career career, final int level) throws IllegalArgumentException {
        final List<TradeOffer> offers = new ArrayList<TradeOffer>();
        generateTradeOffers(career, level, offers);
        return offers;
    }

    @Override
    public void generateTradeOffers(final Career career, final int level, final List<TradeOffer> currentOffers) throws IllegalArgumentException {
        checkNotNull(currentOffers, "currentOffers");
        final List<TradeOfferGenerator> generators = getTradeOffers(career, level);
        for (final TradeOfferGenerator generator : generators) {
            generator.generate(currentOffers);
        }
    }

    private static ITradeList[][][] getForProfession(Career career) {
        validate(career);
        return getFor(career.getProfession());
    }

    private static ITradeList[][][] getFor(Profession profession) {
        final int professionType = validate(profession);
        final ITradeList[][][][] defaultTrades = checkNotNull(EntityVillager.DEFAULT_TRADE_LIST_MAP, "defaultTradeOfferGenerators");
        if (defaultTrades.length < professionType || defaultTrades[professionType - 1] == null) {
            throw new IllegalStateException("Could not find valid entry in default trade offer generators. " +
                    "Is this (" + profession + ") a valid profession?");
        }
        return defaultTrades[professionType - 1];
    }

    private static int validate(Profession profession) {
        checkNotNull(profession, "profession");
        checkArgument(profession instanceof SpongeProfession, "Unsupported Profession: %s (%s)",
                profession.getName(), profession.getClass().getName());
        final int professionType = ((SpongeProfession) profession).type;
        checkArgument(professionType > 0, "ProfessionType cannot be zero or negative");
        return professionType;
    }

    private static ITradeList[][] getFor(Career career) {
        return getFor(career, getFor(career.getProfession()));
    }

    private static ITradeList[][] getFor(Career career, ITradeList[][][] professionTrades) {
        final int careerType = validate(career);
        if (professionTrades.length < careerType || professionTrades[careerType - 1] == null) {
            throw new IllegalStateException("Could not find valid entry in profession trade offer generators. " +
                    "Is this (" + career + ") a valid career?");
        }
        return professionTrades[careerType - 1];
    }

    private static int validate(Career career) {
        checkNotNull(career, "career");
        checkArgument(career instanceof SpongeCareer, "Unsupported Career: %s (%s)",
                career.getName(), career.getClass().getName());
        final int careerType = ((SpongeCareer) career).type;
        checkArgument(careerType > 0, "CareerType cannot be zero or negative");
        return careerType;
    }

    private static ITradeList[] getFor(Career career, int level) {
        validateLevel(level);
        final ITradeList[][] careerTrades = getFor(career);
        if (careerTrades.length < level || careerTrades[level - 1] == null) {
            throw new IllegalStateException("Could not find valid entry in career trade offer generators. " +
                    "Is this level (" + level + " @ " + career + ") a valid career level?");
        }
        return careerTrades[level - 1];
    }

    private static int validateLevel(int level) {
        checkArgument(level > 0, "Level cannot be zero or negative");
        checkArgument(level <= 100, "Level cannot be bigger than 100"); // TODO useful check?
        return level;
    }

    private static ITradeList[][] modify(ITradeList[][] careerTrades, final int level, final List<TradeOfferGenerator> generators,
            final boolean replace) {
        if (careerTrades.length < level) {
            careerTrades = Arrays.copyOf(careerTrades, level);
            // Replace null entries
            for (int i = 0; i < level; i++) {
                if (careerTrades[i] == null) {
                    careerTrades[i] = EMPTY_TRADES;
                }
            }
        }
        ITradeList[] levelTrades = careerTrades[level - 1];

        if (levelTrades == null || levelTrades.length == 0 || replace) {
            // Set/Replace
            levelTrades = new ITradeList[generators.size()];
            for (int i = 0; i < generators.size(); i++) {
                levelTrades[i] = wrap(generators.get(i));
            }
            careerTrades[level - 1] = levelTrades;
        } else {
            // Append
            final int oldLength = levelTrades.length;
            final int newLength = oldLength + generators.size();
            final ITradeList[] newTrades = Arrays.copyOf(levelTrades, newLength);
            for (int i = 0; i < generators.size(); i++) {
                newTrades[oldLength + i] = wrap(generators.get(i));
            }
            careerTrades[level - 1] = newTrades;
        }
        return careerTrades;
    }

    private static ITradeList wrap(final TradeOfferGenerator generator) {
        checkNotNull(generator, "generator");
        if (generator instanceof ITradeList) {
            return (ITradeList) generator;
        }
        return new TradeOfferGeneratorAsITradeList(new TraceableTradeOfferGenerator(generator));
    }

    static class TradeOfferGeneratorAsITradeList implements ITradeList, TradeOfferGenerator {

        private final TradeOfferGenerator generator;

        TradeOfferGeneratorAsITradeList(final TradeOfferGenerator generator) {
            this.generator = generator;
        }

        @Override
        public void generate(final List<TradeOffer> tradeOffers) {
            this.generator.generate(tradeOffers);

        }

        @SuppressWarnings("unchecked")
        @Override
        public final void modifyMerchantRecipeList(final MerchantRecipeList recipeList, final Random random) {
            this.generator.generate(recipeList);
        }

    }

}
