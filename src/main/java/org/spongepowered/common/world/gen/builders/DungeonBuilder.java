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
package org.spongepowered.common.world.gen.builders;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.weighted.LootTable;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.api.world.gen.populator.Dungeon.Builder;

import javax.annotation.Nullable;
import net.minecraft.world.gen.feature.DungeonsFeature;

public class DungeonBuilder implements Dungeon.Builder {

    private VariableAmount attempts;
    private @Nullable MobSpawnerData data;
    private @Nullable WeightedTable<EntityArchetype> choices;
    private LootTable<ItemStackSnapshot> items;

    public DungeonBuilder() {
        reset();
    }

    @Override
    public Builder attempts(VariableAmount attempts) {
        this.attempts = checkNotNull(attempts, "attempts");
        return this;
    }

    @Override
    public Builder mobSpawnerData(MobSpawnerData data) {
        this.data = checkNotNull(data, "data");
        this.choices = null;
        return this;
    }

    @Override
    public Builder choices(WeightedTable<EntityArchetype> choices) {
        this.choices = checkNotNull(choices, "choices");
        this.data = null;
        return this;
    }

    @Override
    public Builder possibleItems(LootTable<ItemStackSnapshot> items) {
        this.items = checkNotNull(items, "items");
        return this;
    }

    @Override
    public Builder from(Dungeon value) {
        attempts(value.getAttemptsPerChunk());
        value.getMobSpawnerData().ifPresent(this::mobSpawnerData);
        value.getChoices().ifPresent(this::choices);
        this.items = new LootTable<>();
        this.items.addAll(value.getPossibleContents());
        return this;
    }

    @Override
    public Builder reset() {
        this.attempts = VariableAmount.fixed(8);
        this.data = null;
        this.choices = null;
        this.items = new LootTable<>();
        return this;
    }

    @Override
    public Dungeon build() throws IllegalStateException {
        Dungeon populator = (Dungeon) new DungeonsFeature();
        populator.setAttemptsPerChunk(this.attempts);
        if (this.data != null) {
            populator.setMobSpawnerData(this.data);
        }
        if (this.choices != null) {
            populator.setChoices(this.choices);
        }
        populator.getPossibleContents().clearPool();
        populator.getPossibleContents().addAll(this.items);
        return populator;
    }

}
