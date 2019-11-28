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
package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.DungeonsFeature;
import net.minecraft.world.gen.feature.Feature;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.weighted.LootTable;
import org.spongepowered.api.util.weighted.UnmodifiableWeightedTable;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.registry.type.world.gen.DungeonMobRegistryModule;

import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

@Mixin(DungeonsFeature.class)
public abstract class WorldGenDungeonsMixin_API extends Feature implements Dungeon {

    // We force this one to be immutable to avoid any wacky changes to it from plugins
    private final WeightedTable<EntityArchetype> api$defaultEntities = new UnmodifiableWeightedTable<>(DungeonMobRegistryModule.getInstance().getRaw());
    private VariableAmount api$attempts = VariableAmount.fixed(8);
    @Nullable private MobSpawnerData api$data;
    @Nullable private WeightedTable<EntityArchetype> api$choices;
    private final LootTable<ItemStackSnapshot> api$loot = new LootTable<>();

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.DUNGEON;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        final BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());

        final int n = this.api$attempts.getFlooredAmount(random);
        int x;
        int y;
        int z;

        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            y = random.nextInt(size.getY());
            z = random.nextInt(size.getZ());
            func_180709_b(world, random, chunkPos.add(x, y, z));
        }
    }

    @Override
    public VariableAmount getAttemptsPerChunk() {
        return this.api$attempts;
    }

    @Override
    public void setAttemptsPerChunk(final VariableAmount attempts) {
        this.api$attempts = checkNotNull(attempts, "attempts");
    }

    @Override
    public Optional<MobSpawnerData> getMobSpawnerData() {
        return Optional.ofNullable(this.api$data);
    }
    
    @Override
    public void setMobSpawnerData(final MobSpawnerData data) {
        this.api$data = checkNotNull(data, "data");
        this.api$choices = null;
    }

    @Override
    public Optional<WeightedTable<EntityArchetype>> getChoices() {
        // Both null means we use the default entity table
        if (this.api$choices == null && this.api$data == null) {
            return Optional.of(this.api$defaultEntities);
        }
        return Optional.ofNullable(this.api$choices);
    }

    @Override
    public void setChoices(final WeightedTable<EntityArchetype> choices) {
        this.api$choices = choices;
        this.api$data = null;
    }

    @Override
    public LootTable<ItemStackSnapshot> getPossibleContents() {
        return this.api$loot;
    }

}
