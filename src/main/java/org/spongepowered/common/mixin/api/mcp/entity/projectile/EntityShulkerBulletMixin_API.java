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
package org.spongepowered.common.mixin.api.mcp.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.block.DirectionalData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.ShulkerBullet;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDirectionalData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.api.mcp.entity.EntityMixin_API;
import org.spongepowered.common.util.Constants;

import java.util.Collection;

import javax.annotation.Nullable;

@Mixin(EntityShulkerBullet.class)
public abstract class EntityShulkerBulletMixin_API extends EntityMixin_API implements ShulkerBullet {

    @Shadow @Nullable private EnumFacing direction;

    @Shadow private EntityLivingBase owner;

    @Nullable public ProjectileSource projectileSource;

    @Override
    public DirectionalData getDirectionalData() {
        return new SpongeDirectionalData( this.direction != null ? Constants.DirectionFunctions.getFor(this.direction) : Direction.NONE);
    }

    @Override
    public Value<Direction> direction() {
        return new SpongeValue<>(Keys.DIRECTION, Direction.NONE, this.direction != null ? Constants.DirectionFunctions.getFor(this.direction) : Direction.NONE);
    }

    @Override
    public ProjectileSource getShooter() {
        if (this.projectileSource != null) {
            return this.projectileSource;
        }

        if (this.owner instanceof ProjectileSource) {
            return (ProjectileSource) this.owner;
        }

        return ProjectileSource.UNKNOWN;
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof EntityLivingBase) {
            this.owner = (EntityLivingBase) shooter;
        } else {
            this.owner = null;
        }

        this.projectileSource = shooter;
    }


    @Override
    protected void spongeApi$supplyVanillaManipulators(final Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(this.getDirectionalData());
        manipulators.add(this.getTargetData());
    }
}
