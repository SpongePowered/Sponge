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
package org.spongepowered.common.mixin.api.minecraft.world.damagesource;

import net.minecraft.core.Holder;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.damagesource.DamageSourceBridge;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

@Mixin(value = net.minecraft.world.damagesource.DamageSource.class)
public abstract class DamageSourceMixin_API implements DamageSource {

    // @formatter:off
    @Shadow @Final private net.minecraft.world.entity.@Nullable Entity directEntity;

    @Shadow @Final private net.minecraft.world.entity.@Nullable Entity causingEntity;
    @Shadow @Final private Holder<net.minecraft.world.damagesource.DamageType> type;
    @Shadow @Final @Nullable private Vec3 damageSourcePosition;
    // @formatter:on

    @Override
    public Optional<Entity> source() {
        return Optional.ofNullable((Entity) this.directEntity);
    }

    @Override
    public Optional<Entity> indirectSource() {
        return Optional.ofNullable((Entity) this.causingEntity);
    }

    @Override
    public boolean doesAffectCreative() {
        // TODO ?
        return false;
    }

    @Override
    public Optional<ServerLocation> location() {
        return Optional.ofNullable(((DamageSourceBridge) this).bridge$blockLocation());
    }

    @Override
    public Optional<BlockSnapshot> blockSnapshot() {
        return Optional.ofNullable(((DamageSourceBridge) this).bridge$blockSnapshot());
    }

    @Override
    public Optional<Vector3d> position() {
        return Optional.ofNullable(VecHelper.toVector3d(this.damageSourcePosition));
    }

    @Override
    public DamageType type() {
        return (DamageType) (Object) this.type.value();
    }

}
