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
package org.spongepowered.common.mixin.api.minecraft.world.level.block.entity;

import org.spongepowered.api.block.entity.carrier.chest.Chest;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin_API extends RandomizableContainerBlockEntityMixin_API<Chest> implements Chest {

    @Override
    public Optional<Chest> connectedChest() {
        // Based off of the logic in ChestBlock.getChestInventory but without a blocked check and returning the TE instead of the inventory.
        ChestBlockEntity chestTileEntity = (ChestBlockEntity) (Object) this;
        BlockState chestState = chestTileEntity.getBlockState();
        ChestType chestType = chestTileEntity.getBlockState().getValue(ChestBlock.TYPE);
        Level world = chestTileEntity.getLevel();

        if (chestType != ChestType.SINGLE) {
            BlockPos connectedPos = chestTileEntity.getBlockPos().relative(ChestBlock.getConnectedDirection(chestState));
            BlockState connectedState = world.getBlockState(connectedPos);

            if (connectedState.getBlock() == chestState.getBlock()) {
                ChestType connectedType = connectedState.getValue(ChestBlock.TYPE);

                if (connectedType != ChestType.SINGLE && chestType != connectedType && chestState.getValue(ChestBlock.FACING) == connectedState.getValue(ChestBlock.FACING)) {
                    BlockEntity connectedTileEntity = world.getBlockEntity(connectedPos);

                    if (connectedTileEntity instanceof ChestBlockEntity) {
                        return Optional.of((Chest) connectedTileEntity);
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.attachmentType().asImmutable());

        return values;
    }

}

