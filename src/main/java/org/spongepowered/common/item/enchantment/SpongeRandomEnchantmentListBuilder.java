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
package org.spongepowered.common.item.enchantment;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.Util;
import net.minecraft.util.WeightedRandom;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class SpongeRandomEnchantmentListBuilder implements Enchantment.RandomListBuilder {

    @Nullable private Integer seed;
    @Nullable private Integer option;
    @Nullable private Integer level;
    private boolean treasure;
    @Nullable private List<Enchantment> pool;
    @Nullable private ItemStack item;

    @Override
    public Enchantment.RandomListBuilder seed(int seed) {
        this.seed = seed;
        return this;
    }

    @Override
    public Enchantment.RandomListBuilder option(int option) {
        this.option = option;
        return this;
    }

    @Override
    public Enchantment.RandomListBuilder level(int level) {
        this.level = level;
        return this;
    }

    @Override
    public Enchantment.RandomListBuilder treasure(boolean treasure) {
        this.treasure = treasure;
        return this;
    }

    @Override
    public Enchantment.RandomListBuilder fixedPool(List<Enchantment> pool) {
        this.pool = pool;
        return this;
    }

    @Override
    public Enchantment.RandomListBuilder item(ItemStack item) {
        this.item = item;
        return this;
    }

    @Override
    public List<Enchantment> build() throws IllegalStateException {
        checkNotNull(seed, "The random seed cannot be null");
        checkNotNull(option, "The option cannot be null");
        checkNotNull(level, "The level cannot be null");
        List<EnchantmentData> enchantments;
        if (this.pool == null || this.pool.isEmpty()) {
            checkNotNull(item, "The item cannot be null");
            enchantments = EnchantmentHelper.buildEnchantmentList(new Random(this.seed + this.option),
                                        ItemStackUtil.toNative(this.item), this.level, this.treasure);


        } else {
            enchantments = this.basedOfFixedPool(new Random(this.seed + this.option), this.pool);
        }

        return fromNative(enchantments);
    }

    /**
     * See {@link EnchantmentHelper#buildEnchantmentList}
     */
    private List<EnchantmentData> basedOfFixedPool(Random randomIn, List<Enchantment> pool) {
        List<EnchantmentData> list = Lists.<EnchantmentData>newArrayList();

        List<EnchantmentData> list1 = toNative(pool);

        // Same code as net.minecraft.enchantment.EnchantmentHelper#buildEnchantmentList
        if (!list1.isEmpty())
        {
            list.add(WeightedRandom.getRandomItem(randomIn, list1));

            while (randomIn.nextInt(50) <= this.level)
            {
                EnchantmentHelper.removeIncompatible(list1, (EnchantmentData) Util.getLastElement(list));

                if (list1.isEmpty())
                {
                    break;
                }

                list.add(WeightedRandom.getRandomItem(randomIn, list1));
                this.level /= 2;
            }
        }

        return list;
    }

    public static List<Enchantment> fromNative(List<EnchantmentData> list) {
        return list.stream().map(data -> Enchantment.of(((EnchantmentType) data.enchantment), data.enchantmentLevel)).collect(Collectors.toList());
    }

    public static List<EnchantmentData> toNative(List<Enchantment> list) {
        return list.stream().map(ench ->
                new EnchantmentData(((net.minecraft.enchantment.Enchantment) ench.getType()), ench.getLevel())
        ).collect(Collectors.toList());
    }

    @Override
    public Enchantment.RandomListBuilder from(List<Enchantment> value) {
        this.pool = value;
        return this;
    }

    @Override
    public Enchantment.RandomListBuilder reset() {
        this.pool = null;
        this.level = null;
        this.option = null;
        this.seed = null;
        this.item = null;
        return this;
    }
}
