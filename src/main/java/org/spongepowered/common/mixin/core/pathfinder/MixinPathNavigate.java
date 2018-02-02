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
package org.spongepowered.common.mixin.core.pathfinder;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.navigation.Navigator;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;

import javax.annotation.Nullable;

@Mixin(value = PathNavigate.class)
@Implements(@Interface(iface = Navigator.class, prefix = "navigator$"))
public abstract class MixinPathNavigate implements Navigator {

    @Shadow protected EntityLiving entity;
    @Shadow protected double speed;
    @Shadow @Final private IAttributeInstance pathSearchRange;

    @Shadow public abstract void shadow$setSpeed(double speedIn);

    @Shadow public abstract float getPathSearchRange();

    @Shadow @Nullable public abstract Path getPathToPos(BlockPos pos);

    @Shadow @Nullable public abstract Path getPathToEntityLiving(net.minecraft.entity.Entity entityIn);

    @Override
    public Agent getOwner() {
        return (Agent) this.entity;
    }

    @Intrinsic
    public void navigator$setSpeed(double speed) {
        shadow$setSpeed(speed);
    }

    @Override
    public double getSpeed() {
        return this.speed;
    }

    @Override
    public void navigate(Location<World> target) {
        getPathToPos(VecHelper.toBlockPos(target));
    }

    @Override
    public void navigate(Entity target) {
        getPathToEntityLiving((net.minecraft.entity.Entity) target);
    }

    @Override
    public double getFollowRange() {
        return getPathSearchRange();
    }

}
