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
package org.spongepowered.common.mixin.api.minecraft.world.entity.boss.enderdragon.phases;

import org.spongepowered.api.entity.living.monster.boss.dragon.phase.DragonPhase;
import org.spongepowered.api.entity.living.monster.boss.dragon.phase.DragonPhaseType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;
import java.util.Optional;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;

@Mixin(DragonPhaseInstance.class)
public interface DragonPhaseInstanceMixin_API extends DragonPhase {

    // @formatter:off
    @Shadow net.minecraft.world.phys.Vec3 shadow$getFlyTargetLocation();
    @Shadow EnderDragonPhase<? extends DragonPhaseInstance> shadow$getPhase();
    // @formatter:on

    @Override
    default DragonPhaseType type() {
        return (DragonPhaseType) this.shadow$getPhase();
    }

    @Override
    default Optional<Vector3d> targetPosition() {
        final net.minecraft.world.phys.Vec3 vec = this.shadow$getFlyTargetLocation();
        return vec == null ? Optional.empty() : Optional.of(VecHelper.toVector3d(vec));
    }

}
