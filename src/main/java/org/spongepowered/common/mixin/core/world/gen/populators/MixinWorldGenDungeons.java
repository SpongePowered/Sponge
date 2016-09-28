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
package org.spongepowered.common.mixin.core.world.gen.populators;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Objects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.weighted.LootTable;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.world.gen.IWorldGenDungeons;

import java.util.Random;

@Mixin(WorldGenDungeons.class)
public abstract class MixinWorldGenDungeons extends WorldGenerator implements Dungeon, IWorldGenDungeons {

    private VariableAmount attempts;
    private MobSpawnerData data;
    private LootTable<ItemStackSnapshot> items;

    @Inject(method = "<init>()V", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.attempts = VariableAmount.fixed(8);
        // TODO data impl of MobSpawnerData
        this.data = null;// Sponge.getSpongeRegistry().getManipulatorRegistry().getBuilder(MobSpawnerData.class).get().create();
        this.items = new LootTable<>();
//        for (Object obj : WorldGenDungeons.CHESTCONTENT) {
//            WeightedRandomChestContent item = (WeightedRandomChestContent) obj;
//            ItemStack stack = (ItemStack) item.theItemId;
//            VariableAmount quantity =
//                    VariableAmount.baseWithRandomAddition(item.theMinimumChanceToGenerateItem, item.theMaximumChanceToGenerateItem
//                            - item.theMinimumChanceToGenerateItem + 1);
//            this.items.add(new WeightedItem(stack.getItem(), quantity, item.itemWeight, stack.getContainers()));
//        }
//        this.items.add(new WeightedEnchantmentBook(VariableAmount.fixed(1), 1));
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.DUNGEON;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());

        int n = this.attempts.getFlooredAmount(random);
        int x, y, z;

        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            y = random.nextInt(size.getY());
            z = random.nextInt(size.getZ());
            generate(world, random, chunkPos.add(x, y, z));
        }
    }

    // TODO Loot Tables
//    private void fillChest(IInventory inv, Random rand) {
//        int n = Math.min(this.count.getFlooredAmount(rand), inv.getSizeInventory());
//        for (int i = 0; i < n;) {
//            WeightedItem item = this.items.get(rand);
//            Collection<ItemStack> items = item.getRandomItem(Sponge.getSpongeRegistry().createItemBuilder(), rand, n - i);
//            for (ItemStack stack : items) {
//                inv.setInventorySlotContents(i++, (net.minecraft.item.ItemStack) stack);
//            }
//        }
//    }

    @Override
    public VariableAmount getAttemptsPerChunk() {
        return this.attempts;
    }

    @Override
    public void setAttemptsPerChunk(VariableAmount attempts) {
        this.attempts = attempts;
    }

    @Override
    public MobSpawnerData getSpawnerData() {
        return this.data;
    }
    
    @Override
    public void setSpawnerData(MobSpawnerData data) {
        this.data = data;
    }

    @Override
    public LootTable<ItemStackSnapshot> getPossibleContents() {
        return this.items;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("Type", "Dungeon")
                .add("PerChunk", this.attempts)
                .add("Data", this.data)
                .toString();
    }

}
