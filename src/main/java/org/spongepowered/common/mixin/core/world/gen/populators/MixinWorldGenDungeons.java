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
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.weighted.LootTable;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.world.gen.IWorldGenDungeons;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Mixin(WorldGenDungeons.class)
public abstract class MixinWorldGenDungeons implements Dungeon, IWorldGenDungeons {

    private VariableAmount attempts;
    private MobSpawnerData data;
    private LootTable<ItemStackSnapshot> items;

    @Shadow
    public abstract String pickMobSpawner(Random p_76543_1_);

    @Inject(method = "<init>()V", at = @At("RETURN") , require = 1)
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
    public void populate(Chunk chunk, Random random) {
        World world = (World) chunk.getWorld();
        Vector3i min = chunk.getBlockMin();
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());

        int n = this.attempts.getFlooredAmount(random);
        int x, y, z;

        for (int i = 0; i < n; ++i) {
            x = random.nextInt(16) + 8;
            y = random.nextInt(256);
            z = random.nextInt(16) + 8;
            generate(world, random, chunkPos.add(x, y, z));
        }
    }

    /*
     * Author: Deamon - December 12th, 2015
     *
     * Purpose: Overwritten to leverage the data manipulator while creating the
     * tile entity and the weighted table while filling chests.
     *
     * TODO can possibly be changed to a pair of injections
     * TODO could we represent this with a populator object to allow plguin
     * modification of the structure
     */
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        // TODO cleanup
        int i = rand.nextInt(2) + 2;
        int j = -i - 1;
        int k = i + 1;
        int l = rand.nextInt(2) + 2;
        int i1 = -l - 1;
        int j1 = l + 1;
        int k1 = 0;
        int l1;
        int i2;
        int j2;
        BlockPos blockpos1;

        for (l1 = j; l1 <= k; ++l1) {
            for (i2 = -1; i2 <= 4; ++i2) {
                for (j2 = i1; j2 <= j1; ++j2) {
                    blockpos1 = position.add(l1, i2, j2);
                    Material material = worldIn.getBlockState(blockpos1).getBlock().getMaterial();
                    boolean flag3 = material.isSolid();

                    if (i2 == -1 && !flag3) {
                        return false;
                    }

                    if (i2 == 4 && !flag3) {
                        return false;
                    }

                    if ((l1 == j || l1 == k || j2 == i1 || j2 == j1) && i2 == 0 && worldIn.isAirBlock(blockpos1)
                            && worldIn.isAirBlock(blockpos1.up())) {
                        ++k1;
                    }
                }
            }
        }

        if (k1 >= 1 && k1 <= 5) {
            for (l1 = j; l1 <= k; ++l1) {
                for (i2 = 3; i2 >= -1; --i2) {
                    for (j2 = i1; j2 <= j1; ++j2) {
                        blockpos1 = position.add(l1, i2, j2);

                        if (l1 != j && i2 != -1 && j2 != i1 && l1 != k && i2 != 4 && j2 != j1) {
                            if (worldIn.getBlockState(blockpos1).getBlock() != Blocks.chest) {
                                worldIn.setBlockToAir(blockpos1);
                            }
                        } else if (blockpos1.getY() >= 0 && !worldIn.getBlockState(blockpos1.down()).getBlock().getMaterial().isSolid()) {
                            worldIn.setBlockToAir(blockpos1);
                        } else if (worldIn.getBlockState(blockpos1).getBlock().getMaterial().isSolid()
                                && worldIn.getBlockState(blockpos1).getBlock() != Blocks.chest) {
                            if (i2 == -1 && rand.nextInt(4) != 0) {
                                worldIn.setBlockState(blockpos1, Blocks.mossy_cobblestone.getDefaultState(), 2);
                            } else {
                                worldIn.setBlockState(blockpos1, Blocks.cobblestone.getDefaultState(), 2);
                            }
                        }
                    }
                }
            }

            l1 = 0;

            while (l1 < 2) {
                i2 = 0;

                while (true) {
                    if (i2 < 3) {
                        label100: {
                            j2 = position.getX() + rand.nextInt(i * 2 + 1) - i;
                            int l2 = position.getY();
                            int i3 = position.getZ() + rand.nextInt(l * 2 + 1) - l;
                            BlockPos blockpos2 = new BlockPos(j2, l2, i3);

                            if (worldIn.isAirBlock(blockpos2)) {
                                int k2 = 0;
                                Iterator iterator = EnumFacing.Plane.HORIZONTAL.iterator();

                                while (iterator.hasNext()) {
                                    EnumFacing enumfacing = (EnumFacing) iterator.next();

                                    if (worldIn.getBlockState(blockpos2.offset(enumfacing)).getBlock().getMaterial().isSolid()) {
                                        ++k2;
                                    }
                                }

                                if (k2 == 1) {
                                    worldIn.setBlockState(blockpos2, Blocks.chest.correctFacing(worldIn, blockpos2, Blocks.chest.getDefaultState()),
                                            2);
                                    TileEntity tileentity1 = worldIn.getTileEntity(blockpos2);
                                    // BEGIN sponge
                                    // TODO pending loot tables
//                                    if (tileentity1 instanceof TileEntityChest)
//                                    {
//                                        fillChest((TileEntityChest) tileentity1, rand);
//                                    }
                                    List list = WeightedRandomChestContent.func_177629_a(WorldGenDungeons.CHESTCONTENT, new WeightedRandomChestContent[] {Items.enchanted_book.getRandom(rand)});

                                    if (tileentity1 instanceof TileEntityChest) {
                                        WeightedRandomChestContent.generateChestContents(rand, list, (TileEntityChest) tileentity1, 8);
                                    }
                                    // END sponge

                                    break label100;
                                }
                            }

                            ++i2;
                            continue;
                        }
                    }

                    ++l1;
                    break;
                }
            }

            worldIn.setBlockState(position, Blocks.mob_spawner.getDefaultState(), 2);
            TileEntity tileentity = worldIn.getTileEntity(position);

            // BEGIN sponge
            // TODO data impl of MobSpawnerData
            // uncomment when implemented
//            if (tileentity instanceof MobSpawner) {
//                ((MobSpawner) tileentity).offer(this.data);
//            }

            if (tileentity instanceof TileEntityMobSpawner) {
                ((TileEntityMobSpawner) tileentity).getSpawnerBaseLogic().setEntityName(this.pickMobSpawner(rand));
            }
//            else
//            {
//                field_175918_a.error("Failed to fetch mob spawner entity at (" + position.getX() + ", " + position.getY() + ", " + position.getZ()
//                        + ")");
//            }
            // END sponge

            return true;
        }
        return false;
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
