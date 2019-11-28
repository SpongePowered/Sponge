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
package org.spongepowered.common.data.processor.multi.tileentity;

import com.google.common.collect.Maps;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableFurnaceData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.FurnaceData;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeFurnaceData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.tileentity.FurnaceTileEntity;

public class FurnaceDataProcessor extends AbstractTileEntityDataProcessor<FurnaceTileEntity, FurnaceData, ImmutableFurnaceData> {

    public FurnaceDataProcessor() {
        super(FurnaceTileEntity.class);
    }

    @Override
    protected boolean doesDataExist(FurnaceTileEntity tileEntity) {
        return true; //if it's an TileEntityFurnace it always has the data
    }

    @Override
    protected boolean set(FurnaceTileEntity tileEntity, Map<Key<?>, Object> keyValues) {
        // getField() / setField()
        //
        // 0 : furnaceBurnTime -> NOT equal to passedBurnTime --> the remaining burn time
        // 1 : currentItemBurnTime -> equal to maxBurnTime
        // 2 : cookTime -> equal to passedCookTime
        // 3 : totalCookTime -> equal to maxCookTime

        final int passedBurnTime = (Integer) keyValues.get(Keys.PASSED_BURN_TIME); //time (int) the fuel item already burned
        final int maxBurnTime = (Integer) keyValues.get(Keys.MAX_BURN_TIME); //time (int) the fuel can burn until its depleted
        final int passedCookTime = (Integer) keyValues.get(Keys.PASSED_COOK_TIME); //time (int) the item already cooked
        final int maxCookTime = (Integer) keyValues.get(Keys.MAX_COOK_TIME); //time (int) the item have to cook

        if (passedBurnTime > maxBurnTime || passedCookTime > maxCookTime) {
            return false;
        }

        final boolean needsUpdate = !tileEntity.func_145950_i() && maxBurnTime > 0 || tileEntity.func_145950_i() && maxBurnTime == 0;

        if (needsUpdate) {
            final World world = (World) tileEntity.func_145831_w();
            world.setBlockType(tileEntity.func_174877_v().func_177958_n(), tileEntity.func_174877_v().func_177956_o(),
                    tileEntity.func_174877_v().func_177952_p(), maxBurnTime > 0 ? BlockTypes.LIT_FURNACE : BlockTypes.FURNACE);
            tileEntity = (FurnaceTileEntity) tileEntity.func_145831_w().func_175625_s(tileEntity.func_174877_v());
        }

        tileEntity.func_174885_b(0, maxBurnTime - passedBurnTime);
        tileEntity.func_174885_b(1, maxBurnTime);
        tileEntity.func_174885_b(2, passedCookTime);
        tileEntity.func_174885_b(3, maxCookTime);

        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(FurnaceTileEntity tileEntity) {
        HashMap<Key<?>, Integer> values = Maps.newHashMapWithExpectedSize(3);

        final int passedBurnTime = tileEntity.func_174887_a_(1) - tileEntity.func_174887_a_(0);
        final int maxBurnTime = tileEntity.func_174887_a_(1);
        final int passedCookTime = tileEntity.func_174887_a_(2);
        final int maxCookTime = tileEntity.func_174887_a_(3);

        values.put(Keys.PASSED_BURN_TIME, passedBurnTime);
        values.put(Keys.MAX_BURN_TIME, maxBurnTime);
        values.put(Keys.PASSED_COOK_TIME, passedCookTime);
        values.put(Keys.MAX_COOK_TIME, maxCookTime);

        return values;
    }

    @Override
    protected FurnaceData createManipulator() {
        return new SpongeFurnaceData();
    }

    @Override
    public Optional<FurnaceData> fill(DataContainer container, FurnaceData furnaceData) {
        if (!container.contains(Keys.PASSED_BURN_TIME.getQuery()) ||
                !container.contains(Keys.MAX_BURN_TIME.getQuery()) ||
                !container.contains(Keys.PASSED_COOK_TIME.getQuery()) ||
                !container.contains(Keys.MAX_COOK_TIME.getQuery())) {
            return Optional.empty();
        }

        final int passedBurnTime = container.getInt(Keys.PASSED_BURN_TIME.getQuery()).get();
        final int maxBurnTime = container.getInt(Keys.MAX_BURN_TIME.getQuery()).get();
        final int passedCookTime = container.getInt(Keys.PASSED_COOK_TIME.getQuery()).get();
        final int maxCookTime = container.getInt(Keys.MAX_COOK_TIME.getQuery()).get();

        furnaceData.set(Keys.PASSED_BURN_TIME, passedBurnTime);
        furnaceData.set(Keys.MAX_BURN_TIME, maxBurnTime);
        furnaceData.set(Keys.PASSED_COOK_TIME, passedCookTime);
        furnaceData.set(Keys.MAX_COOK_TIME, maxCookTime);

        return Optional.of(furnaceData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData(); //cannot be removed
    }
}
