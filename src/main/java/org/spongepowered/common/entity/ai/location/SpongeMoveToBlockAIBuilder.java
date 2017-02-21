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
package org.spongepowered.common.entity.ai.location;

import com.google.common.base.Preconditions;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.entity.ai.task.builtin.creature.location.MoveToBlockAITask;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.interfaces.ai.IMixinEntityAIMoveToBlock;

import java.util.function.Predicate;

public class SpongeMoveToBlockAIBuilder implements MoveToBlockAITask.Builder {

    private double speed;
    private int searchRange;
    private Predicate<Location<World>> destinationPredicate;

    @Override
    public MoveToBlockAITask.Builder from(MoveToBlockAITask value) {
        if (!((IMixinEntityAIMoveToBlock) value).isVanillaTask()) {
            destinationPredicate(value.getDestinationPredicate());
        }
        return this.speed(value.getSpeed()).searchRange(value.getSearchRange());
    }

    @Override
    public MoveToBlockAITask.Builder reset() {
        this.speed = 0d;
        this.searchRange = 0;
        this.destinationPredicate = null;
        return this;
    }

    @Override
    public MoveToBlockAITask build(Creature owner) {
        Preconditions.checkNotNull(destinationPredicate);
        IMixinEntityAIMoveToBlock aiTask = (IMixinEntityAIMoveToBlock) (Object) new EntityAIMoveToBlock((EntityCreature) owner, this.speed, this.searchRange) {
            @Override
            protected boolean shouldMoveTo(net.minecraft.world.World worldIn, BlockPos pos) {
                return ((IMixinEntityAIMoveToBlock) (Object) this).getDestinationPredicate().test(((World) worldIn).getLocation(pos.getX(), pos.getY(), pos.getZ()));
            }
        };

        aiTask.setDestinationPredicate(destinationPredicate);

        return (MoveToBlockAITask) aiTask;
    }

    @Override
    public MoveToBlockAITask.Builder speed(double speed) {
        this.speed = speed;
        return this;
    }

    @Override
    public MoveToBlockAITask.Builder searchRange(int searchRange) {
        this.searchRange = searchRange;
        return this;
    }

    @Override
    public MoveToBlockAITask.Builder destinationPredicate(Predicate<Location<World>> destinationPredicate) {
        this.destinationPredicate = destinationPredicate;
        return this;
    }

}
