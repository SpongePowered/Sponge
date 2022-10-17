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
import net.minecraft.Util;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class SpongeRandomEnchantmentListBuilder implements Enchantment.RandomListBuilder {

    private @Nullable Integer seed;
    private @Nullable Integer option;
    private @Nullable Integer level;
    private boolean treasure;
    private @Nullable List<Enchantment> pool;
    private @Nullable ItemStack item;

    @Override
    public Enchantment.RandomListBuilder seed(final int seed) {
        this.seed = seed;
        return this;
    }

    @Override
    public Enchantment.RandomListBuilder option(final int option) {
        this.option = option;
        return this;
    }

    @Override
    public Enchantment.RandomListBuilder level(final int level) {
        this.level = level;
        return this;
    }

    @Override
    public Enchantment.RandomListBuilder treasure(final boolean treasure) {
        this.treasure = treasure;
        return this;
    }

    @Override
    public Enchantment.RandomListBuilder fixedPool(final List<Enchantment> pool) {
        this.pool = pool;
        return this;
    }

    @Override
    public Enchantment.RandomListBuilder item(final ItemStack item) {
        this.item = item;
        return this;
    }

    @Override
    public List<Enchantment> build() throws IllegalStateException {
        checkNotNull(this.seed, "The random seed cannot be null");
        checkNotNull(this.option, "The option cannot be null");
        checkNotNull(this.level, "The level cannot be null");
        List<EnchantmentInstance> enchantments;
        if (this.pool == null || this.pool.isEmpty()) {
            checkNotNull(this.item, "The item cannot be null");
            enchantments = EnchantmentHelper.selectEnchantment(new Random(this.seed + this.option),
                                        ItemStackUtil.toNative(this.item), this.level, this.treasure);


        } else {
            enchantments = this.basedOfFixedPool(new Random(this.seed + this.option), this.pool);
        }

        return SpongeRandomEnchantmentListBuilder.fromNative(enchantments);
    }

    /**
     * See {@link EnchantmentHelper#selectEnchantment}
     */
    private List<EnchantmentInstance> basedOfFixedPool(final Random randomIn, final List<Enchantment> pool) {
        final List<EnchantmentInstance> list = Lists.<EnchantmentInstance>newArrayList();

        final List<EnchantmentInstance> list1 = SpongeRandomEnchantmentListBuilder.toNative(pool);

        // Same code as net.minecraft.enchantment.EnchantmentHelper#selectEnchantment
        if (!list1.isEmpty())
        {
            WeightedRandom.getRandomItem(randomIn, list1).ifPresent(list::add);

            while (randomIn.nextInt(50) <= this.level)
            {
                EnchantmentHelper.filterCompatibleEnchantments(list1, Util.lastOf(list));

                if (list1.isEmpty())
                {
                    break;
                }

                WeightedRandom.getRandomItem(randomIn, list1).ifPresent(list::add);
                this.level /= 2;
            }
        }

        return list;
    }

    public static List<Enchantment> fromNative(final List<EnchantmentInstance> list) {
        return list.stream().map(data -> Enchantment.of(((EnchantmentType) data.enchantment), data.level)).collect(Collectors.toList());
    }

    public static List<EnchantmentInstance> toNative(final List<Enchantment> list) {
        return list.stream().map(ench ->
                new EnchantmentInstance(((net.minecraft.world.item.enchantment.Enchantment) ench.type()), ench.level())
        ).collect(Collectors.toList());
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
