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

import net.minecraft.world.gen.feature.WorldGenDungeons;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.weighted.LootTable;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.api.world.gen.populator.Dungeon.Builder;
import org.spongepowered.common.interfaces.world.gen.IWorldGenDungeons;

public class DungeonBuilder implements Dungeon.Builder {

    private VariableAmount attempts;
    private MobSpawnerData data;
    private LootTable<ItemStackSnapshot> items;

    public DungeonBuilder() {
        reset();
    }

    @Override
    public Builder attempts(VariableAmount attempts) {
        this.attempts = attempts;
        return this;
    }

    @Override
    public Builder mobSpawnerData(MobSpawnerData data) {
        this.data = data;
        return this;
    }

    @Override
    public Builder minimumSpawnDelay(short delay) {
        this.data.set(Keys.SPAWNER_MINIMUM_DELAY, delay);
        return this;
    }

    @Override
    public Builder maximumSpawnDelay(short delay) {
        this.data.set(Keys.SPAWNER_MAXIMUM_DELAY, delay);
        return this;
    }

    @Override
    public Builder spawnCount(short count) {
        this.data.set(Keys.SPAWNER_SPAWN_COUNT, count);
        return this;
    }

    @Override
    public Builder maximumNearbyEntities(short count) {
        this.data.set(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, count);
        return this;
    }

    @Override
    public Builder requiredPlayerRange(short range) {
        this.data.set(Keys.SPAWNER_REQURED_PLAYER_RANGE, range);
        return this;
    }

    @Override
    public Builder spawnRange(short range) {
        this.data.set(Keys.SPAWNER_SPAWN_RANGE, range);
        return this;
    }

    @Override
    public Builder possibleEntities(WeightedTable<EntitySnapshot> entities) {
        this.data.set(Keys.SPAWNER_ENTITIES, entities);
        return this;
    }

    @Override
    public Builder possibleItems(LootTable<ItemStackSnapshot> items) {
        this.items = items;
        return this;
    }

    @Override
    public Builder reset() {
        this.attempts = VariableAmount.fixed(8);
        //TODO pending mobspawnerdata
        //this.data = Sponge.getSpongeRegistry().getManipulatorRegistry().getBuilder(MobSpawnerData.class).get().create();
        this.items = new LootTable<>();
        return this;
    }

    @Override
    public Dungeon build() throws IllegalStateException {
        Dungeon populator = (Dungeon) new WorldGenDungeons();
        populator.setAttemptsPerChunk(this.attempts);
        ((IWorldGenDungeons) populator).setSpawnerData(this.data);
        populator.getPossibleContents().clearPool();
        populator.getPossibleContents().addAll(this.items);
        return populator;
    }

}
