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
package org.spongepowered.common.mixin.core.statistic;

import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.statistic.BlockStatistic;
import org.spongepowered.api.statistic.ItemStatistic;
import org.spongepowered.api.statistic.StatisticGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.statistic.IMixinStatistic;
import org.spongepowered.common.statistic.SpongeBlockStatistic;
import org.spongepowered.common.statistic.SpongeEntityStatistic;
import org.spongepowered.common.statistic.SpongeStatisticGroup;

@Mixin(StatList.class)
public abstract class MixinStatList {

    private static final String STATBASE_CLASS = "class=net/minecraft/stats/StatBase";
    private static final String STATCRAFTING_CLASS = "class=net/minecraft/stats/StatCrafting";

    @Inject(method = "init", at = @At("RETURN"))
    private static void setStatisticTypes(CallbackInfo ci) {
        SpongeStatisticGroup.init();

        for (StatBase statistic : StatList.ALL_STATS) {
            IMixinStatistic stat = (IMixinStatistic) statistic;
            String translationId = stat.getTranslation().getId();

            if (translationId.equals("stat.entityKill")) {
                stat.setStatisticGroup(StatisticGroups.HAS_KILLED_ENTITY);
            } else if (translationId.equals("stat.entityKilledBy")) {
                stat.setStatisticGroup(StatisticGroups.KILLED_BY_ENTITY);
            } else {
                stat.setStatisticGroup(StatisticGroups.HIDDEN);
            }
        }

        for (StatBase statistic : StatList.BASIC_STATS) {
            ((IMixinStatistic) statistic).setStatisticGroup(StatisticGroups.GENERAL);
        }

        for (StatBase statistic : StatList.CRAFTS_STATS) {
            if (statistic instanceof BlockStatistic) {
                ((IMixinStatistic) statistic).setStatisticGroup(StatisticGroups.CRAFT_BLOCK);
            } else if (statistic instanceof ItemStatistic) {
                ((IMixinStatistic) statistic).setStatisticGroup(StatisticGroups.CRAFT_ITEM);
            }
        }

        for (StatBase statistic : StatList.OBJECT_USE_STATS) {
            if (statistic instanceof BlockStatistic) {
                ((IMixinStatistic) statistic).setStatisticGroup(StatisticGroups.USE_BLOCK);
            } else if (statistic instanceof ItemStatistic) {
                ((IMixinStatistic) statistic).setStatisticGroup(StatisticGroups.USE_ITEM);
            }
        }

        for (StatBase statistic : StatList.BLOCKS_STATS) {
            if (statistic instanceof IMixinStatistic) {
                ((IMixinStatistic) statistic).setStatisticGroup(StatisticGroups.MINE_BLOCK);
            }
        }

        for (StatBase statistic : StatList.OBJECT_BREAK_STATS) {
            if (statistic instanceof IMixinStatistic) {
                ((IMixinStatistic) statistic).setStatisticGroup(StatisticGroups.BREAK_ITEM);
            }
        }

        for (StatBase statistic : StatList.OBJECTS_PICKED_UP_STATS) {
            if (statistic instanceof BlockStatistic) {
                ((IMixinStatistic) statistic).setStatisticGroup(StatisticGroups.PICK_UP_BLOCK);
            } else if (statistic instanceof ItemStatistic) {
                ((IMixinStatistic) statistic).setStatisticGroup(StatisticGroups.PICK_UP_ITEM);
            }
        }

        for (StatBase statistic : StatList.OBJECTS_DROPPED_STATS) {
            if (statistic instanceof BlockStatistic) {
                ((IMixinStatistic) statistic).setStatisticGroup(StatisticGroups.DROP_BLOCK);
            } else if (statistic instanceof ItemStatistic) {
                ((IMixinStatistic) statistic).setStatisticGroup(StatisticGroups.DROP_ITEM);
            }
        }
    }

    // BlockStatistics

    /**
     * Redirects the constructor invocation in
     * {@link StatList#initCraftableStats()} from
     * {@link StatCrafting#StatCrafting(String, String, ITextComponent, Item)}
     * to create a {@link SpongeBlockStatistic} if the given item is an
     * {@link ItemBlock}.
     *
     * @param prefix The prefix part of the new id for the statistic to create
     * @param name The name part of the new id for the statistic to create
     * @param statNameIn The text component used as textual representation
     * @param item The item to create the statistic for
     * @return The statistic that is created
     */
    @Redirect(method = "initCraftableStats", at = @At(value = "NEW", args = STATCRAFTING_CLASS))
    private static StatCrafting initCraftableStats(String prefix, String name, ITextComponent statNameIn, Item item) {
        if (item instanceof ItemBlock) {
            return new SpongeBlockStatistic(prefix, name, statNameIn, (ItemBlock) item);
        } else {
            return new StatCrafting(prefix, name, statNameIn, item);
        }
    }

    /**
     * Redirects the constructor invocation in
     * {@link StatList#initMiningStats()} from
     * {@link StatCrafting#StatCrafting(String, String, ITextComponent, Item)}
     * to create a {@link SpongeBlockStatistic} instead.
     *
     * @param prefix The prefix part of the new id for the statistic to create
     * @param name The name part of the new id for the statistic to create
     * @param statNameIn The text component used as textual representation
     * @param item The item to create the statistic for
     * @return The statistic that is created instead
     */
    @Redirect(method = "initMiningStats", at = @At(value = "NEW", args = STATCRAFTING_CLASS))
    private static StatCrafting initMiningStats(String prefix, String name, ITextComponent statNameIn, Item item) {
        return new SpongeBlockStatistic(prefix, name, statNameIn, (ItemBlock) item);
    }

    /**
     * Redirects the constructor invocation in {@link StatList#initStats()} from
     * {@link StatCrafting#StatCrafting(String, String, ITextComponent, Item)}
     * to create a {@link SpongeBlockStatistic} if the given item is an
     * {@link ItemBlock}.
     *
     * @param prefix The prefix part of the new id for the statistic to create
     * @param name The name part of the new id for the statistic to create
     * @param statNameIn The text component used as textual representation
     * @param item The item to create the statistic for
     * @return The statistic that is created
     */
    @Redirect(method = "initStats", at = @At(value = "NEW", args = STATCRAFTING_CLASS))
    private static StatCrafting initStats(String prefix, String name, ITextComponent statNameIn, Item item) {
        if (item instanceof ItemBlock) {
            return new SpongeBlockStatistic(prefix, name, statNameIn, (ItemBlock) item);
        } else {
            return new StatCrafting(prefix, name, statNameIn, item);
        }
    }

    /**
     * Redirects the two constructor invocations in
     * {@link StatList#initPickedUpAndDroppedStats()} from
     * {@link StatCrafting#StatCrafting(String, String, ITextComponent, Item)}
     * to create a {@link SpongeBlockStatistic} if the given item is an
     * {@link ItemBlock}.
     *
     * @param prefix The prefix part of the new id for the statistic to create
     * @param name The name part of the new id for the statistic to create
     * @param statNameIn The text component used as textual representation
     * @param item The item to create the statistic for
     * @return The statistic that is created
     */
    @Redirect(method = "initPickedUpAndDroppedStats", at = @At(value = "NEW", args = STATCRAFTING_CLASS))
    private static StatCrafting initPickedUpAndDroppedStats(String prefix, String name, ITextComponent statNameIn, Item item) {
        if (item instanceof ItemBlock) {
            return new SpongeBlockStatistic(prefix, name, statNameIn, (ItemBlock) item);
        } else {
            return new StatCrafting(prefix, name, statNameIn, item);
        }
    }

    // EntityStatistics

    /**
     * Redirects the constructor invocation in
     * {@link StatList#getStatKillEntity(EntityList.EntityEggInfo)} from
     * {@link StatBase#StatBase(String, ITextComponent)} to create a
     * {@link SpongeEntityStatistic} instead.
     *
     * @param id The new id for the statistic to create
     * @param statNameIn The text component used as textual representation
     * @param eggInfo The egg info that is used to create the statistic from
     * @return The statistic that is created instead
     */
    @Redirect(method = "getStatKillEntity", at = @At(value = "NEW", args = STATBASE_CLASS))
    private static StatBase getStatKillEntity(String id, ITextComponent statNameIn, EntityList.EntityEggInfo eggInfo) {
        return new SpongeEntityStatistic(id, statNameIn, eggInfo.spawnedID);
    }

    /**
     * Redirects the constructor invocation in
     * {@link StatList#getStatEntityKilledBy(EntityList.EntityEggInfo)} from
     * {@link StatBase#StatBase(String, ITextComponent)} to create a
     * {@link SpongeEntityStatistic} instead.
     *
     * @param id The new id for the statistic to create
     * @param statNameIn The text component used as textual representation
     * @param eggInfo The egg info that is used to create the statistic from
     * @return The statistic that is created instead
     */
    @Redirect(method = "getStatEntityKilledBy", at = @At(value = "NEW", args = STATBASE_CLASS))
    private static StatBase getStatEntityKilledBy(String id, ITextComponent statNameIn, EntityList.EntityEggInfo eggInfo) {
        return new SpongeEntityStatistic(id, statNameIn, eggInfo.spawnedID);
    }

}
