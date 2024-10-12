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
package org.spongepowered.common.item.recipe.ingredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class SpongeItemList implements HolderSet<Item> {

    public static final String INGREDIENT_TYPE = "sponge:type";
    public static final String INGREDIENT_ITEM = "sponge:item";

    protected final ItemStack[] stacks;

    public SpongeItemList(ItemStack... stacks) {
        this.stacks = stacks;
    }

    @Override
    public Stream<Holder<Item>> stream() {
        return Arrays.stream(this.stacks)
            .map(ItemStack::getItemHolder);
    }

    @Override
    public int size() {
        return this.stacks.length;
    }

    @Override
    public boolean isBound() {
        return true;
    }

    @Override
    public Either<TagKey<Item>, List<Holder<Item>>> unwrap() {
        return Either.right(this.stream().toList());
    }

    @Override
    public Optional<Holder<Item>> getRandomElement(RandomSource source) {
        return this.stacks.length == 0 ? Optional.empty() :
            Optional.of(Util.getRandom(this.stacks, source)).
                map(ItemStack::getItemHolder);
    }

    @Override
    public Holder<Item> get(int var1) {
        final var stack = this.stacks[var1];
        return stack.getItemHolder();
    }

    @Override
    public boolean contains(Holder<Item> var1) {
        return false;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<Item> var1) {
        // TODO - maybe? What's this for?
        return true;
    }

    @Override
    public Optional<TagKey<Item>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public @NotNull Iterator<Holder<Item>> iterator() {
        return Arrays.stream(this.stacks)
            .map(ItemStack::getItemHolder)
            .iterator();
    }

    public Collection<ItemStack> getItems() {
        return Arrays.asList(this.stacks);
    }

    public JsonObject serialize() {
        final JsonObject jsonobject = new JsonObject();
        final JsonArray stackArray = new JsonArray();
        for (ItemStack stack : this.stacks) {
            stackArray.add(IngredientResultUtil.serializeItemStack(stack));
        }
        jsonobject.add(SpongeItemList.INGREDIENT_ITEM, stackArray);
        return jsonobject;
    }

    public abstract boolean test(ItemStack stack);

}
